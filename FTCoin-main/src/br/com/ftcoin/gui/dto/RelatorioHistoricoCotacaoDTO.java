package br.com.ftcoin.gui.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import br.com.ftcoin.gui.common.OrigemCotacao;

/**
 * <p>
 * Objeto de Transferência de Dados ({@code DTO}) imutável para a série temporal de cotações.
 * </p>
 *
 * <p>
 * Apresenta a evolução diária dos preços da moeda virtual, a oscilação absoluta, a variação percentual
 * e a fonte originária ({@code ORACULO} ou {@code CACHE}) no <b>Relatório 5</b> e na tela de cotações da {@code GUI}.
 * </p>
 *
 * @param data               Data de referência do registro de preço
 * @param valorCotacao       Valor unitário da moeda virtual na data
 * @param variacaoAbsoluta   Oscilação nominal em relação ao fechamento anterior
 * @param variacaoPercentual Taxa percentual de oscilação em relação ao dia anterior
 * @param origem             Identificador da fonte de dados ({@code OrigemCotacao})
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public record RelatorioHistoricoCotacaoDTO(
    LocalDate data,
    BigDecimal valorCotacao,
    BigDecimal variacaoAbsoluta,
    BigDecimal variacaoPercentual,
    OrigemCotacao origem
) {

    /**
     * Construtor compacto para inicialização e prevenção de nulos nos campos numéricos.
     *
     * @param data               Data da cotação
     * @param valorCotacao       Preço unitário
     * @param variacaoAbsoluta   Variação nominal
     * @param variacaoPercentual Variação percentual
     * @param origem             Procedência do dado
     */
    public RelatorioHistoricoCotacaoDTO {
        valorCotacao = (valorCotacao != null) ? valorCotacao : BigDecimal.ZERO;
        variacaoAbsoluta = (variacaoAbsoluta != null) ? variacaoAbsoluta : BigDecimal.ZERO;
        variacaoPercentual = (variacaoPercentual != null) ? variacaoPercentual : BigDecimal.ZERO;
    }
}
