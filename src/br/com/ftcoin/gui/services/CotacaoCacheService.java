package br.com.ftcoin.gui.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import br.com.ftcoin.daos.OraculoDAOCache;
import br.com.ftcoin.gui.common.ContextoAplicacao;
import br.com.ftcoin.gui.common.OrigemCotacao;
import br.com.ftcoin.models.Cotacao;
import br.com.ftcoin.models.FTCoinException;
import br.com.ftcoin.models.RegraNegocioException;

/**
 * <p>
 * Serviço de consulta de cotações de FTCoins para a interface gráfica.
 * </p>
 *
 * <p>
 * Utiliza <i>Virtual Threads</i> do Java para as consultas ao Oráculo (que pode estar na rede),
 * despachando as respostas para a <i>Event Dispatch Thread</i> (EDT) do Swing. O cache fica no
 * próprio oráculo ({@link OraculoDAOCache}): cada data é buscada uma vez por execução.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class CotacaoCacheService {

    private static final Logger LOG = Logger.getLogger(CotacaoCacheService.class.getName());

    /**
     * Executor baseado em Virtual Threads para processamento simultâneo e não bloqueante.
     */
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final ContextoAplicacao contexto;

    /**
     * Recebe o resultado de uma consulta assíncrona de cotação, já na thread do Swing.
     *
     * @author Grupo B.05
     * @version 1.0.0
     */
    @FunctionalInterface
    public interface OuvinteCotacao {

        /**
         * Recebe a cotação consultada.
         *
         * @param cotacao         cotação da data pedida
         * @param origem          se veio do Oráculo ou do cache
         * @param tempoRespostaMs duração da consulta, em milissegundos
         */
        void receber(Cotacao cotacao, OrigemCotacao origem, long tempoRespostaMs);
    }

    /**
     * Cria o serviço de cotações. O oráculo (e o seu cache) é sempre o da persistência ativa,
     * lido do contexto a cada consulta.
     *
     * @param contexto contexto da aplicação, com o oráculo e as mensagens ativos
     */
    public CotacaoCacheService(ContextoAplicacao contexto) {
        this.contexto = contexto;
    }

    /**
     * Executa a consulta de cotação de forma assíncrona utilizando Virtual Threads.
     *
     * @param data          Data de referência da cotação desejada
     * @param forcarOraculo Se {@code true}, ignora o cache e requisita diretamente ao oráculo
     * @param onSucesso     Callback acionado na Event Dispatch Thread em caso de êxito
     * @param onErro        Callback acionado na Event Dispatch Thread em caso de falha
     */
    public void buscarCotacaoAssincrona(
            LocalDate data,
            boolean forcarOraculo,
            OuvinteCotacao onSucesso,
            Consumer<Throwable> onErro
    ) {
        final LocalDate dataAlvo = (data != null) ? data : LocalDate.now();

        virtualThreadExecutor.submit(() -> {
            long instanteInicio = System.currentTimeMillis();
            try {
                OraculoDAOCache oraculo = contexto.getOraculoDAO();
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
                Cotacao consultada = new Cotacao(dataAlvo, consulta.valor());
                SwingUtilities.invokeLater(() -> onSucesso.receber(consultada, origem, tempoMs));

            } catch (FTCoinException e) {
                // Erro previsto (ex.: sem cotação, falha de rede): quem o registra é o presenter
                SwingUtilities.invokeLater(() -> onErro.accept(e));
            } catch (Throwable e) {
                // Erro inesperado em thread de fundo: registrado aqui, onde o stack trace ainda existe
                LOG.log(Level.SEVERE, "Falha inesperada na consulta da cotação de " + dataAlvo, e);
                SwingUtilities.invokeLater(() -> onErro.accept(e));
            }
        });
    }

    /**
     * Devolve a cotação de uma data, pelo cache ou pelo Oráculo. Pode acessar a rede:
     * não deve ser chamado na thread do Swing.
     *
     * @param data dia da cotação; {@code null} significa hoje
     * @return a cotação do dia, em reais, ou {@code null} se o Oráculo não tiver cotação para a data
     *         (não é erro; quem chama decide o que mostrar)
     */
    public BigDecimal obterCotacaoSincrona(LocalDate data) {
        LocalDate dataAlvo = (data != null) ? data : LocalDate.now();
        BigDecimal cotacao = contexto.getOraculoDAO().buscarCotacaoPorData(dataAlvo);
        if (cotacao == null) {
            LOG.log(Level.FINE, "Oráculo sem cotação para {0}", dataAlvo);
        }
        return cotacao;
    }

    /**
     * Limpa todas as cotações guardadas no cache do oráculo.
     */
    public void limparCache() {
        contexto.getOraculoDAO().limpar();
    }

    /**
     * Lista as cotações dos últimos dias, da mais recente para a mais antiga.
     * Dias sem cotação no Oráculo ficam fora da lista, em vez de receber um valor inventado.
     * Pode acessar a rede: não deve ser chamado na thread do Swing.
     *
     * @param dias Quantidade de dias passados a serem consultados
     * @return as cotações da mais recente para a mais antiga, cada uma com a origem (Oráculo ou cache)
     */
    public Map<Cotacao, OrigemCotacao> obterHistoricoRecente(int dias) {
        LocalDate hoje = LocalDate.now();

        // Uma única consulta de faixa ao oráculo (em vez de uma por dia); vem do dia mais antigo ao mais recente
        List<Map.Entry<LocalDate, OraculoDAOCache.Consulta>> consultas =
                new ArrayList<>(contexto.getOraculoDAO().consultarPeriodo(hoje.minusDays(dias), hoje).entrySet());

        Map<Cotacao, OrigemCotacao> historico = new LinkedHashMap<>();
        for (Map.Entry<LocalDate, OraculoDAOCache.Consulta> item : consultas.reversed()) {
            OraculoDAOCache.Consulta consulta = item.getValue();
            if (consulta.valor() == null) {
                LOG.log(Level.FINE, "Histórico: {0} sem cotação, dia omitido", item.getKey());
                continue;
            }
            OrigemCotacao origem = consulta.doCache() ? OrigemCotacao.CACHE : OrigemCotacao.ORACULO;
            historico.put(new Cotacao(item.getKey(), consulta.valor()), origem);
        }

        LOG.log(Level.FINE, "Histórico de cotações montado: {0} dia(s)", historico.size());
        return historico;
    }
}
