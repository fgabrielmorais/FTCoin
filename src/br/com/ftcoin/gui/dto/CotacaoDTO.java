package br.com.ftcoin.gui.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import br.com.ftcoin.gui.common.OrigemCotacao;

/**
 * <p>
 * Objeto de Transferência de Dados ({@code DTO}) imutável para transporte dos resultados de cotação.
 * </p>
 *
 * <p>
 * Contém o preço unitário apurado, a data de referência, a origem dos dados (Oráculo versus Cache)
 * e telemetria de tempo de resposta em milissegundos para apresentação visual na {@code GUI}.
 * </p>
 *
 * @param data            Data cronológica referente à cotação
 * @param valor           Valor monetário unitário da moeda virtual
 * @param origem          Identificador da procedência do dado ({@code OrigemCotacao})
 * @param horaConsulta    Horário exato em que a cotação foi recuperada
 * @param tempoRespostaMs Tempo total despendido na requisição em milissegundos
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public record CotacaoDTO(
    LocalDate data,
    BigDecimal valor,
    OrigemCotacao origem,
    LocalTime horaConsulta,
    long tempoRespostaMs
) {

    /**
     * Construtor compacto para inicialização segura e sanitização de atributos nulos.
     *
     * @param data            Data da cotação
     * @param valor           Valor apurado
     * @param origem          Origem da informação
     * @param horaConsulta    Horário do processamento
     * @param tempoRespostaMs Latência da chamada
     */
    public CotacaoDTO {
        if (valor == null) {
            valor = BigDecimal.ZERO;
        }
        if (horaConsulta == null) {
            horaConsulta = LocalTime.now();
        }
    }
}
