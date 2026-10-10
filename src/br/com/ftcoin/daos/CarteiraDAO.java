package br.com.ftcoin.daos;

import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.RegraNegocioException;
import java.util.List;


public abstract class CarteiraDAO {

	public abstract void inserir(Carteira carteira);
	public abstract Carteira buscarPorId(int id);
	public abstract List<Carteira> listarTodas();
	public abstract void atualizar(Carteira carteira);
	public abstract void excluir(int id);

	//Busca para as operações que exigem que a carteira exista
	public Carteira buscarExistente(int id) {
		Carteira carteira = buscarPorId(id);
		if (carteira == null) {
			throw new RegraNegocioException("erro.carteira.naoEncontrada", id);
		}
		return carteira;
	}
}
