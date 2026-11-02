package br.com.ftcoin.daos;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionFactory {

	private static final String ARQUIVO_CREDENCIAIS = "database.properties";

	private static final Logger LOG = Logger.getLogger(ConnectionFactory.class.getName());

	private static Properties propriedades;

	// A primeira conexão bem-sucedida é um evento da aplicação (INFO); as seguintes são detalhe (FINEST)
	private static boolean conectadoAntes;

	private ConnectionFactory() {}

	public static Connection getConnection() {
		Properties credenciais = carregarCredenciais();
		String url = credenciais.getProperty("db.url");
		try {
			Connection conexao = DriverManager.getConnection(
				url,
				credenciais.getProperty("db.user"),
				credenciais.getProperty("db.password")
			);
			if (conectadoAntes) {
				LOG.finest("Conexão aberta com o MariaDB");
			} else {
				conectadoAntes = true;
				// Só a URL vai para o log: usuário e senha nunca são registrados
				LOG.log(Level.INFO, "Conectado ao MariaDB em {0}", url);
			}
			return conexao;
		} catch (SQLException e) {
			LOG.log(Level.SEVERE, "Falha ao conectar ao MariaDB em " + url, e);
			throw new PersistenciaException("erro.persistencia.conexao", e);
		}
	}

	//O arquivo é lido apenas na primeira conexão
	private static Properties carregarCredenciais() {
		if (propriedades == null) {
			Properties lidas = new Properties();
			try (InputStream input = ConnectionFactory.class.getClassLoader().getResourceAsStream(ARQUIVO_CREDENCIAIS)) {
				if (input == null) {
					LOG.log(Level.SEVERE, "Arquivo {0} não encontrado no classpath; copie database.properties.example", ARQUIVO_CREDENCIAIS);
					throw new PersistenciaException("erro.configuracao.arquivoAusente", ARQUIVO_CREDENCIAIS);
				}
				lidas.load(input);
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Falha ao ler " + ARQUIVO_CREDENCIAIS, e);
				throw new PersistenciaException("erro.configuracao.leitura", e);
			}
			propriedades = lidas;
			LOG.log(Level.FINE, "Credenciais do banco carregadas de {0}", ARQUIVO_CREDENCIAIS);
		}
		return propriedades;
	}
}
