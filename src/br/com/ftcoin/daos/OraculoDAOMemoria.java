package br.com.ftcoin.daos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Oráculo simulado: a cotação oscila em torno de uma base e a mesma data devolve sempre o mesmo valor.
 * Repete o contrato do servidor real ({@link OraculoDAOApi}): data posterior a hoje é uma falha
 * ({@link OraculoException}), data anterior à primeira cotação publicada não tem cotação ({@code null})
 * e as demais têm valor.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class OraculoDAOMemoria extends OraculoDAO {

	private static final Logger LOG = Logger.getLogger(OraculoDAOMemoria.class.getName());

	/** Primeira data com cotação; espelha a primeira data publicada pelo servidor. */
	public static final LocalDate PRIMEIRA_COTACAO = LocalDate.of(2009, 1, 1);

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
	 * @return a cotação simulada, ou {@code null} se a data for anterior a {@link #PRIMEIRA_COTACAO}
	 * @throws OraculoException se a data for posterior a hoje
	 */
	@Override
	public BigDecimal buscarCotacaoPorData(LocalDate data) {
		if (data.isAfter(LocalDate.now())) {
			throw new OraculoException("erro.oraculo.dataFutura", data);
		}
		if (data.isBefore(PRIMEIRA_COTACAO)) {
			return null;
		}
		int faixaCentavos = 2 * OSCILACAO_MAXIMA_CENTAVOS + 1;
		long oscilacaoCentavos = Math.floorMod(data.toEpochDay() * PASSO_DIARIO_CENTAVOS, faixaCentavos) - OSCILACAO_MAXIMA_CENTAVOS;
		BigDecimal cotacao = COTACAO_BASE.add(BigDecimal.valueOf(oscilacaoCentavos, 2));
		LOG.log(Level.FINEST, "Cotação simulada de {0} = {1}", new Object[] { data, cotacao });
		return cotacao;
	}
}
