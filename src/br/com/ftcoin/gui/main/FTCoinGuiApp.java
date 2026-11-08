package br.com.ftcoin.gui.main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import br.com.ftcoin.config.ConfiguracaoLog;
import br.com.ftcoin.config.RegistroDeFalhas;
import br.com.ftcoin.models.FTCoinException;
import br.com.ftcoin.services.CarteiraService;
import br.com.ftcoin.services.MovimentacaoService;
import br.com.ftcoin.daos.TipoPersistencia;
import br.com.ftcoin.gui.common.ContextoAplicacao;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.views.MainFrame;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.TipoOperacao;

/**
 * <p>
 * Ponto de entrada executável da Interface Gráfica de Usuário ({@code GUI}) do <b>FTCoin Studio</b>.
 * </p>
 *
 * <p>
 * Inicializa a biblioteca Swing com antialiasing, configura o contexto operacional global,
 * carrega massa de dados de demonstração em modo volátil e instancia a janela principal ({@link MainFrame}).
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class FTCoinGuiApp {

    private static final Logger LOG = Logger.getLogger(FTCoinGuiApp.class.getName());

    /**
     * Construtor padrão da aplicação gráfica.
     */
    public FTCoinGuiApp() {
        // Construtor padrão da aplicação
    }

    /**
     * Ponto de entrada principal da aplicação Swing.
     *
     * @param args Argumentos da linha de comando fornecidos na inicialização
     */
    public static void main(String[] args) {
        ConfiguracaoLog.iniciar();
        LOG.info("Iniciando FTCoin Studio (interface gráfica)");

        configurarAparenciaVisual();

        SwingUtilities.invokeLater(() -> {
            try {
                ContextoAplicacao contexto = new ContextoAplicacao(new MensagensGui(Locale.getDefault()), TipoPersistencia.MEMORIA);
                carregarMassaDadosDemonstracao(contexto);

                MainFrame janelaPrincipal = new MainFrame(contexto);
                registrarEncerramento(janelaPrincipal, contexto);
                janelaPrincipal.setVisible(true);

                LOG.log(Level.INFO, "FTCoin Studio pronto (persistência={0}, idioma={1})", new Object[] {
                        contexto.getTipoPersistenciaAtual().getNome(),
                        contexto.getMensagens().getLocaleAtual() });
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Falha ao iniciar a interface gráfica", e);
            }
        });
    }

    /**
     * Registra o encerramento da aplicação quando a janela principal é fechada.
     *
     * @param janelaPrincipal Janela cujo fechamento encerra a aplicação
     * @param contexto Contexto cuja persistência é fechada ao sair
     */
    private static void registrarEncerramento(MainFrame janelaPrincipal, ContextoAplicacao contexto) {
        janelaPrincipal.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                LOG.info("Encerrando FTCoin Studio");
                contexto.encerrar();
            }
        });
    }

    /**
     * Configura propriedades de renderização visual e temas do sistema operacional.
     */
    private static void configurarAparenciaVisual() {
        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Tema visual do sistema indisponível; usando o tema padrão do Swing", e);
        }
    }

    /**
     * Carrega carteiras e movimentações de teste caso a persistência inicial seja em memória e esteja vazia.
     *
     * @param contexto Contexto com os serviços da persistência inicial
     */
    private static void carregarMassaDadosDemonstracao(ContextoAplicacao contexto) {
        if (contexto.getTipoPersistenciaAtual() == TipoPersistencia.MEMORIA) {
            CarteiraService carteiraService = contexto.getCarteiraService();
            MovimentacaoService movimentacaoService = contexto.getMovimentacaoService();

            if (carteiraService.listarTodasCarteiras().isEmpty()) {
                // As operações de demonstração usam a cotação de hoje. Sem cotação (por exemplo, servidor
                // fora do ar), a demonstração é pulada e o programa abre normalmente, sem dados de exemplo
                try {
                    movimentacaoService.buscarCotacaoAtual();
                } catch (FTCoinException e) {
                    RegistroDeFalhas.registrar(LOG, "carregar os dados de demonstração", e);
                    return;
                }

                LOG.fine("Carregando dados de demonstração na persistência em memória");
                // Popula carteiras iniciais para demonstração
                Carteira c1 = carteiraService.criarCarteira("Gabriel Morais Felix", "Binance");
                movimentacaoService.registrarOperacao(c1.getId(), TipoOperacao.COMPRA, new BigDecimal("150.000"));
                movimentacaoService.registrarOperacao(c1.getId(), TipoOperacao.COMPRA, new BigDecimal("50.000"));
                movimentacaoService.registrarOperacao(c1.getId(), TipoOperacao.VENDA, new BigDecimal("30.000"));

                Carteira c2 = carteiraService.criarCarteira("Isabela & Ana UX", "Mercado Bitcoin");
                movimentacaoService.registrarOperacao(c2.getId(), TipoOperacao.COMPRA, new BigDecimal("85.500"));
                movimentacaoService.registrarOperacao(c2.getId(), TipoOperacao.VENDA, new BigDecimal("15.500"));

                Carteira c3 = carteiraService.criarCarteira("UNICAMP Limeira Lab", "Coinbase");
                movimentacaoService.registrarOperacao(c3.getId(), TipoOperacao.COMPRA, new BigDecimal("200.000"));
            }
        }
    }
}
