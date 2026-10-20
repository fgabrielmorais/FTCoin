package br.com.ftcoin.views;

import java.util.ArrayList;
import java.util.List;

import br.com.ftcoin.models.FTCoinException;

// Menu genérico: recebe o título e as opções (texto + ação) e cuida de exibir,
// ler a escolha e executar. As opções são numeradas pela ordem em que foram adicionadas.
public class Menu {

	private static final int OPCAO_SAIDA = 0;
	private static final int PRIMEIRA_OPCAO = 1;

	private record Opcao(String chaveRotulo, Runnable acao) {}

	private final Console console;
	private final String chaveTitulo;
	private final String chaveSaida;
	private final List<Opcao> opcoes = new ArrayList<>();

	public Menu(Console console, String chaveTitulo, String chaveSaida) {
		this.console = console;
		this.chaveTitulo = chaveTitulo;
		this.chaveSaida = chaveSaida;
	}

	public Menu adicionar(String chaveRotulo, Runnable acao) {
		opcoes.add(new Opcao(chaveRotulo, acao));
		return this;
	}

	//Repete o menu até o usuário escolher a opção de saída
	public void executar() {
		while (true) {
			exibir();
			int escolha = console.lerInteiro("menu.escolha");

			if (escolha == OPCAO_SAIDA) {
				return;
			}

			int indice = escolha - PRIMEIRA_OPCAO;
			if (indice < 0 || indice >= opcoes.size()) {
				console.exibirNegativo("menu.opcaoInvalida");
				continue;
			}

			executar(opcoes.get(indice));
		}
	}

	private void exibir() {
		console.exibirTitulo(chaveTitulo);
		for (int i = 0; i < opcoes.size(); i++) {
			console.exibirOpcao(i + PRIMEIRA_OPCAO, opcoes.get(i).chaveRotulo());
		}
		console.exibirOpcao(OPCAO_SAIDA, chaveSaida);
	}

	//Erros de uma ação são exibidos e o menu continua
	private void executar(Opcao opcao) {
		try {
			opcao.acao().run();
		} catch (FTCoinException e) {
			console.exibirNegativo(e.getChave(), e.getArgumentos());
		} catch (RuntimeException e) {
			console.exibirNegativo("erro.inesperado", e.getMessage());
		}
	}
}
