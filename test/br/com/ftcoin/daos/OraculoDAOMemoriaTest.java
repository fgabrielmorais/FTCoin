package br.com.ftcoin.daos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes do oráculo simulado: determinístico por data e sempre dentro da faixa documentada (5,00 a 6,00).
 */
class OraculoDAOMemoriaTest {

    @Test
    @DisplayName("A mesma data devolve sempre o mesmo valor")
    void mesmaDataMesmoValor() {
        OraculoDAOMemoria oraculo = new OraculoDAOMemoria();
        LocalDate data = LocalDate.of(2026, 3, 15);
        assertEquals(oraculo.buscarCotacaoPorData(data), oraculo.buscarCotacaoPorData(data));
    }

    @Test
    @DisplayName("Limite: durante um ano as cotações ficam entre 5,00 e 6,00 com 2 casas")
    void faixaDeValores() {
        OraculoDAOMemoria oraculo = new OraculoDAOMemoria();
        BigDecimal minimo = new BigDecimal("5.00");
        BigDecimal maximo = new BigDecimal("6.00");
        for (LocalDate d = LocalDate.of(2026, 1, 1); d.isBefore(LocalDate.of(2027, 1, 1)); d = d.plusDays(1)) {
            BigDecimal valor = oraculo.buscarCotacaoPorData(d);
            assertTrue(valor.compareTo(minimo) >= 0 && valor.compareTo(maximo) <= 0, d + " -> " + valor);
            assertEquals(2, valor.scale());
        }
    }
}
