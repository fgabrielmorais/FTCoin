package br.com.ftcoin.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Oraculo {	
	private LocalDate data;
	private BigDecimal cotacao;
	
	
	public Oraculo() {}
	
	public Oraculo(LocalDate data, BigDecimal cotacao) {
		this.data = data;
		this.cotacao = cotacao;
	}
	
	public LocalDate getData() {return data;}
	public void setData(LocalDate data) {this.data = data;}
	
	public BigDecimal getCotacao(){ return cotacao; }
	public void setCotacao(BigDecimal cotacao) {this.cotacao = cotacao;}
}


