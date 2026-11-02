package br.com.ftcoin.gui.presenters;

import br.com.ftcoin.gui.dto.ResumoDashboardDTO;
import br.com.ftcoin.gui.services.RelatoriosService;
import br.com.ftcoin.gui.views.contracts.IDashboardView;

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
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class DashboardPresenter {

    /**
     * Interface de contrato para atualização dos componentes visuais do dashboard.
     */
    private final IDashboardView view;

    /**
     * Serviço de relatórios analíticos e apuração de indicadores.
     */
    private final RelatoriosService relatoriosService;

    /**
     * Constrói o apresentador do dashboard vinculando a visão e o serviço analítico.
     *
     * @param view             Visão que implementa o contrato {@link IDashboardView}
     * @param relatoriosService Serviço de cálculo e apuração de dados consolidados
     */
    public DashboardPresenter(IDashboardView view, RelatoriosService relatoriosService) {
        this.view = view;
        this.relatoriosService = relatoriosService;
    }

    /**
     * Recupera os indicadores agregados do sistema e despacha o resumo à visão.
     */
    public void carregarDadosDashboard() {
        try {
            ResumoDashboardDTO resumo = relatoriosService.obterResumoDashboard();
            view.exibirResumo(resumo);
        } catch (Exception e) {
            view.exibirMensagemErro(e.getMessage());
        }
    }
}
