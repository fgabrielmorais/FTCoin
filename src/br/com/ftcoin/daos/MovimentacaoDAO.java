package br.com.ftcoin.daos;

import java.util.List;

import br.com.ftcoin.models.Movimentacao;

/**
 * Contrato de persistência das movimentações (compras e vendas) das carteiras.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public abstract class MovimentacaoDAO {

	/**
	 * Construtor usado pelas implementações em memória e MariaDB.
	 */
	protected MovimentacaoDAO() {
		// Sem estado comum entre as implementações
	}

	/**
	 * Grava uma nova movimentação e preenche o identificador gerado.
	 *
	 * @param movimentacao movimentação a gravar; recebe o ID atribuído pela persistência
	 */
	public abstract void inserir(Movimentacao movimentacao);

	/**
	 * Lista as movimentações de uma carteira, sem ordem garantida.
	 *
	 * @param idCarteira identificador da carteira
	 * @return as movimentações da carteira, possivelmente vazia
	 */
	public abstract List<Movimentacao> buscarPorCarteira(int idCarteira);

	/**
	 * Lista todas as movimentações de todas as carteiras.
	 *
	 * @return as movimentações, possivelmente vazia
	 */
	public abstract List<Movimentacao> listarTodas();
}
