package br.com.ftcoin.gui.common;

import java.util.Map;

/**
 * <p>
 * Classe de metadados e constantes estáticas do sistema gráfico <b>FTCoin Studio</b>.
 * </p>
 *
 * <p>
 * Centraliza o nome, a versão e a plataforma do sistema. Os textos traduzíveis (instituição,
 * curso, copyright) ficam nos arquivos HTML de {@code gui/textos}, um por idioma.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public final class Info {

    /**
     * Construtor privado para impedir instanciação da classe utilitária de metadados.
     */
    private Info() {
        // Construtor privado para classe utilitária
    }

    /**
     * Nome comercial e institucional da aplicação.
     */
    public static final String NOME_SISTEMA = "FTCoin Studio";

    /**
     * Versão corrente de distribuição e homologação do software.
     */
    public static final String VERSAO = "1.0.0";

    /**
     * Identificador da plataforma de linguagem e tempo de execução.
     */
    public static final String PLATAFORMA_JAVA = "Java 21 LTS";

    /**
     * Relação resumida dos autores do sistema.
     */
    public static final String AUTORES = "Ana, Arthur, Erik, Gabriel Morais Felix, Hyago, Isabela, Pedro, Vitor";

    /**
     * Devolve os valores que preenchem as marcações dos textos em HTML (manual e créditos).
     *
     * @return mapa com {@code sistema}, {@code versao} e {@code plataforma}
     */
    public static Map<String, String> valoresDocumentos() {
        return Map.of(
                "sistema", NOME_SISTEMA,
                "versao", VERSAO,
                "plataforma", PLATAFORMA_JAVA
        );
    }
}
