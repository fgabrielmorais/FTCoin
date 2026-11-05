package br.com.ftcoin.daos;

import br.com.ftcoin.models.FTCoinException;

// Falha na consulta ao Oráculo: rede, resposta inválida ou consulta recusada pelo servidor.
// Não ter cotação para uma data NÃO é falha: nesse caso o oráculo devolve null.
public class OraculoException extends FTCoinException {

	private static final long serialVersionUID = 1L;

	public OraculoException(String chave, Object... argumentos) {
		super(chave, null, argumentos);
	}

	//O detalhe técnico da causa vira o argumento da mensagem
	public OraculoException(String chave, Throwable causa) {
		super(chave, causa, causa.getMessage() != null ? causa.getMessage() : causa.getClass().getSimpleName());
	}
}
