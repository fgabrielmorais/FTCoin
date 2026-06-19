package br.com.ftcoin.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.ftcoin.daos.ICarteiraDAO;
import br.com.ftcoin.daos.IMovimentacaoDAO;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.RelatorioDTO;
import br.com.ftcoin.models.TipoOperacao;
import br.com.ftcoin.services.OraculoService;
import br.com.ftcoin.utils.ConsoleColors;

public class MovimentacaoController {
	private IMovimentacaoDAO movimentacaoDAO;
	private ICarteiraDAO carteiraDAO;

	public MovimentacaoController(IMovimentacaoDAO movDAO, ICarteiraDAO cartDAO) {
		this.movimentacaoDAO = movDAO;
		this.carteiraDAO = cartDAO;
	}
	
	public void registrarOperacao(int idCarteira, String tipoEntrada, BigDecimal quantidade) {
		try {
			Carteira carteira = carteiraDAO.buscarPorId(idCarteira);
			if(carteira == null) {
				throw new IllegalArgumentException("Nenhuma carteira com o ID " + idCarteira);
			}
			
			TipoOperacao tipo = TipoOperacao.fromString(tipoEntrada);
			BigDecimal cotacaoHoje = OraculoService.getInstancia().obterCotacaoDoDia();
			BigDecimal valorTotal = quantidade.multiply(cotacaoHoje);
			
			Movimentacao novaMovimentacao = new Movimentacao();
			novaMovimentacao.setIdCarteira(idCarteira); 
			novaMovimentacao.setDataOperacao(LocalDate.now());
			novaMovimentacao.setTipoOperacao(tipo);
			novaMovimentacao.setQuantidadeMovimentada(quantidade);
		
			movimentacaoDAO.inserir(novaMovimentacao);
			
			System.out.println(ConsoleColors.GREEN_BOLD + "\n✅ Operação realizada com sucesso!!" + ConsoleColors.RESET);
			System.out.println(ConsoleColors.CYAN + "Titular: " + carteira.getNomeTitular());
			System.out.println("Detalhes: " + tipo + " de " + quantidade + " FTCoins(s)");
			System.out.println("Cotação aplicada no dia: R$ " + cotacaoHoje);
			System.out.println("Valor total envolvido: R$ " + valorTotal + ConsoleColors.RESET + "\n");
			
		} catch (IllegalArgumentException e) {
			System.err.println(ConsoleColors.RED + "\n❌ Validação falhou: " + e.getMessage() + ConsoleColors.RESET + "\n");
		} catch (Exception e) {
			System.err.println(ConsoleColors.RED + "\n❌ Erro interno do sistema: " + e.getMessage() + ConsoleColors.RESET + "\n");
		}
	}

	public RelatorioDTO gerarRelatorio(int idCarteira) {
		try {
			Carteira carteira = carteiraDAO.buscarPorId(idCarteira);
			if (carteira == null) {
				throw new IllegalArgumentException("Carteira não encontrada com o ID informado.");
			}

			List<Movimentacao> historico = movimentacaoDAO.buscarPorCarteira(idCarteira);

			BigDecimal saldoMoedas = BigDecimal.ZERO;
			for (Movimentacao mov : historico) {
				if (mov.getTipoOperacao() == TipoOperacao.COMPRA) {
					saldoMoedas = saldoMoedas.add(mov.getQuantidadeMovimentada());
				} else if (mov.getTipoOperacao() == TipoOperacao.VENDA) {
					saldoMoedas = saldoMoedas.subtract(mov.getQuantidadeMovimentada());
				}
			}

			BigDecimal cotacaoHoje = OraculoService.getInstancia().obterCotacaoDoDia();
			BigDecimal patrimonioReal = saldoMoedas.multiply(cotacaoHoje);

            // SIMULAÇÃO DE GANHO/PERDA (assumindo que o preço médio de compra histórico foi R$ 100,00)
            BigDecimal precoMedioSimulado = new BigDecimal("100.00");
            BigDecimal valorInvestidoEstimado = saldoMoedas.multiply(precoMedioSimulado);
            BigDecimal ganhoPerda = patrimonioReal.subtract(valorInvestidoEstimado);

			return new RelatorioDTO(
				carteira.getNomeTitular(),
				carteira.getCorretora(),
				saldoMoedas,
				cotacaoHoje,
				patrimonioReal,
                ganhoPerda,
				historico
			);

		} catch (Exception e) {
			System.err.println(ConsoleColors.RED + "❌ Erro ao gerar relatório: " + e.getMessage() + ConsoleColors.RESET);
			return null;
		}
	}
}