package br.com.ftcoin.gui.common;

import java.util.Locale;

/**
 * Representa os idiomas disponíveis na interface gráfica.
 */
public enum IdiomaSelector {

    PORTUGUES(
            MensagensGui.LOCALE_PT,
            "gui.menu.idioma.pt",
            "🇧🇷 Português (BR)"
    ),

    INGLES(
            MensagensGui.LOCALE_EN,
            "gui.menu.idioma.en",
            "🇺🇸 English (US)"
    ),

    ESPANHOL(
            MensagensGui.LOCALE_ES,
            "gui.menu.idioma.es",
            "🇪🇸 Español (ES)"
    );

    private final Locale locale;
    private final String chaveMensagem;
    private final String descricaoPadrao;

    IdiomaSelector(
            Locale locale,
            String chaveMensagem,
            String descricaoPadrao
    ) {
        this.locale = locale;
        this.chaveMensagem = chaveMensagem;
        this.descricaoPadrao = descricaoPadrao;
    }

    /**
     * Retorna a localidade associada ao idioma.
     *
     * @return localidade do idioma
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Retorna a chave de tradução do nome do idioma.
     *
     * @return chave da mensagem
     */
    public String getChaveMensagem() {
        return chaveMensagem;
    }

    /**
     * Retorna a descrição utilizada como alternativa.
     *
     * @return descrição padrão
     */
    public String getDescricaoPadrao() {
        return descricaoPadrao;
    }

    /**
     * Retorna o nome do idioma acompanhado de sua bandeira.
     *
     * @return descrição para o seletor
     */
    public String getDescricaoLocalizada() {
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        if (mensagens == null) {
            return descricaoPadrao;
        }

        String descricao = mensagens.get(chaveMensagem);

        if (descricao.equals("!" + chaveMensagem + "!")) {
            return descricaoPadrao;
        }

        String prefixo = switch (this) {
            case PORTUGUES -> "🇧🇷 ";
            case INGLES -> "🇺🇸 ";
            case ESPANHOL -> "🇪🇸 ";
        };

        return prefixo + descricao;
    }

    /**
     * Retorna o texto exibido no componente de seleção.
     *
     * @return descrição do idioma
     */
    @Override
    public String toString() {
        return getDescricaoLocalizada();
    }

    /**
     * Identifica a opção correspondente à localidade informada.
     *
     * @param locale localidade a identificar
     * @return idioma correspondente ou português como padrão
     */
    public static IdiomaSelector porLocale(Locale locale) {
        if (locale == null) {
            return PORTUGUES;
        }

        for (IdiomaSelector idioma : values()) {
            if (idioma.getLocale().getLanguage()
                    .equalsIgnoreCase(locale.getLanguage())) {
                return idioma;
            }
        }

        return PORTUGUES;
    }
}
