package br.com.ftcoin.gui.presenters;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import br.com.ftcoin.config.RegistroDeFalhas;
import br.com.ftcoin.gui.common.AppContext;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.dto.CotacaoDTO;
import br.com.ftcoin.gui.dto.RelatorioHistoricoCotacaoDTO;
import br.com.ftcoin.gui.services.CotacaoCacheService;
import br.com.ftcoin.gui.views.contracts.ICotacaoView;
import br.com.ftcoin.models.FTCoinException;

/**
 * <p>
 * Apresentador ({@code Presenter}) da tela de Cotações no padrão arquitetural <b>MVP</b>.
 * </p>
 *
 * <p>
 * Utiliza o {@link CotacaoCacheService} com <i>Virtual Threads</i> para recuperação assíncrona
 * de preços sem bloqueio da interface gráfica ({@code EDT}), enviando dados e telemetria à visão.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class CotacaoPresenter {

    private static final Logger LOG = Logger.getLogger(CotacaoPresenter.class.getName());

    /**
     * Interface de contrato para atualização dos componentes visuais de cotação.
     */
    private final ICotacaoView view;

    /**
     * Serviço de cache e consulta de cotações via oráculo.
     */
    private final CotacaoCacheService cotacaoService;

    /**
     * Constrói o apresentador de cotações associando a visão e o serviço especializado.
     *
     * @param view           Visão que implementa o contrato {@link ICotacaoView}
     * @param cotacaoService Serviço assíncrono de cotações e cache
     */
    public CotacaoPresenter(ICotacaoView view, CotacaoCacheService cotacaoService) {
        this.view = view;
        this.cotacaoService = cotacaoService;
    }

    /**
     * Inicializa a tela disparando a consulta do preço do dia corrente e o histórico recente.
     */
    public void inicializar() {
        consultarCotacao(LocalDate.now(), false);
        carregarHistorico();
    }

    /**
     * Dispara a busca assíncrona de cotação para a data especificada.
     *
     * @param data          Data cronológica desejada para a consulta
     * @param forcarOraculo Se {@code true}, ignora o cache e força chamada direta ao oráculo
     */
    public void consultarCotacao(LocalDate data, boolean forcarOraculo) {
        LOG.log(Level.FINE, "Usuário pediu: cotação de {0} (ignorar cache={1})",
                new Object[] { data != null ? data : LocalDate.now(), forcarOraculo });
        view.exibirCarregando(true);

        cotacaoService.buscarCotacaoAssincrona(
                data,
                forcarOraculo,
                dto -> {
                    view.exibirCarregando(false);
                    view.exibirCotacao(dto);
                    carregarHistorico();
                },
                erro -> {
                    view.exibirCarregando(false);
                    MensagensGui mensagens = AppContext.getInstancia().getMensagens();
                    // Erros inesperados já foram registrados com stack trace pelo serviço, na thread de fundo
                    if (erro instanceof FTCoinException fe) {
                        RegistroDeFalhas.registrar(LOG, "consultar a cotação", fe);
                    }
                    view.exibirMensagemErro(mensagens.descreverErro(erro));
                }
        );
    }

    /**
     * Limpa o cache local em memória e executa nova consulta direta ao oráculo para o dia atual.
     */
    public void limparCache() {
        LOG.fine("Usuário pediu: limpar o cache de cotações");
        cotacaoService.limparCache();
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();
        view.exibirMensagemSucesso(mensagens.get("gui.cotacao.sucessoCache"));
        consultarCotacao(LocalDate.now(), true);
    }

    /**
     * Recupera o histórico das cotações dos últimos 10 dias e atualiza a tabela da visão.
     */
    public void carregarHistorico() {
        try {
            List<RelatorioHistoricoCotacaoDTO> historico = cotacaoService.obterHistoricoRecente(10);
            view.exibirHistoricoOscilacoes(historico);
        } catch (Exception e) {
            RegistroDeFalhas.registrar(LOG, "carregar histórico de cotações", e);
            view.exibirMensagemErro(AppContext.getInstancia().getMensagens().descreverErro(e));
        }
    }
}
