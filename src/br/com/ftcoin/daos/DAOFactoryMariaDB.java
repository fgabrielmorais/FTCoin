package br.com.ftcoin.daos;

/**
 * Família de DAOs do MariaDB. Todos os DAOs criados usam a mesma {@link ConexaoMariaDB}.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class DAOFactoryMariaDB extends DAOFactory {

	private final ConexaoMariaDB conexao;

	/**
	 * Cria a família de DAOs do MariaDB. A conexão só é aberta na primeira consulta.
	 *
	 * @throws PersistenciaException se o arquivo de credenciais não existir ou não puder ser lido
	 */
	public DAOFactoryMariaDB() {
		super();
		this.conexao = ConexaoMariaDB.getInstancia();
	}

	@Override
	public CarteiraDAO criarCarteiraDAO() {
		return new CarteiraDAOMariaDB(conexao);
	}

	@Override
	public MovimentacaoDAO criarMovimentacaoDAO() {
		return new MovimentacaoDAOMariaDB(conexao);
	}

	@Override
	public OraculoDAO criarOraculoDAO() {
		return new OraculoDAOMariaDB(conexao);
	}

	/**
	 * Fecha a conexão compartilhada com o MariaDB.
	 */
	@Override
	public void fechar() {
		conexao.fechar();
	}
}
