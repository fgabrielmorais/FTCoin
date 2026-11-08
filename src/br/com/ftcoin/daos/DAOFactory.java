package br.com.ftcoin.daos;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fábrica abstrata das famílias de DAOs (padrão Abstract Factory).
 * <p>
 * Cada subclasse cria os DAOs de um mecanismo de persistência; o resto do sistema recebe os DAOs
 * sem saber se os dados estão em memória ou no MariaDB. A fábrica não é global: quem a cria
 * (a montagem da aplicação) decide quanto tempo ela vive e a fecha ao final.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public abstract class DAOFactory {

	private static final Logger LOG = Logger.getLogger(DAOFactory.class.getName());

	/**
	 * Construtor usado pelas fábricas concretas.
	 */
	protected DAOFactory() {
		// Sem estado comum entre as fábricas concretas
	}

	/**
	 * Cria a fábrica do mecanismo de persistência pedido. Único ponto do sistema que escolhe a implementação.
	 *
	 * @param tipo mecanismo de persistência desejado
	 * @return uma nova fábrica para o mecanismo
	 * @throws PersistenciaException se o MariaDB for pedido sem o arquivo de credenciais
	 */
	public static DAOFactory criar(TipoPersistencia tipo) {
		LOG.log(Level.INFO, "Criando fábrica de DAOs: {0}", tipo.getNome());
		return switch (tipo) {
			case MEMORIA -> new DAOFactoryMemoria();
			case MARIADB -> new DAOFactoryMariaDB();
		};
	}

	/**
	 * Devolve o DAO de carteiras desta família.
	 *
	 * @return o DAO de carteiras
	 */
	public abstract CarteiraDAO criarCarteiraDAO();

	/**
	 * Devolve o DAO de movimentações desta família.
	 *
	 * @return o DAO de movimentações
	 */
	public abstract MovimentacaoDAO criarMovimentacaoDAO();

	/**
	 * Devolve o oráculo de cotações: o simulado (calculado localmente, igual nas duas famílias, pois o
	 * oráculo não é uma tabela) ou o servidor de cotações (API), conforme {@code oracle.provider} em
	 * {@code oracle.properties}, sempre com cache.
	 *
	 * @return o oráculo ativo, com cache
	 * @throws PersistenciaException se {@code oracle.properties} pedir a API com valores inválidos ou ausentes
	 */
	public final OraculoDAOCache criarOraculoDAO() {
		return new OraculoDAOCache(FabricaOraculo.criar(new OraculoDAOMemoria()));
	}

	/**
	 * Libera os recursos da família, como a conexão com o banco. A implementação padrão não faz nada.
	 */
	public void fechar() {
		// Persistência sem recursos externos não precisa liberar nada
	}
}
