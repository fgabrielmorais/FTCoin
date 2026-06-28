package br.com.ftcoin.daos;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface IOraculoDAO {
	BigDecimal buscarCotacaoPorData(LocalDate data);
}
