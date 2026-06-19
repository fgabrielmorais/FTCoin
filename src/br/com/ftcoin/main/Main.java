package br.com.ftcoin.main;

import br.com.ftcoin.controllers.CarteiraController;
import br.com.ftcoin.controllers.MovimentacaoController;
import br.com.ftcoin.daos.CarteiraDAOdb;
import br.com.ftcoin.daos.ICarteiraDAO;
import br.com.ftcoin.daos.IMovimentacaoDAO;
import br.com.ftcoin.daos.MovimentacaoDAOdb;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.RelatorioDTO;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.utils.ConsoleColors;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Scanner;

public class Main {

    // Criando os formatadores globais para usar em toda a tela
    private static final DecimalFormat dfMoeda = new DecimalFormat("#,##0.00");
    private static final DecimalFormat dfCrypto = new DecimalFormat("#,##0.000");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(ConsoleColors.YELLOW + "Inicializando o sistema..." + ConsoleColors.RESET);
        
        // Mantendo a conexão real com o MariaDB
        ICarteiraDAO carteiraDAO = new CarteiraDAOdb();
        IMovimentacaoDAO movimentacaoDAO = new MovimentacaoDAOdb();

        CarteiraController carteiraController = new CarteiraController(carteiraDAO);
        MovimentacaoController movimentacaoController = new MovimentacaoController(movimentacaoDAO, carteiraDAO);

        int opcao = -1;

        while (opcao != 0) {
            exibirMenuPrincipal();
            try {
                System.out.print(ConsoleColors.CYAN + "Escolha uma opção do Menu Principal: " + ConsoleColors.RESET);
                opcao = Integer.parseInt(scanner.nextLine());

                switch (opcao) {
                    case 1:
                        menuCarteira(scanner, carteiraController);
                        break;
                    case 2:
                        menuMovimentacao(scanner, movimentacaoController);
                        break;
                    case 3:
                        menuRelatorio(scanner, movimentacaoController);
                        break;
                    case 4:
                        exibirAjuda();
                        break;
                    case 0:
                        System.out.println(ConsoleColors.GREEN_BOLD + "Encerrando o sistema FTCoin. Até logo!" + ConsoleColors.RESET);
                        break;
                    default:
                        System.out.println(ConsoleColors.RED_BOLD + "❌ Opção inválida." + ConsoleColors.RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(ConsoleColors.RED_BOLD + "❌ Erro: Digite um número válido." + ConsoleColors.RESET);
            }
        }
        scanner.close();
    }

    private static void exibirMenuPrincipal() {
        System.out.println(ConsoleColors.CYAN_BOLD + "\n========================================");
        System.out.println("          SISTEMA FTCOIN v2.0           ");
        System.out.println("========================================" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.YELLOW + "[1]" + ConsoleColors.RESET + " Carteira (Incluir, Consultar, Editar, Excluir)");
        System.out.println(ConsoleColors.YELLOW + "[2]" + ConsoleColors.RESET + " Movimentação (Registrar Compra/Venda)");
        System.out.println(ConsoleColors.YELLOW + "[3]" + ConsoleColors.RESET + " Relatórios (Extrato e Ganhos)");
        System.out.println(ConsoleColors.YELLOW + "[4]" + ConsoleColors.RESET + " Ajuda");
        System.out.println(ConsoleColors.RED + "[0]" + ConsoleColors.RESET + " Sair");
        System.out.println(ConsoleColors.CYAN_BOLD + "========================================" + ConsoleColors.RESET);
    }

    private static void menuCarteira(Scanner scanner, CarteiraController carteiraController) {
        int opCarteira = -1;
        while (opCarteira != 0) {
            System.out.println(ConsoleColors.BLUE_BOLD + "\n--- GERENCIAMENTO DE CARTEIRAS ---" + ConsoleColors.RESET);
            System.out.println("[1] Incluir Carteira");
            System.out.println("[2] Consultar Carteira (Por ID)");
            System.out.println("[3] Listar Todas as Carteiras");
            System.out.println("[4] Editar Carteira");
            System.out.println("[5] Excluir Carteira");
            System.out.println("[0] Voltar ao Menu Principal");
            System.out.print(ConsoleColors.CYAN + "Opção: " + ConsoleColors.RESET);
            
            try {
                opCarteira = Integer.parseInt(scanner.nextLine());
                switch (opCarteira) {
                    case 1:
                        System.out.print("Nome do Titular: ");
                        String titular = scanner.nextLine();
                        System.out.print("Corretora: ");
                        String corretora = scanner.nextLine();
                        carteiraController.criarCarteira(titular, corretora);
                        break;
                    case 2:
                        System.out.print("Digite o ID da Carteira: ");
                        int idBusca = Integer.parseInt(scanner.nextLine());
                        Carteira c = carteiraController.buscarCarteira(idBusca);
                        if (c != null) {
                            System.out.println(ConsoleColors.GREEN + "ID: " + c.getId() + " | Titular: " + c.getNomeTitular() + " | Corretora: " + c.getCorretora() + ConsoleColors.RESET);
                        }
                        break;
                    case 3:
                        List<Carteira> carteiras = carteiraController.listarTodasCarteiras();
                        if (carteiras != null && !carteiras.isEmpty()) {
                            for (Carteira cart : carteiras) {
                                System.out.println("ID: " + ConsoleColors.YELLOW_BOLD + cart.getId() + ConsoleColors.RESET + " | Titular: " + cart.getNomeTitular() + " | Corretora: " + cart.getCorretora());
                            }
                        } else {
                            System.out.println(ConsoleColors.YELLOW + "Nenhuma carteira cadastrada." + ConsoleColors.RESET);
                        }
                        break;
                    case 4:
                        System.out.print("ID da Carteira para Editar: ");
                        int idEditar = Integer.parseInt(scanner.nextLine());
                        System.out.print("Novo Nome do Titular: ");
                        String novoTitular = scanner.nextLine();
                        System.out.print("Nova Corretora: ");
                        String novaCorretora = scanner.nextLine();
                        carteiraController.editarCarteira(idEditar, novoTitular, novaCorretora);
                        break;
                    case 5:
                        System.out.print("ID da Carteira para Excluir: ");
                        int idExcluir = Integer.parseInt(scanner.nextLine());
                        carteiraController.excluirCarteira(idExcluir);
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println(ConsoleColors.RED + "Opção inválida." + ConsoleColors.RESET);
                }
            } catch (NumberFormatException e) {
                System.out.println(ConsoleColors.RED + "Digite um número válido." + ConsoleColors.RESET);
            }
        }
    }

    private static void menuMovimentacao(Scanner scanner, MovimentacaoController movimentacaoController) {
        System.out.println(ConsoleColors.BLUE_BOLD + "\n--- REGISTRAR MOVIMENTAÇÃO ---" + ConsoleColors.RESET);
        try {
            System.out.print("Digite o ID da Carteira: ");
            int idCarteira = Integer.parseInt(scanner.nextLine());
            System.out.print("Tipo de Operação (C para Compra, V para Venda): ");
            String tipo = scanner.nextLine();
            System.out.print("Quantidade de FTCoin: ");
            BigDecimal quantidade = new BigDecimal(scanner.nextLine().replace(",", "."));
            
            movimentacaoController.registrarOperacao(idCarteira, tipo, quantidade);
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "Erro na entrada de dados." + ConsoleColors.RESET);
        }
    }

    private static void menuRelatorio(Scanner scanner, MovimentacaoController movimentacaoController) {
        System.out.println(ConsoleColors.BLUE_BOLD + "\n--- RELATÓRIOS E EXTRATOS ---" + ConsoleColors.RESET);
        try {
            System.out.print("Digite o ID da Carteira: ");
            int idCarteiraRel = Integer.parseInt(scanner.nextLine());
            RelatorioDTO relatorio = movimentacaoController.gerarRelatorio(idCarteiraRel);
            
            if (relatorio != null) {
                System.out.println(ConsoleColors.PURPLE + "\n========================================");
                System.out.println("      RELATÓRIO FINANCEIRO FTCOIN       ");
                System.out.println("========================================" + ConsoleColors.RESET);
                System.out.println("Titular: " + ConsoleColors.CYAN_BOLD + relatorio.getNomeTitular() + ConsoleColors.RESET + " (" + relatorio.getCorretora() + ")");
                
                // Aplicando o DecimalFormat nas saídas financeiras
                System.out.println("Cotação Atual: " + ConsoleColors.CYAN + "R$ " + dfMoeda.format(relatorio.getCotacaoAtual()) + ConsoleColors.RESET);
                System.out.println("Saldo de Moedas: " + ConsoleColors.YELLOW_BOLD + dfCrypto.format(relatorio.getSaldoMoedas()) + " FTC" + ConsoleColors.RESET);
                System.out.println("Patrimônio Total: R$ " + dfMoeda.format(relatorio.getPatrimonioReal()));
                
                BigDecimal ganhoPerda = relatorio.getGanhoPerdaTotal();
                if (ganhoPerda.compareTo(BigDecimal.ZERO) >= 0) {
                    System.out.println("Lucro/Prejuízo Estimado: " + ConsoleColors.GREEN_BOLD + "+ R$ " + dfMoeda.format(ganhoPerda) + " (LUCRO)" + ConsoleColors.RESET);
                } else {
                    System.out.println("Lucro/Prejuízo Estimado: " + ConsoleColors.RED_BOLD + "- R$ " + dfMoeda.format(ganhoPerda.abs()) + " (PREJUÍZO)" + ConsoleColors.RESET);
                }

                System.out.println(ConsoleColors.PURPLE + "----------------------------------------" + ConsoleColors.RESET);
                System.out.println("Histórico de Operações:");
                
                if (relatorio.getHistorico().isEmpty()) {
                    System.out.println(ConsoleColors.YELLOW + "  Sem movimentações." + ConsoleColors.RESET);
                } else {
                    for (Movimentacao mov : relatorio.getHistorico()) {
                        String corOperacao = mov.getTipoOperacao().name().equals("COMPRA") ? ConsoleColors.GREEN : ConsoleColors.RED;
                        
                        // Aplicando formatação na listagem do histórico também
                        System.out.println("  > " + mov.getDataOperacao() + " | " + corOperacao + mov.getTipoOperacao() + ConsoleColors.RESET + " | " + dfCrypto.format(mov.getQuantidadeMovimentada()) + " FTC");
                    }
                }
                System.out.println(ConsoleColors.PURPLE + "========================================\n" + ConsoleColors.RESET);
            }
        } catch (Exception e) {
            System.out.println(ConsoleColors.RED + "Erro ao puxar relatório." + ConsoleColors.RESET);
        }
    }

    private static void exibirAjuda() {
        System.out.println(ConsoleColors.GREEN_BOLD + "\n--- CENTRAL DE AJUDA FTCOIN ---" + ConsoleColors.RESET);
        System.out.println(ConsoleColors.CYAN + "1. Como começar?" + ConsoleColors.RESET + " Vá no menu [1] Carteira e crie uma nova conta informando seu nome e corretora.");
        System.out.println(ConsoleColors.CYAN + "2. Onde vejo meu ID?" + ConsoleColors.RESET + " No menu [1] Carteira, acesse a opção 'Listar Todas as Carteiras' para ver o ID numérico gerado pelo sistema.");
        System.out.println(ConsoleColors.CYAN + "3. Como investir?" + ConsoleColors.RESET + " Vá no menu [2] Movimentação. Digite seu ID, escolha 'C' (Compra) ou 'V' (Venda) e a quantidade desejada.");
        System.out.println(ConsoleColors.CYAN + "4. Lucros e Prejuízos:" + ConsoleColors.RESET + " O menu [3] Relatórios calcula automaticamente seu patrimônio multiplicando seu saldo pela cotação do momento fornecida pelo Oráculo.");
        System.out.println("Pressione ENTER para voltar...");
        new Scanner(System.in).nextLine();
    }
}