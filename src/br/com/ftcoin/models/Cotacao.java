package br.com.ftcoin.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Cotação da FTCoin em um dia, em reais, como informada pelo Oráculo.
 * Também calcula a variação em relação a uma cotação anterior.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public final class Cotacao {

	private static final int ESCALA_PORCENTAGEM = 2;
	private static final BigDecimal CEM = BigDecimal.valueOf(100);

	private final LocalDate data;
	private final BigDecimal valor;

	/**
	 * Cria a cotação de um dia.
	 *
	 * @param data dia da cotação
	 * @param valor valor da FTCoin no dia, em reais
	 * @throws NullPointerException se a data ou o valor forem nulos
	 */
	public Cotacao(LocalDate data, BigDecimal valor) {
		this.data = Objects.requireNonNull(data, "data");
		this.valor = Objects.requireNonNull(valor, "valor");
	}

	/**
	 * Devolve o dia da cotação.
	 *
	 * @return a data
	 */
	public LocalDate getData() {
		return data;
	}

	/**
	 * Devolve o valor da FTCoin no dia.
	 *
	 * @return o valor, em reais
	 */
	public BigDecimal getValor() {
		return valor;
	}

	/**
	 * Calcula quanto o valor mudou desde uma cotação anterior.
	 *
	 * @param anterior cotação de um dia anterior; {@code null} quando não há
	 * @return a diferença em reais, ou zero sem cotação anterior
	 */
	public BigDecimal calcularVariacao(Cotacao anterior) {
		return (anterior == null) ? BigDecimal.ZERO : valor.subtract(anterior.valor);
	}

	/**
	 * Calcula a variação desde uma cotação anterior, em porcentagem.
	 *
	 * @param anterior cotação de um dia anterior; {@code null} quando não há
	 * @return a variação com 2 casas, ou zero sem cotação anterior positiva
	 */
	public BigDecimal calcularVariacaoPercentual(Cotacao anterior) {
		if (anterior == null || anterior.valor.signum() <= 0) {
			return BigDecimal.ZERO;
		}
		return calcularVariacao(anterior).multiply(CEM).divide(anterior.valor, ESCALA_PORCENTAGEM, RoundingMode.HALF_EVEN);
	}

	@Override
	public boolean equals(Object outro) {
		return outro instanceof Cotacao cotacao && data.equals(cotacao.data) && valor.compareTo(cotacao.valor) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(data, valor.stripTrailingZeros());
	}

	@Override
	public String toString() {
		return "Cotacao[" + data + " = " + valor.toPlainString() + "]";
	}
}
