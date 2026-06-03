package br.com.ftcoin.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.ftcoin.daos.ICarteiraDAO;
import br.com.ftcoin.daos.IMovimentacaoDAO;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.TipoOperacao;
import br.com.ftcoin.services.OraculoService;

public class MovimentacaoController {
	private IMovimentacaoDAO movimentacaoDAO;
	private ICarteiraDAO carteiraDAO;

	
	public MovimentacaoController(IMovimentacaoDAO movDAO, ICarteiraDAO cartDAO) {
		this.movimentacaoDAO = movDAO;
		this.carteiraDAO = cartDAO;
	}
	
	
	public void registrarOperacao(int idCarteira, String tipoEntrada, BigDecimal quantidade) {
		try {
			//Validação se a carteira existe ou não
			Carteira carteira = carteiraDAO.buscarPorId(idCarteira);
			if(carteira == null) {
				throw new IllegalArgumentException("ERRO: Nenhuma carteira com o ID " + idCarteira);
			}
			
			//Conversao do que for digitado pelo usuário em "c" = compra ou "v" = venda
			TipoOperacao tipo = TipoOperacao.fromString(tipoEntrada);
			
			//Oraculo com Cache: Busca da cotação instantânea
			BigDecimal cotacaoHoje = OraculoService.getInstancia().obterCotacaoDoDia();
			
			//calcula o valor financeiro total da operação
			BigDecimal valorTotal = quantidade.multiply(cotacaoHoje);
			
			//montando o objeto que vai para o BD
			Movimentacao novaMovimentacao = new Movimentacao();
			novaMovimentacao.setIdCarteira(idCarteira);
			novaMovimentacao.setDataOperacao(LocalDate.now());
			novaMovimentacao.setTipoOperacao(tipo);
			novaMovimentacao.setQuantidadeMovimentada(quantidade);
		
			//salvando no WindServer
			movimentacaoDAO.inserir(novaMovimentacao);
			
			//Retorno de que deu tudo certo!
			System.out.println("\n Operação realizada com sucesso!!");
			System.out.println("Titular: " + carteira.getNomeTitular());
			System.out.println("Detalhes: " + tipo + " de " + quantidade + " FTCoins(s)");
			System.out.println("Cotação aplicada no dia: R$ " + cotacaoHoje);
			System.out.println("valor total envolvido: R$ " + valorTotal + "\n");
			
			
		} catch (IllegalArgumentException e) {
			System.err.println("\n Validação falhou: " + e.getMessage() + "\n");
		} catch (Exception e) {
			System.err.println("\n Erro interno do sistema: " + e.getMessage() + "\n");
		}
	}
}
