package br.com.ftcoin.gui.common;

/**
 * <p>
 * Classe de metadados e constantes estáticas do sistema gráfico <b>FTCoin Studio</b>.
 * </p>
 *
 * <p>
 * Centraliza identificadores de versão, dados de autoria, atribuições de equipe,
 * diretivas de copyright institucional e informações acadêmicas consumidas exclusivamente
 * pela camada de interface gráfica ({@code GUI}).
 * </p>
 *
 * @author Equipe FTCoin
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
    public static final String VERSAO = "2.5 (LTS)";

    /**
     * Identificador da plataforma de linguagem e tempo de execução.
     */
    public static final String PLATAFORMA_JAVA = "Java 21 LTS";

    /**
     * Data de publicação ou homologação do build atual.
     */
    public static final String DATA_LANCAMENTO = "Outubro de 2026";

    /**
     * Relação resumida dos autores e engenheiros do sistema.
     */
    public static final String AUTORES = "Ana, Arthur, Erik, Gabriel Morais Felix, Hyago, Isabela, Pedro, Vitor";

    /**
     * Declaração formal de reserva de direitos autorais (copyright).
     */
    public static final String COPYRIGHT = "© 2026 FTCoin Studio. Todos os direitos reservados.";

    /**
     * Instituição acadêmica vinculada ao projeto.
     */
    public static final String INSTITUICAO = "UNICAMP - Universidade Estadual de Campinas (Campus Limeira)";

    /**
     * Curso de graduação associado.
     */
    public static final String CURSO = "Bacharelado em Sistemas de Informação";

    /**
     * Disciplina de desenvolvimento de software.
     */
    public static final String DISCIPLINA = "Programação Orientada a Objetos (POO)";
}
