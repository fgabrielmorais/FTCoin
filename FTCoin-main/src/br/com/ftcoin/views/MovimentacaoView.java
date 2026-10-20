package br.com.ftcoin.views;

import java.math.BigDecimal;
import java.util.List;

import br.com.ftcoin.controllers.MovimentacaoController;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.TipoOperacao;

public class MovimentacaoView {

	private final Console console;
	private final Formatador formatador;
	private final MovimentacaoController movimentacaoController;

	public MovimentacaoView(Console console, MovimentacaoController movimentacaoController) {
		this.console = console;
		this.formatador = console.getFormatador();
		this.movimentacaoController = movimentacaoController;
	}

	public void registrar() {
		console.exibirTitulo("movimentacao.titulo");
		int idCarteira = console.lerInteiro("entrada.idCarteira");
		TipoOperacao tipo = console.escolher("movimentacao.tipo", List.of(TipoOperacao.values()), TipoOperacao::getChaveMensagem);
		BigDecimal quantidade = console.lerDecimal("movimentacao.quantidade");

		Movimentacao registrada = movimentacaoController.registrarOperacao(idCarteira, tipo, quantidade);

		console.exibirPositivo("movimentacao.sucesso");
		console.exibir("movimentacao.detalhes",
				console.texto(tipo.getChaveMensagem()),
				formatador.quantidade(registrada.getQuantidadeMovimentada()));
		console.exibir("movimentacao.cotacao",
				formatador.moeda(movimentacaoController.buscarCotacao(registrada.getDataOperacao())));
		console.exibir("movimentacao.valorTotal",
				formatador.moeda(movimentacaoController.calcularValorDaOperacao(registrada)));
	}
}
