package br.com.ftcoin.gui.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes de {@link GuiFormatador}: formato único de data nos três idiomas, leitura de datas digitadas e
 * percentuais com e sem sinal.
 */
class GuiFormatadorTest {

    @Test
    @DisplayName("A data é mostrada como DD/MM/AAAA em qualquer idioma")
    void dataComFormatoUnico() {
        LocalDate data = LocalDate.of(2026, 11, 9);
        assertEquals("09/11/2026", new GuiFormatador(Locale.forLanguageTag("pt-BR")).data(data));
        assertEquals("09/11/2026", new GuiFormatador(Locale.US).data(data));
        assertEquals("09/11/2026", new GuiFormatador(Locale.forLanguageTag("es-ES")).data(data));
    }

    @Test
    @DisplayName("Data nula vira um traço")
    void dataNula() {
        assertEquals("-", new GuiFormatador(Locale.US).data(null));
    }

    @Test
    @DisplayName("A leitura aceita DD/MM/AAAA, AAAA-MM-DD e espaços nas pontas")
    void lerDataNosDoisFormatos() {
        LocalDate esperada = LocalDate.of(2026, 11, 9);
        assertEquals(esperada, GuiFormatador.lerData("09/11/2026"));
        assertEquals(esperada, GuiFormatador.lerData("2026-11-09"));
        assertEquals(esperada, GuiFormatador.lerData("  09/11/2026  "));
    }

    @Test
    @DisplayName("Exceção: texto que não é data, ou data que não existe")
    void lerDataInvalida() {
        assertThrows(DateTimeParseException.class, () -> GuiFormatador.lerData("abc"));
        assertThrows(DateTimeParseException.class, () -> GuiFormatador.lerData("31/02/2026"));
        assertThrows(DateTimeParseException.class, () -> GuiFormatador.lerData(""));
    }

    @Test
    @DisplayName("Percentual com sinal e sem sinal")
    void percentuais() {
        GuiFormatador formatador = new GuiFormatador(Locale.US);
        assertEquals("+12.50%", formatador.percentual(new BigDecimal("12.5")));
        assertEquals("-3.00%", formatador.percentual(new BigDecimal("-3")));
        assertEquals("12.50%", formatador.percentualSimples(new BigDecimal("12.5")));
        assertEquals("0.00%", formatador.percentualSimples(null));
    }
}
