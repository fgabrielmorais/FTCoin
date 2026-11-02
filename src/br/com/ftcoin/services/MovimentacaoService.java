package br.com.ftcoin.services;

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

/**
 * Casos de uso das movimentações: compra, venda, cotação, histórico e apuração da posição pelo custo médio.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class MovimentacaoService {

	private static final Logger LOG = Logger.getLogger(MovimentacaoService.class.getName());

	private final MovimentacaoDAO movimentacaoDAO;
	private final CarteiraDAO carteiraDAO;
	private final OraculoDAO oraculoDAO;

	/**
	 * Cria o serviço sobre os DAOs da persistência ativa.
	 *
	 * @param movimentacaoDAO DAO das movimentações
	 * @param carteiraDAO DAO das carteiras, usado para validar a carteira da operação
	 * @param oraculoDAO DAO das cotações diárias
	 */
	public MovimentacaoService(MovimentacaoDAO movimentacaoDAO, CarteiraDAO carteiraDAO, OraculoDAO oraculoDAO) {
		this.movimentacaoDAO = movimentacaoDAO;
		this.carteiraDAO = carteiraDAO;
		this.oraculoDAO = oraculoDAO;
	}

	/**
	 * Registra uma compra ou venda na data de hoje, pela cotação do dia.
	 *
	 * @param idCarteira identificador da carteira
	 * @param tipo compra ou venda
	 * @param quantidade quantidade de FTCoins, maior que zero
	 * @return a movimentação registrada
	 * @throws RegraNegocioException se a carteira não existir, a quantidade for inválida,
	 *         não houver cotação para hoje ou a venda superar o saldo
	 */
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

	/**
	 * Calcula o valor em reais de uma movimentação, pela cotação da data em que ocorreu.
	 *
	 * @param movimentacao movimentação registrada
	 * @return quantidade multiplicada pela cotação do dia da operação
	 * @throws RegraNegocioException se não houver cotação para a data da operação
	 */
	public BigDecimal calcularValorDaOperacao(Movimentacao movimentacao) {
		return movimentacao.getQuantidadeMovimentada().multiply(buscarCotacao(movimentacao.getDataOperacao()));
	}

	/**
	 * Lista as movimentações de uma carteira em ordem cronológica, a ordem exigida pelo cálculo do custo médio.
	 *
	 * @param idCarteira identificador da carteira
	 * @return o histórico ordenado por data e, no mesmo dia, por ordem de registro
	 */
	public List<Movimentacao> buscarHistorico(int idCarteira) {
		List<Movimentacao> historico = new ArrayList<>(movimentacaoDAO.buscarPorCarteira(idCarteira));
		historico.sort(Comparator.comparing(Movimentacao::getDataOperacao).thenComparing(Movimentacao::getIdMovimento));
		return historico;
	}

	/**
	 * Apura a posição de uma carteira, valorizando cada movimentação pela cotação do dia em que ocorreu.
	 *
	 * @param idCarteira identificador da carteira
	 * @return saldo, custo médio e ganho realizado da carteira
	 * @throws RegraNegocioException se faltar cotação para alguma data do histórico
	 */
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

	/**
	 * Busca a cotação de hoje.
	 *
	 * @return a cotação do dia, em reais
	 * @throws RegraNegocioException se não houver cotação para hoje
	 */
	public BigDecimal buscarCotacaoAtual() {
		return buscarCotacao(LocalDate.now());
	}

	/**
	 * Busca a cotação de uma data.
	 *
	 * @param data dia da cotação
	 * @return a cotação do dia, em reais
	 * @throws RegraNegocioException se não houver cotação para a data
	 */
	public BigDecimal buscarCotacao(LocalDate data) {
		BigDecimal cotacao = oraculoDAO.buscarCotacaoPorData(data);
		if (cotacao == null) {
			throw new RegraNegocioException("erro.oraculo.semCotacao", data);
		}
		return cotacao;
	}
}
