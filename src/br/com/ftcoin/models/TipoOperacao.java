package br.com.ftcoin.models;

import java.math.BigDecimal;

/**
 * Tipos de operação sobre uma carteira. Cada tipo sabe aplicar o próprio efeito sobre a {@link Posicao}.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public enum TipoOperacao {

	/** Compra de FTCoins, gravada com o código {@code C}. */
	COMPRA('C', "operacao.compra") {
		@Override
		public void aplicar(Posicao posicao, BigDecimal quantidade, BigDecimal preco) {
			posicao.comprar(quantidade, preco);
		}
	},

	/** Venda de FTCoins, gravada com o código {@code V}. */
	VENDA('V', "operacao.venda") {
		@Override
		public void aplicar(Posicao posicao, BigDecimal quantidade, BigDecimal preco) {
			posicao.vender(quantidade, preco);
		}
	};

	private final char operacao;
	private final String chaveMensagem;

	TipoOperacao(char operacao, String chaveMensagem) {
		this.operacao = operacao;
		this.chaveMensagem = chaveMensagem;
	}

	/**
	 * Devolve o código gravado na base de dados.
	 *
	 * @return {@code C} para compra ou {@code V} para venda
	 */
	public char getOperacao() {
		return operacao;
	}

	/**
	 * Devolve a chave do nome da operação nos arquivos de mensagens.
	 *
	 * @return a chave de mensagem
	 */
	public String getChaveMensagem() {
		return chaveMensagem;
	}

	/**
	 * Aplica o efeito da operação sobre a posição da carteira.
	 *
	 * @param posicao posição a alterar
	 * @param quantidade quantidade de FTCoins
	 * @param preco cotação usada na operação
	 * @throws RegraNegocioException se a operação for uma venda acima do saldo
	 */
	public abstract void aplicar(Posicao posicao, BigDecimal quantidade, BigDecimal preco);

	/**
	 * Converte o código gravado na base de dados no tipo correspondente.
	 *
	 * @param codigo {@code C} ou {@code V}, sem diferenciar maiúsculas
	 * @return o tipo correspondente
	 * @throws RegraNegocioException se o código for desconhecido
	 */
	public static TipoOperacao fromCodigo(String codigo) {
		for (TipoOperacao tipo : values()) {
			if (codigo != null && String.valueOf(tipo.operacao).equalsIgnoreCase(codigo.trim())) {
				return tipo;
			}
		}

		throw new RegraNegocioException("erro.operacao.codigoDesconhecido", codigo);
	}
}
