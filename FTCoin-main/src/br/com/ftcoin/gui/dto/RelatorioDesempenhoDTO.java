package br.com.ftcoin.gui.dto;

import java.math.BigDecimal;

/**
 * <p>
 * Objeto de Transferência de Dados ({@code DTO}) imutável para análise de desempenho e rentabilidade.
 * </p>
 *
 * <p>
 * Alimenta a tabela analítica do <b>Relatório 4</b> da interface gráfica, relacionando capital
 * aportado, patrimônio atualizado, lucro/prejuízo nominal e percentual de retorno sobre o investimento (ROI).
 * </p>
 *
 * @param idCarteira     Identificador numérico da carteira
 * @param titular        Nome completo do titular
 * @param corretora      Nome da corretora vinculada
 * @param valorInvestido Montante total investido a custo de aquisição
 * @param valorAtual     Avaliação de mercado atualizada da carteira
 * @param lucroPrejuizo  Resultado financeiro nominal apurado (positivo para lucro, negativo para prejuízo)
 * @param roiPercentual  Taxa de retorno percentual sobre o capital investido
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public record RelatorioDesempenhoDTO(
    int idCarteira,
    String titular,
    String corretora,
    BigDecimal valorInvestido,
    BigDecimal valorAtual,
    BigDecimal lucroPrejuizo,
    BigDecimal roiPercentual
) {

    /**
     * Construtor compacto para inicialização e prevenção de ponteiros nulos em campos numéricos.
     *
     * @param idCarteira     Identificador da carteira
     * @param titular        Nome do titular
     * @param corretora      Nome da corretora
     * @param valorInvestido Total investido
     * @param valorAtual     Valor de mercado atual
     * @param lucroPrejuizo  Lucro ou prejuízo
     * @param roiPercentual  Retorno percentual
     */
    public RelatorioDesempenhoDTO {
        valorInvestido = (valorInvestido != null) ? valorInvestido : BigDecimal.ZERO;
        valorAtual = (valorAtual != null) ? valorAtual : BigDecimal.ZERO;
        lucroPrejuizo = (lucroPrejuizo != null) ? lucroPrejuizo : BigDecimal.ZERO;
        roiPercentual = (roiPercentual != null) ? roiPercentual : BigDecimal.ZERO;
    }
}
