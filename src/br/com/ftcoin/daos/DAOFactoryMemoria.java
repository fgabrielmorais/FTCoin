package br.com.ftcoin.daos;

public class DAOFactoryMemoria extends DAOFactory {

	@Override
	public CarteiraDAO criarCarteiraDAO() {
		return new CarteiraDAOMemoria();
	}

	@Override
	public MovimentacaoDAO criarMovimentacaoDAO() {
		return new MovimentacaoDAOMemoria();
	}

	@Override
	public OraculoDAO criarOraculoDAO() {
		return new OraculoDAOMemoria();
	}
}
