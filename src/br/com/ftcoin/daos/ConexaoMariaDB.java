package br.com.ftcoin.daos;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Conexão única com o MariaDB, compartilhada por todos os DAOs de banco (padrão Singleton).
 * <p>
 * A conexão é aberta na primeira consulta, reaberta se cair e fechada uma única vez ao encerrar a
 * aplicação, por {@link #fechar()}. As credenciais vêm do arquivo {@code database.properties}.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public final class ConexaoMariaDB {

	private static final String ARQUIVO_CREDENCIAIS = "database.properties";
	private static final int TIMEOUT_VALIDACAO_SEGUNDOS = 2;

	private static final Logger LOG = Logger.getLogger(ConexaoMariaDB.class.getName());

	private static ConexaoMariaDB instancia;

	private final Properties credenciais;
	private Connection conexao;

	private ConexaoMariaDB() {
		this.credenciais = carregarCredenciais();
	}

	/**
	 * Devolve a instância única, criando-a no primeiro acesso.
	 *
	 * @return a conexão compartilhada com o MariaDB
	 * @throws PersistenciaException se o arquivo de credenciais não existir ou não puder ser lido
	 */
	public static synchronized ConexaoMariaDB getInstancia() {
		if (instancia == null) {
			instancia = new ConexaoMariaDB();
		}
		return instancia;
	}

	/**
	 * Devolve a conexão aberta, abrindo-a no primeiro uso ou reabrindo-a se tiver caído.
	 * Quem usa a conexão não deve fechá-la; o fechamento é feito por {@link #fechar()}.
	 *
	 * @return a conexão JDBC ativa
	 * @throws PersistenciaException se não for possível conectar ao banco
	 */
	public synchronized Connection getConexao() {
		try {
			if (conexao == null || !conexao.isValid(TIMEOUT_VALIDACAO_SEGUNDOS)) {
				conexao = abrir();
			}
			return conexao;
		} catch (SQLException e) {
			LOG.log(Level.SEVERE, "Falha ao validar a conexão com o MariaDB", e);
			throw new PersistenciaException("erro.persistencia.conexao", e);
		}
	}

	/**
	 * Fecha a conexão, se estiver aberta. Chamado uma vez, ao encerrar a aplicação.
	 */
	public synchronized void fechar() {
		if (conexao == null) {
			return;
		}
		try {
			conexao.close();
			LOG.info("Conexão com o MariaDB fechada");
		} catch (SQLException e) {
			// Falha ao fechar não impede o encerramento; fica só o registro
			LOG.log(Level.WARNING, "Falha ao fechar a conexão com o MariaDB", e);
		} finally {
			conexao = null;
		}
	}

	private Connection abrir() {
		String url = credenciais.getProperty("db.url");
		try {
			Connection nova = DriverManager.getConnection(url, credenciais.getProperty("db.user"), credenciais.getProperty("db.password"));
			// Só a URL vai para o log: usuário e senha nunca são registrados
			LOG.log(Level.INFO, "Conectado ao MariaDB em {0}", url);
			return nova;
		} catch (SQLException e) {
			LOG.log(Level.SEVERE, "Falha ao conectar ao MariaDB em " + url, e);
			throw new PersistenciaException("erro.persistencia.conexao", e);
		}
	}

	private static Properties carregarCredenciais() {
		Properties lidas = new Properties();
		try (InputStream input = ConexaoMariaDB.class.getClassLoader().getResourceAsStream(ARQUIVO_CREDENCIAIS)) {
			if (input == null) {
				LOG.log(Level.SEVERE, "Arquivo {0} não encontrado no classpath; copie database.properties.example", ARQUIVO_CREDENCIAIS);
				throw new PersistenciaException("erro.configuracao.arquivoAusente", ARQUIVO_CREDENCIAIS);
			}
			lidas.load(input);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Falha ao ler " + ARQUIVO_CREDENCIAIS, e);
			throw new PersistenciaException("erro.configuracao.leitura", e);
		}
		LOG.log(Level.FINE, "Credenciais do banco carregadas de {0}", ARQUIVO_CREDENCIAIS);
		return lidas;
	}
}
