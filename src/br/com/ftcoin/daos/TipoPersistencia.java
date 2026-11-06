package br.com.ftcoin.daos;

/**
 * Mecanismos de persistência disponíveis e o nome curto de cada um, usado nos logs.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public enum TipoPersistencia {

	/** Dados em memória, perdidos ao encerrar. */
	MEMORIA("memory"),

	/** Dados no banco relacional MariaDB. */
	MARIADB("mariadb");

	private final String nome;

	TipoPersistencia(String nome) {
		this.nome = nome;
	}

	/**
	 * Devolve o nome curto do mecanismo, usado nos logs.
	 *
	 * @return o nome, como {@code memory} ou {@code mariadb}
	 */
	public String getNome() {
		return nome;
	}
}
