package br.com.ftcoin.gui.common;

import java.util.Locale;

/**
 * <p>
 * Enumeração para o seletor de idiomas suportados na interface gráfica.
 * </p>
 *
 * <p>
 * Representa os estados fixos de localização disponíveis para alternância dinâmica
 * pelo usuário nos componentes da {@code GUI}.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public enum IdiomaSelector {

    /**
     * Localização em Português do Brasil.
     */
    PORTUGUES(MensagensGui.LOCALE_PT, "gui.menu.idioma.pt", "🇧🇷 Português (BR)"),

    /**
     * Localização em Inglês dos Estados Unidos.
     */
    INGLES(MensagensGui.LOCALE_EN, "gui.menu.idioma.en", "🇺🇸 English (US)");

    /**
     * Localidade do sistema associada ao seletor.
     */
    private final Locale locale;

    /**
     * Chave do arquivo de propriedades para o nome do idioma.
     */
    private final String chaveMensagem;

    /**
     * Rótulo descritivo padrão com emoji de bandeira.
     */
    private final String descricaoPadrao;

    /**
     * Construtor da enumeração do seletor de idioma.
     *
     * @param locale          Instância de {@code Locale} correspondente
     * @param chaveMensagem   Chave do bundle de mensagens para internacionalização
     * @param descricaoPadrao Texto padrão de fallback para o componente visual
     */
    IdiomaSelector(Locale locale, String chaveMensagem, String descricaoPadrao) {
        this.locale = locale;
        this.chaveMensagem = chaveMensagem;
        this.descricaoPadrao = descricaoPadrao;
    }

    /**
     * Retorna a localidade ({@code Locale}) associada a este seletor.
     *
     * @return O {@code Locale} correspondente
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Retorna a chave do bundle de mensagens para este seletor.
     *
     * @return A chave de internacionalização {@code String}
     */
    public String getChaveMensagem() {
        return chaveMensagem;
    }

    /**
     * Retorna a descrição padrão com ícone para este seletor.
     *
     * @return A descrição padrão {@code String}
     */
    public String getDescricaoPadrao() {
        return descricaoPadrao;
    }

    /**
     * Retorna o texto formatado e internacionalizado para o seletor.
     *
     * @return O rótulo localizado do seletor {@code String}
     */
    public String getDescricaoLocalizada() {
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();
        if (mensagens != null) {
            String prefixo = (this == PORTUGUES) ? "🇧🇷 " : "🇺🇸 ";
            return prefixo + mensagens.get(chaveMensagem);
        }
        return descricaoPadrao;
    }

    /**
     * Retorna a representação textual para renderização no componente de seleção da GUI.
     *
     * @return O texto de apresentação no {@code JComboBox}
     */
    @Override
    public String toString() {
        return getDescricaoLocalizada();
    }

    /**
     * Converte um {@code Locale} no seletor de idioma correspondente.
     *
     * @param locale Localidade a ser verificada
     * @return A instância de {@code IdiomaSelector} correspondente
     */
    public static IdiomaSelector porLocale(Locale locale) {
        if (locale != null && "en".equalsIgnoreCase(locale.getLanguage())) {
            return INGLES;
        }
        return PORTUGUES;
    }
}
