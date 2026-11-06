package br.com.ftcoin.daos;

import br.com.ftcoin.models.FTCoinException;

/**
 * Falha na configuração ou no acesso ao mecanismo de persistência.
 * A mensagem é identificada por uma chave dos arquivos de mensagens, para ser exibida no idioma do usuário.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class PersistenciaException extends FTCoinException {

	private static final long serialVersionUID = 1L;

	/**
	 * Cria a exceção a partir de uma chave de mensagem.
	 *
	 * @param chave chave da mensagem nos arquivos {@code Messages_*.properties}
	 * @param argumentos valores inseridos na mensagem
	 */
	public PersistenciaException(String chave, Object... argumentos) {
		super(chave, null, argumentos);
	}

	/**
	 * Cria a exceção a partir de uma falha técnica; o detalhe da causa vira o argumento da mensagem.
	 *
	 * @param chave chave da mensagem nos arquivos {@code Messages_*.properties}
	 * @param causa falha original (por exemplo, uma {@code SQLException})
	 */
	public PersistenciaException(String chave, Throwable causa) {
		super(chave, causa, causa.getMessage());
	}
}
