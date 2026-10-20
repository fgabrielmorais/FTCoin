package br.com.ftcoin.gui.common;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

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
 * @author Equipe FTCoin
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
     * Constrói o formatador gráfico associado à localidade informada.
     *
     * @param locale Localidade desejada; caso seja {@code null}, adota Português do Brasil
     */
    public GuiFormatador(Locale locale) {
        this.locale = (locale != null) ? locale : Locale.of("pt", "BR");
        this.formatoMoeda = NumberFormat.getCurrencyInstance(this.locale);
        this.formatoMoeda.setMinimumFractionDigits(CASAS_MOEDA);
        this.formatoMoeda.setMaximumFractionDigits(CASAS_MOEDA);

        this.formatoQuantidade = NumberFormat.getNumberInstance(this.locale);
        this.formatoQuantidade.setMinimumFractionDigits(CASAS_QUANTIDADE);
        this.formatoQuantidade.setMaximumFractionDigits(CASAS_QUANTIDADE);

        this.formatoPercentual = NumberFormat.getNumberInstance(this.locale);
        this.formatoPercentual.setMinimumFractionDigits(CASAS_PORCENTAGEM);
        this.formatoPercentual.setMaximumFractionDigits(CASAS_PORCENTAGEM);

        this.formatoData = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(this.locale);
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
     * @param quantidade Quantidade de criptomoedas a ser formatada
     * @return A quantidade formatada acompanhada do sufixo (ex: {@code "10,000 FTC"})
     */
    public String quantidade(BigDecimal quantidade) {
        if (quantidade == null) {
            return formatoQuantidade.format(BigDecimal.ZERO);
        }
        return formatoQuantidade.format(quantidade) + " FTC";
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
     * Formata um valor percentual com sinal explícito de positivo ou negativo.
     *
     * @param percentual Taxa percentual a ser formatada
     * @return O percentual com símbolo e sinal (ex: {@code "+12,50%"})
     */
    public String percentual(BigDecimal percentual) {
        if (percentual == null) {
            return "0,00%";
        }
        String sinal = percentual.signum() > 0 ? "+" : "";
        return sinal + formatoPercentual.format(percentual) + "%";
    }

    /**
     * Formata uma data cronológica no padrão resumido da localidade ativa.
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
