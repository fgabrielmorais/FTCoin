package br.com.ftcoin.gui.dto;

import java.math.BigDecimal;

/**
 * <p>
 * Objeto de Transferência de Dados ({@code DTO}) imutável para representação de carteiras na interface.
 * </p>
 *
 * <p>
 * Transporta os atributos cadastrais e financeiros de uma carteira para renderização nas tabelas
 * e caixas de seleção da camada de visão ({@code View}).
 * </p>
 *
 * @param id           Identificador numérico exclusivo da carteira
 * @param nomeTitular  Nome completo do titular cadastrado
 * @param corretora    Nome da corretora ou instituição custodiante
 * @param saldo        Saldo quantitativo de moedas em custódia
 * @param valorMercado Avaliação financeira estimada a preço de mercado
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public record CarteiraDTO(
    int id,
    String nomeTitular,
    String corretora,
    BigDecimal saldo,
    BigDecimal valorMercado
) {

    /**
     * Construtor compacto para validação e garantia de valores nulos substituídos por zero.
     *
     * @param id           Identificador numérico da carteira
     * @param nomeTitular  Nome do titular
     * @param corretora    Nome da corretora
     * @param saldo        Saldo em moedas
     * @param valorMercado Valor financeiro a mercado
     */
    public CarteiraDTO {
        if (saldo == null) {
            saldo = BigDecimal.ZERO;
        }
        if (valorMercado == null) {
            valorMercado = BigDecimal.ZERO;
        }
    }

    /**
     * Retorna uma instância básica de {@code CarteiraDTO} com saldos zerados.
     *
     * @param id          Identificador da carteira
     * @param nomeTitular Nome do titular
     * @param corretora   Nome da corretora
     * @return Uma nova instância de {@code CarteiraDTO}
     */
    public static CarteiraDTO basico(int id, String nomeTitular, String corretora) {
        return new CarteiraDTO(id, nomeTitular, corretora, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
