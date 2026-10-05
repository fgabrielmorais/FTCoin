package br.com.ftcoin.daos;

import java.util.List;

import br.com.ftcoin.models.Movimentacao;


public abstract class MovimentacaoDAO {

	public abstract void inserir(Movimentacao movimentacao);
	public abstract List<Movimentacao> buscarPorCarteira(int idCarteira);
	public abstract List<Movimentacao> listarTodas();
}
