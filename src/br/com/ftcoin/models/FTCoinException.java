package br.com.ftcoin.models;

// Base das exceções do sistema. Carrega a chave da mensagem (e seus argumentos)
// para que a view a exiba no idioma do usuário.
public abstract class FTCoinException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String chave;
	private final transient Object[] argumentos;

	protected FTCoinException(String chave, Throwable causa, Object... argumentos) {
		super(chave, causa);
		this.chave = chave;
		this.argumentos = argumentos;
	}

	public String getChave() { return chave; }

	public Object[] getArgumentos() { return argumentos; }
}
