package br.com.ftcoin.gui.presenters;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import br.com.ftcoin.config.RegistroDeFalhas;
import br.com.ftcoin.services.MovimentacaoService;
import br.com.ftcoin.gui.common.ContextoAplicacao;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.TarefaEmSegundoPlano;
import br.com.ftcoin.gui.services.CotacaoCacheService;
import br.com.ftcoin.gui.views.contracts.IMovimentacaoView;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.FTCoinException;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.RegraNegocioException;
import br.com.ftcoin.models.TipoOperacao;

/**
 * <p>
 * Apresentador ({@code Presenter}) do Módulo de Movimentações no padrão arquitetural <b>MVP</b>.
 * </p>
 *
 * <p>
 * Coordena validações rigorosas de interface, cálculos prévios de estimativa financeira
 * e submissão de ordens de Compra e Venda de FTCoins aos serviços do modelo.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class MovimentacaoPresenter {

    private static final Logger LOG = Logger.getLogger(MovimentacaoPresenter.class.getName());

    /** Quantas operações recentes aparecem no histórico da tela. */
    private static final int QUANTIDADE_HISTORICO = 8;

    /**
     * Interface de contrato para atualização da visão de movimentações.
     */
    private final IMovimentacaoView view;

    /**
     * Serviço de cotações para cálculo imediato de estimativas financeiras.
     */
    private final CotacaoCacheService cotacaoService;

    private final ContextoAplicacao contexto;

    /**
     * Número da última estimativa pedida em cada operação; respostas de pedidos anteriores são descartadas.
     */
    private final Map<TipoOperacao, AtomicLong> ultimoPedidoEstimativa = new EnumMap<>(Map.of(
            TipoOperacao.COMPRA, new AtomicLong(),
            TipoOperacao.VENDA, new AtomicLong()));

    /**
     * Constrói o apresentador de movimentações injetando visão e serviços requeridos.
     *
     * @param view             Visão que implementa o contrato {@link IMovimentacaoView}
     * @param cotacaoService   Serviço de consulta de cotações
     * @param contexto contexto da aplicação, com os serviços e as mensagens ativos
     */
    public MovimentacaoPresenter(IMovimentacaoView view, CotacaoCacheService cotacaoService, ContextoAplicacao contexto) {
        this.contexto = contexto;
        this.view = view;
        this.cotacaoService = cotacaoService;
    }

    /**
     * Inicializa a tela carregando as carteiras disponíveis e o histórico geral de transações.
     */
    public void inicializar() {
        carregarCarteiras();
        carregarHistoricoGeral();
    }

    /**
     * Carrega as carteiras ativas e as encaminha para preenchimento das caixas de seleção da visão.
     */
    public void carregarCarteiras() {
        try {
            List<Carteira> carteiras = contexto.getCarteiraService().listarTodasCarteiras();
            view.exibirCarteirasDisponiveis(contexto.getMovimentacaoService().apurarPosicoes(carteiras));
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "carregar carteiras", e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }

    /**
     * Carrega as movimentações mais recentes cadastradas no sistema e atualiza a tabela da visão.
     */
    public void carregarHistoricoGeral() {
        try {
            MovimentacaoService movimentacoes = contexto.getMovimentacaoService();
            List<Carteira> carteiras = contexto.getCarteiraService().listarTodasCarteiras();
            List<Movimentacao> ultimas = movimentacoes.buscarUltimasMovimentacoes(QUANTIDADE_HISTORICO);
            Map<Integer, Carteira> carteirasPorId =
                    carteiras.stream().collect(Collectors.toMap(Carteira::getId, Function.identity()));
            view.exibirHistoricoMovimentacoes(ultimas, carteirasPorId, movimentacoes.buscarCotacoesDasOperacoes(ultimas));
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "carregar histórico de movimentações", e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }

    /**
     * Calcula o valor estimado de uma operação a partir da quantidade e da data informadas.
     * Cada formulário (compra ou venda) recebe só a própria estimativa.
     *
     * @param tipo          operação cujo formulário recebe a estimativa
     * @param data          Data informada para apuração da cotação
     * @param strQuantidade Quantidade digitada no campo de texto
     */
    public void calcularEstimativas(TipoOperacao tipo, LocalDate data, String strQuantidade) {
        // Invalida qualquer consulta anterior desta operação ainda em andamento
        AtomicLong contador = ultimoPedidoEstimativa.get(tipo);
        long pedido = contador.incrementAndGet();
        if (strQuantidade == null || strQuantidade.trim().isEmpty()) {
            view.atualizarEstimativas(tipo, BigDecimal.ZERO, BigDecimal.ZERO);
            return;
        }

        try {
            String sanitizada = strQuantidade.trim().replace(",", ".");
            BigDecimal qtd = new BigDecimal(sanitizada);
            if (qtd.signum() <= 0) {
                view.atualizarEstimativas(tipo, BigDecimal.ZERO, BigDecimal.ZERO);
                return;
            }

            LocalDate dataAlvo = (data != null) ? data : LocalDate.now();
            // Consulta fora da thread do Swing; só a resposta da última digitação é exibida
            TarefaEmSegundoPlano.executar(
                    () -> cotacaoService.obterCotacaoSincrona(dataAlvo),
                    cotacao -> {
                        if (pedido != contador.get()) {
                            return;
                        }
                        if (cotacao == null) {
                            MensagensGui mensagens = contexto.getMensagens();
                            view.informarCotacaoIndisponivel(tipo,
                                    mensagens.descreverErro(new RegraNegocioException("erro.oraculo.semCotacao", dataAlvo)));
                        } else {
                            view.atualizarEstimativas(tipo, cotacao, qtd.multiply(cotacao));
                        }
                    },
                    e -> {
                        if (pedido != contador.get()) {
                            return;
                        }
                        // Falha do oráculo (rede, autenticação): avisa na prévia sem derrubar a tela
                        RegistroDeFalhas.registrar(LOG, "calcular estimativa", e);
                        view.informarCotacaoIndisponivel(tipo, contexto.getMensagens().descreverErro(e));
                    });
        } catch (NumberFormatException e) {
            LOG.log(Level.FINEST, "Estimativa ignorada: quantidade digitada ainda não é um número ({0})", strQuantidade);
            view.atualizarEstimativas(tipo, BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    /**
     * Valida os campos da operação e submete o registro de compra ou venda ao serviço.
     *
     * @param idCarteira    Identificador da carteira selecionada
     * @param tipo          Tipo da operação ({@code COMPRA} ou {@code VENDA})
     * @param data          Data informada para a operação
     * @param strQuantidade Quantidade informada em formato texto
     */
    public void registrarOperacao(Integer idCarteira, TipoOperacao tipo, LocalDate data, String strQuantidade) {
        MensagensGui mensagens = contexto.getMensagens();
        LOG.log(Level.FINE, "Usuário pediu: {0} de {1} FTC na carteira {2}", new Object[] { tipo, strQuantidade, idCarteira });

        if (idCarteira == null || idCarteira <= 0) {
            LOG.log(Level.FINE, "Entrada rejeitada: {0}", "gui.mov.erro.carteiraObrigatoria");
            view.exibirMensagemErro(mensagens.get("gui.mov.erro.carteiraObrigatoria"));
            return;
        }

        if (data == null) {
            data = LocalDate.now();
        } else if (data.isAfter(LocalDate.now())) {
            LOG.log(Level.FINE, "Entrada rejeitada: {0}", "gui.mov.erro.dataFutura");
            view.exibirMensagemErro(mensagens.get("gui.mov.erro.dataFutura"));
            return;
        }

        if (strQuantidade == null || strQuantidade.trim().isEmpty()) {
            LOG.log(Level.FINE, "Entrada rejeitada: {0}", "gui.mov.erro.formatoNumero");
            view.exibirMensagemErro(mensagens.get("gui.mov.erro.formatoNumero"));
            return;
        }

        BigDecimal quantidade;
        try {
            String sanitizada = strQuantidade.trim().replace(",", ".");
            quantidade = new BigDecimal(sanitizada);
            if (quantidade.signum() <= 0) {
                LOG.log(Level.FINE, "Entrada rejeitada: {0}", "gui.mov.erro.quantidadeZero");
                view.exibirMensagemErro(mensagens.get("gui.mov.erro.quantidadeZero"));
                return;
            }
        } catch (NumberFormatException e) {
            LOG.log(Level.FINE, "Entrada rejeitada: {0}", "gui.mov.erro.formatoNumero");
            view.exibirMensagemErro(mensagens.get("gui.mov.erro.formatoNumero"));
            return;
        }

        try {
            MovimentacaoService servico = contexto.getMovimentacaoService();
            servico.registrarOperacao(idCarteira, tipo, quantidade);

            view.limparFormulario();
            carregarCarteiras();
            carregarHistoricoGeral();

            String chaveSucesso = (tipo == TipoOperacao.COMPRA) ? "gui.mov.sucesso.compra" : "gui.mov.sucesso.venda";
            view.exibirMensagemSucesso(mensagens.get(chaveSucesso, quantidade, idCarteira));
        } catch (FTCoinException e) {
            RegistroDeFalhas.registrar(LOG, "registrar " + tipo + " na carteira " + idCarteira, e);
            view.exibirMensagemErro(mensagens.get(e.getChave(), e.getArgumentos()));
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "registrar " + tipo + " na carteira " + idCarteira, e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }
}
