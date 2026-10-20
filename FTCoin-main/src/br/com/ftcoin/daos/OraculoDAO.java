package br.com.ftcoin.daos;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class OraculoDAO {

	//Devolve null quando não há cotação cadastrada para a data
	public abstract BigDecimal buscarCotacaoPorData(LocalDate data);
}
