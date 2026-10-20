package br.com.ftcoin.models;

// Violação de uma regra do domínio
public class RegraNegocioException extends FTCoinException {

	private static final long serialVersionUID = 1L;

	public RegraNegocioException(String chave, Object... argumentos) {
		super(chave, null, argumentos);
	}
}
