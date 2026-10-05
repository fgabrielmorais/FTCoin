
package br.com.ftcoin.models;

import java.math.BigDecimal;

public enum TipoOperacao {
	COMPRA('C', "operacao.compra") {
		@Override
		public void aplicar(Posicao posicao, BigDecimal quantidade, BigDecimal preco) {
			posicao.comprar(quantidade, preco);
		}
	},
	VENDA('V', "operacao.venda") {
		@Override
		public void aplicar(Posicao posicao, BigDecimal quantidade, BigDecimal preco) {
			posicao.vender(quantidade, preco);
		}
	};

	private final char operacao;
	private final String chaveMensagem;

	TipoOperacao(char operacao, String chaveMensagem){
		this.operacao = operacao;
		this.chaveMensagem = chaveMensagem;
	}

	//Código gravado na base de dados
	public char getOperacao() {
		return operacao;
	}

	//Chave do nome da operação nos arquivos de mensagens
	public String getChaveMensagem() {
		return chaveMensagem;
	}

	//Efeito da operação sobre a posição da carteira
	public abstract void aplicar(Posicao posicao, BigDecimal quantidade, BigDecimal preco);


	public static TipoOperacao fromCodigo(String codigo) {
		for (TipoOperacao tipo : values()) {
			if (codigo != null && String.valueOf(tipo.operacao).equalsIgnoreCase(codigo.trim())) {
				return tipo;
			}
		}

		throw new RegraNegocioException("erro.operacao.codigoDesconhecido", codigo);
	}
}
