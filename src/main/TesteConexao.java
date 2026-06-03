package main;

import java.sql.Connection;

import br.com.ftcoin.daos.ConnectionFactory;

public class TesteConexao {
	public static void main(String[] args) {
		System.out.println("Testando a conexão do Banco de Dados");
	
		try(Connection conexao = ConnectionFactory.getConnection()){
			if(conexao != null) {
				System.out.println("Deu green!");
			}
			
		} catch(Exception e) {
			System.err.println("Deu red");
			e.printStackTrace();
		}
	}
}
