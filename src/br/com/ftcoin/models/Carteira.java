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
	
	
	//get e setter do ID
	public int getId() { return id; }
	
	public void setId(int id) {
		this.id = id;
	}
	
	
	//get e setter do nome do titular
	public String getNomeTitular() { return nomeTitular; }
	
	public void setNomeTitular(String nomeTitular) {
		this.nomeTitular = nomeTitular;
	}
	
	
	//get e setter da corretora
	public String getCorretora() { return corretora; }
	
	public void setCorretora(String corretora) {
		this.corretora = corretora;
	}
}
