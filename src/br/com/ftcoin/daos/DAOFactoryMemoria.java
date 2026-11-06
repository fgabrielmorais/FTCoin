package br.com.ftcoin.daos;

/**
 * Família de DAOs em memória, usada no vídeo de demonstração e nos testes.
 * <p>
 * Cada DAO é criado uma vez por fábrica, para que todas as telas que usam a mesma fábrica
 * enxerguem os mesmos dados.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class DAOFactoryMemoria extends DAOFactory {

	private final CarteiraDAO carteiraDAO = new CarteiraDAOMemoria();
	private final MovimentacaoDAO movimentacaoDAO = new MovimentacaoDAOMemoria();
	private final OraculoDAO oraculoDAO = new OraculoDAOMemoria();

	/**
	 * Cria uma família vazia de DAOs em memória.
	 */
	public DAOFactoryMemoria() {
		super();
	}

	@Override
	public CarteiraDAO criarCarteiraDAO() {
		return carteiraDAO;
	}

	@Override
	public MovimentacaoDAO criarMovimentacaoDAO() {
		return movimentacaoDAO;
	}

	@Override
	protected OraculoDAO criarOraculoSimulado() {
		return oraculoDAO;
	}
}
