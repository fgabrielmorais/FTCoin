package br.com.ftcoin.gui.views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import br.com.ftcoin.daos.TipoPersistencia;
import br.com.ftcoin.gui.common.AppContext;
import br.com.ftcoin.gui.common.IdiomaSelector;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.TemaCores;
import br.com.ftcoin.gui.services.CotacaoCacheService;
import br.com.ftcoin.gui.services.RelatoriosService;
import br.com.ftcoin.gui.views.components.ModernButton;

/**
 * Janela principal da aplicação FTCoin Studio.
 * Gerencia a barra de navegação superior, os seletores de idioma
 * e armazenamento, a barra de status e a navegação entre telas.
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

    private final JComboBox<IdiomaSelector> cmbIdioma =
            new JComboBox<>(IdiomaSelector.values());

    private final JComboBox<String> cmbBanco =
            new JComboBox<>(new String[]{"memory", "mariadb"});

    private final JLabel lblIdioma = new JLabel();
    private final JLabel lblBanco = new JLabel();

    private final ModernButton btnDash =
            new ModernButton("", ModernButton.Variante.NEUTRO);

    private final ModernButton btnCart =
            new ModernButton("", ModernButton.Variante.NEUTRO);

    private final ModernButton btnMov =
            new ModernButton("", ModernButton.Variante.NEUTRO);

    private final ModernButton btnCot =
            new ModernButton("", ModernButton.Variante.NEUTRO);

    private final ModernButton btnRel =
            new ModernButton("", ModernButton.Variante.NEUTRO);

    private final ModernButton btnAjudaTop =
            new ModernButton("", ModernButton.Variante.NEUTRO);

    private final ModernButton btnSobreTop =
            new ModernButton("", ModernButton.Variante.NEUTRO);

    public MainFrame() {
        super(AppContext.getInstancia().getMensagens().get("gui.app.titulo"));

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

        atualizarTextosInterface();
    }


        private JPanel criarBarraNavegacaoSuperior() {
        JPanel pnlBarra = new JPanel(new BorderLayout(0, 8));
        pnlBarra.setBackground(TemaCores.SUPERFICIE_CARD);
        pnlBarra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0, TemaCores.BORDA_SUAVE
                ),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JPanel pnlBotoesNav =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlBotoesNav.setOpaque(false);

        btnDash.addActionListener(e -> navegarPara(TELA_DASHBOARD));
        btnCart.addActionListener(e -> navegarPara(TELA_CARTEIRAS));
        btnMov.addActionListener(e -> navegarPara(TELA_MOVIMENTACOES));
        btnCot.addActionListener(e -> navegarPara(TELA_COTACAO));
        btnRel.addActionListener(e -> navegarPara(TELA_RELATORIOS));

        pnlBotoesNav.add(btnDash);
        pnlBotoesNav.add(btnCart);
        pnlBotoesNav.add(btnMov);
        pnlBotoesNav.add(btnCot);
        pnlBotoesNav.add(btnRel);

        JPanel pnlConfig =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlConfig.setOpaque(false);

        lblIdioma.setFont(TemaCores.FONTE_LABEL);
        cmbIdioma.setFont(TemaCores.FONTE_PADRAO);
        cmbIdioma.setPreferredSize(new Dimension(180, 30));

        lblBanco.setFont(TemaCores.FONTE_LABEL);
        cmbBanco.setFont(TemaCores.FONTE_PADRAO);
        cmbBanco.setPreferredSize(new Dimension(240, 30));

        javax.swing.DefaultListCellRenderer renderizadorBanco =
                new javax.swing.DefaultListCellRenderer();

        cmbBanco.setRenderer((lista, valor, indice, selecionado, foco) -> {
            JLabel rotulo = (JLabel) renderizadorBanco
                    .getListCellRendererComponent(
                            lista, valor, indice, selecionado, foco
                    );

            if (valor == null) {
                rotulo.setText("");
            } else {
                MensagensGui mensagens =
                        AppContext.getInstancia().getMensagens();

                String chave = "mariadb".equals(valor)
                        ? "gui.menu.banco.mariadb"
                        : "gui.menu.banco.memoria";

                rotulo.setText(mensagens.get(chave));
            }

            return rotulo;
        });

        btnAjudaTop.addActionListener(
                e -> new AjudaDialog(this).setVisible(true)
        );

        btnSobreTop.addActionListener(
                e -> new CreditosDialog(this).setVisible(true)
        );

        pnlConfig.add(lblIdioma);
        pnlConfig.add(cmbIdioma);
        pnlConfig.add(Box.createHorizontalStrut(5));
        pnlConfig.add(lblBanco);
        pnlConfig.add(cmbBanco);
        pnlConfig.add(Box.createHorizontalStrut(5));
        pnlConfig.add(btnAjudaTop);
        pnlConfig.add(btnSobreTop);

        pnlBarra.add(pnlBotoesNav, BorderLayout.NORTH);
        pnlBarra.add(pnlConfig, BorderLayout.SOUTH);

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
                // Seleciona inicialmente o idioma que já está ativo.
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        cmbIdioma.setSelectedItem(
                IdiomaSelector.porLocale(mensagens.getLocaleAtual())
        );

        // Aplica o idioma escolhido pelo usuário.
        cmbIdioma.addActionListener(e -> {
            IdiomaSelector selecionado =
                    (IdiomaSelector) cmbIdioma.getSelectedItem();

            if (selecionado == null) {
                return;
            }

            MensagensGui msg = AppContext.getInstancia().getMensagens();

            if (!selecionado.getLocale().equals(msg.getLocaleAtual())) {
                msg.setLocale(selecionado.getLocale());
            }
        });

        // Seletor de Banco de Dados
        cmbBanco.addActionListener(e -> {
            int idx = cmbBanco.getSelectedIndex();
            TipoPersistencia desejado = (idx == 0) ? TipoPersistencia.MEMORIA : TipoPersistencia.MARIADB;

            try {
                AppContext.getInstancia().alternarPersistencia(desejado);
                atualizarStatusBar();
                                MensagensGui msg =
                        AppContext.getInstancia().getMensagens();

                String chaveBanco = desejado == TipoPersistencia.MEMORIA
                        ? "gui.menu.banco.memoria"
                        : "gui.menu.banco.mariadb";

                lblStatusMensagem.setText(
                        msg.get(
                                "gui.status.persistencia.alterada",
                                msg.get(chaveBanco)
                        )
                );
                recarregarTodosPaineis();
            } catch (Exception ex) {
                // Reverte seleção caso o banco falhe
                cmbBanco.setSelectedIndex(0);
                                MensagensGui msg =
                        AppContext.getInstancia().getMensagens();

                JOptionPane.showMessageDialog(
                        this,
                        msg.get(
                                "gui.status.erro.conexao.msg",
                                ex.getMessage()
                        ),
                        msg.get("gui.status.erro.conexao.titulo"),
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
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(
                    this::atualizarTextosInterface
            );
            return;
        }

        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        setTitle(mensagens.get("gui.app.titulo"));

        btnDash.setText(mensagens.get("gui.nav.btn.dashboard"));
        btnCart.setText(mensagens.get("gui.nav.btn.carteiras"));
        btnMov.setText(mensagens.get("gui.nav.btn.movimentacoes"));
        btnCot.setText(mensagens.get("gui.nav.btn.cotacao"));
        btnRel.setText(mensagens.get("gui.nav.btn.relatorios"));

        btnAjudaTop.setText(mensagens.get("gui.nav.btn.ajuda"));
        btnSobreTop.setText(mensagens.get("gui.nav.creditos"));

        lblIdioma.setText(mensagens.get("gui.nav.label.idioma"));
        lblBanco.setText(mensagens.get("gui.nav.label.banco"));

        IdiomaSelector idiomaAtual =
                IdiomaSelector.porLocale(mensagens.getLocaleAtual());

        if (cmbIdioma.getSelectedItem() != idiomaAtual) {
            cmbIdioma.setSelectedItem(idiomaAtual);
        }

        cmbIdioma.repaint();
        cmbBanco.repaint();

        // O aviso da última troca de armazenamento é transitório.
        // O banco ativo continua indicado na barra de status.
        lblStatusMensagem.setText("");

        atualizarStatusBar();
        revalidate();
        repaint();
    }

        private void atualizarStatusBar() {
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        TipoPersistencia tipo =
                AppContext.getInstancia().getTipoPersistenciaAtual();

        String chaveBanco = tipo == TipoPersistencia.MEMORIA
                ? "gui.status.banco.memoria"
                : "gui.status.banco.mariadb";

        lblStatusBanco.setText(
                mensagens.get(
                        "gui.status.label.persistencia",
                        mensagens.get(chaveBanco)
                )
        );

        String nomeIdioma = mensagens.getLocaleAtual()
                .getDisplayLanguage(mensagens.getLocaleAtual());

        lblStatusIdioma.setText(
                mensagens.get("gui.status.label.idioma", nomeIdioma)
        );
    }
}
