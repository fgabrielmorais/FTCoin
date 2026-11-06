package br.com.ftcoin.daos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Oráculo simulado: a cotação oscila em torno de uma base e a mesma data devolve sempre o mesmo valor.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class OraculoDAOMemoria extends OraculoDAO {

	private static final Logger LOG = Logger.getLogger(OraculoDAOMemoria.class.getName());

	private static final BigDecimal COTACAO_BASE = new BigDecimal("5.50");
	private static final int OSCILACAO_MAXIMA_CENTAVOS = 50;
	private static final int PASSO_DIARIO_CENTAVOS = 37;

	/**
	 * Cria o oráculo simulado.
	 */
	public OraculoDAOMemoria() {
		super();
	}

	/**
	 * Calcula a cotação simulada da data, entre a base menos e a base mais a oscilação máxima.
	 *
	 * @param data dia da cotação
	 * @return a cotação simulada; nunca {@code null}
	 */
	@Override
	public BigDecimal buscarCotacaoPorData(LocalDate data) {
		int faixaCentavos = 2 * OSCILACAO_MAXIMA_CENTAVOS + 1;
		long oscilacaoCentavos = Math.floorMod(data.toEpochDay() * PASSO_DIARIO_CENTAVOS, faixaCentavos) - OSCILACAO_MAXIMA_CENTAVOS;
		BigDecimal cotacao = COTACAO_BASE.add(BigDecimal.valueOf(oscilacaoCentavos, 2));
		LOG.log(Level.FINEST, "Cotação simulada de {0} = {1}", new Object[] { data, cotacao });
		return cotacao;
	}
}
