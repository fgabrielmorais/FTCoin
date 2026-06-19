package br.com.ftcoin.controllers;

import br.com.ftcoin.daos.ICarteiraDAO;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.utils.ConsoleColors;
import java.util.List;

public class CarteiraController {

    private ICarteiraDAO carteiraDAO;

    public CarteiraController(ICarteiraDAO carteiraDAO) {
        this.carteiraDAO = carteiraDAO;
    }

    public void criarCarteira(String nomeTitular, String corretora) {
        try {
            if (nomeTitular == null || nomeTitular.trim().isEmpty()) {
                throw new IllegalArgumentException("O nome do titular é obrigatório.");
            }
            if (corretora == null || corretora.trim().isEmpty()) {
                throw new IllegalArgumentException("O nome da corretora é obrigatório.");
            }

            Carteira novaCarteira = new Carteira();
            novaCarteira.setNomeTitular(nomeTitular.trim());
            novaCarteira.setCorretora(corretora.trim());

            carteiraDAO.inserir(novaCarteira);
            System.out.println(ConsoleColors.GREEN_BOLD + "Carteira de " + nomeTitular + " criada com sucesso na corretora " + corretora + "!" + ConsoleColors.RESET);

        } catch (IllegalArgumentException e) {
            System.err.println(ConsoleColors.RED + "Erro de validação: " + e.getMessage() + ConsoleColors.RESET);
        } catch (Exception e) {
            System.err.println(ConsoleColors.RED + "Erro ao criar a carteira: " + e.getMessage() + ConsoleColors.RESET);
        }
    }

    public List<Carteira> listarTodasCarteiras() {
        return carteiraDAO.listarTodas();
    }
    
     public Carteira buscarCarteira(int id) {
         Carteira carteira = carteiraDAO.buscarPorId(id);
         if (carteira == null) {
             System.out.println(ConsoleColors.YELLOW + "Nenhuma carteira encontrada com o ID: " + id + ConsoleColors.RESET);
         }
         return carteira;
     }

     public void editarCarteira(int id, String novoTitular, String novaCorretora) {
         try {
             Carteira carteiraExistente = carteiraDAO.buscarPorId(id);
             if (carteiraExistente == null) {
                 System.out.println(ConsoleColors.YELLOW + "Nenhuma carteira encontrada com o ID: " + id + " para edição." + ConsoleColors.RESET);
                 return;
             }

             carteiraExistente.setNomeTitular(novoTitular);
             carteiraExistente.setCorretora(novaCorretora);
             carteiraDAO.atualizar(carteiraExistente);

             System.out.println(ConsoleColors.GREEN_BOLD + "Carteira atualizada com sucesso!" + ConsoleColors.RESET);
         } catch (Exception e) {
             System.err.println(ConsoleColors.RED + "Erro ao editar a carteira: " + e.getMessage() + ConsoleColors.RESET);
         }
     }

     public void excluirCarteira(int id) {
         try {
             Carteira carteiraExistente = carteiraDAO.buscarPorId(id);
             if (carteiraExistente == null) {
                 System.out.println(ConsoleColors.YELLOW + "Nenhuma carteira encontrada com o ID: " + id + " para exclusão." + ConsoleColors.RESET);
                 return;
             }
             
             carteiraDAO.excluir(id);
             System.out.println(ConsoleColors.GREEN_BOLD + "Carteira ID " + id + " excluída com sucesso!" + ConsoleColors.RESET);
         } catch (Exception e) {
             System.err.println(ConsoleColors.RED + "Erro ao excluir a carteira: " + e.getMessage() + ConsoleColors.RESET);
         }
     }
}