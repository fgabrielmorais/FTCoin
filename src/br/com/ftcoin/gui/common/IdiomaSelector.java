package br.com.ftcoin.gui.common;

import java.util.Locale;

/**
 * Opções do seletor de idioma da barra superior, ligadas ao {@link Locale} correspondente.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public enum IdiomaSelector {

    /** Português do Brasil. */
    PORTUGUES(MensagensGui.LOCALE_PT, "gui.menu.idioma.pt"),

    /** Inglês dos Estados Unidos. */
    INGLES(MensagensGui.LOCALE_EN, "gui.menu.idioma.en"),

    /** Espanhol da Espanha. */
    ESPANHOL(MensagensGui.LOCALE_ES, "gui.menu.idioma.es");

    private final Locale locale;
    private final String chaveMensagem;

    IdiomaSelector(Locale locale, String chaveMensagem) {
        this.locale = locale;
        this.chaveMensagem = chaveMensagem;
    }

    /**
     * Devolve o idioma da opção.
     *
     * @return o {@link Locale} da opção
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Devolve o nome do idioma, como aparece no seletor.
     *
     * @param mensagens textos do idioma ativo
     * @return o nome do idioma
     */
    public String getDescricaoLocalizada(MensagensGui mensagens) {
        return mensagens.get(chaveMensagem);
    }

    /**
     * Devolve a opção correspondente a um idioma.
     *
     * @param locale idioma desejado
     * @return a opção com o mesmo idioma, sem olhar o país; português quando o idioma não tem tradução
     */
    public static IdiomaSelector porLocale(Locale locale) {
        if (locale != null) {
            for (IdiomaSelector opcao : values()) {
                if (opcao.locale.getLanguage().equalsIgnoreCase(locale.getLanguage())) {
                    return opcao;
                }
            }
        }
        return PORTUGUES;
    }
}
