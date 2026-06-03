package br.com.ftcoin.daos;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class ConnectionFactory {
	
	private static Properties propriedades = new Properties();
	
	//Irá executar apenas uma vez para verificar a existência do arquivo
	static {
		try (InputStream input = ConnectionFactory.class.getClassLoader().getResourceAsStream("database.properties")){
			if(input == null){
				throw new RuntimeException("Arquivo não encontrado na pasta!");
			}
			propriedades.load(input);
		} catch (Exception e) {
			throw new RuntimeException("Erro ao carregar as configurações do banco: " + e.getMessage(), e);
		}
	}
	
	
	public static Connection getConnection() {
		try {
			return DriverManager.getConnection(
				propriedades.getProperty("db.url"),
				propriedades.getProperty("db.user"),
				propriedades.getProperty("db.password")
			);
		} catch (SQLException e) {
			throw new RuntimeException("Erro ao conectar com o banco de dados" + e.getMessage(), e);
		}
	}
}
