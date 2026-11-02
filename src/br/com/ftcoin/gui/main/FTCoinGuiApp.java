package br.com.ftcoin.gui.main;

import java.math.BigDecimal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import br.com.ftcoin.controllers.CarteiraController;
import br.com.ftcoin.controllers.MovimentacaoController;
import br.com.ftcoin.daos.TipoPersistencia;
import br.com.ftcoin.gui.common.AppContext;
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
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class FTCoinGuiApp {

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
        configurarAparenciaVisual();

        SwingUtilities.invokeLater(() -> {
            try {
                carregarMassaDadosDemonstracao();

                MainFrame janelaPrincipal = new MainFrame();
                janelaPrincipal.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
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
        } catch (Exception ignored) {
        }
    }

    /**
     * Carrega carteiras e movimentações de teste caso a persistência inicial seja em memória e esteja vazia.
     */
    private static void carregarMassaDadosDemonstracao() {
        AppContext contexto = AppContext.getInstancia();
        if (contexto.getTipoPersistenciaAtual() == TipoPersistencia.MEMORIA) {
            CarteiraController carteiraController = contexto.getCarteiraController();
            MovimentacaoController movimentacaoController = contexto.getMovimentacaoController();

            if (carteiraController.listarTodasCarteiras().isEmpty()) {
                // Popula carteiras iniciais para demonstração
                Carteira c1 = carteiraController.criarCarteira("Gabriel Morais Felix", "Binance");
                movimentacaoController.registrarOperacao(c1.getId(), TipoOperacao.COMPRA, new BigDecimal("150.000"));
                movimentacaoController.registrarOperacao(c1.getId(), TipoOperacao.COMPRA, new BigDecimal("50.000"));
                movimentacaoController.registrarOperacao(c1.getId(), TipoOperacao.VENDA, new BigDecimal("30.000"));

                Carteira c2 = carteiraController.criarCarteira("Isabela & Ana UX", "Mercado Bitcoin");
                movimentacaoController.registrarOperacao(c2.getId(), TipoOperacao.COMPRA, new BigDecimal("85.500"));
                movimentacaoController.registrarOperacao(c2.getId(), TipoOperacao.VENDA, new BigDecimal("15.500"));

                Carteira c3 = carteiraController.criarCarteira("UNICAMP Limeira Lab", "Coinbase");
                movimentacaoController.registrarOperacao(c3.getId(), TipoOperacao.COMPRA, new BigDecimal("200.000"));
            }
        }
    }
}
