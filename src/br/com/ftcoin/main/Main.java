package br.com.ftcoin.main;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.ftcoin.config.ConfiguracaoLog;
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

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        ConfiguracaoLog.iniciar();
        LOG.log(Level.INFO, "Iniciando FTCoin (modo texto, idioma={0})", Locale.getDefault());

        Console console = new Console(new Mensagens(Locale.getDefault()));

        try {
            criarMenuPrincipal(console).exibir();
        } catch (FTCoinException e) {
            // A causa técnica já foi registrada com stack trace onde o erro surgiu (DAOFactory, DAOs)
            LOG.log(Level.SEVERE, "FTCoin encerrado por erro: {0}", e.getChave());
            console.exibirNegativo(e.getChave(), e.getArgumentos());
        } finally {
            LOG.info("Encerrando FTCoin (modo texto)");
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
