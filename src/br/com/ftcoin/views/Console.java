package br.com.ftcoin.views;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

// Entrada e saída do terminal. Todo texto exibido vem de uma chave dos arquivos de mensagens.
public class Console {

	private static final String SEPARADOR_PROMPT = ": ";
	private static final int PRIMEIRA_OPCAO = 1;

	private final Mensagens mensagens;
	private final Scanner entrada = new Scanner(System.in);
	private final PrintStream saida = System.out;

	public Console(Mensagens mensagens) {
		this.mensagens = mensagens;
	}

	public Formatador getFormatador() {
		return mensagens.getFormatador();
	}

	public String texto(String chave, Object... argumentos) {
		return mensagens.get(chave, argumentos);
	}


	// ---- Saída ----

	public void exibir(String chave, Object... argumentos) {
		saida.println(texto(chave, argumentos));
	}

	public void exibirTitulo(String chave, Object... argumentos) {
		saida.println();
		exibirColorido(ConsoleColors.CYAN_BOLD, chave, argumentos);
	}

	public void exibirPositivo(String chave, Object... argumentos) {
		exibirColorido(ConsoleColors.GREEN, chave, argumentos);
	}

	public void exibirNegativo(String chave, Object... argumentos) {
		exibirColorido(ConsoleColors.RED, chave, argumentos);
	}

	public void exibirAviso(String chave, Object... argumentos) {
		exibirColorido(ConsoleColors.YELLOW, chave, argumentos);
	}

	public void exibirOpcao(int numero, String chaveRotulo) {
		saida.println(ConsoleColors.YELLOW + "[" + numero + "]" + ConsoleColors.RESET + " " + texto(chaveRotulo));
	}

	private void exibirColorido(String cor, String chave, Object... argumentos) {
		saida.println(cor + texto(chave, argumentos) + ConsoleColors.RESET);
	}


	// ---- Entrada ----

	public String lerTexto(String chavePrompt) {
		saida.print(texto(chavePrompt) + SEPARADOR_PROMPT);
		return entrada.nextLine();
	}

	//Repete a pergunta até receber um número inteiro
	public int lerInteiro(String chavePrompt) {
		while (true) {
			try {
				return Integer.parseInt(lerTexto(chavePrompt).trim());
			} catch (NumberFormatException e) {
				exibirNegativo("entrada.numeroInvalido");
			}
		}
	}

	//Repete a pergunta até receber um número; aceita vírgula ou ponto como separador decimal
	public BigDecimal lerDecimal(String chavePrompt) {
		while (true) {
			try {
				return new BigDecimal(lerTexto(chavePrompt).trim().replace(',', '.'));
			} catch (NumberFormatException e) {
				exibirNegativo("entrada.numeroInvalido");
			}
		}
	}

	//Lista os itens numerados e devolve o escolhido
	public <T> T escolher(String chavePrompt, List<T> itens, Function<T, String> chaveDoRotulo) {
		for (int i = 0; i < itens.size(); i++) {
			exibirOpcao(i + PRIMEIRA_OPCAO, chaveDoRotulo.apply(itens.get(i)));
		}

		while (true) {
			int indice = lerInteiro(chavePrompt) - PRIMEIRA_OPCAO;
			if (indice >= 0 && indice < itens.size()) {
				return itens.get(indice);
			}
			exibirNegativo("menu.opcaoInvalida");
		}
	}

	public void aguardarEnter() {
		saida.print(texto("entrada.continuar"));
		entrada.nextLine();
	}
}
