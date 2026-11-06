package br.com.ftcoin.gui.presenters;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import br.com.ftcoin.config.RegistroDeFalhas;
import br.com.ftcoin.gui.common.ContextoAplicacao;
import br.com.ftcoin.gui.views.contracts.IDashboardView;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.Posicao;
import br.com.ftcoin.services.MovimentacaoService;

/**
 * <p>
 * Apresentador ({@code Presenter}) da tela principal de Dashboard no padrão arquitetural <b>MVP</b>.
 * </p>
 *
 * <p>
 * Coordena a recuperação consolidada dos indicadores patrimoniais do sistema,
 * saldo total de moedas, cotação corrente e histórico de transações recentes para o feed.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class DashboardPresenter {

    private static final Logger LOG = Logger.getLogger(DashboardPresenter.class.getName());

    /** Quantas operações recentes aparecem no dashboard. */
    private static final int QUANTIDADE_ULTIMAS_OPERACOES = 8;

    /**
     * Interface de contrato para atualização dos componentes visuais do dashboard.
     */
    private final IDashboardView view;

    private final ContextoAplicacao contexto;

    /**
     * Constrói o apresentador do dashboard vinculando a visão e o serviço analítico.
     *
     * @param view             Visão que implementa o contrato {@link IDashboardView}
     * @param contexto contexto da aplicação, com os serviços e as mensagens ativos
     */
    public DashboardPresenter(IDashboardView view, ContextoAplicacao contexto) {
        this.contexto = contexto;
        this.view = view;
    }

    /**
     * Recupera os indicadores agregados do sistema e despacha o resumo à visão.
     */
    public void carregarDadosDashboard() {
        try {
            MovimentacaoService movimentacoes = contexto.getMovimentacaoService();
            List<Carteira> carteiras = contexto.getCarteiraService().listarTodasCarteiras();
            Posicao portfolio = Posicao.consolidar(movimentacoes.apurarPosicoes(carteiras).values());
            view.exibirIndicadores(portfolio, carteiras.size(), movimentacoes.buscarCotacaoAtual());

            List<Movimentacao> ultimas = movimentacoes.buscarUltimasMovimentacoes(QUANTIDADE_ULTIMAS_OPERACOES);
            Map<Integer, Carteira> carteirasPorId =
                    carteiras.stream().collect(Collectors.toMap(Carteira::getId, Function.identity()));
            view.exibirUltimasMovimentacoes(ultimas, carteirasPorId, movimentacoes.buscarCotacoesDasOperacoes(ultimas));
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "carregar o dashboard", e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }
}
