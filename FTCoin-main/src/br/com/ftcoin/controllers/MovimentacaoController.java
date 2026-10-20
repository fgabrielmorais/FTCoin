package br.com.ftcoin.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import br.com.ftcoin.daos.CarteiraDAO;
import br.com.ftcoin.daos.MovimentacaoDAO;
import br.com.ftcoin.daos.OraculoDAO;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.Posicao;
import br.com.ftcoin.models.RegraNegocioException;
import br.com.ftcoin.models.TipoOperacao;

public class MovimentacaoController {
	private MovimentacaoDAO movimentacaoDAO;
	private CarteiraDAO carteiraDAO;
	private OraculoDAO oraculoDAO;

	public MovimentacaoController(MovimentacaoDAO movDAO, CarteiraDAO cartDAO, OraculoDAO oraculoDAO) {
		this.movimentacaoDAO = movDAO;
		this.carteiraDAO = cartDAO;
		this.oraculoDAO = oraculoDAO;
	}

	public Movimentacao registrarOperacao(int idCarteira, TipoOperacao tipo, BigDecimal quantidade) {
		carteiraDAO.buscarExistente(idCarteira);
		
		Movimentacao novaMovimentacao = new Movimentacao();
		novaMovimentacao.setIdCarteira(idCarteira);
		novaMovimentacao.setDataOperacao(LocalDate.now());
		novaMovimentacao.setTipoOperacao(tipo);
		novaMovimentacao.setQuantidadeMovimentada(quantidade);
		
		// Aplicar na posição atual valida a operação (ex.: saldo insuficiente na venda) antes de gravar
		tipo.aplicar(apurarPosicao(idCarteira), quantidade, buscarCotacaoAtual());
	
		movimentacaoDAO.inserir(novaMovimentacao);
		return novaMovimentacao;
	}

	public BigDecimal calcularValorDaOperacao(Movimentacao movimentacao) {
		return movimentacao.getQuantidadeMovimentada().multiply(buscarCotacao(movimentacao.getDataOperacao()));
	}

	// Histórico em ordem cronológica, que é a ordem exigida pelo cálculo do custo médio
	public List<Movimentacao> buscarHistorico(int idCarteira) {
		List<Movimentacao> historico = new ArrayList<>(movimentacaoDAO.buscarPorCarteira(idCarteira));
		historico.sort(Comparator.comparing(Movimentacao::getDataOperacao).thenComparing(Movimentacao::getIdMovimento));
		return historico;
	}

	// Cada movimentação é valorizada pela cotação do Oráculo na data em que ocorreu
	public Posicao apurarPosicao(int idCarteira) {
		Posicao posicao = new Posicao();
		for (Movimentacao mov : buscarHistorico(idCarteira)) {
			BigDecimal cotacaoNaData = buscarCotacao(mov.getDataOperacao());
			mov.getTipoOperacao().aplicar(posicao, mov.getQuantidadeMovimentada(), cotacaoNaData);
		}
		return posicao;
	}

	public BigDecimal buscarCotacaoAtual() {
		return buscarCotacao(LocalDate.now());
	}

	public BigDecimal buscarCotacao(LocalDate data) {
		BigDecimal cotacao = oraculoDAO.buscarCotacaoPorData(data);
		if (cotacao == null) {
			throw new RegraNegocioException("erro.oraculo.semCotacao", data);
		}
		return cotacao;
	}
}
