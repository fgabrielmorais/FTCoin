package br.com.ftcoin.views;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

// Textos da interface, lidos dos arquivos Messages_<idioma>.properties
public class Mensagens {

	private static final String NOME_BUNDLE = "Messages";
	private static final Locale LOCALE_PADRAO = Locale.of("pt", "BR");
	private static final Set<String> IDIOMAS_DISPONIVEIS = Set.of("pt", "en");

	private final Locale locale;
	private final ResourceBundle textos;
	private final Formatador formatador;

	//Idioma sem tradução disponível cai no padrão (português)
	public Mensagens(Locale localePreferido) {
		this.locale = IDIOMAS_DISPONIVEIS.contains(localePreferido.getLanguage()) ? localePreferido : LOCALE_PADRAO;
		this.textos = ResourceBundle.getBundle(NOME_BUNDLE, locale);
		this.formatador = new Formatador(locale);
	}

	public String get(String chave, Object... argumentos) {
		Object[] argumentosFormatados = new Object[argumentos.length];
		for (int i = 0; i < argumentos.length; i++) {
			argumentosFormatados[i] = formatarArgumento(argumentos[i]);
		}
		return new MessageFormat(textos.getString(chave), locale).format(argumentosFormatados);
	}

	public Formatador getFormatador() {
		return formatador;
	}

	//Datas e quantidades vindas do domínio (ex.: em exceções) saem no formato do idioma
	private Object formatarArgumento(Object argumento) {
		if (argumento instanceof LocalDate data) {
			return formatador.data(data);
		}
		if (argumento instanceof BigDecimal quantidade) {
			return formatador.quantidade(quantidade);
		}
		return argumento;
	}
}
