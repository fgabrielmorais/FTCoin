package br.com.ftcoin.gui.presenters;

import java.util.List;
import br.com.ftcoin.controllers.CarteiraController;
import br.com.ftcoin.gui.common.AppContext;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.dto.CarteiraDTO;
import br.com.ftcoin.gui.services.RelatoriosService;
import br.com.ftcoin.gui.views.contracts.ICarteiraView;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.FTCoinException;

/**
 * <p>
 * Apresentador ({@code Presenter}) do Módulo de Carteiras no padrão arquitetural <b>MVP</b>.
 * </p>
 *
 * <p>
 * Isola a interface gráfica das regras de negócio, coordenando a validação de dados de entrada,
 * operações de inclusão, alteração e exclusão de carteiras junto aos controladores do sistema.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class CarteiraPresenter {

    /**
     * Interface de contrato para manipulação da tela de carteiras.
     */
    private final ICarteiraView view;

    /**
     * Serviço de relatórios e consulta de dados consolidados.
     */
    private final RelatoriosService relatoriosService;

    /**
     * Constrói o apresentador de carteiras associando a visão e o serviço de dados.
     *
     * @param view             Visão que implementa o contrato {@link ICarteiraView}
     * @param relatoriosService Serviço de relatórios e dados consolidados
     */
    public CarteiraPresenter(ICarteiraView view, RelatoriosService relatoriosService) {
        this.view = view;
        this.relatoriosService = relatoriosService;
    }

    /**
     * Carrega a lista atualizada de carteiras e repassa à visão para renderização na tabela.
     */
    public void carregarCarteiras() {
        try {
            List<CarteiraDTO> carteiras = relatoriosService.listarCarteirasDTO();
            view.exibirCarteiras(carteiras);
        } catch (Exception e) {
            view.exibirMensagemErro(e.getMessage());
        }
    }

    /**
     * Valida os campos fornecidos e cadastra uma nova carteira de custódia.
     *
     * @param titular   Nome completo informado para o titular da carteira
     * @param corretora Nome da corretora ou instituição custodiante
     */
    public void salvarNovaCarteira(String titular, String corretora) {
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        // Validação de regras de tamanho de campos da interface
        if (titular == null || titular.trim().length() < 3 || titular.trim().length() > 100) {
            view.exibirMensagemErro(mensagens.get("gui.carteira.erro.titularTamanho"));
            return;
        }

        if (corretora == null || corretora.trim().length() < 2 || corretora.trim().length() > 50) {
            view.exibirMensagemErro(mensagens.get("gui.carteira.erro.corretoraTamanho"));
            return;
        }

        try {
            CarteiraController controller = AppContext.getInstancia().getCarteiraController();
            Carteira criada = controller.criarCarteira(titular.trim(), corretora.trim());
            view.fecharModal();
            carregarCarteiras();
            view.exibirMensagemSucesso(mensagens.get("gui.carteira.sucesso.criada", criada.getId()));
        } catch (FTCoinException e) {
            view.exibirMensagemErro(mensagens.get(e.getChave(), e.getArgumentos()));
        } catch (Exception e) {
            view.exibirMensagemErro(e.getMessage());
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
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        if (novoTitular == null || novoTitular.trim().length() < 3 || novoTitular.trim().length() > 100) {
            view.exibirMensagemErro(mensagens.get("gui.carteira.erro.titularTamanho"));
            return;
        }

        if (novaCorretora == null || novaCorretora.trim().length() < 2 || novaCorretora.trim().length() > 50) {
            view.exibirMensagemErro(mensagens.get("gui.carteira.erro.corretoraTamanho"));
            return;
        }

        try {
            CarteiraController controller = AppContext.getInstancia().getCarteiraController();
            controller.editarCarteira(id, novoTitular.trim(), novaCorretora.trim());
            view.fecharModal();
            carregarCarteiras();
            view.exibirMensagemSucesso(mensagens.get("gui.carteira.sucesso.atualizada", id));
        } catch (FTCoinException e) {
            view.exibirMensagemErro(mensagens.get(e.getChave(), e.getArgumentos()));
        } catch (Exception e) {
            view.exibirMensagemErro(e.getMessage());
        }
    }

    /**
     * Exclui a carteira selecionada na tabela da visão caso não possua movimentações vinculadas.
     */
    public void excluirCarteira() {
        CarteiraDTO selecionada = view.getCarteiraSelecionada();
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        if (selecionada == null) {
            view.exibirMensagemErro(mensagens.get("gui.carteira.erro.selecione"));
            return;
        }

        try {
            CarteiraController controller = AppContext.getInstancia().getCarteiraController();
            controller.excluirCarteira(selecionada.id());
            carregarCarteiras();
            view.exibirMensagemSucesso(mensagens.get("gui.carteira.sucesso.excluida", selecionada.id()));
        } catch (FTCoinException e) {
            view.exibirMensagemErro(mensagens.get(e.getChave(), e.getArgumentos()));
        } catch (Exception e) {
            view.exibirMensagemErro(e.getMessage());
        }
    }
}
