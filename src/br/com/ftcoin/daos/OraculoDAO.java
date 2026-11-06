package br.com.ftcoin.daos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrato de consulta à cotação diária do FTCoin.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public abstract class OraculoDAO {

	/**
	 * Construtor usado pelas implementações em memória e MariaDB.
	 */
	protected OraculoDAO() {
		// Sem estado comum entre as implementações
	}

	/**
	 * Busca a cotação do FTCoin, em reais, de uma data.
	 *
	 * @param data dia da cotação
	 * @return a cotação do dia, ou {@code null} se não houver cotação para a data
	 */
	public abstract BigDecimal buscarCotacaoPorData(LocalDate data);
}
