package br.com.ftcoin.gui.dto;

import java.math.BigDecimal;

/**
 * <p>
 * Objeto de Transferência de Dados ({@code DTO}) imutável para os itens do relatório consolidado.
 * </p>
 *
 * <p>
 * Representa uma linha analítica no <b>Relatório 3</b> da interface gráfica, exibindo saldo,
 * valor a mercado, percentual de participação no patrimônio geral e ganho financeiro total.
 * </p>
 *
 * @param idCarteira       Identificador único da carteira
 * @param titular          Nome completo do titular
 * @param corretora        Nome da corretora vinculada
 * @param saldo            Saldo em moedas sob custódia
 * @param valorMercado     Valor financeiro total avaliado a mercado
 * @param participacaoPerc Percentual de participação da carteira no portfólio global
 * @param ganhoTotal       Resultado financeiro líquido total apurado
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public record RelatorioConsolidadoItemDTO(
    int idCarteira,
    String titular,
    String corretora,
    BigDecimal saldo,
    BigDecimal valorMercado,
    BigDecimal participacaoPerc,
    BigDecimal ganhoTotal
) {

    /**
     * Construtor compacto para validação e garantia de valores monetários não nulos.
     *
     * @param idCarteira       Identificador da carteira
     * @param titular          Nome do titular
     * @param corretora        Nome da corretora
     * @param saldo            Saldo em moedas
     * @param valorMercado     Valor a mercado
     * @param participacaoPerc Percentual no portfólio
     * @param ganhoTotal       Resultado financeiro líquido
     */
    public RelatorioConsolidadoItemDTO {
        saldo = (saldo != null) ? saldo : BigDecimal.ZERO;
        valorMercado = (valorMercado != null) ? valorMercado : BigDecimal.ZERO;
        participacaoPerc = (participacaoPerc != null) ? participacaoPerc : BigDecimal.ZERO;
        ganhoTotal = (ganhoTotal != null) ? ganhoTotal : BigDecimal.ZERO;
    }
}
