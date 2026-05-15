package br.com.ftcoin.models;

public enum TipoOperacao {
	COMPRA('C'),
	VENDA('V');
	
	private final char operacao;
	
	TipoOperacao(char operacao){
		this.operacao = operacao;
	}
	
	public char getOperacao() {
		return operacao;
	}
	
	
	//Validação e tratamento de erros dos tipos de operação
	public static TipoOperacao fromString(String entrada) {
		if(entrada == null || entrada.trim().isEmpty()) {
			throw new IllegalArgumentException("A operação não pode ser vazia.");
		}
		
		String operacaoFormatada = entrada.trim().toLowerCase();
		
		if(operacaoFormatada.equals("compra") || operacaoFormatada.equals("c")) {
			return COMPRA;
		} else if (operacaoFormatada.equals("venda") || operacaoFormatada.equals("v")) {
			return VENDA;
		}
		

		throw new IllegalArgumentException("Tipo de operação inválido. Por favor, digite novamente 'Compra' ou 'Venda'.");
	}
}
