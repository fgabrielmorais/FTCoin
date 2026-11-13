package br.com.ftcoin.gui.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.ftcoin.models.RegraNegocioException;

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

    private static BigDecimal n(String valor) {
        return new BigDecimal(valor);
    }

    private static void assertQuantidade(String esperado, GuiFormatador formatador, String texto) {
        assertEquals(0, n(esperado).compareTo(formatador.lerQuantidade(texto)), texto);
    }

    private static void assertInvalida(GuiFormatador formatador, String texto) {
        assertThrows(NumberFormatException.class, () -> formatador.lerQuantidade(texto), texto);
    }

    @Test
    @DisplayName("Português: ponto é milhar e vírgula é decimal")
    void quantidadeEmPortugues() {
        GuiFormatador pt = new GuiFormatador(Locale.forLanguageTag("pt-BR"));
        assertQuantidade("1000", pt, "1.000");
        assertQuantidade("2600", pt, "2.600");
        assertQuantidade("1000", pt, "1000");
        assertQuantidade("1.5", pt, "1,5");
        assertQuantidade("1000.5", pt, "1.000,50");
        assertQuantidade("1234567.125", pt, " 1.234.567,125 ");
        assertQuantidade("-5", pt, "-5");
        assertInvalida(pt, "1.5");
        assertInvalida(pt, "1.2.3");
        assertInvalida(pt, "1.00");
    }

    @Test
    @DisplayName("Espanhol lê como o português")
    void quantidadeEmEspanhol() {
        GuiFormatador es = new GuiFormatador(Locale.forLanguageTag("es-ES"));
        assertQuantidade("1000", es, "1.000");
        assertQuantidade("1000", es, "1000");
        assertQuantidade("1.5", es, "1,5");
        assertQuantidade("1000.5", es, "1.000,50");
        assertInvalida(es, "1.5");
    }

    @Test
    @DisplayName("Inglês: vírgula é milhar e ponto é decimal")
    void quantidadeEmIngles() {
        GuiFormatador en = new GuiFormatador(Locale.US);
        assertQuantidade("1000", en, "1,000");
        assertQuantidade("1000", en, "1000");
        assertQuantidade("1.5", en, "1.5");
        assertQuantidade("1000.5", en, "1,000.50");
        assertInvalida(en, "1,5");
        assertInvalida(en, "1,2,3");
    }

    @Test
    @DisplayName("Texto que não é número é recusado em todos os idiomas")
    void quantidadeInvalida() {
        for (Locale locale : new Locale[] { Locale.forLanguageTag("pt-BR"), Locale.US, Locale.forLanguageTag("es-ES") }) {
            GuiFormatador formatador = new GuiFormatador(locale);
            assertInvalida(formatador, "abc");
            assertInvalida(formatador, "12abc");
            assertInvalida(formatador, "");
            assertInvalida(formatador, "   ");
            assertInvalida(formatador, "--5");
            assertInvalida(formatador, null);
        }
    }

    @Test
    @DisplayName("Mais de 3 casas decimais é recusado, não arredondado")
    void quantidadeComCasasDemais() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> new GuiFormatador(Locale.forLanguageTag("pt-BR")).lerQuantidade("1,2345"));
        assertEquals("gui.mov.erro.casasDecimais", erro.getChave());
        assertThrows(RegraNegocioException.class, () -> new GuiFormatador(Locale.US).lerQuantidade("1.2345"));
        assertQuantidade("1.234", new GuiFormatador(Locale.US), "1.234");
    }
}
