package br.com.ftcoin.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Compra ou venda de FTCoins registrada em uma carteira.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class Movimentacao {

	private int idMovimento;
	private int idCarteira;
	private LocalDate dataOperacao;
	private TipoOperacao tipoOperacao;
	private BigDecimal quantidadeMovimentada;

	/**
	 * Cria uma movimentação vazia, a ser preenchida pelos setters.
	 */
	public Movimentacao() {
		// Campos preenchidos depois, com validação da quantidade no setter
	}

	/**
	 * Cria uma movimentação completa.
	 *
	 * @param idMov identificador da movimentação
	 * @param idCarteira identificador da carteira
	 * @param data dia da operação
	 * @param tipo compra ou venda
	 * @param qtd quantidade de FTCoins, maior que zero
	 * @throws RegraNegocioException se a quantidade não for maior que zero
	 */
	public Movimentacao(int idMov, int idCarteira, LocalDate data, TipoOperacao tipo, BigDecimal qtd) {
		this.idMovimento = idMov;
		this.idCarteira = idCarteira;
		this.dataOperacao = data;
		this.tipoOperacao = tipo;
		setQuantidadeMovimentada(qtd);
	}

	/**
	 * Devolve o identificador.
	 *
	 * @return o identificador da movimentação
	 */
	public int getIdMovimento() {
		return idMovimento;
	}

	/**
	 * Define o identificador, atribuído pela persistência.
	 *
	 * @param id identificador da movimentação
	 */
	public void setIdMovimento(int id) {
		this.idMovimento = id;
	}

	/**
	 * Devolve a carteira da operação.
	 *
	 * @return o identificador da carteira
	 */
	public int getIdCarteira() {
		return idCarteira;
	}

	/**
	 * Define a carteira da operação.
	 *
	 * @param id identificador da carteira
	 */
	public void setIdCarteira(int id) {
		this.idCarteira = id;
	}

	/**
	 * Devolve o dia da operação.
	 *
	 * @return a data da operação
	 */
	public LocalDate getDataOperacao() {
		return dataOperacao;
	}

	/**
	 * Define o dia da operação.
	 *
	 * @param data data da operação
	 */
	public void setDataOperacao(LocalDate data) {
		this.dataOperacao = data;
	}

	/**
	 * Devolve o tipo da operação.
	 *
	 * @return compra ou venda
	 */
	public TipoOperacao getTipoOperacao() {
		return tipoOperacao;
	}

	/**
	 * Define o tipo da operação.
	 *
	 * @param tipo compra ou venda
	 */
	public void setTipoOperacao(TipoOperacao tipo) {
		this.tipoOperacao = tipo;
	}

	/**
	 * Devolve a quantidade de FTCoins movimentada.
	 *
	 * @return a quantidade, sempre maior que zero
	 */
	public BigDecimal getQuantidadeMovimentada() {
		return quantidadeMovimentada;
	}

	/**
	 * Define a quantidade de FTCoins movimentada.
	 *
	 * @param quantidade quantidade de FTCoins
	 * @throws RegraNegocioException se a quantidade for nula ou não for maior que zero
	 */
	public void setQuantidadeMovimentada(BigDecimal quantidade) {
		if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
			throw new RegraNegocioException("erro.movimentacao.quantidadeInvalida");
		}
		this.quantidadeMovimentada = quantidade;
	}

	/**
	 * Calcula o valor em reais da operação por uma cotação.
	 *
	 * @param cotacao cotação da FTCoin no dia da operação
	 * @return quantidade movimentada vezes a cotação
	 */
	public BigDecimal calcularValor(BigDecimal cotacao) {
		return quantidadeMovimentada.multiply(cotacao);
	}
}
