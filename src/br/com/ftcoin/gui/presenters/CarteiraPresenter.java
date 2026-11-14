package br.com.ftcoin.gui.presenters;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import br.com.ftcoin.config.RegistroDeFalhas;
import br.com.ftcoin.services.CarteiraService;
import br.com.ftcoin.services.MovimentacaoService;
import br.com.ftcoin.gui.common.ContextoAplicacao;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.views.contracts.ICarteiraView;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.FTCoinException;
import br.com.ftcoin.models.Posicao;

/**
 * <p>
 * Apresentador ({@code Presenter}) do Módulo de Carteiras no padrão arquitetural <b>MVP</b>.
 * </p>
 *
 * <p>
 * Isola a interface gráfica das regras de negócio, coordenando a validação de dados de entrada,
 * operações de inclusão, alteração e exclusão de carteiras junto aos serviços do modelo.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class CarteiraPresenter {

    private static final Logger LOG = Logger.getLogger(CarteiraPresenter.class.getName());

    /**
     * Interface de contrato para manipulação da tela de carteiras.
     */
    private final ICarteiraView view;

    private final ContextoAplicacao contexto;

    /**
     * Constrói o apresentador de carteiras associando a visão e o serviço de dados.
     *
     * @param view             Visão que implementa o contrato {@link ICarteiraView}
     * @param contexto contexto da aplicação, com os serviços e as mensagens ativos
     */
    public CarteiraPresenter(ICarteiraView view, ContextoAplicacao contexto) {
        this.contexto = contexto;
        this.view = view;
    }

    /**
     * Carrega a lista atualizada de carteiras e repassa à visão para renderização na tabela.
     */
    public void carregarCarteiras() {
        try {
            MovimentacaoService movimentacoes = contexto.getMovimentacaoService();
            List<Carteira> carteiras = contexto.getCarteiraService().listarTodasCarteiras();
            view.exibirCarteiras(movimentacoes.apurarPosicoes(carteiras), movimentacoes.buscarCotacaoAtual());
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "carregar carteiras", e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }

    /**
     * Descobre a carteira alvo de uma ação: a do identificador digitado ou, se o campo estiver vazio, a da linha
     * selecionada na tabela. A carteira é sempre buscada de novo no serviço, para recusar uma que já foi excluída.
     *
     * @param textoId identificador digitado pelo usuário; vazio para usar a linha selecionada
     * @return a carteira encontrada, ou {@code null} depois de mostrar o motivo à visão
     */
    private Carteira localizarCarteira(String textoId) {
        MensagensGui mensagens = contexto.getMensagens();
        int id;
        if (textoId == null || textoId.isBlank()) {
            Carteira selecionada = view.getCarteiraSelecionada();
            if (selecionada == null) {
                LOG.log(Level.FINE, "Entrada rejeitada: {0}", "gui.carteira.erro.selecione");
                view.exibirMensagemErro(mensagens.get("gui.carteira.erro.selecione"));
                return null;
            }
            id = selecionada.getId();
        } else {
            try {
                id = Integer.parseInt(textoId.trim());
            } catch (NumberFormatException e) {
                LOG.log(Level.FINE, "Entrada rejeitada: {0}", "gui.carteira.erro.idInvalido");
                view.exibirMensagemErro(mensagens.get("gui.carteira.erro.idInvalido"));
                return null;
            }
        }

        try {
            return contexto.getCarteiraService().buscarCarteira(id);
        } catch (FTCoinException e) {
            RegistroDeFalhas.registrar(LOG, "buscar carteira " + id, e);
            view.exibirMensagemErro(mensagens.get(e.getChave(), e.getArgumentos()));
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "buscar carteira " + id, e);
            view.exibirMensagemErro(mensagens.descreverErro(e));
        }
        return null;
    }

    /**
     * Consulta uma carteira e mostra seus dados à visão.
     *
     * @param textoId identificador digitado; vazio para usar a linha selecionada
     */
    public void consultarCarteira(String textoId) {
        LOG.log(Level.FINE, "Usuário pediu: consultar carteira {0}", textoId);
        Carteira carteira = localizarCarteira(textoId);
        if (carteira == null) {
            return;
        }
        try {
            Posicao posicao = contexto.getMovimentacaoService().apurarPosicao(carteira.getId());
            view.exibirDetalhesCarteira(carteira, posicao);
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "consultar carteira " + carteira.getId(), e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }

    /**
     * Abre a edição de uma carteira, se ela existir.
     *
     * @param textoId identificador digitado; vazio para usar a linha selecionada
     */
    public void iniciarEdicao(String textoId) {
        LOG.log(Level.FINE, "Usuário pediu: abrir edição da carteira {0}", textoId);
        Carteira carteira = localizarCarteira(textoId);
        if (carteira != null) {
            view.abrirModalEdicao(carteira);
        }
    }

    /**
     * Valida os campos fornecidos e cadastra uma nova carteira de custódia.
     *
     * @param titular   Nome completo informado para o titular da carteira
     * @param corretora Nome da corretora ou instituição custodiante
     */
    public void salvarNovaCarteira(String titular, String corretora) {
        LOG.fine("Usuário pediu: criar carteira");
        MensagensGui mensagens = contexto.getMensagens();

        // Validação de regras de tamanho de campos da interface
        if (titular == null || titular.trim().length() < 3 || titular.trim().length() > 100) {
            LOG.log(Level.FINE, "Entrada rejeitada: {0}", "gui.carteira.erro.titularTamanho");
            view.exibirMensagemErro(mensagens.get("gui.carteira.erro.titularTamanho"));
            return;
        }

        if (corretora == null || corretora.trim().length() < 2 || corretora.trim().length() > 50) {
            LOG.log(Level.FINE, "Entrada rejeitada: {0}", "gui.carteira.erro.corretoraTamanho");
            view.exibirMensagemErro(mensagens.get("gui.carteira.erro.corretoraTamanho"));
            return;
        }

        try {
            CarteiraService servico = contexto.getCarteiraService();
            Carteira criada = servico.criarCarteira(titular.trim(), corretora.trim());
            view.fecharModal();
            carregarCarteiras();
            view.exibirMensagemSucesso(mensagens.get("gui.carteira.sucesso.criada", criada.getId()));
        } catch (FTCoinException e) {
            RegistroDeFalhas.registrar(LOG, "criar carteira", e);
            view.exibirMensagemErro(mensagens.get(e.getChave(), e.getArgumentos()));
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "criar carteira", e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }

    /**
     * Valida as alterações informadas e atualiza o cadastro de uma carteira existente.
     *
     * @param id            Identificador exclusivo da carteira a ser modificada
     * @param novoTitular   Novo nome para o titular da conta
     * @param novaCorretora Novo nome para a instituição corretora
     */
    public void atualizarCarteira(int id, String novoTitular, String novaCorretora) {
        LOG.log(Level.FINE, "Usuário pediu: editar carteira {0}", id);
        MensagensGui mensagens = contexto.getMensagens();

        if (novoTitular == null || novoTitular.trim().length() < 3 || novoTitular.trim().length() > 100) {
            LOG.log(Level.FINE, "Entrada rejeitada: {0}", "gui.carteira.erro.titularTamanho");
            view.exibirMensagemErro(mensagens.get("gui.carteira.erro.titularTamanho"));
            return;
        }

        if (novaCorretora == null || novaCorretora.trim().length() < 2 || novaCorretora.trim().length() > 50) {
            LOG.log(Level.FINE, "Entrada rejeitada: {0}", "gui.carteira.erro.corretoraTamanho");
            view.exibirMensagemErro(mensagens.get("gui.carteira.erro.corretoraTamanho"));
            return;
        }

        try {
            CarteiraService servico = contexto.getCarteiraService();
            servico.editarCarteira(id, novoTitular.trim(), novaCorretora.trim());
            view.fecharModal();
            carregarCarteiras();
            view.exibirMensagemSucesso(mensagens.get("gui.carteira.sucesso.atualizada", id));
        } catch (FTCoinException e) {
            RegistroDeFalhas.registrar(LOG, "editar carteira " + id, e);
            view.exibirMensagemErro(mensagens.get(e.getChave(), e.getArgumentos()));
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "editar carteira " + id, e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }

    /**
     * Exclui uma carteira, depois da confirmação do usuário, caso ela exista e não tenha movimentações.
     *
     * @param textoId identificador digitado; vazio para usar a linha selecionada
     */
    public void excluirCarteira(String textoId) {
        LOG.log(Level.FINE, "Usuário pediu: excluir carteira {0}", textoId);
        MensagensGui mensagens = contexto.getMensagens();

        Carteira selecionada = localizarCarteira(textoId);
        if (selecionada == null || !view.confirmarExclusao(selecionada)) {
            return;
        }

        try {
            CarteiraService servico = contexto.getCarteiraService();
            servico.excluirCarteira(selecionada.getId());
            carregarCarteiras();
            view.exibirMensagemSucesso(mensagens.get("gui.carteira.sucesso.excluida", selecionada.getId()));
        } catch (FTCoinException e) {
            RegistroDeFalhas.registrar(LOG, "excluir carteira " + selecionada.getId(), e);
            view.exibirMensagemErro(mensagens.get(e.getChave(), e.getArgumentos()));
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "excluir carteira " + selecionada.getId(), e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }
}
