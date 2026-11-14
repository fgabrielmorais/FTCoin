package br.com.ftcoin.gui.common;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Currency;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.ftcoin.models.RegraNegocioException;

/**
 * <p>
 * Utilitário da interface gráfica responsável pela formatação precisa de moedas, quantidades,
 * datas e percentuais.
 * </p>
 *
 * <p>
 * Respeita a localização ({@code Locale}) ativa no sistema e provê saídas padronizadas para
 * tabelas, cartões de KPI e campos de formulário.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class GuiFormatador {

    /**
     * Quantidade padrão de casas decimais para valores monetários.
     */
    private static final int CASAS_MOEDA = 2;

    /**
     * Quantidade padrão de casas decimais para frações de moedas virtuais.
     */
    private static final int CASAS_QUANTIDADE = 3;

    /**
     * Quantidade padrão de casas decimais para percentuais e taxas.
     */
    private static final int CASAS_PORCENTAGEM = 2;

    /**
     * Tamanho de cada grupo de milhar na digitação de quantidades.
     */
    private static final int DIGITOS_POR_GRUPO_MILHAR = 3;

    /**
     * Localidade ativa para padronização de números e datas.
     */
    private final Locale locale;

    /**
     * Formatador numérico monetário conforme a localização.
     */
    private final NumberFormat formatoMoeda;

    /**
     * Formatador numérico decimal para quantidades de criptomoedas.
     */
    private final NumberFormat formatoQuantidade;

    /**
     * Formatador numérico para rentabilidades e índices percentuais.
     */
    private final NumberFormat formatoPercentual;

    /**
     * Formatador temporal para datas de operações e cotações.
     */
    private final DateTimeFormatter formatoData;

    /**
     * Formato único de data da interface, igual em todos os idiomas: dia/mês/ano com quatro dígitos.
     */
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(java.time.format.ResolverStyle.STRICT);

    /**
     * Constrói o formatador gráfico associado à localidade informada.
     *
     * @param locale Localidade desejada; caso seja {@code null}, adota Português do Brasil
     */
    public GuiFormatador(Locale locale) {
        this.locale = (locale != null) ? locale : Locale.of("pt", "BR");
        this.formatoMoeda = NumberFormat.getCurrencyInstance(this.locale);
        // Os valores do sistema são em reais; o idioma muda só a apresentação, não a moeda
        this.formatoMoeda.setCurrency(Currency.getInstance("BRL"));
        this.formatoMoeda.setMinimumFractionDigits(CASAS_MOEDA);
        this.formatoMoeda.setMaximumFractionDigits(CASAS_MOEDA);

        this.formatoQuantidade = NumberFormat.getNumberInstance(this.locale);
        this.formatoQuantidade.setMinimumFractionDigits(CASAS_QUANTIDADE);
        this.formatoQuantidade.setMaximumFractionDigits(CASAS_QUANTIDADE);

        this.formatoPercentual = NumberFormat.getNumberInstance(this.locale);
        this.formatoPercentual.setMinimumFractionDigits(CASAS_PORCENTAGEM);
        this.formatoPercentual.setMaximumFractionDigits(CASAS_PORCENTAGEM);

        this.formatoData = FORMATO_DATA;
    }

    /**
     * Formata um valor numérico em texto monetário com símbolo da moeda ativa.
     *
     * @param valor Montante numérico a ser formatado
     * @return O valor formatado como moeda (ex: {@code "R$ 1.500,00"})
     */
    public String moeda(BigDecimal valor) {
        if (valor == null) {
            return formatoMoeda.format(BigDecimal.ZERO);
        }
        return formatoMoeda.format(valor);
    }

    /**
     * Formata uma quantidade numérica acrescida da sigla oficial da moeda.
     *
     * @param quantidade Quantidade de criptomoedas a ser formatada; {@code null} vale zero
     * @return A quantidade formatada acompanhada do sufixo (ex: {@code "10,000 FTC"})
     */
    public String quantidade(BigDecimal quantidade) {
        return quantidadeSemUnidade(quantidade) + " FTC";
    }

    /**
     * Formata uma quantidade numérica sem inclusão de sigla ou unidade.
     *
     * @param quantidade Quantidade numérica a ser formatada
     * @return O valor numérico formatado com três casas decimais
     */
    public String quantidadeSemUnidade(BigDecimal quantidade) {
        if (quantidade == null) {
            return formatoQuantidade.format(BigDecimal.ZERO);
        }
        return formatoQuantidade.format(quantidade);
    }

    /**
     * Lê uma quantidade digitada pelo usuário, segundo o idioma ativo. Em português e espanhol o ponto separa
     * milhares e a vírgula separa decimais ({@code 1.000,5}); em inglês é o contrário ({@code 1,000.5}).
     * Texto sem separadores ({@code 1000}) vale em todos os idiomas. O separador de milhar só é aceito
     * em grupos de três dígitos, e o texto inteiro precisa ser consumido.
     *
     * @param texto Texto digitado
     * @return A quantidade lida, possivelmente negativa ou zero (a regra de quantidade positiva é do presenter)
     * @throws NumberFormatException se o texto não for um número no formato do idioma
     * @throws RegraNegocioException se houver mais de três casas decimais
     */
    public BigDecimal lerQuantidade(String texto) {
        if (texto == null) {
            throw new NumberFormatException("Quantidade ausente");
        }
        DecimalFormatSymbols simbolos = DecimalFormatSymbols.getInstance(locale);
        String separadorMilhar = String.valueOf(simbolos.getGroupingSeparator());
        String milhar = Pattern.quote(separadorMilhar);
        String decimal = Pattern.quote(String.valueOf(simbolos.getDecimalSeparator()));
        Pattern padrao = Pattern.compile("(-?)(\\d+|\\d{1,3}(?:" + milhar + "\\d{" + DIGITOS_POR_GRUPO_MILHAR + "})+)(?:"
                + decimal + "(\\d+))?");
        Matcher casamento = padrao.matcher(texto.trim());
        if (!casamento.matches()) {
            throw new NumberFormatException("Quantidade inválida: " + texto);
        }

        String fracao = casamento.group(3);
        if (fracao != null && fracao.length() > CASAS_QUANTIDADE) {
            throw new RegraNegocioException("gui.mov.erro.casasDecimais", CASAS_QUANTIDADE);
        }
        String inteiro = casamento.group(2).replace(separadorMilhar, "");
        return new BigDecimal(casamento.group(1) + inteiro + (fracao != null ? "." + fracao : ""));
    }

    /**
     * Formata um valor percentual com sinal explícito de positivo ou negativo.
     *
     * @param percentual Taxa percentual a ser formatada; {@code null} vale zero
     * @return O percentual com símbolo e sinal (ex: {@code "+12,50%"})
     */
    public String percentual(BigDecimal percentual) {
        if (percentual == null) {
            percentual = BigDecimal.ZERO;
        }
        String sinal = percentual.signum() > 0 ? "+" : "";
        return sinal + formatoPercentual.format(percentual) + "%";
    }

    /**
     * Formata um valor percentual sem sinal, como a participação de uma carteira no portfólio.
     *
     * @param percentual Taxa percentual a ser formatada; {@code null} vale zero
     * @return O percentual com símbolo, sem sinal explícito (ex: {@code "12,50%"})
     */
    public String percentualSimples(BigDecimal percentual) {
        if (percentual == null) {
            percentual = BigDecimal.ZERO;
        }
        return formatoPercentual.format(percentual) + "%";
    }

    /**
     * Lê uma data digitada pelo usuário, no formato {@code DD/MM/AAAA}. O formato {@code AAAA-MM-DD}
     * também é aceito.
     *
     * @param texto Texto digitado
     * @return A data lida
     * @throws java.time.format.DateTimeParseException se o texto não for uma data válida
     */
    public static LocalDate lerData(String texto) {
        String limpo = texto.trim();
        if (limpo.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return LocalDate.parse(limpo);
        }
        return LocalDate.parse(limpo, FORMATO_DATA);
    }

    /**
     * Formata uma data cronológica no formato {@code DD/MM/AAAA}.
     *
     * @param data Data a ser convertida
     * @return A data formatada em texto, ou {@code "-"} caso seja {@code null}
     */
    public String data(LocalDate data) {
        if (data == null) {
            return "-";
        }
        return formatoData.format(data);
    }

    /**
     * Retorna a localidade atualmente em uso por este formatador.
     *
     * @return A instância ativa de {@code Locale}
     */
    public Locale getLocale() {
        return locale;
    }
}
