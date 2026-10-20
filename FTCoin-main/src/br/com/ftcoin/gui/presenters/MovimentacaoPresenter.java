package br.com.ftcoin.gui.presenters;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import br.com.ftcoin.controllers.MovimentacaoController;
import br.com.ftcoin.gui.common.AppContext;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.dto.CarteiraDTO;
import br.com.ftcoin.gui.dto.MovimentacaoDTO;
import br.com.ftcoin.gui.services.CotacaoCacheService;
import br.com.ftcoin.gui.services.RelatoriosService;
import br.com.ftcoin.gui.views.contracts.IMovimentacaoView;
import br.com.ftcoin.models.FTCoinException;
import br.com.ftcoin.models.TipoOperacao;

/**
 * <p>
 * Apresentador ({@code Presenter}) do Módulo de Movimentações no padrão arquitetural <b>MVP</b>.
 * </p>
 *
 * <p>
 * Coordena validações rigorosas de interface, cálculos prévios de estimativa financeira
 * e submissão de ordens de Compra e Venda de FTCoins aos controladores de domínio.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class MovimentacaoPresenter {

    /**
     * Interface de contrato para atualização da visão de movimentações.
     */
    private final IMovimentacaoView view;

    /**
     * Serviço de relatórios e apuração de carteiras.
     */
    private final RelatoriosService relatoriosService;

    /**
     * Serviço de cotações para cálculo imediato de estimativas financeiras.
     */
    private final CotacaoCacheService cotacaoService;

    /**
     * Constrói o apresentador de movimentações injetando visão e serviços requeridos.
     *
     * @param view             Visão que implementa o contrato {@link IMovimentacaoView}
     * @param relatoriosService Serviço de dados e carteiras
     * @param cotacaoService   Serviço de consulta de cotações
     */
    public MovimentacaoPresenter(IMovimentacaoView view, RelatoriosService relatoriosService, CotacaoCacheService cotacaoService) {
        this.view = view;
        this.relatoriosService = relatoriosService;
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
            List<CarteiraDTO> carteiras = relatoriosService.listarCarteirasDTO();
            view.exibirCarteirasDisponiveis(carteiras);
        } catch (Exception e) {
            view.exibirMensagemErro(e.getMessage());
        }
    }

    /**
     * Carrega as movimentações mais recentes cadastradas no sistema e atualiza a tabela da visão.
     */
    public void carregarHistoricoGeral() {
        try {
            List<MovimentacaoDTO> ultimas = relatoriosService.obterResumoDashboard().ultimasMovimentacoes();
            view.exibirHistoricoMovimentacoes(ultimas);
        } catch (Exception e) {
            view.exibirMensagemErro(e.getMessage());
        }
    }

    /**
     * Calcula dinamicamente o valor estimado da operação com base na quantidade e data informadas.
     *
     * @param data          Data informada para apuração da cotação
     * @param strQuantidade Quantidade digitada no campo de texto
     */
    public void calcularEstimativas(LocalDate data, String strQuantidade) {
        if (strQuantidade == null || strQuantidade.trim().isEmpty()) {
            view.atualizarEstimativas(BigDecimal.ZERO, BigDecimal.ZERO);
            return;
        }

        try {
            String sanitizada = strQuantidade.trim().replace(",", ".");
            BigDecimal qtd = new BigDecimal(sanitizada);
            if (qtd.signum() <= 0) {
                view.atualizarEstimativas(BigDecimal.ZERO, BigDecimal.ZERO);
                return;
            }

            LocalDate dataAlvo = (data != null) ? data : LocalDate.now();
            BigDecimal cotacao = cotacaoService.obterCotacaoSincrona(dataAlvo);
            BigDecimal total = qtd.multiply(cotacao);

            view.atualizarEstimativas(cotacao, total);
        } catch (NumberFormatException e) {
            view.atualizarEstimativas(BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    /**
     * Valida os campos da operação e submete o registro de compra ou venda ao controlador.
     *
     * @param idCarteira    Identificador da carteira selecionada
     * @param tipo          Tipo da operação ({@code COMPRA} ou {@code VENDA})
     * @param data          Data informada para a operação
     * @param strQuantidade Quantidade informada em formato texto
     */
    public void registrarOperacao(Integer idCarteira, TipoOperacao tipo, LocalDate data, String strQuantidade) {
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        if (idCarteira == null || idCarteira <= 0) {
            view.exibirMensagemErro(mensagens.get("gui.mov.erro.carteiraObrigatoria"));
            return;
        }

        if (data == null) {
            data = LocalDate.now();
        } else if (data.isAfter(LocalDate.now())) {
            view.exibirMensagemErro(mensagens.get("gui.mov.erro.dataFutura"));
            return;
        }

        if (strQuantidade == null || strQuantidade.trim().isEmpty()) {
            view.exibirMensagemErro(mensagens.get("gui.mov.erro.formatoNumero"));
            return;
        }

        BigDecimal quantidade;
        try {
            String sanitizada = strQuantidade.trim().replace(",", ".");
            quantidade = new BigDecimal(sanitizada);
            if (quantidade.signum() <= 0) {
                view.exibirMensagemErro(mensagens.get("gui.mov.erro.quantidadeZero"));
                return;
            }
        } catch (NumberFormatException e) {
            view.exibirMensagemErro(mensagens.get("gui.mov.erro.formatoNumero"));
            return;
        }

        try {
            MovimentacaoController controller = AppContext.getInstancia().getMovimentacaoController();
            controller.registrarOperacao(idCarteira, tipo, quantidade);

            view.limparFormulario();
            carregarCarteiras();
            carregarHistoricoGeral();

            String chaveSucesso = (tipo == TipoOperacao.COMPRA) ? "gui.mov.sucesso.compra" : "gui.mov.sucesso.venda";
            view.exibirMensagemSucesso(mensagens.get(chaveSucesso, quantidade, idCarteira));
        } catch (FTCoinException e) {
            view.exibirMensagemErro(mensagens.get(e.getChave(), e.getArgumentos()));
        } catch (Exception e) {
            view.exibirMensagemErro(e.getMessage());
        }
    }
}
