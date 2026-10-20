package br.com.ftcoin.gui.dto;

import java.math.BigDecimal;

/**
 * <p>
 * Objeto de Transferência de Dados ({@code DTO}) imutável para apuração de posição da carteira.
 * </p>
 *
 * <p>
 * Consolida as métricas do cálculo de Custo Médio Ponderado (CMP), incluindo valor a custo,
 * valor de mercado, ganhos realizados, ganhos não realizados, lucro total e retorno sobre investimento
 * (ROI) para o <b>Relatório 1</b> da interface gráfica.
 * </p>
 *
 * @param idCarteira        Identificador numérico da carteira analisada
 * @param titular           Nome do titular cadastrado
 * @param corretora         Nome da corretora vinculada
 * @param cotacaoAtual      Preço de mercado da moeda na data corrente
 * @param saldo             Quantidade de moedas em custódia
 * @param custoMedio        Custo médio ponderado por unidade de moeda
 * @param valorACusto       Montante total desembolsado nas aquisições
 * @param valorDeMercado    Valor financeiro total avaliado a mercado
 * @param ganhoRealizado    Lucro ou prejuízo já concretizado através de vendas passadas
 * @param ganhoNaoRealizado Lucro ou prejuízo potencial não realizado das moedas em custódia
 * @param ganhoTotal        Resultado financeiro líquido consolidado
 * @param roiPercentual     Taxa de retorno sobre o investimento percentual apurada
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public record PosicaoRelatorioDTO(
    int idCarteira,
    String titular,
    String corretora,
    BigDecimal cotacaoAtual,
    BigDecimal saldo,
    BigDecimal custoMedio,
    BigDecimal valorACusto,
    BigDecimal valorDeMercado,
    BigDecimal ganhoRealizado,
    BigDecimal ganhoNaoRealizado,
    BigDecimal ganhoTotal,
    BigDecimal roiPercentual
) {

    /**
     * Construtor compacto para sanitização e garantia de inicialização contra valores nulos.
     *
     * @param idCarteira        Identificador da carteira
     * @param titular           Nome do titular
     * @param corretora         Nome da corretora
     * @param cotacaoAtual      Cotação atual
     * @param saldo             Saldo em moedas
     * @param custoMedio        Custo médio apurado
     * @param valorACusto       Total a custo
     * @param valorDeMercado    Valor a mercado
     * @param ganhoRealizado    Ganho realizado
     * @param ganhoNaoRealizado Ganho não realizado
     * @param ganhoTotal        Ganho líquido total
     * @param roiPercentual     Percentual de retorno
     */
    public PosicaoRelatorioDTO {
        saldo = (saldo != null) ? saldo : BigDecimal.ZERO;
        custoMedio = (custoMedio != null) ? custoMedio : BigDecimal.ZERO;
        valorACusto = (valorACusto != null) ? valorACusto : BigDecimal.ZERO;
        valorDeMercado = (valorDeMercado != null) ? valorDeMercado : BigDecimal.ZERO;
        ganhoRealizado = (ganhoRealizado != null) ? ganhoRealizado : BigDecimal.ZERO;
        ganhoNaoRealizado = (ganhoNaoRealizado != null) ? ganhoNaoRealizado : BigDecimal.ZERO;
        ganhoTotal = (ganhoTotal != null) ? ganhoTotal : BigDecimal.ZERO;
        roiPercentual = (roiPercentual != null) ? roiPercentual : BigDecimal.ZERO;
    }
}
