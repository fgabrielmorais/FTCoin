package br.com.ftcoin.models;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Posição de uma carteira apurada pelo custo médio ponderado de aquisição (CMP)
public class Posicao {

	private static final int ESCALA_CUSTO_MEDIO = 8;
	private static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_EVEN;

	private BigDecimal saldo = BigDecimal.ZERO;
	private BigDecimal valorACusto = BigDecimal.ZERO;
	private BigDecimal ganhoRealizado = BigDecimal.ZERO;

	// A compra aumenta o saldo e o custo total, o que recalcula o custo médio
	public void comprar(BigDecimal quantidade, BigDecimal preco) {
		saldo = saldo.add(quantidade);
		valorACusto = valorACusto.add(quantidade.multiply(preco));
	}

	// A venda mantém o custo médio e realiza o ganho ou a perda sobre a quantidade vendida
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

	public BigDecimal getSaldo() { return saldo; }

	public BigDecimal getCustoMedio() {
		if (saldo.signum() == 0) {
			return BigDecimal.ZERO;
		}
		return valorACusto.divide(saldo, ESCALA_CUSTO_MEDIO, ARREDONDAMENTO);
	}

	public BigDecimal getValorACusto() { return valorACusto; }

	public BigDecimal getGanhoRealizado() { return ganhoRealizado; }

	public BigDecimal calcularValorDeMercado(BigDecimal cotacao) {
		return saldo.multiply(cotacao);
	}

	public BigDecimal calcularGanhoNaoRealizado(BigDecimal cotacao) {
		return calcularValorDeMercado(cotacao).subtract(valorACusto);
	}

	public BigDecimal calcularGanhoTotal(BigDecimal cotacao) {
		return ganhoRealizado.add(calcularGanhoNaoRealizado(cotacao));
	}
}
