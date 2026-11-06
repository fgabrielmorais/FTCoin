package br.com.ftcoin.gui.presenters;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import br.com.ftcoin.config.RegistroDeFalhas;
import br.com.ftcoin.gui.common.ContextoAplicacao;
import br.com.ftcoin.gui.services.CotacaoCacheService;
import br.com.ftcoin.gui.views.contracts.IRelatoriosView;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.Posicao;
import br.com.ftcoin.services.MovimentacaoService;

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
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class RelatoriosPresenter {

    private static final Logger LOG = Logger.getLogger(RelatoriosPresenter.class.getName());

    /**
     * Interface de contrato para renderização dos relatórios na visão.
     */
    private final IRelatoriosView view;

    /** Serviço de cotações com cache, usado no histórico do Oráculo. */
    private final CotacaoCacheService cotacaoService;

    private final ContextoAplicacao contexto;

    /**
     * Constrói o apresentador de relatórios injetando a visão e o serviço analítico.
     *
     * @param view             Visão que implementa o contrato {@link IRelatoriosView}
     * @param cotacaoService   serviço de cotações com cache, usado no relatório 5
     * @param contexto contexto da aplicação, com os serviços e as mensagens ativos
     */
    public RelatoriosPresenter(IRelatoriosView view, CotacaoCacheService cotacaoService, ContextoAplicacao contexto) {
        this.contexto = contexto;
        this.view = view;
        this.cotacaoService = cotacaoService;
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
            List<Carteira> carteiras = contexto.getCarteiraService().listarTodasCarteiras();
            view.exibirCarteirasFiltro(carteiras);
            if (!carteiras.isEmpty()) {
                gerarRelatoriosCarteiraEspecifica(carteiras.getFirst().getId());
            }
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "carregar o filtro de carteiras", e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
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
            MovimentacaoService movimentacoes = contexto.getMovimentacaoService();
            view.exibirRelatorio1Posicao(movimentacoes.apurarPosicao(idCarteira), movimentacoes.buscarCotacaoAtual());
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "gerar o relatório 1 (posição) da carteira " + idCarteira, e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }

    /**
     * Apura e despacha à visão o extrato detalhado de operações da carteira (Relatório 2).
     *
     * @param idCarteira Identificador da carteira
     */
    public void gerarRelatorio2Historico(int idCarteira) {
        try {
            MovimentacaoService movimentacoes = contexto.getMovimentacaoService();
            List<Movimentacao> historico = movimentacoes.buscarHistorico(idCarteira).reversed();
            view.exibirRelatorio2Historico(historico, movimentacoes.buscarCotacoesDasOperacoes(historico));
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "gerar o relatório 2 (histórico) da carteira " + idCarteira, e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }

    /**
     * Apura e despacha a composição patrimonial de todas as carteiras (Relatório 3).
     */
    public void gerarRelatorio3Consolidado() {
        try {
            MovimentacaoService movimentacoes = contexto.getMovimentacaoService();
            Map<Carteira, Posicao> posicoes =
                    movimentacoes.apurarPosicoes(contexto.getCarteiraService().listarTodasCarteiras());
            view.exibirRelatorio3Consolidado(posicoes, Posicao.consolidar(posicoes.values()),
                    movimentacoes.buscarCotacaoAtual());
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "gerar o relatório 3 (consolidado)", e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }

    /**
     * Apura e despacha os cálculos de taxa de rentabilidade e ROI (Relatório 4).
     */
    public void gerarRelatorio4Desempenho() {
        try {
            MovimentacaoService movimentacoes = contexto.getMovimentacaoService();
            Map<Carteira, Posicao> posicoes =
                    movimentacoes.apurarPosicoes(contexto.getCarteiraService().listarTodasCarteiras());
            view.exibirRelatorio4Desempenho(posicoes, movimentacoes.buscarCotacaoAtual());
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "gerar o relatório 4 (desempenho)", e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }

    /**
     * Apura e despacha a série temporal de oscilações do oráculo (Relatório 5).
     *
     * @param dias Quantidade de dias a serem apurados no histórico
     */
    public void gerarRelatorio5Cotacoes(int dias) {
        try {
            view.exibirRelatorio5Cotacoes(cotacaoService.obterHistoricoRecente(dias));
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "gerar o relatório 5 (cotações)", e);
            view.exibirMensagemErro(contexto.getMensagens().descreverErro(e));
        }
    }
}
