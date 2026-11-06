package br.com.ftcoin.gui.services;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import br.com.ftcoin.daos.OraculoDAO;
import br.com.ftcoin.gui.common.ContextoAplicacao;
import br.com.ftcoin.gui.common.OrigemCotacao;
import br.com.ftcoin.gui.dto.CotacaoDTO;
import br.com.ftcoin.gui.dto.RelatorioHistoricoCotacaoDTO;
import br.com.ftcoin.models.FTCoinException;
import br.com.ftcoin.models.RegraNegocioException;

/**
 * <p>
 * Serviço de alta performance para consulta e gerenciamento de cache de cotações de FTCoins.
 * </p>
 *
 * <p>
 * Utiliza <i>Virtual Threads</i> do Java para execução de requisições de rede assíncronas ao Oráculo,
 * despachando as respostas para a <i>Event Dispatch Thread</i> (EDT) do Swing e reduzindo a latência
 * por meio de um cache temporal em memória.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class CotacaoCacheService {

    private static final Logger LOG = Logger.getLogger(CotacaoCacheService.class.getName());

    /**
     * Tempo de vida limite (TTL) de uma cotação armazenada no cache local.
     */
    private static final Duration TTL_CACHE = Duration.ofSeconds(60);

    /**
     * <p>
     * Registro interno que encapsula uma entrada de cache temporal com carimbo de data/hora.
     * </p>
     *
     * @param cotacao        Valor unitário da cotação
     * @param momentoCriacao Instante exato do registro em memória
     * @author Grupo B.05
     * @version 1.0.0
     * @since 2026-10-20
     */
    private record EntradaCache(BigDecimal cotacao, Instant momentoCriacao) {

        /**
         * Retorna {@code true} se o registro ainda estiver dentro da janela de validade temporal.
         *
         * @return {@code true} se o item for válido; {@code false} caso tenha expirado
         */
        boolean estaValida() {
            return Duration.between(momentoCriacao, Instant.now()).compareTo(TTL_CACHE) < 0;
        }
    }

    /**
     * Mapa concorrente thread-safe para armazenamento temporário das cotações por data.
     */
    private final Map<LocalDate, EntradaCache> mapaCache = new ConcurrentHashMap<>();

    /**
     * Executor baseado em Virtual Threads para processamento simultâneo e não bloqueante.
     */
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final ContextoAplicacao contexto;

    /**
     * Cria o serviço de cotações. O cache é esvaziado sempre que a persistência muda,
     * para não misturar cotações de mecanismos diferentes.
     *
     * @param contexto contexto da aplicação, com os serviços e as mensagens ativos
     */
    public CotacaoCacheService(ContextoAplicacao contexto) {
        this.contexto = contexto;
        contexto.adicionarOuvintePersistencia(this::limparCache);
    }

    /**
     * Executa a consulta de cotação de forma assíncrona utilizando Virtual Threads.
     *
     * @param data          Data de referência da cotação desejada
     * @param forcarOraculo Se {@code true}, ignora o cache local e requisita diretamente ao oráculo
     * @param onSucesso     Callback acionado na Event Dispatch Thread em caso de êxito
     * @param onErro        Callback acionado na Event Dispatch Thread em caso de falha
     */
    public void buscarCotacaoAssincrona(
            LocalDate data,
            boolean forcarOraculo,
            Consumer<CotacaoDTO> onSucesso,
            Consumer<Throwable> onErro
    ) {
        final LocalDate dataAlvo = (data != null) ? data : LocalDate.now();

        virtualThreadExecutor.submit(() -> {
            long instanteInicio = System.currentTimeMillis();
            try {
                // 1. Verificação de existência válida em cache
                if (!forcarOraculo) {
                    EntradaCache itemCache = mapaCache.get(dataAlvo);
                    if (itemCache != null && itemCache.estaValida()) {
                        long tempoMs = Math.max(1, System.currentTimeMillis() - instanteInicio);
                        LOG.log(Level.FINE, "Cotação de {0} servida pelo cache ({1} ms)", new Object[] { dataAlvo, tempoMs });
                        CotacaoDTO dto = new CotacaoDTO(
                                dataAlvo,
                                itemCache.cotacao(),
                                OrigemCotacao.CACHE,
                                LocalTime.now(),
                                tempoMs
                        );
                        SwingUtilities.invokeLater(() -> onSucesso.accept(dto));
                        return;
                    }
                }

                // 2. Consulta ao Oráculo com simulação de latência de I/O em thread virtual
                LOG.log(Level.FINE, "Cotação de {0}: consultando o Oráculo ({1})",
                        new Object[] { dataAlvo, forcarOraculo ? "cache ignorado a pedido" : "cache vazio ou expirado" });
                OraculoDAO oraculoDAO = contexto.getOraculoDAO();
                BigDecimal cotacao = oraculoDAO.buscarCotacaoPorData(dataAlvo);

                if (cotacao == null) {
                    throw new RegraNegocioException("erro.oraculo.semCotacao", dataAlvo);
                }

                mapaCache.put(dataAlvo, new EntradaCache(cotacao, Instant.now()));

                long tempoMs = System.currentTimeMillis() - instanteInicio;
                LOG.log(Level.FINE, "Cotação de {0} = {1}, respondida pelo Oráculo em {2} ms",
                        new Object[] { dataAlvo, cotacao, tempoMs });
                CotacaoDTO dto = new CotacaoDTO(
                        dataAlvo,
                        cotacao,
                        OrigemCotacao.ORACULO,
                        LocalTime.now(),
                        tempoMs
                );

                SwingUtilities.invokeLater(() -> onSucesso.accept(dto));

            } catch (FTCoinException e) {
                // Erro previsto (ex.: sem cotação para a data): quem o registra é o presenter
                SwingUtilities.invokeLater(() -> onErro.accept(e));
            } catch (Throwable e) {
                // Erro inesperado em thread de fundo: registrado aqui, onde o stack trace ainda existe
                LOG.log(Level.SEVERE, "Falha inesperada na consulta da cotação de " + dataAlvo, e);
                SwingUtilities.invokeLater(() -> onErro.accept(e));
            }
        });
    }

    /**
     * Devolve a cotação de uma data, pelo cache quando ainda válido ou pelo Oráculo.
     *
     * @param data dia da cotação; {@code null} significa hoje
     * @return a cotação do dia, em reais
     * @throws RegraNegocioException se o Oráculo não tiver cotação para a data
     */
    public BigDecimal obterCotacaoSincrona(LocalDate data) {
        LocalDate dataAlvo = (data != null) ? data : LocalDate.now();
        BigDecimal cotacao = buscarNoCacheOuOraculo(dataAlvo);
        if (cotacao == null) {
            throw new RegraNegocioException("erro.oraculo.semCotacao", dataAlvo);
        }
        return cotacao;
    }

    // Devolve null quando o Oráculo não tem cotação para a data; nunca inventa um valor
    private BigDecimal buscarNoCacheOuOraculo(LocalDate data) {
        EntradaCache item = mapaCache.get(data);
        if (item != null && item.estaValida()) {
            LOG.log(Level.FINEST, "Cotação de {0} servida pelo cache", data);
            return item.cotacao();
        }

        BigDecimal cotacao = contexto.getOraculoDAO().buscarCotacaoPorData(data);
        if (cotacao != null) {
            mapaCache.put(data, new EntradaCache(cotacao, Instant.now()));
            LOG.log(Level.FINEST, "Cotação de {0} = {1}, guardada no cache", new Object[] { data, cotacao });
        } else {
            LOG.log(Level.FINE, "Oráculo sem cotação para {0}", data);
        }
        return cotacao;
    }

    /**
     * Limpa todas as entradas de cotação armazenadas na memória cache.
     */
    public void limparCache() {
        LOG.log(Level.FINE, "Cache de cotações limpo ({0} entrada(s) descartada(s))", mapaCache.size());
        mapaCache.clear();
    }

    /**
     * Lista as cotações dos últimos dias, da mais recente para a mais antiga.
     * Dias sem cotação no Oráculo ficam fora da lista, em vez de receber um valor inventado.
     *
     * @param dias Quantidade de dias passados a serem consultados
     * @return A lista de registros em formato {@link RelatorioHistoricoCotacaoDTO}
     */
    public List<RelatorioHistoricoCotacaoDTO> obterHistoricoRecente(int dias) {
        List<RelatorioHistoricoCotacaoDTO> lista = new ArrayList<>();
        LocalDate hoje = LocalDate.now();
        BigDecimal cotacaoAnterior = null;

        for (int i = dias; i >= 0; i--) {
            LocalDate data = hoje.minusDays(i);
            // A origem é decidida antes da busca, que guarda a cotação no cache
            EntradaCache noCache = mapaCache.get(data);
            OrigemCotacao origem = (noCache != null && noCache.estaValida()) ? OrigemCotacao.CACHE : OrigemCotacao.ORACULO;
            BigDecimal cotacao = buscarNoCacheOuOraculo(data);
            if (cotacao == null) {
                continue;
            }
            BigDecimal variacaoAbs = BigDecimal.ZERO;
            BigDecimal variacaoPerc = BigDecimal.ZERO;

            if (cotacaoAnterior != null && cotacaoAnterior.signum() > 0) {
                variacaoAbs = cotacao.subtract(cotacaoAnterior);
                variacaoPerc = variacaoAbs.multiply(BigDecimal.valueOf(100))
                        .divide(cotacaoAnterior, 2, java.math.RoundingMode.HALF_EVEN);
            }

            lista.add(new RelatorioHistoricoCotacaoDTO(data, cotacao, variacaoAbs, variacaoPerc, origem));
            cotacaoAnterior = cotacao;
        }

        LOG.log(Level.FINE, "Histórico de cotações montado: {0} dia(s)", lista.size());
        return lista.reversed();
    }
}
