package br.com.ftcoin.views;

import java.util.List;

import br.com.ftcoin.services.CarteiraService;
import br.com.ftcoin.services.MovimentacaoService;

public class MenuPrincipalView {

	private static final List<String> CHAVES_AJUDA = List.of(
			"ajuda.comecar", "ajuda.identificador", "ajuda.operar", "ajuda.resultado");

	private final Console console;
	private final CarteiraView carteiraView;
	private final MovimentacaoView movimentacaoView;
	private final RelatorioView relatorioView;

	public MenuPrincipalView(Console console, CarteiraService carteiraService, MovimentacaoService movimentacaoService) {
		this.console = console;
		this.carteiraView = new CarteiraView(console, carteiraService);
		this.movimentacaoView = new MovimentacaoView(console, movimentacaoService);
		this.relatorioView = new RelatorioView(console, carteiraService, movimentacaoService);
	}

	public void exibir() {
		new Menu(console, "principal.titulo", "principal.sair")
			.adicionar("principal.carteiras", carteiraView::exibirMenu)
			.adicionar("principal.movimentacao", movimentacaoView::registrar)
			.adicionar("principal.relatorios", relatorioView::exibir)
			.adicionar("principal.ajuda", this::exibirAjuda)
			.executar();

		console.exibirPositivo("principal.encerramento");
	}

	private void exibirAjuda() {
		console.exibirTitulo("ajuda.titulo");
		CHAVES_AJUDA.forEach(console::exibir);
		console.aguardarEnter();
	}
}
