package br.com.ftcoin.models;

import java.math.BigDecimal;

public class RelatorioDTO {
	private int idCarteira;
	private String titular;
	private BigDecimal saldoAtual;
	private BigDecimal ganhoPerdaTotal;

	public RelatorioDTO(int id, String titular, BigDecimal saldo, BigDecimal ganhoPerda) {
		this.idCarteira = id;
		this.titular = titular;
		this.saldoAtual = saldo;
		this.ganhoPerdaTotal = ganhoPerda;
	}
	
	// Apenas Getters (DTOs de relatório costumam ser imutáveis após a criação)
    public int getIdCarteira() { return idCarteira; }
    public String getTitular() { return titular; }
    public BigDecimal getSaldoAtual() { return saldoAtual; }
    public BigDecimal getGanhoPerdaTotal() { return ganhoPerdaTotal; }
}
