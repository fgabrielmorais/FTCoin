package br.com.ftcoin.views;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

// Formata valores e datas conforme o idioma do usuário
public class Formatador {

	private static final int CASAS_MOEDA = 2;
	private static final int CASAS_QUANTIDADE = 3;

	private final NumberFormat formatoMoeda;
	private final NumberFormat formatoQuantidade;
	private final DateTimeFormatter formatoData;

	public Formatador(Locale locale) {
		this.formatoMoeda = criarFormatoDecimal(locale, CASAS_MOEDA);
		this.formatoQuantidade = criarFormatoDecimal(locale, CASAS_QUANTIDADE);
		this.formatoData = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale);
	}

	public String moeda(BigDecimal valor) {
		return formatoMoeda.format(valor);
	}

	public String quantidade(BigDecimal quantidade) {
		return formatoQuantidade.format(quantidade);
	}

	public String data(LocalDate data) {
		return formatoData.format(data);
	}

	private static NumberFormat criarFormatoDecimal(Locale locale, int casasDecimais) {
		NumberFormat formato = NumberFormat.getNumberInstance(locale);
		formato.setMinimumFractionDigits(casasDecimais);
		formato.setMaximumFractionDigits(casasDecimais);
		return formato;
	}
}
