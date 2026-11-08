package br.com.ftcoin.daos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import network.ExchangeRate;

/**
 * Cache em volta de qualquer oráculo, para reduzir as consultas pela rede.
 * <p>
 * Cada data é buscada no oráculo uma vez por execução. A ausência de cotação (que não é erro) de uma data
 * passada também fica guardada. A data de hoje sem cotação não fica no cache, porque o servidor pode
 * publicá-la mais tarde. Falhas (rede, autenticação) nunca ficam no cache.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class OraculoDAOCache extends OraculoDAO {

	private static final Logger LOG = Logger.getLogger(OraculoDAOCache.class.getName());

	private static final int CASAS_DECIMAIS = 2;

	/**
	 * Resultado de uma consulta ao cache.
	 *
	 * @param valor cotação do dia, ou {@code null} se o oráculo não tem cotação para a data
	 * @param doCache {@code true} se o valor veio do cache, {@code false} se veio do oráculo
	 */
	public record Consulta(BigDecimal valor, boolean doCache) {}

	private final OraculoDAO oraculo;
	private final Map<LocalDate, Optional<BigDecimal>> cache = new ConcurrentHashMap<>();

	/**
	 * Cria o cache em volta de um oráculo.
	 *
	 * @param oraculo oráculo consultado nas datas que ainda não estão no cache
	 */
	public OraculoDAOCache(OraculoDAO oraculo) {
		this.oraculo = oraculo;
	}

	/**
	 * Devolve a cotação de uma data, pelo cache quando possível.
	 *
	 * @param data dia da cotação
	 * @return a cotação, ou {@code null} se o oráculo não tem cotação para a data
	 * @throws OraculoException se o oráculo falhar; a falha não fica no cache
	 */
	@Override
	public BigDecimal buscarCotacaoPorData(LocalDate data) {
		return consultar(data).valor();
	}

	/**
	 * Consulta a cotação de uma data e informa se ela veio do cache.
	 *
	 * @param data dia da cotação
	 * @return o valor (ou {@code null} sem cotação) e a origem
	 * @throws OraculoException se o oráculo falhar; a falha não fica no cache
	 */
	public Consulta consultar(LocalDate data) {
		Optional<BigDecimal> emCache = cache.get(data);
		if (emCache != null) {
			LOG.log(Level.FINE, "Cotação de {0} servida pelo cache", data);
			return new Consulta(emCache.orElse(null), true);
		}

		BigDecimal valor = oraculo.buscarCotacaoPorData(data);
		guardar(data, valor);
		return new Consulta(valor, false);
	}

	/**
	 * Devolve a cotação de cada dia de um período, em ordem. Com o oráculo da API, os dias que faltam no cache
	 * são pedidos numa única requisição de faixa; nos demais oráculos, a consulta é feita dia a dia.
	 *
	 * @param inicio primeiro dia do período
	 * @param fim último dia do período
	 * @return uma consulta por dia, do primeiro ao último, na ordem das datas
	 * @throws OraculoException se o oráculo falhar; a falha não fica no cache
	 */
	public Map<LocalDate, Consulta> consultarPeriodo(LocalDate inicio, LocalDate fim) {
		Map<LocalDate, Consulta> resultado = new LinkedHashMap<>();

		if (oraculo instanceof OraculoDAOApi api && algumDiaForaDoCache(inicio, fim)) {
			Map<LocalDate, BigDecimal> recebidas = new java.util.HashMap<>();
			for (ExchangeRate cotacao : api.buscarCotacoesPorPeriodo(inicio, fim)) {
				if (cotacao.getDate() != null && cotacao.getValue() != null) {
					recebidas.put(cotacao.getDate(), cotacao.getValue().setScale(CASAS_DECIMAIS, RoundingMode.HALF_EVEN));
				}
			}
			for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) {
				Optional<BigDecimal> emCache = cache.get(d);
				if (emCache != null) {
					resultado.put(d, new Consulta(emCache.orElse(null), true));
				} else {
					BigDecimal valor = recebidas.get(d);
					guardar(d, valor);
					resultado.put(d, new Consulta(valor, false));
				}
			}
			return resultado;
		}

		for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) {
			resultado.put(d, consultar(d));
		}
		return resultado;
	}

	/**
	 * Faz a próxima consulta dessa data ir ao oráculo.
	 *
	 * @param data dia cuja cotação guardada deve ser esquecida
	 */
	public void descartar(LocalDate data) {
		cache.remove(data);
	}

	/**
	 * Esquece todas as cotações guardadas.
	 */
	public void limpar() {
		LOG.log(Level.FINE, "Cache do oráculo limpo ({0} data(s) descartada(s))", cache.size());
		cache.clear();
	}

	private boolean algumDiaForaDoCache(LocalDate inicio, LocalDate fim) {
		for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) {
			if (!cache.containsKey(d)) {
				return true;
			}
		}
		return false;
	}

	// Guarda valores e a ausência de cotação de datas passadas; hoje sem cotação fica de fora
	private void guardar(LocalDate data, BigDecimal valor) {
		if (valor != null || data.isBefore(LocalDate.now())) {
			cache.put(data, Optional.ofNullable(valor));
		}
	}
}
