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

// Cache em volta de qualquer oráculo: cada data é buscada no oráculo uma vez por execução.
// Guarda também a ausência de cotação (não é erro) para datas passadas.
// A data de hoje sem cotação não fica no cache: o servidor pode publicá-la mais tarde.
// Falhas (rede, autenticação) nunca ficam no cache.
public class OraculoDAOCache extends OraculoDAO {

	private static final Logger LOG = Logger.getLogger(OraculoDAOCache.class.getName());

	//Resultado de uma consulta: o valor (null se não há cotação) e se veio do cache
	public record Consulta(BigDecimal valor, boolean doCache) {}

	private final OraculoDAO oraculo;
	private final Map<LocalDate, Optional<BigDecimal>> cache = new ConcurrentHashMap<>();

	public OraculoDAOCache(OraculoDAO oraculo) {
		this.oraculo = oraculo;
	}

	@Override
	public BigDecimal buscarCotacaoPorData(LocalDate data) {
		return consultar(data).valor();
	}

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

	//Cotações de cada dia de [inicio, fim], em ordem. Com o oráculo da API, os dias que
	//faltam no cache são pedidos numa única requisição de faixa.
	public Map<LocalDate, Consulta> consultarPeriodo(LocalDate inicio, LocalDate fim) {
		Map<LocalDate, Consulta> resultado = new LinkedHashMap<>();

		if (oraculo instanceof OraculoDAOApi api && algumDiaForaDoCache(inicio, fim)) {
			Map<LocalDate, BigDecimal> recebidas = new java.util.HashMap<>();
			for (ExchangeRate cotacao : api.buscarCotacoesPorPeriodo(inicio, fim)) {
				if (cotacao.getDate() != null && cotacao.getValue() != null) {
					recebidas.put(cotacao.getDate(), cotacao.getValue().setScale(2, RoundingMode.HALF_EVEN));
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

	//Faz a próxima consulta dessa data ir ao oráculo
	public void descartar(LocalDate data) {
		cache.remove(data);
	}

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

	private void guardar(LocalDate data, BigDecimal valor) {
		if (valor != null || data.isBefore(LocalDate.now())) {
			cache.put(data, Optional.ofNullable(valor));
		}
	}
}
