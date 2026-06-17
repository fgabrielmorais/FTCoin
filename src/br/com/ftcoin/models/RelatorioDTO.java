package br.com.ftcoin.models;

import java.math.BigDecimal;
import java.util.List;

public class RelatorioDTO {
    
    private String nomeTitular;
    private String corretora;
    private BigDecimal saldoMoedas;
    private BigDecimal cotacaoAtual;
    private BigDecimal patrimonioReal;
    private List<Movimentacao> historico;

    // Construtor atualizado para receber todos os dados consolidados pelo Controller
    public RelatorioDTO(String nomeTitular, String corretora, BigDecimal saldoMoedas, 
                        BigDecimal cotacaoAtual, BigDecimal patrimonioReal, List<Movimentacao> historico) {
        this.nomeTitular = nomeTitular;
        this.corretora = corretora;
        this.saldoMoedas = saldoMoedas;
        this.cotacaoAtual = cotacaoAtual;
        this.patrimonioReal = patrimonioReal;
        this.historico = historico;
    }
    
    // Apenas Getters (DTOs de relatório continuam imutáveis após a criação)
    public String getNomeTitular() { return nomeTitular; }
    public String getCorretora() { return corretora; }
    public BigDecimal getSaldoMoedas() { return saldoMoedas; }
    public BigDecimal getCotacaoAtual() { return cotacaoAtual; }
    public BigDecimal getPatrimonioReal() { return patrimonioReal; }
    public List<Movimentacao> getHistorico() { return historico; }
}