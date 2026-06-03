package br.com.ftcoin.daos;

import java.util.List;

import br.com.ftcoin.models.Movimentacao;


public interface IMovimentacaoDAO {
	void inserir(Movimentacao movimentacao);
	
	//Método para tirar os relatórios depois
	List<Movimentacao> buscarPorCarteira(int idCarteira);
	List<Movimentacao> listarTodas();
}
