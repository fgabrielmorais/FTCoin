package br.com.ftcoin.gui.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Centraliza as traduções da interface gráfica.
 *
 * Suporta português, inglês e espanhol, com troca de idioma
 * durante a execução e notificação dos componentes registrados.
 */
public class MensagensGui {

    private static final String BUNDLE_BASE_NAME = "Messages";

    /**
     * Português do Brasil.
     */
    public static final Locale LOCALE_PT = Locale.of("pt", "BR");

    /**
     * Inglês dos Estados Unidos.
     */
    public static final Locale LOCALE_EN = Locale.of("en", "US");

    /**
     * Espanhol da Espanha.
     */
    public static final Locale LOCALE_ES = Locale.of("es", "ES");

    private Locale localeAtual;
    private ResourceBundle bundle;
    private GuiFormatador formatador;

    private final List<Runnable> ouvintesMudancaIdioma = new ArrayList<>();

    /**
     * Inicializa as mensagens no idioma informado.
     *
     * @param localeInicial idioma inicial; null utiliza português
     */
    public MensagensGui(Locale localeInicial) {
        setLocale(localeInicial);
    }

    /**
     * Troca o idioma, recarrega as traduções e avisa os componentes.
     *
     * Idiomas não disponíveis utilizam português.
     *
     * @param novoLocale idioma desejado
     */
    public synchronized void setLocale(Locale novoLocale) {
        String idioma = novoLocale != null
                ? novoLocale.getLanguage()
                : "pt";

        Locale localeSelecionado;

        if ("en".equalsIgnoreCase(idioma)) {
            localeSelecionado = LOCALE_EN;
        } else if ("es".equalsIgnoreCase(idioma)) {
            localeSelecionado = LOCALE_ES;
        } else {
            localeSelecionado = LOCALE_PT;
        }

        ResourceBundle novoBundle = ResourceBundle.getBundle(
                BUNDLE_BASE_NAME,
                localeSelecionado
        );

        GuiFormatador novoFormatador = new GuiFormatador(localeSelecionado);

        this.localeAtual = localeSelecionado;
        this.bundle = novoBundle;
        this.formatador = novoFormatador;

        notificarOuvintes();
    }

    /**
     * Obtém uma tradução e substitui os parâmetros, quando houver.
     *
     * @param chave identificação da mensagem
     * @param argumentos valores para os parâmetros da mensagem
     * @return mensagem traduzida ou a chave entre exclamações se ausente
     */
    public synchronized String get(String chave, Object... argumentos) {
        try {
            String padrao = bundle.getString(chave);

            if (argumentos == null || argumentos.length == 0) {
                return padrao;
            }

            return new MessageFormat(padrao, localeAtual).format(argumentos);
        } catch (MissingResourceException e) {
            return "!" + chave + "!";
        }
    }

    /**
     * Retorna o idioma ativo.
     *
     * @return localidade atual
     */
    public synchronized Locale getLocaleAtual() {
        return localeAtual;
    }

    /**
     * Retorna o formatador de números e datas do idioma ativo.
     *
     * @return formatador atual
     */
    public synchronized GuiFormatador getFormatador() {
        return formatador;
    }

    /**
     * Registra uma ação executada quando o idioma muda.
     *
     * @param ouvinte ação de atualização
     */
    public synchronized void adicionarOuvinte(Runnable ouvinte) {
        if (ouvinte != null && !ouvintesMudancaIdioma.contains(ouvinte)) {
            ouvintesMudancaIdioma.add(ouvinte);
        }
    }

    /**
     * Remove uma ação de atualização anteriormente registrada.
     *
     * @param ouvinte ação a remover
     */
    public synchronized void removerOuvinte(Runnable ouvinte) {
        ouvintesMudancaIdioma.remove(ouvinte);
    }

    /**
     * Notifica os componentes registrados sobre a troca de idioma.
     */
    private void notificarOuvintes() {
        for (Runnable ouvinte : new ArrayList<>(ouvintesMudancaIdioma)) {
            try {
                ouvinte.run();
            } catch (Exception ignored) {
                // Permite que os demais componentes sejam notificados.
            }
        }
    }
}
