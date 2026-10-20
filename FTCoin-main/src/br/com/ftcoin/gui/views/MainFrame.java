package br.com.ftcoin.gui.views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import br.com.ftcoin.daos.TipoPersistencia;
import br.com.ftcoin.gui.common.AppContext;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.TemaCores;
import br.com.ftcoin.gui.services.CotacaoCacheService;
import br.com.ftcoin.gui.services.RelatoriosService;
import br.com.ftcoin.gui.views.components.ModernButton;

/**
 * Janela Principal unificada da aplicação FTCoin Studio.
 * Gerencia a barra de menus com submenus, a barra de navegação superior,
 * os seletores dinâmicos de Idioma e Banco de Dados, e o CardLayout de telas.
 */
public class MainFrame extends JFrame {

    public static final String TELA_DASHBOARD = "DASHBOARD";
    public static final String TELA_CARTEIRAS = "CARTEIRAS";
    public static final String TELA_MOVIMENTACOES = "MOVIMENTACOES";
    public static final String TELA_COTACAO = "COTACAO";
    public static final String TELA_RELATORIOS = "RELATORIOS";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pnlConteudo = new JPanel(cardLayout);

    private final DashboardPanel pnlDashboard;
    private final CarteirasPanel pnlCarteiras;
    private final MovimentacoesPanel pnlMovimentacoes;
    private final CotacaoPanel pnlCotacao;
    private final RelatoriosPanel pnlRelatorios;

    private final JLabel lblStatusBanco = new JLabel();
    private final JLabel lblStatusIdioma = new JLabel();
    private final JLabel lblStatusMensagem = new JLabel("");

    private final JComboBox<String> cmbIdioma = new JComboBox<>(new String[]{"🇧🇷 Português (BR)", "🇺🇸 English (US)"});
    private final JComboBox<String> cmbBanco = new JComboBox<>(new String[]{"💾 Memória Volátil", "🗄️ MariaDB (SQL)"});

    public MainFrame() {
        super("FTCoin Studio - Gestão Financeira de Criptomoedas (Java 25 LTS)");

        // Configurações da Janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setSize(1240, 780);
        setLocationRelativeTo(null);

        // Inicialização de Serviços
        CotacaoCacheService cotacaoService = new CotacaoCacheService();
        RelatoriosService relatoriosService = new RelatoriosService(cotacaoService);

        // Instanciação dos Painéis de Conteúdo
        pnlDashboard = new DashboardPanel(relatoriosService, this::navegarPara);
        pnlCarteiras = new CarteirasPanel(relatoriosService);
        pnlMovimentacoes = new MovimentacoesPanel(relatoriosService, cotacaoService);
        pnlCotacao = new CotacaoPanel(cotacaoService);
        pnlRelatorios = new RelatoriosPanel(relatoriosService);

        pnlConteudo.add(pnlDashboard, TELA_DASHBOARD);
        pnlConteudo.add(pnlCarteiras, TELA_CARTEIRAS);
        pnlConteudo.add(pnlMovimentacoes, TELA_MOVIMENTACOES);
        pnlConteudo.add(pnlCotacao, TELA_COTACAO);
        pnlConteudo.add(pnlRelatorios, TELA_RELATORIOS);

        // Monta Interface - Mantém apenas a Barra de Navegação Superior (mais moderna e pertinente)
        JPanel pnlPrincipal = new JPanel(new BorderLayout());
        pnlPrincipal.add(criarBarraNavegacaoSuperior(), BorderLayout.NORTH);
        pnlPrincipal.add(pnlConteudo, BorderLayout.CENTER);
        pnlPrincipal.add(criarBarraStatus(), BorderLayout.SOUTH);

        setContentPane(pnlPrincipal);

        // Configuração dos Seletores
        configurarSeletores();

        // Ouvinte de mudança de persistência e idioma
        AppContext.getInstancia().adicionarOuvintePersistencia(this::recarregarTodosPaineis);
        AppContext.getInstancia().getMensagens().adicionarOuvinte(this::atualizarTextosInterface);

        // Inicializa telas
        pnlDashboard.inicializar();
        pnlCarteiras.inicializar();
        pnlMovimentacoes.inicializar();
        pnlCotacao.inicializar();
        pnlRelatorios.inicializar();

        atualizarStatusBar();
    }

    private JMenuBar criarBarraMenus() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(TemaCores.SUPERFICIE_HEADER);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TemaCores.BORDA_SUAVE));

        // Submenu: Arquivo
        JMenu menuArquivo = new JMenu("Arquivo");
        menuArquivo.setFont(TemaCores.FONTE_LABEL);
        JMenuItem itemDash = new JMenuItem("Dashboard Principal");
        itemDash.addActionListener(e -> navegarPara(TELA_DASHBOARD));
        JMenuItem itemRecarregar = new JMenuItem("Atualizar Todos os Dados");
        itemRecarregar.addActionListener(e -> recarregarTodosPaineis());
        JMenuItem itemSair = new JMenuItem("Sair do Sistema");
        itemSair.addActionListener(e -> System.exit(0));
        menuArquivo.add(itemDash);
        menuArquivo.add(itemRecarregar);
        menuArquivo.addSeparator();
        menuArquivo.add(itemSair);

        // Submenu: Carteiras
        JMenu menuCarteiras = new JMenu("Carteiras");
        menuCarteiras.setFont(TemaCores.FONTE_LABEL);
        JMenuItem itemGerenciarCart = new JMenuItem("Gerenciar Carteiras");
        itemGerenciarCart.addActionListener(e -> navegarPara(TELA_CARTEIRAS));
        JMenuItem itemNovaCart = new JMenuItem("+ Nova Carteira");
        itemNovaCart.addActionListener(e -> {
            navegarPara(TELA_CARTEIRAS);
            pnlCarteiras.abrirModalCadastro();
        });
        menuCarteiras.add(itemGerenciarCart);
        menuCarteiras.add(itemNovaCart);

        // Submenu: Movimentações
        JMenu menuMov = new JMenu("Movimentações");
        menuMov.setFont(TemaCores.FONTE_LABEL);
        JMenuItem itemComprar = new JMenuItem("🛒 Registrar Compra");
        itemComprar.addActionListener(e -> navegarPara(TELA_MOVIMENTACOES));
        JMenuItem itemVender = new JMenuItem("💰 Registrar Venda");
        itemVender.addActionListener(e -> navegarPara(TELA_MOVIMENTACOES));
        JMenuItem itemCotacao = new JMenuItem("🌐 Cotações & Oráculo");
        itemCotacao.addActionListener(e -> navegarPara(TELA_COTACAO));
        menuMov.add(itemComprar);
        menuMov.add(itemVender);
        menuMov.addSeparator();
        menuMov.add(itemCotacao);

        // Submenu: Relatórios
        JMenu menuRel = new JMenu("Relatórios");
        menuRel.setFont(TemaCores.FONTE_LABEL);
        JMenuItem itemRel1 = new JMenuItem("1. Posição da Carteira (CMP)");
        itemRel1.addActionListener(e -> navegarParaRelatorio(0));
        JMenuItem itemRel2 = new JMenuItem("2. Histórico de Operações");
        itemRel2.addActionListener(e -> navegarParaRelatorio(1));
        JMenuItem itemRel3 = new JMenuItem("3. Consolidado do Portfólio");
        itemRel3.addActionListener(e -> navegarParaRelatorio(2));
        JMenuItem itemRel4 = new JMenuItem("4. Desempenho e Lucratividade");
        itemRel4.addActionListener(e -> navegarParaRelatorio(3));
        JMenuItem itemRel5 = new JMenuItem("5. Histórico do Oráculo");
        itemRel5.addActionListener(e -> navegarParaRelatorio(4));
        menuRel.add(itemRel1);
        menuRel.add(itemRel2);
        menuRel.add(itemRel3);
        menuRel.add(itemRel4);
        menuRel.add(itemRel5);

        // Submenu: Sistema & Ajuda
        JMenu menuSistema = new JMenu("Sistema");
        menuSistema.setFont(TemaCores.FONTE_LABEL);
        JMenuItem itemAjuda = new JMenuItem("📖 Manual Estendido do Sistema");
        itemAjuda.addActionListener(e -> new AjudaDialog(this).setVisible(true));
        JMenuItem itemCreditos = new JMenuItem("ℹ️ Créditos & Autores");
        itemCreditos.addActionListener(e -> new CreditosDialog(this).setVisible(true));
        menuSistema.add(itemAjuda);
        menuSistema.add(itemCreditos);

        menuBar.add(menuArquivo);
        menuBar.add(menuCarteiras);
        menuBar.add(menuMov);
        menuBar.add(menuRel);
        menuBar.add(menuSistema);

        return menuBar;
    }

    private JPanel criarBarraNavegacaoSuperior() {
        JPanel pnlBarra = new JPanel(new BorderLayout(15, 0));
        pnlBarra.setBackground(TemaCores.SUPERFICIE_CARD);
        pnlBarra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, TemaCores.BORDA_SUAVE),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Botões de Navegação Unificada
        JPanel pnlBotoesNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlBotoesNav.setOpaque(false);

        ModernButton btnDash = new ModernButton("📊 Dashboard", ModernButton.Variante.NEUTRO);
        btnDash.addActionListener(e -> navegarPara(TELA_DASHBOARD));

        ModernButton btnCart = new ModernButton("💼 Carteiras", ModernButton.Variante.NEUTRO);
        btnCart.addActionListener(e -> navegarPara(TELA_CARTEIRAS));

        ModernButton btnMov = new ModernButton("🔄 Movimentações", ModernButton.Variante.NEUTRO);
        btnMov.addActionListener(e -> navegarPara(TELA_MOVIMENTACOES));

        ModernButton btnCot = new ModernButton("🌐 Cotação & Oráculo", ModernButton.Variante.NEUTRO);
        btnCot.addActionListener(e -> navegarPara(TELA_COTACAO));

        ModernButton btnRel = new ModernButton("📈 Relatórios", ModernButton.Variante.NEUTRO);
        btnRel.addActionListener(e -> navegarPara(TELA_RELATORIOS));

        pnlBotoesNav.add(btnDash);
        pnlBotoesNav.add(btnCart);
        pnlBotoesNav.add(btnMov);
        pnlBotoesNav.add(btnCot);
        pnlBotoesNav.add(btnRel);

        pnlBarra.add(pnlBotoesNav, BorderLayout.WEST);

        // Controles de Configuração: Seletor de Idioma e Seletor de Banco de Dados
        JPanel pnlConfig = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        pnlConfig.setOpaque(false);

        JLabel lblIdioma = new JLabel("Idioma:");
        lblIdioma.setFont(TemaCores.FONTE_LABEL);
        cmbIdioma.setFont(TemaCores.FONTE_PADRAO);
        cmbIdioma.setPreferredSize(new Dimension(150, 30));

        JLabel lblBanco = new JLabel("Banco:");
        lblBanco.setFont(TemaCores.FONTE_LABEL);
        cmbBanco.setFont(TemaCores.FONTE_PADRAO);
        cmbBanco.setPreferredSize(new Dimension(160, 30));

        ModernButton btnAjudaTop = new ModernButton("❓ Ajuda", ModernButton.Variante.NEUTRO);
        btnAjudaTop.addActionListener(e -> new AjudaDialog(this).setVisible(true));

        ModernButton btnSobreTop = new ModernButton("ℹ️ Sobre", ModernButton.Variante.NEUTRO);
        btnSobreTop.addActionListener(e -> new CreditosDialog(this).setVisible(true));

        pnlConfig.add(lblIdioma);
        pnlConfig.add(cmbIdioma);
        pnlConfig.add(Box.createHorizontalStrut(5));
        pnlConfig.add(lblBanco);
        pnlConfig.add(cmbBanco);
        pnlConfig.add(Box.createHorizontalStrut(5));
        pnlConfig.add(btnAjudaTop);
        pnlConfig.add(btnSobreTop);

        pnlBarra.add(pnlConfig, BorderLayout.EAST);

        return pnlBarra;
    }

    private JPanel criarBarraStatus() {
        JPanel pnlStatus = new JPanel(new BorderLayout(15, 0));
        pnlStatus.setBackground(TemaCores.SUPERFICIE_HEADER);
        pnlStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, TemaCores.BORDA_SUAVE),
                BorderFactory.createEmptyBorder(6, 15, 6, 15)
        ));

        lblStatusMensagem.setFont(TemaCores.FONTE_SUBTITULO);
        lblStatusMensagem.setForeground(TemaCores.TEXTO_PRINCIPAL);
        pnlStatus.add(lblStatusMensagem, BorderLayout.WEST);

        JPanel pnlInfoDir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        pnlInfoDir.setOpaque(false);

        lblStatusBanco.setFont(TemaCores.FONTE_LABEL);
        lblStatusBanco.setForeground(TemaCores.SECUNDARIA);

        lblStatusIdioma.setFont(TemaCores.FONTE_LABEL);
        lblStatusIdioma.setForeground(TemaCores.TEXTO_SECUNDARIO);

        pnlInfoDir.add(lblStatusBanco);
        pnlInfoDir.add(lblStatusIdioma);

        pnlStatus.add(pnlInfoDir, BorderLayout.EAST);
        return pnlStatus;
    }

    private void configurarSeletores() {
        // Seletor de Idioma
        cmbIdioma.addActionListener(e -> {
            int idx = cmbIdioma.getSelectedIndex();
            MensagensGui msg = AppContext.getInstancia().getMensagens();
            if (idx == 0) {
                msg.setLocale(MensagensGui.LOCALE_PT);
            } else {
                msg.setLocale(MensagensGui.LOCALE_EN);
            }
            atualizarStatusBar();
        });

        // Seletor de Banco de Dados
        cmbBanco.addActionListener(e -> {
            int idx = cmbBanco.getSelectedIndex();
            TipoPersistencia desejado = (idx == 0) ? TipoPersistencia.MEMORIA : TipoPersistencia.MARIADB;

            try {
                AppContext.getInstancia().alternarPersistencia(desejado);
                atualizarStatusBar();
                lblStatusMensagem.setText("Mecanismo de persistência alterado para: " + desejado.getValorConfiguracao());
                recarregarTodosPaineis();
            } catch (Exception ex) {
                // Reverte seleção caso o banco falhe
                cmbBanco.setSelectedIndex(0);
                JOptionPane.showMessageDialog(
                        this,
                        "Não foi possível conectar ao banco MariaDB.\nCertifique-se de que o serviço está ativo e configurado em database.properties.\n\nDetalhes: " + ex.getMessage(),
                        "Falha na Conexão com MariaDB",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    public void navegarPara(String tela) {
        cardLayout.show(pnlConteudo, tela);
        switch (tela) {
            case TELA_DASHBOARD -> pnlDashboard.inicializar();
            case TELA_CARTEIRAS -> pnlCarteiras.inicializar();
            case TELA_MOVIMENTACOES -> pnlMovimentacoes.inicializar();
            case TELA_COTACAO -> pnlCotacao.inicializar();
            case TELA_RELATORIOS -> pnlRelatorios.inicializar();
        }
    }

    /**
     * Navega para a tela de relatórios selecionando a aba desejada.
     *
     * @param tabIndex Índice da aba (0 a 4)
     */
    public void navegarParaRelatorio(int tabIndex) {
        navegarPara(TELA_RELATORIOS);
        pnlRelatorios.selecionarAba(tabIndex);
    }

    private void recarregarTodosPaineis() {
        pnlDashboard.inicializar();
        pnlCarteiras.inicializar();
        pnlMovimentacoes.inicializar();
        pnlCotacao.inicializar();
        pnlRelatorios.inicializar();
        atualizarStatusBar();
    }

    private void atualizarTextosInterface() {
        revalidate();
        repaint();
    }

    private void atualizarStatusBar() {
        TipoPersistencia tipo = AppContext.getInstancia().getTipoPersistenciaAtual();
        lblStatusBanco.setText("Persistência: " + (tipo == TipoPersistencia.MEMORIA ? "💾 MEMÓRIA (Ativa)" : "🗄️ MARIADB (Conectado)"));
        lblStatusIdioma.setText("Idioma: " + AppContext.getInstancia().getMensagens().getLocaleAtual().getDisplayLanguage());
    }
}
