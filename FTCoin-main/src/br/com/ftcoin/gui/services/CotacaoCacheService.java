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
import javax.swing.SwingUtilities;

import br.com.ftcoin.daos.OraculoDAO;
import br.com.ftcoin.gui.common.AppContext;
import br.com.ftcoin.gui.common.OrigemCotacao;
import br.com.ftcoin.gui.dto.CotacaoDTO;
import br.com.ftcoin.gui.dto.RelatorioHistoricoCotacaoDTO;
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
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class CotacaoCacheService {

    /**
     * Tempo de vida limite (TTL) de uma cotação armazenada no cache local.
     */
    private static final Duration TTL_CACHE = Duration.ofSeconds(60);

    /**
     * Tempo em milissegundos simulado para representar o tráfego de rede ao Oráculo externo.
     */
    private static final long LATENCIA_SIMULADA_ORACULO_MS = 380;

    /**
     * <p>
     * Registro interno que encapsula uma entrada de cache temporal com carimbo de data/hora.
     * </p>
     *
     * @param cotacao        Valor unitário da cotação
     * @param momentoCriacao Instante exato do registro em memória
     * @author Equipe FTCoin
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

    /**
     * Construtor padrão do serviço de cotações e cache.
     */
    public CotacaoCacheService() {
        // Inicialização padrão do serviço
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
                Thread.sleep(LATENCIA_SIMULADA_ORACULO_MS);

                OraculoDAO oraculoDAO = AppContext.getInstancia().getOraculoDAO();
                BigDecimal cotacao = oraculoDAO.buscarCotacaoPorData(dataAlvo);

                if (cotacao == null) {
                    throw new RegraNegocioException("erro.oraculo.semCotacao", dataAlvo);
                }

                mapaCache.put(dataAlvo, new EntradaCache(cotacao, Instant.now()));

                long tempoMs = System.currentTimeMillis() - instanteInicio;
                CotacaoDTO dto = new CotacaoDTO(
                        dataAlvo,
                        cotacao,
                        OrigemCotacao.ORACULO,
                        LocalTime.now(),
                        tempoMs
                );

                SwingUtilities.invokeLater(() -> onSucesso.accept(dto));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                SwingUtilities.invokeLater(() -> onErro.accept(e));
            } catch (Throwable e) {
                SwingUtilities.invokeLater(() -> onErro.accept(e));
            }
        });
    }

    /**
     * Retorna a cotação síncrona com fallback ao cache local para apuração dos relatórios.
     *
     * @param data Data de apuração desejada
     * @return O valor de cotação apurado em {@code BigDecimal}
     */
    public BigDecimal obterCotacaoSincrona(LocalDate data) {
        LocalDate dataAlvo = (data != null) ? data : LocalDate.now();
        EntradaCache item = mapaCache.get(dataAlvo);
        if (item != null && item.estaValida()) {
            return item.cotacao();
        }

        BigDecimal cotacao = AppContext.getInstancia().getOraculoDAO().buscarCotacaoPorData(dataAlvo);
        if (cotacao != null) {
            mapaCache.put(dataAlvo, new EntradaCache(cotacao, Instant.now()));
            return cotacao;
        }
        return BigDecimal.valueOf(5.50);
    }

    /**
     * Limpa todas as entradas de cotação armazenadas na memória cache.
     */
    public void limparCache() {
        mapaCache.clear();
    }

    /**
     * Retorna a lista cronológica das cotações dos últimos dias para renderização do histórico.
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
            BigDecimal cotacao = obterCotacaoSincrona(data);
            BigDecimal variacaoAbs = BigDecimal.ZERO;
            BigDecimal variacaoPerc = BigDecimal.ZERO;

            if (cotacaoAnterior != null && cotacaoAnterior.signum() > 0) {
                variacaoAbs = cotacao.subtract(cotacaoAnterior);
                variacaoPerc = variacaoAbs.multiply(BigDecimal.valueOf(100))
                        .divide(cotacaoAnterior, 2, java.math.RoundingMode.HALF_EVEN);
            }

            OrigemCotacao origem = mapaCache.containsKey(data) ? OrigemCotacao.CACHE : OrigemCotacao.ORACULO;
            lista.add(new RelatorioHistoricoCotacaoDTO(data, cotacao, variacaoAbs, variacaoPerc, origem));
            cotacaoAnterior = cotacao;
        }

        return lista.reversed();
    }
}
