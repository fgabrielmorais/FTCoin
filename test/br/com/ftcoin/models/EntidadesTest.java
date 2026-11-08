package br.com.ftcoin.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes das validações das entidades {@link Carteira}, {@link Movimentacao} e {@link Cotacao}.
 */
class EntidadesTest {

    @Test
    @DisplayName("Carteira remove espaços das pontas do titular e da corretora")
    void carteiraRemoveEspacos() {
        Carteira carteira = new Carteira(1, "  Ana  ", " XP ");
        carteira.setNomeTitular("  Ana  ");
        carteira.setCorretora(" XP ");
        assertEquals("Ana", carteira.getNomeTitular());
        assertEquals("XP", carteira.getCorretora());
    }

    @Test
    @DisplayName("Exceção: titular nulo, vazio ou só com espaços")
    void titularInvalido() {
        Carteira carteira = new Carteira();
        for (String invalido : new String[] { null, "", "   " }) {
            RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> carteira.setNomeTitular(invalido));
            assertEquals("erro.carteira.titularObrigatorio", erro.getChave());
        }
    }

    @Test
    @DisplayName("Exceção: corretora vazia")
    void corretoraInvalida() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> new Carteira().setCorretora(" "));
        assertEquals("erro.carteira.corretoraObrigatoria", erro.getChave());
    }

    @Test
    @DisplayName("Movimentação: quantidade maior que zero é aceita e calcula o valor")
    void movimentacaoQuantidadeValida() {
        Movimentacao mov = new Movimentacao(1, 1, LocalDate.of(2026, 1, 1), TipoOperacao.COMPRA, new BigDecimal("2.5"));
        assertEquals(0, new BigDecimal("12.50").compareTo(mov.calcularValor(new BigDecimal("5"))));
    }

    @Test
    @DisplayName("Exceção: quantidade nula, zero ou negativa")
    void movimentacaoQuantidadeInvalida() {
        Movimentacao mov = new Movimentacao();
        for (BigDecimal invalida : new BigDecimal[] { null, BigDecimal.ZERO, new BigDecimal("-1") }) {
            RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> mov.setQuantidadeMovimentada(invalida));
            assertEquals("erro.movimentacao.quantidadeInvalida", erro.getChave());
        }
    }

    @Test
    @DisplayName("Cotação: variação sobre a anterior e sem anterior")
    void cotacaoVariacao() {
        Cotacao hoje = new Cotacao(LocalDate.of(2026, 1, 2), new BigDecimal("12.00"));
        Cotacao ontem = new Cotacao(LocalDate.of(2026, 1, 1), new BigDecimal("10.00"));
        assertEquals(0, new BigDecimal("2.00").compareTo(hoje.calcularVariacao(ontem)));
        assertEquals(0, BigDecimal.ZERO.compareTo(hoje.calcularVariacao(null)));
    }

    @Test
    @DisplayName("Exceção: cotação sem data ou sem valor")
    void cotacaoNula() {
        assertThrows(NullPointerException.class, () -> new Cotacao(null, BigDecimal.ONE));
        assertThrows(NullPointerException.class, () -> new Cotacao(LocalDate.now(), null));
    }
}
