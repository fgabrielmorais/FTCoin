package br.com.ftcoin.main;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.ftcoin.config.ConfiguracaoLog;
import br.com.ftcoin.services.CarteiraService;
import br.com.ftcoin.services.MovimentacaoService;
import br.com.ftcoin.daos.CarteiraDAO;
import br.com.ftcoin.daos.DAOFactory;
import br.com.ftcoin.daos.MovimentacaoDAO;
import br.com.ftcoin.models.FTCoinException;
import br.com.ftcoin.views.Console;
import br.com.ftcoin.views.Mensagens;
import br.com.ftcoin.views.MenuPrincipalView;

/**
 * Ponto de entrada da versão de terminal: monta DAOs, serviços e telas e inicia o menu principal.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public final class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private Main() {
        // Classe só de inicialização
    }

    /**
     * Inicia a versão de terminal e fecha a persistência ao sair.
     *
     * @param args argumentos da linha de comando (não usados)
     */
    public static void main(String[] args) {
        ConfiguracaoLog.iniciar();
        LOG.log(Level.INFO, "Iniciando FTCoin (modo texto, idioma={0})", Locale.getDefault());

        Console console = new Console(new Mensagens(Locale.getDefault()));
        DAOFactory daoFactory = null;

        try {
            daoFactory = DAOFactory.criar(DAOFactory.lerTipoConfigurado());
            criarMenuPrincipal(console, daoFactory).exibir();
        } catch (FTCoinException e) {
            // A causa técnica já foi registrada com stack trace onde o erro surgiu (DAOFactory, DAOs)
            LOG.log(Level.SEVERE, "FTCoin encerrado por erro: {0}", e.getChave());
            console.exibirNegativo(e.getChave(), e.getArgumentos());
        } finally {
            if (daoFactory != null) {
                daoFactory.fechar();
            }
            LOG.info("Encerrando FTCoin (modo texto)");
        }
    }

    private static MenuPrincipalView criarMenuPrincipal(Console console, DAOFactory daoFactory) {
        CarteiraDAO carteiraDAO = daoFactory.criarCarteiraDAO();
        MovimentacaoDAO movimentacaoDAO = daoFactory.criarMovimentacaoDAO();

        CarteiraService carteiraService = new CarteiraService(carteiraDAO, movimentacaoDAO);
        MovimentacaoService movimentacaoService = new MovimentacaoService(movimentacaoDAO, carteiraDAO, daoFactory.criarOraculoDAO());

        return new MenuPrincipalView(console, carteiraService, movimentacaoService);
    }
}
