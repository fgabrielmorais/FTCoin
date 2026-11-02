package br.com.ftcoin.daos;

/**
 * Mecanismos de persistência disponíveis e o valor que identifica cada um na configuração.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public enum TipoPersistencia {

	/** Dados em memória, perdidos ao encerrar. */
	MEMORIA("memory"),

	/** Dados no banco relacional MariaDB. */
	MARIADB("mariadb");

	private final String valorConfiguracao;

	TipoPersistencia(String valorConfiguracao) {
		this.valorConfiguracao = valorConfiguracao;
	}

	/**
	 * Devolve o valor que identifica o mecanismo em {@code application.properties}.
	 *
	 * @return o valor de configuração, como {@code memory} ou {@code mariadb}
	 */
	public String getValorConfiguracao() {
		return valorConfiguracao;
	}

	/**
	 * Converte o valor de configuração no mecanismo correspondente, sem diferenciar maiúsculas.
	 *
	 * @param entrada valor lido da configuração
	 * @return o mecanismo correspondente
	 * @throws PersistenciaException se o valor estiver vazio ou não corresponder a nenhum mecanismo
	 */
	public static TipoPersistencia fromString(String entrada) {
		if (entrada == null || entrada.trim().isEmpty()) {
			throw new PersistenciaException("erro.configuracao.persistenciaAusente", valoresAceitos());
		}

		for (TipoPersistencia tipo : values()) {
			if (tipo.valorConfiguracao.equalsIgnoreCase(entrada.trim())) {
				return tipo;
			}
		}

		throw new PersistenciaException("erro.configuracao.persistenciaInvalida", entrada, valoresAceitos());
	}

	private static String valoresAceitos() {
		return MEMORIA.valorConfiguracao + ", " + MARIADB.valorConfiguracao;
	}
}
