package br.com.ftcoin.daos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

// Cache em volta de qualquer oráculo: cada data é buscada no oráculo uma vez por execução.
// Só guarda cotações encontradas. Data sem cotação e falhas não ficam no cache,
// para que a próxima consulta tente de novo.
public class OraculoDAOCache extends OraculoDAO {

	private static final Logger LOG = Logger.getLogger(OraculoDAOCache.class.getName());

	//Resultado de uma consulta: o valor (null se não há cotação) e se veio do cache
	public record Consulta(BigDecimal valor, boolean doCache) {}

	private final OraculoDAO oraculo;
	private final Map<LocalDate, BigDecimal> cache = new ConcurrentHashMap<>();

	public OraculoDAOCache(OraculoDAO oraculo) {
		this.oraculo = oraculo;
	}

	@Override
	public BigDecimal buscarCotacaoPorData(LocalDate data) {
		return consultar(data).valor();
	}

	public Consulta consultar(LocalDate data) {
		BigDecimal emCache = cache.get(data);
		if (emCache != null) {
			LOG.log(Level.FINE, "Cotação de {0} servida pelo cache", data);
			return new Consulta(emCache, true);
		}

		BigDecimal valor = oraculo.buscarCotacaoPorData(data);
		if (valor != null) {
			cache.put(data, valor);
		}
		return new Consulta(valor, false);
	}

	//Faz a próxima consulta dessa data ir ao oráculo
	public void descartar(LocalDate data) {
		cache.remove(data);
	}

	public void limpar() {
		LOG.log(Level.FINE, "Cache do oráculo limpo ({0} data(s) descartada(s))", cache.size());
		cache.clear();
	}
}
