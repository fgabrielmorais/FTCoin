package br.com.ftcoin.models;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Posição de uma carteira apurada pelo custo médio ponderado de aquisição.
 * <p>
 * A compra recalcula o custo médio; a venda mantém o custo médio e realiza o ganho ou a perda.
 * O ganho não realizado compara o valor de mercado com o valor a custo do saldo.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class Posicao {

	private static final int ESCALA_CUSTO_MEDIO = 8;
	private static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_EVEN;

	private BigDecimal saldo = BigDecimal.ZERO;
	private BigDecimal valorACusto = BigDecimal.ZERO;
	private BigDecimal ganhoRealizado = BigDecimal.ZERO;

	/**
	 * Cria uma posição zerada, sem movimentações.
	 */
	public Posicao() {
		// Saldo, custo e ganho começam em zero
	}

	/**
	 * Aplica uma compra: aumenta o saldo e o custo total, o que recalcula o custo médio.
	 *
	 * @param quantidade quantidade comprada
	 * @param preco cotação usada na compra
	 */
	public void comprar(BigDecimal quantidade, BigDecimal preco) {
		saldo = saldo.add(quantidade);
		valorACusto = valorACusto.add(quantidade.multiply(preco));
	}

	/**
	 * Aplica uma venda: mantém o custo médio e realiza o ganho ou a perda sobre a quantidade vendida.
	 *
	 * @param quantidade quantidade vendida
	 * @param preco cotação usada na venda
	 * @throws RegraNegocioException se a quantidade for maior que o saldo
	 */
	public void vender(BigDecimal quantidade, BigDecimal preco) {
		if (quantidade.compareTo(saldo) > 0) {
			throw new RegraNegocioException("erro.movimentacao.saldoInsuficiente", saldo, quantidade);
		}

		BigDecimal custoMedio = getCustoMedio();
		ganhoRealizado = ganhoRealizado.add(quantidade.multiply(preco.subtract(custoMedio)));
		saldo = saldo.subtract(quantidade);
		// Carteira zerada não carrega resíduo de arredondamento do custo médio
		valorACusto = saldo.signum() == 0 ? BigDecimal.ZERO : valorACusto.subtract(quantidade.multiply(custoMedio));
	}

	/**
	 * Devolve o saldo de FTCoins.
	 *
	 * @return a quantidade de FTCoins na carteira
	 */
	public BigDecimal getSaldo() {
		return saldo;
	}

	/**
	 * Calcula o custo médio ponderado do saldo, com 8 casas e arredondamento {@code HALF_EVEN}.
	 *
	 * @return o custo médio por FTCoin, ou zero se o saldo for zero
	 */
	public BigDecimal getCustoMedio() {
		if (saldo.signum() == 0) {
			return BigDecimal.ZERO;
		}
		return valorACusto.divide(saldo, ESCALA_CUSTO_MEDIO, ARREDONDAMENTO);
	}

	/**
	 * Devolve o valor a custo do saldo (saldo vezes custo médio).
	 *
	 * @return o valor a custo, em reais
	 */
	public BigDecimal getValorACusto() {
		return valorACusto;
	}

	/**
	 * Devolve a soma dos ganhos e perdas já realizados nas vendas.
	 *
	 * @return o ganho realizado, em reais; negativo indica perda
	 */
	public BigDecimal getGanhoRealizado() {
		return ganhoRealizado;
	}

	/**
	 * Calcula o valor de mercado do saldo.
	 *
	 * @param cotacao cotação atual
	 * @return saldo vezes a cotação, em reais
	 */
	public BigDecimal calcularValorDeMercado(BigDecimal cotacao) {
		return saldo.multiply(cotacao);
	}

	/**
	 * Calcula o ganho ou a perda que se realizaria vendendo todo o saldo pela cotação informada.
	 *
	 * @param cotacao cotação atual
	 * @return valor de mercado menos valor a custo, em reais
	 */
	public BigDecimal calcularGanhoNaoRealizado(BigDecimal cotacao) {
		return calcularValorDeMercado(cotacao).subtract(valorACusto);
	}

	/**
	 * Calcula o ganho ou a perda total: realizado mais não realizado.
	 *
	 * @param cotacao cotação atual
	 * @return o resultado total da carteira, em reais
	 */
	public BigDecimal calcularGanhoTotal(BigDecimal cotacao) {
		return ganhoRealizado.add(calcularGanhoNaoRealizado(cotacao));
	}
}
