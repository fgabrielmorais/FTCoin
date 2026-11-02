package br.com.ftcoin.views;

import java.math.BigDecimal;
import java.util.List;

import br.com.ftcoin.services.CarteiraService;
import br.com.ftcoin.services.MovimentacaoService;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.Posicao;

public class RelatorioView {

	private final Console console;
	private final Formatador formatador;
	private final CarteiraService carteiraService;
	private final MovimentacaoService movimentacaoService;

	public RelatorioView(Console console, CarteiraService carteiraService, MovimentacaoService movimentacaoService) {
		this.console = console;
		this.formatador = console.getFormatador();
		this.carteiraService = carteiraService;
		this.movimentacaoService = movimentacaoService;
	}

	public void exibir() {
		console.exibirTitulo("relatorio.titulo");
		int idCarteira = console.lerInteiro("entrada.idCarteira");

		Carteira carteira = carteiraService.buscarCarteira(idCarteira);
		List<Movimentacao> historico = movimentacaoService.buscarHistorico(idCarteira);
		Posicao posicao = movimentacaoService.apurarPosicao(idCarteira);
		BigDecimal cotacaoAtual = movimentacaoService.buscarCotacaoAtual();

		console.exibir("relatorio.titular", carteira.getNomeTitular(), carteira.getCorretora());
		exibirPosicao(posicao, cotacaoAtual);
		exibirHistorico(historico);
	}

	private void exibirPosicao(Posicao posicao, BigDecimal cotacaoAtual) {
		console.exibir("relatorio.cotacao", formatador.moeda(cotacaoAtual));
		console.exibir("relatorio.saldo", formatador.quantidade(posicao.getSaldo()));
		console.exibir("relatorio.custoMedio", formatador.moeda(posicao.getCustoMedio()));
		console.exibir("relatorio.valorACusto", formatador.moeda(posicao.getValorACusto()));
		console.exibir("relatorio.valorDeMercado", formatador.moeda(posicao.calcularValorDeMercado(cotacaoAtual)));
		exibirGanhoPerda("relatorio.ganhoRealizado", posicao.getGanhoRealizado());
		exibirGanhoPerda("relatorio.ganhoNaoRealizado", posicao.calcularGanhoNaoRealizado(cotacaoAtual));
		exibirGanhoPerda("relatorio.ganhoTotal", posicao.calcularGanhoTotal(cotacaoAtual));
	}

	//Ganho em verde, perda em vermelho
	private void exibirGanhoPerda(String chave, BigDecimal valor) {
		if (valor.signum() >= 0) {
			console.exibirPositivo(chave, formatador.moeda(valor));
		} else {
			console.exibirNegativo(chave, formatador.moeda(valor));
		}
	}

	private void exibirHistorico(List<Movimentacao> historico) {
		console.exibirTitulo("relatorio.historico.titulo");
		if (historico.isEmpty()) {
			console.exibirAviso("relatorio.historico.vazio");
			return;
		}

		for (Movimentacao movimentacao : historico) {
			console.exibir("relatorio.historico.linha",
					formatador.data(movimentacao.getDataOperacao()),
					console.texto(movimentacao.getTipoOperacao().getChaveMensagem()),
					formatador.quantidade(movimentacao.getQuantidadeMovimentada()));
		}
	}
}
