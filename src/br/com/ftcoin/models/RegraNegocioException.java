package br.com.ftcoin.models;

/**
 * Violação de uma regra do domínio, como venda acima do saldo ou carteira inexistente.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class RegraNegocioException extends FTCoinException {

	private static final long serialVersionUID = 1L;

	/**
	 * Cria a exceção a partir de uma chave de mensagem.
	 *
	 * @param chave chave da mensagem nos arquivos {@code Messages_*.properties}
	 * @param argumentos valores inseridos na mensagem
	 */
	public RegraNegocioException(String chave, Object... argumentos) {
		super(chave, null, argumentos);
	}
}
