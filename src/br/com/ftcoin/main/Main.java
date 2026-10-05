package br.com.ftcoin.main;

import java.util.Locale;

import br.com.ftcoin.controllers.CarteiraController;
import br.com.ftcoin.controllers.MovimentacaoController;
import br.com.ftcoin.daos.CarteiraDAO;
import br.com.ftcoin.daos.DAOFactory;
import br.com.ftcoin.daos.MovimentacaoDAO;
import br.com.ftcoin.models.FTCoinException;
import br.com.ftcoin.views.Console;
import br.com.ftcoin.views.Mensagens;
import br.com.ftcoin.views.MenuPrincipalView;

public class Main {

    public static void main(String[] args) {
        Console console = new Console(new Mensagens(Locale.getDefault()));

        try {
            criarMenuPrincipal(console).exibir();
        } catch (FTCoinException e) {
            console.exibirNegativo(e.getChave(), e.getArgumentos());
        }
    }

    private static MenuPrincipalView criarMenuPrincipal(Console console) {
        DAOFactory daoFactory = DAOFactory.getInstancia();
        CarteiraDAO carteiraDAO = daoFactory.criarCarteiraDAO();
        MovimentacaoDAO movimentacaoDAO = daoFactory.criarMovimentacaoDAO();

        CarteiraController carteiraController = new CarteiraController(carteiraDAO, movimentacaoDAO);
        MovimentacaoController movimentacaoController = new MovimentacaoController(movimentacaoDAO, carteiraDAO, daoFactory.criarOraculoDAO());

        return new MenuPrincipalView(console, carteiraController, movimentacaoController);
    }
}
