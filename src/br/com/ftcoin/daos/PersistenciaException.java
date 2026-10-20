package br.com.ftcoin.daos;

import br.com.ftcoin.models.FTCoinException;

// Falha na configuração ou no acesso ao mecanismo de persistência
public class PersistenciaException extends FTCoinException {

	private static final long serialVersionUID = 1L;

	public PersistenciaException(String chave, Object... argumentos) {
		super(chave, null, argumentos);
	}

	//O detalhe técnico da causa vira o argumento da mensagem
	public PersistenciaException(String chave, Throwable causa) {
		super(chave, causa, causa.getMessage());
	}
}
