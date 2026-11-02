package br.com.ftcoin.services;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.ftcoin.daos.CarteiraDAO;
import br.com.ftcoin.daos.MovimentacaoDAO;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.RegraNegocioException;

/**
 * Casos de uso das carteiras: cadastro, consulta, edição e exclusão.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class CarteiraService {

	private static final Logger LOG = Logger.getLogger(CarteiraService.class.getName());

	private final CarteiraDAO carteiraDAO;
	private final MovimentacaoDAO movimentacaoDAO;

	/**
	 * Cria o serviço sobre os DAOs da persistência ativa.
	 *
	 * @param carteiraDAO DAO das carteiras
	 * @param movimentacaoDAO DAO das movimentações, usado para impedir a exclusão de carteiras com histórico
	 */
	public CarteiraService(CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO) {
		this.carteiraDAO = carteiraDAO;
		this.movimentacaoDAO = movimentacaoDAO;
	}

	/**
	 * Cadastra uma nova carteira.
	 *
	 * @param nomeTitular nome do titular
	 * @param corretora nome da corretora
	 * @return a carteira criada, já com o identificador
	 * @throws RegraNegocioException se o titular ou a corretora estiverem vazios
	 */
	public Carteira criarCarteira(String nomeTitular, String corretora) {
		Carteira novaCarteira = new Carteira();
		novaCarteira.setNomeTitular(nomeTitular);
		novaCarteira.setCorretora(corretora);

		carteiraDAO.inserir(novaCarteira);
		// O nome do titular é dado pessoal: a carteira é identificada pelo ID
		LOG.log(Level.INFO, "Carteira {0} criada (corretora={1})", new Object[] { novaCarteira.getId(), corretora });
		return novaCarteira;
	}

	/**
	 * Lista todas as carteiras cadastradas.
	 *
	 * @return as carteiras, possivelmente vazia
	 */
	public List<Carteira> listarTodasCarteiras() {
		return carteiraDAO.listarTodas();
	}

	/**
	 * Busca uma carteira pelo identificador.
	 *
	 * @param id identificador da carteira
	 * @return a carteira encontrada
	 * @throws RegraNegocioException se não houver carteira com esse ID
	 */
	public Carteira buscarCarteira(int id) {
		return carteiraDAO.buscarExistente(id);
	}

	/**
	 * Altera o titular e a corretora de uma carteira.
	 *
	 * @param id identificador da carteira
	 * @param novoTitular novo nome do titular
	 * @param novaCorretora novo nome da corretora
	 * @return a carteira com os dados novos
	 * @throws RegraNegocioException se a carteira não existir ou se algum campo estiver vazio
	 */
	public Carteira editarCarteira(int id, String novoTitular, String novaCorretora) {
		carteiraDAO.buscarExistente(id);

		Carteira carteiraEditada = new Carteira();
		carteiraEditada.setId(id);
		carteiraEditada.setNomeTitular(novoTitular);
		carteiraEditada.setCorretora(novaCorretora);

		carteiraDAO.atualizar(carteiraEditada);
		LOG.log(Level.INFO, "Carteira {0} editada (corretora={1})", new Object[] { id, novaCorretora });
		return carteiraEditada;
	}

	/**
	 * Exclui uma carteira sem movimentações.
	 *
	 * @param id identificador da carteira
	 * @throws RegraNegocioException se a carteira não existir ou se tiver movimentações registradas
	 */
	public void excluirCarteira(int id) {
		carteiraDAO.buscarExistente(id);

		int movimentacoes = movimentacaoDAO.buscarPorCarteira(id).size();
		if (movimentacoes > 0) {
			// A recusa é registrada como WARNING por quem trata a exceção (presenter/menu)
			LOG.log(Level.FINE, "Exclusão da carteira {0} recusada: {1} movimentação(ões) vinculada(s)",
					new Object[] { id, movimentacoes });
			throw new RegraNegocioException("erro.carteira.possuiMovimentacoes");
		}

		carteiraDAO.excluir(id);
		LOG.log(Level.INFO, "Carteira {0} excluída", id);
	}
}
