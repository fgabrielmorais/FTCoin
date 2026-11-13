package br.com.ftcoin.daos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes do oráculo simulado: determinístico por data, dentro da faixa documentada (5,00 a 6,00) e fiel ao contrato do servidor
 * (sem cotação antes de 01/01/2009, erro para data futura).
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
    @DisplayName("Limite: de 01/01/2026 até hoje as cotações ficam entre 5,00 e 6,00 com 2 casas")
    void faixaDeValores() {
        OraculoDAOMemoria oraculo = new OraculoDAOMemoria();
        BigDecimal minimo = new BigDecimal("5.00");
        BigDecimal maximo = new BigDecimal("6.00");
        for (LocalDate d = LocalDate.of(2026, 1, 1); !d.isAfter(LocalDate.now()); d = d.plusDays(1)) {
            BigDecimal valor = oraculo.buscarCotacaoPorData(d);
            assertTrue(valor.compareTo(minimo) >= 0 && valor.compareTo(maximo) <= 0, d + " -> " + valor);
            assertEquals(2, valor.scale());
        }
    }

    @Test
    @DisplayName("Antes da primeira cotação (01/01/2009) não há cotação")
    void antesDaPrimeiraCotacao() {
        OraculoDAOMemoria oraculo = new OraculoDAOMemoria();
        assertNull(oraculo.buscarCotacaoPorData(LocalDate.of(2008, 12, 31)));
        assertNull(oraculo.buscarCotacaoPorData(LocalDate.of(2000, 6, 1)));
    }

    @Test
    @DisplayName("Da primeira cotação até hoje há valor, inclusive nas datas do roteiro")
    void haValorDaPrimeiraCotacaoAteHoje() {
        OraculoDAOMemoria oraculo = new OraculoDAOMemoria();
        assertNotNull(oraculo.buscarCotacaoPorData(OraculoDAOMemoria.PRIMEIRA_COTACAO));
        assertNotNull(oraculo.buscarCotacaoPorData(LocalDate.of(2025, 12, 31)));
        assertNotNull(oraculo.buscarCotacaoPorData(LocalDate.of(2026, 1, 1)));
        assertNotNull(oraculo.buscarCotacaoPorData(LocalDate.of(2026, 10, 31)));
        assertNotNull(oraculo.buscarCotacaoPorData(LocalDate.now()));
    }

    @Test
    @DisplayName("Exceção: data futura lança OraculoException, como o servidor")
    void dataFutura() {
        OraculoDAOMemoria oraculo = new OraculoDAOMemoria();
        OraculoException erro = assertThrows(OraculoException.class,
                () -> oraculo.buscarCotacaoPorData(LocalDate.of(2050, 11, 15)));
        assertEquals("erro.oraculo.dataFutura", erro.getChave());
        assertThrows(OraculoException.class, () -> oraculo.buscarCotacaoPorData(LocalDate.now().plusDays(1)));
    }
}
