package br.com.ftcoin.gui.main;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import br.com.ftcoin.config.ConfiguracaoLog;
import br.com.ftcoin.config.RegistroDeFalhas;
import br.com.ftcoin.daos.PersistenciaException;
import br.com.ftcoin.models.FTCoinException;
import br.com.ftcoin.services.CarteiraService;
import br.com.ftcoin.services.MovimentacaoService;
import br.com.ftcoin.daos.TipoPersistencia;
import br.com.ftcoin.gui.common.ContextoAplicacao;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.TemaCores;
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
 * lê os argumentos da linha de comando (idioma, banco e demonstração) e instancia a janela principal ({@link MainFrame}).
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class FTCoinGuiApp {

    private static final Logger LOG = Logger.getLogger(FTCoinGuiApp.class.getName());

    private static final LocalDate DIA_DEMO_1 = LocalDate.of(2026, 2, 1);
    private static final LocalDate DIA_DEMO_2 = LocalDate.of(2026, 2, 2);
    private static final LocalDate DIA_DEMO_3 = LocalDate.of(2026, 2, 3);

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

        ArgumentosAplicacao argumentos;
        try {
            argumentos = ArgumentosAplicacao.ler(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(ArgumentosAplicacao.USO);
            System.exit(ArgumentosAplicacao.CODIGO_SAIDA_ARGUMENTO_INVALIDO);
            return;
        }
        LOG.info("Iniciando FTCoin Studio (interface gráfica)");

        configurarAparenciaVisual();

        SwingUtilities.invokeLater(() -> {
            try {
                MensagensGui mensagens = new MensagensGui(argumentos.locale());
                ContextoAplicacao contexto = criarContexto(mensagens, argumentos.persistencia());
                if (argumentos.demonstracao()) {
                    carregarMassaDadosDemonstracao(contexto);
                }

                MainFrame janelaPrincipal = new MainFrame(contexto);
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
     * Cria o contexto com a persistência pedida. Se o MariaDB não conectar, avisa o usuário e abre em memória.
     *
     * @param mensagens textos da interface no idioma inicial
     * @param persistencia mecanismo de persistência pedido na linha de comando
     * @return o contexto da aplicação
     */
    private static ContextoAplicacao criarContexto(MensagensGui mensagens, TipoPersistencia persistencia) {
        try {
            return new ContextoAplicacao(mensagens, persistencia);
        } catch (PersistenciaException e) {
            RegistroDeFalhas.registrar(LOG, "abrir a persistência " + persistencia.getNome(), e);
            JOptionPane.showMessageDialog(null, mensagens.descreverErro(e),
                    mensagens.get("gui.status.erro.conexao.titulo"), JOptionPane.ERROR_MESSAGE);
            return new ContextoAplicacao(mensagens, TipoPersistencia.MEMORIA);
        }
    }

    /**
     * Configura propriedades de renderização visual e temas do sistema operacional.
     */
    private static void configurarAparenciaVisual() {
        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            TemaCores.aplicarPadroesSwing();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Tema visual do sistema indisponível; usando o tema padrão do Swing", e);
        }
    }

    /**
     * Carrega carteiras e movimentações de demonstração (datas fixas de 2026) quando a persistência
     * em memória está vazia. Se faltar cotação para alguma data, a carga é interrompida e o programa abre normalmente.
     *
     * @param contexto Contexto com os serviços da persistência inicial
     */
    private static void carregarMassaDadosDemonstracao(ContextoAplicacao contexto) {
        if (contexto.getTipoPersistenciaAtual() != TipoPersistencia.MEMORIA) {
            return;
        }
        CarteiraService carteiraService = contexto.getCarteiraService();
        MovimentacaoService movimentacaoService = contexto.getMovimentacaoService();
        if (!carteiraService.listarTodasCarteiras().isEmpty()) {
            return;
        }

        LOG.fine("Carregando dados de demonstração na persistência em memória");
        try {
            Carteira c1 = carteiraService.criarCarteira("Gabriel Morais Felix", "Binance");
            movimentacaoService.registrarOperacao(c1.getId(), TipoOperacao.COMPRA, DIA_DEMO_1, new BigDecimal("150.000"));
            movimentacaoService.registrarOperacao(c1.getId(), TipoOperacao.COMPRA, DIA_DEMO_2, new BigDecimal("50.000"));
            movimentacaoService.registrarOperacao(c1.getId(), TipoOperacao.VENDA, DIA_DEMO_3, new BigDecimal("30.000"));

            Carteira c2 = carteiraService.criarCarteira("Isabela & Ana UX", "Mercado Bitcoin");
            movimentacaoService.registrarOperacao(c2.getId(), TipoOperacao.COMPRA, DIA_DEMO_1, new BigDecimal("85.500"));
            movimentacaoService.registrarOperacao(c2.getId(), TipoOperacao.VENDA, DIA_DEMO_3, new BigDecimal("15.500"));

            Carteira c3 = carteiraService.criarCarteira("UNICAMP Limeira Lab", "Coinbase");
            movimentacaoService.registrarOperacao(c3.getId(), TipoOperacao.COMPRA, DIA_DEMO_2, new BigDecimal("200.000"));
        } catch (FTCoinException e) {
            RegistroDeFalhas.registrar(LOG, "carregar os dados de demonstração", e);
        }
    }
}
