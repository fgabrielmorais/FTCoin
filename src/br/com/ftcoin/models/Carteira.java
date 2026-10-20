package br.com.ftcoin.models;

public class Carteira {
	private int id;
	private String nomeTitular;
	private String corretora;

	public Carteira() {}

	public Carteira(int id, String nomeTitular, String corretora) {
		this.id = id;
		this.nomeTitular = nomeTitular;
		this.corretora = corretora;
	}

	public int getId() { return id; }

	public void setId(int id) {
		this.id = id;
	}

	public String getNomeTitular() { return nomeTitular; }

	public void setNomeTitular(String nomeTitular) {
		this.nomeTitular = exigirPreenchido(nomeTitular, "erro.carteira.titularObrigatorio");
	}

	public String getCorretora() { return corretora; }

	public void setCorretora(String corretora) {
		this.corretora = exigirPreenchido(corretora, "erro.carteira.corretoraObrigatoria");
	}

	private static String exigirPreenchido(String valor, String chaveDoErro) {
		if (valor == null || valor.trim().isEmpty()) {
			throw new RegraNegocioException(chaveDoErro);
		}
		return valor.trim();
	}
}
