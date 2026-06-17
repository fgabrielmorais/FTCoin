package br.com.ftcoin.controllers;

import java.util.List;

import br.com.ftcoin.daos.ICarteiraDAO;
import br.com.ftcoin.models.Carteira;

public class CarteiraController {
	private ICarteiraDAO carteiraDAO;
	
	
	public CarteiraController(ICarteiraDAO carteiraDAO) {
		this.carteiraDAO = carteiraDAO;
	}
	
	//Registra uma nova carteira no sistema com validações básicas de negócio
	public void criarCarteira(String nomeTitular, String corretora) {
		try {
			
			// Validação: Campos vazios
			if(nomeTitular == null || nomeTitular.trim().isEmpty()) {
				throw new IllegalArgumentException("O nome do titular é obrigatório!");
			}
			if(corretora == null || corretora.trim().isEmpty()) {
				throw new IllegalArgumentException("O nome da corretora é obrigatório!");
			}
			
			//Criacao da entidade
			Carteira novaCarteira = new Carteira();
			novaCarteira.setNomeTitular(nomeTitular.trim());
			novaCarteira.setCorretora(corretora.trim());
			
			
			//Persistencia
			carteiraDAO.inserir(novaCarteira);
		    System.out.println("Carteira de " + nomeTitular + "criada com sucesso na corretora " + corretora + "!");
		} catch (IllegalArgumentException e) {
			System.err.println("Erro de validação: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erro ao criar a carteira no banco de dados: " + e.getMessage());
		}
	}
	
	
	//Lista de todas as carteiras cadastradas
	public List<Carteira> listarTodasCarteiras(){
		try {
			return carteiraDAO.listarTodas();
		} catch (Exception e) {
			System.err.println("Erro ao listar as carteiras: " + e.getMessage());
			return null;
		}
	}
	
	//Busca uma carteira específica pelo ID
	public Carteira buscarCarteira(int id) {
		try{
			Carteira carteira = carteiraDAO.buscarPorId(id);
			if(carteira == null) {
				System.out.println("Nenhuma carteira encontrada com o ID: " + id);
			}
			return carteira;
			
		} catch (Exception e) {
			System.err.println("Erro ao buscar a carteira: " + e.getMessage());
			return null;
		}
	}
}
