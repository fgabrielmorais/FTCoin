package br.com.ftcoin.gui.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.ftcoin.models.FTCoinException;

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
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class MensagensGui {

    private static final Logger LOG = Logger.getLogger(MensagensGui.class.getName());

    /**
     * Nome base dos arquivos de propriedades (bundle).
     */
    private static final String BUNDLE_BASE_NAME = "Messages";

    /**
     * Pasta dos textos longos em HTML (manual e créditos), um arquivo por idioma.
     */
    private static final String PASTA_DOCUMENTOS = "/br/com/ftcoin/gui/textos/";

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
     * Chaves sem tradução já avisadas no log; a tela redesenha muitas vezes e o aviso sai uma vez só.
     */
    private final Set<String> chavesAusentesAvisadas = new HashSet<>();

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
        LOG.log(Level.INFO, "Idioma da interface: {0}", this.localeAtual);
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
            if (chavesAusentesAvisadas.add(localeAtual + ":" + chave)) {
                LOG.log(Level.WARNING, "Chave de tradução ausente em {0}: {1}", new Object[] { localeAtual, chave });
            }
            return "!" + chave + "!";
        }
    }

    /**
     * Converte uma exceção no texto a exibir ao usuário, no idioma atual.
     *
     * @param erro Exceção capturada pelo presenter
     * @return O texto da chave da {@link FTCoinException}; para outras exceções, a mensagem de erro inesperado
     */
    public String descreverErro(Throwable erro) {
        if (erro instanceof FTCoinException falhaPrevista) {
            return get(falhaPrevista.getChave(), falhaPrevista.getArgumentos());
        }
        String detalhe = (erro.getMessage() != null) ? erro.getMessage() : erro.getClass().getSimpleName();
        return get("erro.inesperado", detalhe);
    }

    /**
     * Carrega um texto longo em HTML no idioma ativo e preenche as marcações {@code ${nome}}.
     * O arquivo procurado é {@code <documento>_<idioma>.html}; sem a versão do idioma, usa a em português.
     *
     * @param documento nome base do arquivo, como {@code "ajuda"}
     * @param valores   valores das marcações, indexados pelo nome usado dentro de {@code ${...}}
     * @return o HTML pronto para exibição
     * @throws UncheckedIOException se o arquivo não existir em nenhum idioma ou não puder ser lido
     */
    public String carregarDocumento(String documento, Map<String, String> valores) {
        String html = lerDocumento(documento, getLocaleAtual().getLanguage());
        if (html == null) {
            html = lerDocumento(documento, LOCALE_PT.getLanguage());
        }
        if (html == null) {
            throw new UncheckedIOException(new IOException("Documento não encontrado: " + documento));
        }
        for (Map.Entry<String, String> valor : valores.entrySet()) {
            html = html.replace("${" + valor.getKey() + "}", valor.getValue());
        }
        return html;
    }

    private static String lerDocumento(String documento, String idioma) {
        String caminho = PASTA_DOCUMENTOS + documento + "_" + idioma + ".html";
        try (InputStream entrada = MensagensGui.class.getResourceAsStream(caminho)) {
            return (entrada == null) ? null : new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
            } catch (Exception e) {
                // Uma tela que falha ao traduzir não impede as demais
                LOG.log(Level.SEVERE, "Falha ao avisar uma tela sobre a troca de idioma", e);
            }
        }
    }
}
