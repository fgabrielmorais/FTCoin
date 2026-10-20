package br.com.ftcoin.views;

import java.util.List;

import br.com.ftcoin.controllers.CarteiraController;
import br.com.ftcoin.models.Carteira;

public class CarteiraView {

	private final Console console;
	private final CarteiraController carteiraController;

	public CarteiraView(Console console, CarteiraController carteiraController) {
		this.console = console;
		this.carteiraController = carteiraController;
	}

	public void exibirMenu() {
		new Menu(console, "carteira.menu.titulo", "menu.voltar")
			.adicionar("carteira.menu.incluir", this::incluir)
			.adicionar("carteira.menu.consultar", this::consultar)
			.adicionar("carteira.menu.listar", this::listar)
			.adicionar("carteira.menu.editar", this::editar)
			.adicionar("carteira.menu.excluir", this::excluir)
			.executar();
	}

	private void incluir() {
		String titular = console.lerTexto("carteira.titular");
		String corretora = console.lerTexto("carteira.corretora");

		Carteira criada = carteiraController.criarCarteira(titular, corretora);
		console.exibirPositivo("carteira.criada", criada.getId(), criada.getNomeTitular(), criada.getCorretora());
	}

	private void consultar() {
		exibirLinha(carteiraController.buscarCarteira(console.lerInteiro("entrada.idCarteira")));
	}

	private void listar() {
		List<Carteira> carteiras = carteiraController.listarTodasCarteiras();
		if (carteiras.isEmpty()) {
			console.exibirAviso("carteira.listaVazia");
			return;
		}
		carteiras.forEach(this::exibirLinha);
	}

	private void editar() {
		int id = console.lerInteiro("entrada.idCarteira");
		String novoTitular = console.lerTexto("carteira.novoTitular");
		String novaCorretora = console.lerTexto("carteira.novaCorretora");

		carteiraController.editarCarteira(id, novoTitular, novaCorretora);
		console.exibirPositivo("carteira.atualizada", id);
	}

	private void excluir() {
		int id = console.lerInteiro("entrada.idCarteira");

		carteiraController.excluirCarteira(id);
		console.exibirPositivo("carteira.excluida", id);
	}

	private void exibirLinha(Carteira carteira) {
		console.exibir("carteira.linha", carteira.getId(), carteira.getNomeTitular(), carteira.getCorretora());
	}
}
