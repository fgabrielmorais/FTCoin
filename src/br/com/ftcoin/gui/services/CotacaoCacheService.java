package br.com.ftcoin.gui.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import br.com.ftcoin.daos.OraculoDAOCache;
import br.com.ftcoin.gui.common.AppContext;
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
 * por meio do cache do oráculo ({@link OraculoDAOCache}).
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class CotacaoCacheService {

    private static final Logger LOG = Logger.getLogger(CotacaoCacheService.class.getName());

    /**
     * Cotação usada quando o Oráculo não tem valor para a data pedida.
     */
    private static final BigDecimal COTACAO_PADRAO = BigDecimal.valueOf(5.50);

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
                // O cache fica no OraculoDAOCache: cada data vai ao oráculo uma vez por execução
                OraculoDAOCache oraculo = AppContext.getInstancia().getOraculoDAO();
                if (forcarOraculo) {
                    oraculo.descartar(dataAlvo);
                }
                OraculoDAOCache.Consulta consulta = oraculo.consultar(dataAlvo);

                if (consulta.valor() == null) {
                    throw new RegraNegocioException("erro.oraculo.semCotacao", dataAlvo);
                }

                long tempoMs = Math.max(1, System.currentTimeMillis() - instanteInicio);
                OrigemCotacao origem = consulta.doCache() ? OrigemCotacao.CACHE : OrigemCotacao.ORACULO;
                LOG.log(Level.FINE, "Cotação de {0} = {1}, origem {2}, {3} ms",
                        new Object[] { dataAlvo, consulta.valor(), origem, tempoMs });
                CotacaoDTO dto = new CotacaoDTO(dataAlvo, consulta.valor(), origem, LocalTime.now(), tempoMs);

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
     * Retorna a cotação síncrona com fallback ao cache local para apuração dos relatórios.
     *
     * @param data Data de apuração desejada
     * @return O valor de cotação apurado em {@code BigDecimal}
     */
    public BigDecimal obterCotacaoSincrona(LocalDate data) {
        LocalDate dataAlvo = (data != null) ? data : LocalDate.now();
        BigDecimal cotacao = AppContext.getInstancia().getOraculoDAO().buscarCotacaoPorData(dataAlvo);
        if (cotacao != null) {
            return cotacao;
        }
        LOG.log(Level.WARNING, "Oráculo sem cotação para {0}; usando o valor padrão {1}", new Object[] { dataAlvo, COTACAO_PADRAO });
        return COTACAO_PADRAO;
    }

    /**
     * Limpa todas as entradas de cotação armazenadas na memória cache.
     */
    public void limparCache() {
        AppContext.getInstancia().getOraculoDAO().limpar();
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
            OraculoDAOCache.Consulta consulta = AppContext.getInstancia().getOraculoDAO().consultar(data);
            BigDecimal cotacao = consulta.valor() != null ? consulta.valor() : obterCotacaoSincrona(data);
            BigDecimal variacaoAbs = BigDecimal.ZERO;
            BigDecimal variacaoPerc = BigDecimal.ZERO;

            if (cotacaoAnterior != null && cotacaoAnterior.signum() > 0) {
                variacaoAbs = cotacao.subtract(cotacaoAnterior);
                variacaoPerc = variacaoAbs.multiply(BigDecimal.valueOf(100))
                        .divide(cotacaoAnterior, 2, java.math.RoundingMode.HALF_EVEN);
            }

            OrigemCotacao origem = consulta.doCache() ? OrigemCotacao.CACHE : OrigemCotacao.ORACULO;
            lista.add(new RelatorioHistoricoCotacaoDTO(data, cotacao, variacaoAbs, variacaoPerc, origem));
            cotacaoAnterior = cotacao;
        }

        LOG.log(Level.FINE, "Histórico de cotações montado: {0} dia(s)", lista.size());
        return lista.reversed();
    }
}
