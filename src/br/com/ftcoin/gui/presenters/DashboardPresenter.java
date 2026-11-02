package br.com.ftcoin.gui.presenters;

import java.util.logging.Logger;

import br.com.ftcoin.config.RegistroDeFalhas;
import br.com.ftcoin.gui.common.ContextoAplicacao;
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
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class DashboardPresenter {

    private static final Logger LOG = Logger.getLogger(DashboardPresenter.class.getName());

    /**
     * Interface de contrato para atualização dos componentes visuais do dashboard.
     */
    private final IDashboardView view;

    /**
     * Serviço de relatórios analíticos e apuração de indicadores.
     */
    private final RelatoriosService relatoriosService;

    private final ContextoAplicacao contexto;

    /**
     * Constrói o apresentador do dashboard vinculando a visão e o serviço analítico.
     *
     * @param view             Visão que implementa o contrato {@link IDashboardView}
     * @param relatoriosService Serviço de cálculo e apuração de dados consolidados
     * @param contexto contexto da aplicação, com os serviços e as mensagens ativos
     */
    public DashboardPresenter(IDashboardView view, RelatoriosService relatoriosService, ContextoAplicacao contexto) {
        this.contexto = contexto;
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
            RegistroDeFalhas.registrar(LOG, "carregar o dashboard", e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }
}
