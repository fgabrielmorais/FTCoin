package br.com.ftcoin.daos;

import br.com.ftcoin.models.Carteira;
import java.util.List;


public interface ICarteiraDAO {
	
	//métodos para o BD implementar
	void inserir(Carteira carteira);
	Carteira buscarPorId(int id);
	List<Carteira> listarTodas();
	void atualizar(Carteira carteira);
	void excluir(int id);
}
