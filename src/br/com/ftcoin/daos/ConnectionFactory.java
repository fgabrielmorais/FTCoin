package br.com.ftcoin.daos;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {

	private static final String ARQUIVO_CREDENCIAIS = "database.properties";

	private static Properties propriedades;

	private ConnectionFactory() {}

	public static Connection getConnection() {
		Properties credenciais = carregarCredenciais();
		try {
			return DriverManager.getConnection(
				credenciais.getProperty("db.url"),
				credenciais.getProperty("db.user"),
				credenciais.getProperty("db.password")
			);
		} catch (SQLException e) {
			throw new PersistenciaException("erro.persistencia.conexao", e);
		}
	}

	//O arquivo é lido apenas na primeira conexão
	private static Properties carregarCredenciais() {
		if (propriedades == null) {
			Properties lidas = new Properties();
			try (InputStream input = ConnectionFactory.class.getClassLoader().getResourceAsStream(ARQUIVO_CREDENCIAIS)) {
				if (input == null) {
					throw new PersistenciaException("erro.configuracao.arquivoAusente", ARQUIVO_CREDENCIAIS);
				}
				lidas.load(input);
			} catch (IOException e) {
				throw new PersistenciaException("erro.configuracao.leitura", e);
			}
			propriedades = lidas;
		}
		return propriedades;
	}
}
