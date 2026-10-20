package br.com.ftcoin.gui.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * Objeto de Transferência de Dados ({@code DTO}) imutável para a visão consolidada do Dashboard.
 * </p>
 *
 * <p>
 * Agrupa os indicadores-chave de desempenho (KPIs) patrimoniais, incluindo valor total em carteira,
 * saldo total de moedas, cotação atual, quantidade de carteiras, resultado líquido consolidado e a lista
 * com as movimentações mais recentes para o feed em tempo real da {@code GUI}.
 * </p>
 *
 * @param patrimonioTotal       Soma do valor de mercado de todas as carteiras cadastradas
 * @param saldoTotalFTC         Saldo consolidado de moedas sob custódia
 * @param cotacaoAtual          Preço atual da moeda virtual apurado para o dia
 * @param quantidadeCarteiras   Número total de carteiras cadastradas no sistema
 * @param ganhoTotalConsolidado Soma de todos os ganhos e perdas realizados e potenciais
 * @param ultimasMovimentacoes  Lista imutável com as transações recentes para o feed
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public record ResumoDashboardDTO(
    BigDecimal patrimonioTotal,
    BigDecimal saldoTotalFTC,
    BigDecimal cotacaoAtual,
    int quantidadeCarteiras,
    BigDecimal ganhoTotalConsolidado,
    List<MovimentacaoDTO> ultimasMovimentacoes
) {

    /**
     * Construtor compacto para sanitização de campos nulos e garantia de imutabilidade da lista.
     *
     * @param patrimonioTotal       Patrimônio total
     * @param saldoTotalFTC         Saldo total
     * @param cotacaoAtual          Cotação vigente
     * @param quantidadeCarteiras   Contagem de carteiras
     * @param ganhoTotalConsolidado Lucro total consolidado
     * @param ultimasMovimentacoes  Lista de movimentações
     */
    public ResumoDashboardDTO {
        patrimonioTotal = (patrimonioTotal != null) ? patrimonioTotal : BigDecimal.ZERO;
        saldoTotalFTC = (saldoTotalFTC != null) ? saldoTotalFTC : BigDecimal.ZERO;
        cotacaoAtual = (cotacaoAtual != null) ? cotacaoAtual : BigDecimal.ZERO;
        ganhoTotalConsolidado = (ganhoTotalConsolidado != null) ? ganhoTotalConsolidado : BigDecimal.ZERO;
        ultimasMovimentacoes = (ultimasMovimentacoes != null) ? List.copyOf(ultimasMovimentacoes) : List.of();
    }
}
