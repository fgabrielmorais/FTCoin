package br.com.ftcoin.gui.main;

import java.util.Locale;

import br.com.ftcoin.daos.TipoPersistencia;
import br.com.ftcoin.gui.common.MensagensGui;

/**
 * Opções da linha de comando da interface gráfica: idioma inicial, banco de dados inicial e carga de demonstração.
 * A leitura não depende de Swing, o que a torna testável.
 *
 * @param locale idioma inicial da interface
 * @param persistencia mecanismo de persistência inicial
 * @param demonstracao {@code true} para carregar os dados de demonstração na partida
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public record ArgumentosAplicacao(Locale locale, TipoPersistencia persistencia, boolean demonstracao) {

	/** Nome da opção de idioma. */
	public static final String OPCAO_IDIOMA = "--lang=";

	/** Nome da opção de banco de dados. */
	public static final String OPCAO_BANCO = "--db=";

	/** Nome da opção que liga a carga de demonstração. */
	public static final String OPCAO_DEMONSTRACAO = "--demo";

	/** Linha de uso mostrada quando os argumentos são inválidos. */
	public static final String USO = "Uso: FTCoinGuiApp [--lang=pt|en|es] [--db=memory|mariadb] [--demo]";

	/** Código de saída do processo quando os argumentos são inválidos. */
	public static final int CODIGO_SAIDA_ARGUMENTO_INVALIDO = 2;

	private static final String IDIOMA_PT = "pt";
	private static final String IDIOMA_EN = "en";
	private static final String IDIOMA_ES = "es";

	/**
	 * Lê os argumentos usando o idioma do sistema como padrão.
	 *
	 * @param args argumentos recebidos pelo {@code main}
	 * @return as opções lidas
	 * @throws IllegalArgumentException se houver argumento desconhecido ou valor inválido
	 */
	public static ArgumentosAplicacao ler(String[] args) {
		return ler(args, Locale.getDefault());
	}

	/**
	 * Lê os argumentos. Sem {@code --lang}, usa o idioma do sistema se for português, inglês ou espanhol;
	 * caso contrário, português.
	 *
	 * @param args argumentos recebidos pelo {@code main}
	 * @param localeDoSistema idioma do sistema, usado como padrão
	 * @return as opções lidas
	 * @throws IllegalArgumentException se houver argumento desconhecido ou valor inválido
	 */
	public static ArgumentosAplicacao ler(String[] args, Locale localeDoSistema) {
		Locale locale = escolherIdioma(localeDoSistema.getLanguage());
		TipoPersistencia persistencia = TipoPersistencia.MEMORIA;
		boolean demonstracao = false;

		for (String arg : args) {
			if (arg.regionMatches(true, 0, OPCAO_IDIOMA, 0, OPCAO_IDIOMA.length())) {
				String valor = arg.substring(OPCAO_IDIOMA.length());
				locale = idiomaDoValor(valor);
			} else if (arg.regionMatches(true, 0, OPCAO_BANCO, 0, OPCAO_BANCO.length())) {
				persistencia = persistenciaDoValor(arg.substring(OPCAO_BANCO.length()));
			} else if (arg.equalsIgnoreCase(OPCAO_DEMONSTRACAO)) {
				demonstracao = true;
			} else {
				throw new IllegalArgumentException("Argumento desconhecido: " + arg);
			}
		}
		return new ArgumentosAplicacao(locale, persistencia, demonstracao);
	}

	private static Locale escolherIdioma(String idioma) {
		return switch (idioma.toLowerCase(Locale.ROOT)) {
			case IDIOMA_EN -> MensagensGui.LOCALE_EN;
			case IDIOMA_ES -> MensagensGui.LOCALE_ES;
			default -> MensagensGui.LOCALE_PT;
		};
	}

	private static Locale idiomaDoValor(String valor) {
		String normalizado = valor.toLowerCase(Locale.ROOT).replace('-', '_');
		return switch (normalizado) {
			case IDIOMA_PT, "pt_br" -> MensagensGui.LOCALE_PT;
			case IDIOMA_EN, "en_us" -> MensagensGui.LOCALE_EN;
			case IDIOMA_ES, "es_es" -> MensagensGui.LOCALE_ES;
			default -> throw new IllegalArgumentException("Idioma inválido: " + valor);
		};
	}

	private static TipoPersistencia persistenciaDoValor(String valor) {
		for (TipoPersistencia tipo : TipoPersistencia.values()) {
			if (tipo.getNome().equalsIgnoreCase(valor)) {
				return tipo;
			}
		}
		throw new IllegalArgumentException("Banco inválido: " + valor);
	}
}
