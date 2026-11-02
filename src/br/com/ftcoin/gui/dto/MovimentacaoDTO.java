package br.com.ftcoin.gui.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import br.com.ftcoin.models.TipoOperacao;

/**
 * <p>
 * Objeto de Transferência de Dados ({@code DTO}) imutável para movimentações financeiras.
 * </p>
 *
 * <p>
 * Agrupa os atributos de uma transação de compra ou venda de moedas virtuais para exibição
 * nos históricos do painel de movimentações, do feed do dashboard e dos relatórios da {@code GUI}.
 * </p>
 *
 * @param idMovimento        Identificador único da operação
 * @param idCarteira         Identificador da carteira associada à movimentação
 * @param nomeTitular        Nome completo do titular da conta
 * @param corretora          Nome da corretora vinculada
 * @param dataOperacao       Data em que a operação foi executada
 * @param tipoOperacao       Classificação da transação ({@code COMPRA} ou {@code VENDA})
 * @param quantidade         Quantidade de moedas movimentadas
 * @param cotacaoAplicada    Preço unitário aplicado conforme o oráculo na data
 * @param valorTotalOperacao Montante financeiro total liquidado na operação
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public record MovimentacaoDTO(
    int idMovimento,
    int idCarteira,
    String nomeTitular,
    String corretora,
    LocalDate dataOperacao,
    TipoOperacao tipoOperacao,
    BigDecimal quantidade,
    BigDecimal cotacaoAplicada,
    BigDecimal valorTotalOperacao
) {

    /**
     * Construtor compacto para sanitização de campos monetários e quantitativos nulos.
     *
     * @param idMovimento        Identificador da transação
     * @param idCarteira         Identificador da carteira
     * @param nomeTitular        Nome do titular
     * @param corretora          Nome da corretora
     * @param dataOperacao       Data da operação
     * @param tipoOperacao       Tipo da operação
     * @param quantidade         Quantidade de moedas
     * @param cotacaoAplicada    Cotação aplicada
     * @param valorTotalOperacao Montante total financeiro
     */
    public MovimentacaoDTO {
        if (quantidade == null) {
            quantidade = BigDecimal.ZERO;
        }
        if (cotacaoAplicada == null) {
            cotacaoAplicada = BigDecimal.ZERO;
        }
        if (valorTotalOperacao == null) {
            valorTotalOperacao = BigDecimal.ZERO;
        }
    }
}
