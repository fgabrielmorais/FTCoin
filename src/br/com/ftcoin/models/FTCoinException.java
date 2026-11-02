package br.com.ftcoin.models;

/**
 * Base das exceções do sistema. Carrega a chave da mensagem e seus argumentos, para que a tela
 * exiba o erro no idioma do usuário.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public abstract class FTCoinException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/** Chave da mensagem; faz parte da forma serializada da exceção. */
	private final String chave;
	private final transient Object[] argumentos;

	/**
	 * Cria a exceção.
	 *
	 * @param chave chave da mensagem nos arquivos {@code Messages_*.properties}
	 * @param causa falha original, ou {@code null}
	 * @param argumentos valores inseridos na mensagem
	 */
	protected FTCoinException(String chave, Throwable causa, Object... argumentos) {
		super(chave, causa);
		this.chave = chave;
		this.argumentos = argumentos;
	}

	/**
	 * Devolve a chave da mensagem.
	 *
	 * @return a chave nos arquivos de mensagens
	 */
	public String getChave() {
		return chave;
	}

	/**
	 * Devolve os valores inseridos na mensagem.
	 *
	 * @return os argumentos da mensagem
	 */
	public Object[] getArgumentos() {
		return argumentos;
	}
}
