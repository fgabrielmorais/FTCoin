package br.com.ftcoin.daos;

public class DAOFactoryMariaDB extends DAOFactory {

	@Override
	public CarteiraDAO criarCarteiraDAO() {
		return new CarteiraDAOMariaDB();
	}

	@Override
	public MovimentacaoDAO criarMovimentacaoDAO() {
		return new MovimentacaoDAOMariaDB();
	}

	@Override
	public OraculoDAO criarOraculoDAO() {
		return new OraculoDAOMariaDB();
	}
}
