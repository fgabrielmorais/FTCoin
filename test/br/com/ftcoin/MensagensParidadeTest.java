package br.com.ftcoin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Garante a internacionalização: os arquivos de mensagens em português, inglês e espanhol têm exatamente as
 * mesmas chaves, para nenhuma tela ficar sem texto ao trocar de idioma.
 */
class MensagensParidadeTest {

    private static Properties carregar(String idioma) throws IOException {
        InputStream entrada = MensagensParidadeTest.class.getClassLoader()
                .getResourceAsStream("Messages_" + idioma + ".properties");
        assertNotNull(entrada, "Messages_" + idioma + ".properties não está no classpath");
        Properties propriedades = new Properties();
        propriedades.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        entrada.close();
        return propriedades;
    }

    @Test
    @DisplayName("Inglês tem as mesmas chaves do português")
    void inglesTemAsMesmasChaves() throws IOException {
        assertEquals(carregar("pt").keySet(), carregar("en").keySet());
    }

    @Test
    @DisplayName("Espanhol tem as mesmas chaves do português")
    void espanholTemAsMesmasChaves() throws IOException {
        assertEquals(carregar("pt").keySet(), carregar("es").keySet());
    }
}
