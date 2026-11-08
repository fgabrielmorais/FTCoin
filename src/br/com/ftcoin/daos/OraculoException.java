package br.com.ftcoin.daos;

import br.com.ftcoin.models.FTCoinException;

/**
 * Falha na consulta ao Oráculo: rede, resposta inválida ou consulta recusada pelo servidor.
 * <p>
 * Não ter cotação para uma data <strong>não</strong> é falha: nesse caso o oráculo devolve {@code null}.
 * Como toda {@link FTCoinException}, carrega a chave de uma mensagem traduzível, que a interface
 * exibe no idioma ativo.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class OraculoException extends FTCoinException {

	private static final long serialVersionUID = 1L;

	/**
	 * Cria a falha a partir da chave da mensagem e dos seus argumentos.
	 *
	 * @param chave chave da mensagem nos arquivos {@code Messages_<idioma>.properties}
	 * @param argumentos valores que preenchem os {@code {0}}, {@code {1}}... da mensagem
	 */
	public OraculoException(String chave, Object... argumentos) {
		super(chave, null, argumentos);
	}

	/**
	 * Cria a falha a partir de uma causa técnica; o detalhe da causa vira o argumento da mensagem.
	 *
	 * @param chave chave da mensagem nos arquivos {@code Messages_<idioma>.properties}
	 * @param causa exceção original (por exemplo, uma falha de entrada e saída na rede)
	 */
	public OraculoException(String chave, Throwable causa) {
		super(chave, causa, causa.getMessage() != null ? causa.getMessage() : causa.getClass().getSimpleName());
	}
}
