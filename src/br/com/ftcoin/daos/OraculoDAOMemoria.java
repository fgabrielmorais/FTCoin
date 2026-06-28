package br.com.ftcoin.daos;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OraculoDAOMemoria implements IOraculoDAO {

    @Override
    public BigDecimal buscarCotacaoPorData(LocalDate data) {
        // Simula que o banco de dados tem a cotação de hoje registada
        return new BigDecimal("5.50");
    }
}