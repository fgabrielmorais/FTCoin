package br.com.ftcoin.gui.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.ftcoin.daos.TipoPersistencia;
import br.com.ftcoin.gui.common.MensagensGui;

/**
 * Testes da leitura dos argumentos de partida, sem depender de Swing.
 */
class ArgumentosAplicacaoTest {

    private static ArgumentosAplicacao ler(Locale sistema, String... args) {
        return ArgumentosAplicacao.ler(args, sistema);
    }

    @Test
    @DisplayName("Sem argumentos: idioma do sistema (pt/en/es), memória e sem demonstração")
    void padroes() {
        ArgumentosAplicacao a = ler(Locale.of("es", "MX"));
        assertEquals(MensagensGui.LOCALE_ES, a.locale());
        assertEquals(TipoPersistencia.MEMORIA, a.persistencia());
        assertFalse(a.demonstracao());
        assertEquals(MensagensGui.LOCALE_EN, ler(Locale.US).locale());
    }

    @Test
    @DisplayName("Idioma do sistema desconhecido cai em português")
    void idiomaDoSistemaDesconhecido() {
        assertEquals(MensagensGui.LOCALE_PT, ler(Locale.JAPAN).locale());
    }

    @Test
    @DisplayName("Cada valor válido de --lang")
    void idiomas() {
        assertEquals(MensagensGui.LOCALE_PT, ler(Locale.US, "--lang=pt").locale());
        assertEquals(MensagensGui.LOCALE_PT, ler(Locale.US, "--lang=pt_BR").locale());
        assertEquals(MensagensGui.LOCALE_EN, ler(Locale.JAPAN, "--lang=en").locale());
        assertEquals(MensagensGui.LOCALE_EN, ler(Locale.JAPAN, "--lang=en_US").locale());
        assertEquals(MensagensGui.LOCALE_ES, ler(Locale.JAPAN, "--lang=es").locale());
        assertEquals(MensagensGui.LOCALE_ES, ler(Locale.JAPAN, "--lang=es_ES").locale());
    }

    @Test
    @DisplayName("Cada valor válido de --db")
    void bancos() {
        assertEquals(TipoPersistencia.MEMORIA, ler(Locale.US, "--db=memory").persistencia());
        assertEquals(TipoPersistencia.MARIADB, ler(Locale.US, "--db=mariadb").persistencia());
    }

    @Test
    @DisplayName("--demo liga a demonstração")
    void demonstracao() {
        assertTrue(ler(Locale.US, "--demo").demonstracao());
    }

    @Test
    @DisplayName("Caixa mista é aceita")
    void caixaMista() {
        ArgumentosAplicacao a = ler(Locale.JAPAN, "--LANG=Es_es", "--Db=MariaDB", "--DEMO");
        assertEquals(MensagensGui.LOCALE_ES, a.locale());
        assertEquals(TipoPersistencia.MARIADB, a.persistencia());
        assertTrue(a.demonstracao());
    }

    @Test
    @DisplayName("Combinação das três opções, em qualquer ordem")
    void combinacao() {
        ArgumentosAplicacao a = ler(Locale.JAPAN, "--demo", "--db=mariadb", "--lang=en");
        assertEquals(MensagensGui.LOCALE_EN, a.locale());
        assertEquals(TipoPersistencia.MARIADB, a.persistencia());
        assertTrue(a.demonstracao());
    }

    @Test
    @DisplayName("Valores inválidos são recusados")
    void valoresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> ler(Locale.US, "--lang=xx"));
        assertThrows(IllegalArgumentException.class, () -> ler(Locale.US, "--lang="));
        assertThrows(IllegalArgumentException.class, () -> ler(Locale.US, "--db=oracle"));
    }

    @Test
    @DisplayName("Argumento desconhecido é recusado")
    void argumentoDesconhecido() {
        assertThrows(IllegalArgumentException.class, () -> ler(Locale.US, "--foo"));
        assertThrows(IllegalArgumentException.class, () -> ler(Locale.US, "--demo=sim"));
        assertThrows(IllegalArgumentException.class, () -> ler(Locale.US, "pt"));
    }
}
