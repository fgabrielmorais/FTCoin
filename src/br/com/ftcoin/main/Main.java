package br.com.ftcoin.main;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

import br.com.ftcoin.controllers.CarteiraController;
import br.com.ftcoin.controllers.MovimentacaoController;
import br.com.ftcoin.daos.CarteiraDAOMemoria;
import br.com.ftcoin.daos.ICarteiraDAO;
import br.com.ftcoin.daos.IMovimentacaoDAO;
import br.com.ftcoin.daos.MovimentacaoDAOMemoria;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.RelatorioDTO;
import br.com.ftcoin.utils.ConsoleColors;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(ConsoleColors.YELLOW + "Inicializando o sistema e conectando ao WindServer..." + ConsoleColors.RESET);
//        ICarteiraDAO carteiraDAO = new CarteiraDAOdb();
//        IMovimentacaoDAO movimentacaoDAO = new MovimentacaoDAOdb();
        ICarteiraDAO carteiraDAO = new CarteiraDAOMemoria();
        IMovimentacaoDAO movimentacaoDAO = new MovimentacaoDAOMemoria();
        
        
        CarteiraController carteiraController = new CarteiraController(carteiraDAO);
        MovimentacaoController movimentacaoController = new MovimentacaoController(movimentacaoDAO, carteiraDAO);

        int opcao = -1;

        while (opcao != 0) {
            exibirMenu();
            try {
                System.out.print(ConsoleColors.CYAN + "Escolha uma opção: " + ConsoleColors.RESET);
                opcao = Integer.parseInt(scanner.nextLine());

                switch (opcao) {
                    case 1:
                        System.out.println(ConsoleColors.BLUE + "\n--- CRIAR NOVA CARTEIRA ---" + ConsoleColors.RESET);
                        System.out.print("Digite o nome do Titular: ");
                        String titular = scanner.nextLine();
                        System.out.print("Digite o nome da Corretora: ");
                        String corretora = scanner.nextLine();
                        carteiraController.criarCarteira(titular, corretora);
                        break;

                    case 2:
                        System.out.println(ConsoleColors.BLUE + "\n--- LISTA DE CARTEIRAS ---" + ConsoleColors.RESET);
                        List<Carteira> carteiras = carteiraController.listarTodasCarteiras();
                        if (carteiras != null && !carteiras.isEmpty()) {
                            for (Carteira c : carteiras) {
                                System.out.println("ID: " + ConsoleColors.YELLOW_BOLD + c.getId() + ConsoleColors.RESET + 
                                                   " | Titular: " + c.getNomeTitular() + " | Corretora: " + c.getCorretora());
                            }
                        } else {
                            System.out.println(ConsoleColors.YELLOW + "Nenhuma carteira cadastrada no momento." + ConsoleColors.RESET);
                        }
                        break;

                    case 3:
                        System.out.println(ConsoleColors.BLUE + "\n--- REGISTRAR MOVIMENTAÇÃO (COMPRA/VENDA) ---" + ConsoleColors.RESET);
                        System.out.print("Digite o ID da Carteira: ");
                        int idCarteiraMov = Integer.parseInt(scanner.nextLine());
                        
                        System.out.print("Tipo de Operação (C para Compra, V para Venda): ");
                        String tipo = scanner.nextLine();
                        
                        System.out.print("Quantidade de FTCoin: ");
                        BigDecimal quantidade = new BigDecimal(scanner.nextLine().replace(",", "."));
                        
                        movimentacaoController.registrarOperacao(idCarteiraMov, tipo, quantidade);
                        break;

                    case 4:
                        System.out.println(ConsoleColors.BLUE + "\n--- EXTRATO E RELATÓRIO DA CARTEIRA ---" + ConsoleColors.RESET);
                        System.out.print("Digite o ID da Carteira: ");
                        int idCarteiraRel = Integer.parseInt(scanner.nextLine());
                        
                        RelatorioDTO relatorio = movimentacaoController.gerarRelatorio(idCarteiraRel);
                        
                        if (relatorio != null) {
                            System.out.println(ConsoleColors.PURPLE + "\n========================================");
                            System.out.println("      RELATÓRIO FINANCEIRO FTCOIN       ");
                            System.out.println("========================================" + ConsoleColors.RESET);
                            System.out.println("Titular: " + ConsoleColors.CYAN_BOLD + relatorio.getNomeTitular() + ConsoleColors.RESET + " (" + relatorio.getCorretora() + ")");
                            System.out.println("Cotação Atual: " + ConsoleColors.GREEN + "R$ " + relatorio.getCotacaoAtual() + ConsoleColors.RESET);
                            System.out.println("Saldo de Moedas: " + ConsoleColors.YELLOW_BOLD + relatorio.getSaldoMoedas() + " FTC" + ConsoleColors.RESET);
                            System.out.println("Patrimônio Total: " + ConsoleColors.GREEN_BOLD + "R$ " + relatorio.getPatrimonioReal() + ConsoleColors.RESET);
                            System.out.println(ConsoleColors.PURPLE + "----------------------------------------" + ConsoleColors.RESET);
                            System.out.println("Histórico de Operações:");
                            
                            if (relatorio.getHistorico().isEmpty()) {
                                System.out.println(ConsoleColors.YELLOW + "  Sem movimentações." + ConsoleColors.RESET);
                            } else {
                                for (Movimentacao mov : relatorio.getHistorico()) {
                                    // Pinta a palavra COMPRA de verde e VENDA de vermelho
                                    String corOperacao = mov.getTipoOperacao().name().equals("COMPRA") ? ConsoleColors.GREEN : ConsoleColors.RED;
                                    System.out.println("  > " + mov.getDataOperacao() + " | " + corOperacao + mov.getTipoOperacao() + ConsoleColors.RESET + " | " + mov.getQuantidadeMovimentada() + " FTC");
                                }
                            }
                            System.out.println(ConsoleColors.PURPLE + "========================================\n" + ConsoleColors.RESET);
                        }
                        break;

                    case 0:
                        System.out.println(ConsoleColors.GREEN_BOLD + "Encerrando o sistema FTCoin. Até logo!" + ConsoleColors.RESET);
                        break;

                    default:
                        System.out.println(ConsoleColors.RED_BOLD + "❌ Opção inválida. Tente novamente." + ConsoleColors.RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(ConsoleColors.RED_BOLD + "❌ Erro: Por favor, digite um número válido." + ConsoleColors.RESET);
            } catch (Exception e) {
                System.out.println(ConsoleColors.RED_BOLD + "❌ Ocorreu um erro inesperado: " + e.getMessage() + ConsoleColors.RESET);
            }
        }
        
        scanner.close();
    }

    private static void exibirMenu() {
        System.out.println(ConsoleColors.CYAN_BOLD + "\n========================================");
        System.out.println("          SISTEMA FTCOIN v1.0           ");
        System.out.println("========================================" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.YELLOW + "[1]" + ConsoleColors.RESET + " Criar Nova Carteira");
        System.out.println(ConsoleColors.YELLOW + "[2]" + ConsoleColors.RESET + " Listar Todas as Carteiras");
        System.out.println(ConsoleColors.YELLOW + "[3]" + ConsoleColors.RESET + " Registrar Movimentação (Compra/Venda)");
        System.out.println(ConsoleColors.YELLOW + "[4]" + ConsoleColors.RESET + " Ver Extrato da Carteira (Relatório)");
        System.out.println(ConsoleColors.RED + "[0]" + ConsoleColors.RESET + " Sair");
        System.out.println(ConsoleColors.CYAN_BOLD + "========================================" + ConsoleColors.RESET);
    }
}