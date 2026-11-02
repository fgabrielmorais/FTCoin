package br.com.ftcoin.daos;

import java.util.List;

import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.RegraNegocioException;

/**
 * Contrato de persistência das carteiras, com uma implementação em memória e outra em MariaDB.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public abstract class CarteiraDAO {

	/**
	 * Construtor usado pelas implementações em memória e MariaDB.
	 */
	protected CarteiraDAO() {
		// Sem estado comum entre as implementações
	}

	/**
	 * Grava uma nova carteira e preenche o identificador gerado.
	 *
	 * @param carteira carteira a gravar; recebe o ID atribuído pela persistência
	 */
	public abstract void inserir(Carteira carteira);

	/**
	 * Busca uma carteira pelo identificador.
	 *
	 * @param id identificador da carteira
	 * @return a carteira encontrada, ou {@code null} se não existir
	 */
	public abstract Carteira buscarPorId(int id);

	/**
	 * Lista todas as carteiras cadastradas.
	 *
	 * @return as carteiras, possivelmente vazia
	 */
	public abstract List<Carteira> listarTodas();

	/**
	 * Regrava titular e corretora de uma carteira existente.
	 *
	 * @param carteira carteira com os dados novos e o ID da que será alterada
	 */
	public abstract void atualizar(Carteira carteira);

	/**
	 * Remove uma carteira.
	 *
	 * @param id identificador da carteira a remover
	 */
	public abstract void excluir(int id);

	/**
	 * Busca uma carteira que precisa existir para a operação continuar.
	 *
	 * @param id identificador da carteira
	 * @return a carteira encontrada
	 * @throws RegraNegocioException se não houver carteira com esse ID
	 */
	public Carteira buscarExistente(int id) {
		Carteira carteira = buscarPorId(id);
		if (carteira == null) {
			throw new RegraNegocioException("erro.carteira.naoEncontrada", id);
		}
		return carteira;
	}
}
