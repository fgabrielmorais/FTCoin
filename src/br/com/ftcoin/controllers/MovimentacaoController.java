package br.com.ftcoin.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.ftcoin.daos.CarteiraDAO;
import br.com.ftcoin.daos.MovimentacaoDAO;
import br.com.ftcoin.daos.OraculoDAO;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.Posicao;
import br.com.ftcoin.models.RegraNegocioException;
import br.com.ftcoin.models.TipoOperacao;

public class MovimentacaoController {
	private static final Logger LOG = Logger.getLogger(MovimentacaoController.class.getName());

	private MovimentacaoDAO movimentacaoDAO;
	private CarteiraDAO carteiraDAO;
	private OraculoDAO oraculoDAO;

	public MovimentacaoController(MovimentacaoDAO movDAO, CarteiraDAO cartDAO, OraculoDAO oraculoDAO) {
		this.movimentacaoDAO = movDAO;
		this.carteiraDAO = cartDAO;
		this.oraculoDAO = oraculoDAO;
	}

	public Movimentacao registrarOperacao(int idCarteira, TipoOperacao tipo, BigDecimal quantidade) {
		LOG.log(Level.FINE, "Validando {0} de {1} FTC na carteira {2}", new Object[] { tipo, quantidade, idCarteira });
		carteiraDAO.buscarExistente(idCarteira);
		
		Movimentacao novaMovimentacao = new Movimentacao();
		novaMovimentacao.setIdCarteira(idCarteira);
		novaMovimentacao.setDataOperacao(LocalDate.now());
		novaMovimentacao.setTipoOperacao(tipo);
		novaMovimentacao.setQuantidadeMovimentada(quantidade);
		
		// Aplicar na posição atual valida a operação (ex.: saldo insuficiente na venda) antes de gravar
		BigDecimal cotacaoAtual = buscarCotacaoAtual();
		tipo.aplicar(apurarPosicao(idCarteira), quantidade, cotacaoAtual);
	
		movimentacaoDAO.inserir(novaMovimentacao);
		LOG.log(Level.INFO, "{0} de {1} FTC registrada na carteira {2} (cotação {3})",
				new Object[] { tipo, quantidade, idCarteira, cotacaoAtual });
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
		List<Movimentacao> historico = buscarHistorico(idCarteira);
		for (Movimentacao mov : historico) {
			BigDecimal cotacaoNaData = buscarCotacao(mov.getDataOperacao());
			mov.getTipoOperacao().aplicar(posicao, mov.getQuantidadeMovimentada(), cotacaoNaData);
			// Passo a passo do custo médio, para conferir com a planilha da disciplina
			LOG.finest(() -> "Carteira " + idCarteira + ", movimentação " + mov.getIdMovimento() + ": "
					+ mov.getTipoOperacao() + " " + mov.getQuantidadeMovimentada() + " a " + cotacaoNaData
					+ " -> saldo=" + posicao.getSaldo().toPlainString()
					+ ", custo médio=" + posicao.getCustoMedio().toPlainString()
					+ ", ganho realizado=" + posicao.getGanhoRealizado().toPlainString());
		}
		LOG.log(Level.FINE, "Posição da carteira {0} apurada: {1} movimentação(ões), saldo={2}, custo médio={3}",
				new Object[] { idCarteira, historico.size(), posicao.getSaldo(), posicao.getCustoMedio() });
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
