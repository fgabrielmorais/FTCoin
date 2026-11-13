package br.com.ftcoin.gui.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.ftcoin.models.RegraNegocioException;

/**
 * Testes de {@link MensagensGui}: datas nas mensagens saem como DD/MM/AAAA, e não em formato ISO.
 */
class MensagensGuiTest {

    @Test
    @DisplayName("Data em mensagem de erro sai como DD/MM/AAAA nos três idiomas")
    void dataNaMensagem() {
        MensagensGui mensagens = new MensagensGui(MensagensGui.LOCALE_PT);
        for (java.util.Locale locale : new java.util.Locale[] { MensagensGui.LOCALE_PT, MensagensGui.LOCALE_EN,
                MensagensGui.LOCALE_ES }) {
            mensagens.setLocale(locale);
            String texto = mensagens.descreverErro(new RegraNegocioException("erro.oraculo.semCotacao", LocalDate.of(2000, 6, 1)));
            assertTrue(texto.contains("01/06/2000"), texto);
            String futura = mensagens.descreverErro(new RegraNegocioException("erro.oraculo.dataFutura", LocalDate.of(2050, 11, 15)));
            assertTrue(futura.contains("15/11/2050"), futura);
        }
    }

    @Test
    @DisplayName("Mensagem de saldo negativo na data mostra a data formatada")
    void saldoNegativoNaData() {
        MensagensGui mensagens = new MensagensGui(MensagensGui.LOCALE_PT);
        String texto = mensagens.get("erro.movimentacao.saldoNegativoNaData", LocalDate.of(2026, 1, 1), "0", "500");
        assertEquals("Operação recusada: em 01/01/2026 a carteira teria 0 FTC e a venda é de 500 FTC.", texto);
    }
}
