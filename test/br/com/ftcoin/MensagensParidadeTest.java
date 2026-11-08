package br.com.ftcoin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Garante a internacionalização: os arquivos de mensagens em português, inglês e espanhol têm exatamente as
 * mesmas chaves, para nenhuma tela ficar sem texto ao trocar de idioma.
 */
class MensagensParidadeTest {

    private static Set<Object> chaves(String idioma) throws IOException {
        try (InputStream entrada = MensagensParidadeTest.class.getClassLoader()
                .getResourceAsStream("Messages_" + idioma + ".properties")) {
            assertNotNull(entrada, "Messages_" + idioma + ".properties não está no classpath");
            Properties propriedades = new Properties();
            propriedades.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
            return new TreeSet<>(propriedades.keySet());
        }
    }

    @ParameterizedTest(name = "{0} tem as mesmas chaves do português")
    @ValueSource(strings = { "en", "es" })
    @DisplayName("Chaves de mensagens iguais nos três idiomas")
    void mesmasChavesDoPortugues(String idioma) throws IOException {
        assertEquals(chaves("pt"), chaves(idioma));
    }
}
