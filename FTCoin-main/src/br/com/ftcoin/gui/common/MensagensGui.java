package br.com.ftcoin.gui.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>
 * Provedor centralizado de mensagens internacionalizadas para a interface gráfica ({@code GUI}).
 * </p>
 *
 * <p>
 * Suporta alternância dinâmica de idioma em tempo de execução via {@code ResourceBundle},
 * com notificação automática a componentes observadores registrados.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class MensagensGui {

    /**
     * Nome base dos arquivos de propriedades (bundle).
     */
    private static final String BUNDLE_BASE_NAME = "Messages";

    /**
     * Localização padrão para o idioma Português (Brasil).
     */
    public static final Locale LOCALE_PT = Locale.of("pt", "BR");

    /**
     * Localização padrão para o idioma Inglês (Estados Unidos).
     */
    public static final Locale LOCALE_EN = Locale.of("en", "US");

    /**
     * Localidade atualmente aplicada aos textos da interface.
     */
    private Locale localeAtual;

    /**
     * Conjunto de recursos com os textos traduzidos ativos.
     */
    private ResourceBundle bundle;

    /**
     * Utilitário de formatação de valores sincronizado com a localidade ativa.
     */
    private GuiFormatador formatador;

    /**
     * Lista de ouvintes notificados quando ocorre mudança dinâmica de idioma.
     */
    private final List<Runnable> ouvintesMudancaIdioma = new ArrayList<>();

    /**
     * Constrói o provedor de mensagens com a localidade inicial fornecida.
     *
     * @param localeInicial Localidade de inicialização; se {@code null}, adota {@code LOCALE_PT}
     */
    public MensagensGui(Locale localeInicial) {
        setLocale(localeInicial != null ? localeInicial : LOCALE_PT);
    }

    /**
     * Define a nova localidade da interface, recarregando bundles e notificando ouvintes.
     *
     * @param novoLocale Nova localidade desejada para a interface
     */
    public synchronized void setLocale(Locale novoLocale) {
        this.localeAtual = (novoLocale != null && "en".equalsIgnoreCase(novoLocale.getLanguage()))
                ? LOCALE_EN : LOCALE_PT;
        this.bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, this.localeAtual);
        this.formatador = new GuiFormatador(this.localeAtual);
        notificarOuvintes();
    }

    /**
     * Retorna a mensagem internacionalizada associada à chave fornecida.
     *
     * @param chave      Chave de identificação no arquivo de propriedades
     * @param argumentos Parâmetros variáveis para interpolação via {@code MessageFormat}
     * @return O texto traduzido e formatado, ou a chave envolvida em exclamações em caso de ausência
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
     * Retorna a localidade atualmente em operação na interface.
     *
     * @return A instância ativa de {@code Locale}
     */
    public synchronized Locale getLocaleAtual() {
        return localeAtual;
    }

    /**
     * Retorna o utilitário de formatação configurado para a localidade ativa.
     *
     * @return O formatador gráfico {@code GuiFormatador}
     */
    public synchronized GuiFormatador getFormatador() {
        return formatador;
    }

    /**
     * Registra um novo ouvinte para receber notificações após a alteração de idioma.
     *
     * @param ouvinte Ação executável a ser disparada
     */
    public synchronized void adicionarOuvinte(Runnable ouvinte) {
        if (ouvinte != null && !ouvintesMudancaIdioma.contains(ouvinte)) {
            ouvintesMudancaIdioma.add(ouvinte);
        }
    }

    /**
     * Remove um ouvinte previamente cadastrado.
     *
     * @param ouvinte Ação executável a ser desvinculada
     */
    public synchronized void removerOuvinte(Runnable ouvinte) {
        ouvintesMudancaIdioma.remove(ouvinte);
    }

    /**
     * Dispara as notificações a todos os observadores de alteração de idioma.
     */
    private void notificarOuvintes() {
        for (Runnable ouvinte : new ArrayList<>(ouvintesMudancaIdioma)) {
            try {
                ouvinte.run();
            } catch (Exception ignored) {
            }
        }
    }
}
