package br.com.ftcoin.daos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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

	private static final String ARQUIVO_CONFIGURACAO = "application.properties";
	private static final String CHAVE_PERSISTENCIA = "storage.provider";

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
		LOG.log(Level.INFO, "Criando fábrica de DAOs: {0}", tipo.getValorConfiguracao());
		return switch (tipo) {
			case MEMORIA -> new DAOFactoryMemoria();
			case MARIADB -> new DAOFactoryMariaDB();
		};
	}

	/**
	 * Lê o mecanismo de persistência configurado em {@code application.properties} ({@code storage.provider}).
	 *
	 * @return o mecanismo configurado
	 * @throws PersistenciaException se o arquivo não existir, não puder ser lido ou tiver valor inválido
	 */
	public static TipoPersistencia lerTipoConfigurado() {
		Properties propriedades = new Properties();
		try (InputStream input = DAOFactory.class.getClassLoader().getResourceAsStream(ARQUIVO_CONFIGURACAO)) {
			if (input == null) {
				LOG.log(Level.SEVERE, "Arquivo {0} não encontrado no classpath", ARQUIVO_CONFIGURACAO);
				throw new PersistenciaException("erro.configuracao.arquivoAusente", ARQUIVO_CONFIGURACAO);
			}
			propriedades.load(input);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Falha ao ler " + ARQUIVO_CONFIGURACAO, e);
			throw new PersistenciaException("erro.configuracao.leitura", e);
		}

		String valor = propriedades.getProperty(CHAVE_PERSISTENCIA);
		try {
			return TipoPersistencia.fromString(valor);
		} catch (PersistenciaException e) {
			LOG.log(Level.SEVERE, "Valor inválido para {0} em {1}: [{2}]", new Object[] { CHAVE_PERSISTENCIA, ARQUIVO_CONFIGURACAO, valor });
			throw e;
		}
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
	 * Devolve o DAO de cotações desta família.
	 *
	 * @return o DAO de cotações
	 */
	public abstract OraculoDAO criarOraculoDAO();

	/**
	 * Libera os recursos da família, como a conexão com o banco. A implementação padrão não faz nada.
	 */
	public void fechar() {
		// Persistência sem recursos externos não precisa liberar nada
	}
}
