package br.com.ftcoin.daos;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OraculoDAOMemoria extends OraculoDAO {

    private static final BigDecimal COTACAO_BASE = new BigDecimal("5.50");
    private static final int OSCILACAO_MAXIMA_CENTAVOS = 50;
    private static final int PASSO_DIARIO_CENTAVOS = 37;

    // Simula o oráculo: a cotação oscila em torno da base e a mesma data devolve sempre o mesmo valor
    @Override
    public BigDecimal buscarCotacaoPorData(LocalDate data) {
        int faixaCentavos = 2 * OSCILACAO_MAXIMA_CENTAVOS + 1;
        long oscilacaoCentavos = Math.floorMod(data.toEpochDay() * PASSO_DIARIO_CENTAVOS, faixaCentavos) - OSCILACAO_MAXIMA_CENTAVOS;
        return COTACAO_BASE.add(BigDecimal.valueOf(oscilacaoCentavos, 2));
    }
}
