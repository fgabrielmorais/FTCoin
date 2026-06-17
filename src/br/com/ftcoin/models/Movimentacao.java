package br.com.ftcoin.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Movimentacao {
	private int idMovimento;
	private int idCarteira;
	private LocalDate dataOperacao;
	private TipoOperacao tipoOperacao;
	private BigDecimal quantidadeMovimentada;

	public Movimentacao(){}
	
	public Movimentacao(int idMov, int idCarteira, LocalDate data, TipoOperacao tipo, BigDecimal qtd) {
		this.idMovimento = idMov;
		this.idCarteira = idCarteira;
		this.dataOperacao = data;
		this.tipoOperacao = tipo;
	}
	
	// Getters e Setters
    public int getIdMovimento() { return idMovimento; }
    public void setIdMovimento(int id) { this.idMovimento = id; }

    public int getIdCarteira() { return idCarteira; }
    public void setIdCarteira(int id) { this.idCarteira = id; }

    public LocalDate getDataOperacao() { return dataOperacao; }
    public void setDataOperacao(LocalDate data) { this.dataOperacao = data; }

    public TipoOperacao getTipoOperacao() { return tipoOperacao; }
    public void setTipoOperacao(TipoOperacao tipo) { this.tipoOperacao = tipo; }

    public BigDecimal getQuantidadeMovimentada() { return quantidadeMovimentada; }
    
    public void setQuantidadeMovimentada(BigDecimal quantidade) {
    	if(quantidade == null || quantidade.compareTo(BigDecimal.ZERO) < 0) {
    		throw new IllegalArgumentException("A quantidade movimentada não pode ser negativa.");
    	}
    	
    	this.quantidadeMovimentada = quantidade;
    }
}