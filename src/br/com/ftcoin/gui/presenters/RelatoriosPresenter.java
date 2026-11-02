package br.com.ftcoin.gui.presenters;

import java.util.List;
import br.com.ftcoin.gui.dto.CarteiraDTO;
import br.com.ftcoin.gui.dto.MovimentacaoDTO;
import br.com.ftcoin.gui.dto.PosicaoRelatorioDTO;
import br.com.ftcoin.gui.dto.RelatorioConsolidadoItemDTO;
import br.com.ftcoin.gui.dto.RelatorioDesempenhoDTO;
import br.com.ftcoin.gui.dto.RelatorioHistoricoCotacaoDTO;
import br.com.ftcoin.gui.services.RelatoriosService;
import br.com.ftcoin.gui.views.contracts.IRelatoriosView;

/**
 * <p>
 * Apresentador ({@code Presenter}) do Módulo de Relatórios no padrão arquitetural <b>MVP</b>.
 * </p>
 *
 * <p>
 * Coordena a geração e atualização dos cinco relatórios financeiros do sistema FTCoin,
 * alimentando tabelas analíticas, séries temporais e cartões de rentabilidade (ROI).
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class RelatoriosPresenter {

    /**
     * Interface de contrato para renderização dos relatórios na visão.
     */
    private final IRelatoriosView view;

    /**
     * Serviço de cálculo e agregação de dados analíticos.
     */
    private final RelatoriosService relatoriosService;

    /**
     * Constrói o apresentador de relatórios injetando a visão e o serviço analítico.
     *
     * @param view             Visão que implementa o contrato {@link IRelatoriosView}
     * @param relatoriosService Serviço de cálculo e apuração financeira
     */
    public RelatoriosPresenter(IRelatoriosView view, RelatoriosService relatoriosService) {
        this.view = view;
        this.relatoriosService = relatoriosService;
    }

    /**
     * Inicializa o módulo de relatórios disparando a geração de todas as abas analíticas.
     */
    public void inicializar() {
        carregarCarteirasFiltro();
        gerarRelatorio3Consolidado();
        gerarRelatorio4Desempenho();
        gerarRelatorio5Cotacoes(15);
    }

    /**
     * Carrega as carteiras ativas para o filtro e processa os relatórios individuais da primeira carteira.
     */
    public void carregarCarteirasFiltro() {
        try {
            List<CarteiraDTO> carteiras = relatoriosService.listarCarteirasDTO();
            view.exibirCarteirasFiltro(carteiras);
            if (!carteiras.isEmpty()) {
                gerarRelatoriosCarteiraEspecifica(carteiras.getFirst().id());
            }
        } catch (Exception e) {
            view.exibirMensagemErro(e.getMessage());
        }
    }

    /**
     * Atualiza os relatórios individuais (Posição e Histórico) para a carteira selecionada.
     *
     * @param idCarteira Identificador numérico da carteira
     */
    public void gerarRelatoriosCarteiraEspecifica(int idCarteira) {
        gerarRelatorio1Posicao(idCarteira);
        gerarRelatorio2Historico(idCarteira);
    }

    /**
     * Apura e encaminha à visão os indicadores de posição pelo CMP (Relatório 1).
     *
     * @param idCarteira Identificador da carteira
     */
    public void gerarRelatorio1Posicao(int idCarteira) {
        try {
            PosicaoRelatorioDTO posicao = relatoriosService.obterPosicaoCarteira(idCarteira);
            view.exibirRelatorio1Posicao(posicao);
        } catch (Exception e) {
            view.exibirMensagemErro(e.getMessage());
        }
    }

    /**
     * Apura e despacha à visão o extrato detalhado de operações da carteira (Relatório 2).
     *
     * @param idCarteira Identificador da carteira
     */
    public void gerarRelatorio2Historico(int idCarteira) {
        try {
            List<MovimentacaoDTO> historico = relatoriosService.obterHistoricoOperacoes(idCarteira);
            view.exibirRelatorio2Historico(historico);
        } catch (Exception e) {
            view.exibirMensagemErro(e.getMessage());
        }
    }

    /**
     * Apura e despacha a composição patrimonial de todas as carteiras (Relatório 3).
     */
    public void gerarRelatorio3Consolidado() {
        try {
            List<RelatorioConsolidadoItemDTO> consolidado = relatoriosService.obterConsolidadoCarteiras();
            view.exibirRelatorio3Consolidado(consolidado);
        } catch (Exception e) {
            view.exibirMensagemErro(e.getMessage());
        }
    }

    /**
     * Apura e despacha os cálculos de taxa de rentabilidade e ROI (Relatório 4).
     */
    public void gerarRelatorio4Desempenho() {
        try {
            List<RelatorioDesempenhoDTO> desempenho = relatoriosService.obterDesempenhoLucratividade();
            view.exibirRelatorio4Desempenho(desempenho);
        } catch (Exception e) {
            view.exibirMensagemErro(e.getMessage());
        }
    }

    /**
     * Apura e despacha a série temporal de oscilações do oráculo (Relatório 5).
     *
     * @param dias Quantidade de dias a serem apurados no histórico
     */
    public void gerarRelatorio5Cotacoes(int dias) {
        try {
            List<RelatorioHistoricoCotacaoDTO> cotacoes = relatoriosService.obterHistoricoCotacoes(dias);
            view.exibirRelatorio5Cotacoes(cotacoes);
        } catch (Exception e) {
            view.exibirMensagemErro(e.getMessage());
        }
    }
}
