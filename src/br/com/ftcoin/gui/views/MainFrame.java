package br.com.ftcoin.gui.views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.EnumMap;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import br.com.ftcoin.daos.TipoPersistencia;
import br.com.ftcoin.gui.common.ContextoAplicacao;
import br.com.ftcoin.gui.common.DatabaseSelector;
import br.com.ftcoin.gui.common.IdiomaSelector;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.ModoNavegacao;
import br.com.ftcoin.gui.common.TemaCores;
import br.com.ftcoin.gui.services.CotacaoCacheService;
import br.com.ftcoin.gui.views.components.ModernButton;

/**
 * Janela principal do FTCoin Studio: barra de navegação, seletores de idioma e de banco de dados,
 * barra de status e o {@link CardLayout} com as telas. Todos os textos vêm dos arquivos de mensagens
 * e são reaplicados quando o usuário troca o idioma.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    /** Identificador da tela de dashboard no {@link CardLayout}. */
    public static final String TELA_DASHBOARD = ModoNavegacao.DASHBOARD.getIdentificadorCard();
    /** Identificador da tela de carteiras no {@link CardLayout}. */
    public static final String TELA_CARTEIRAS = ModoNavegacao.CARTEIRAS.getIdentificadorCard();
    /** Identificador da tela de movimentações no {@link CardLayout}. */
    public static final String TELA_MOVIMENTACOES = ModoNavegacao.MOVIMENTACOES.getIdentificadorCard();
    /** Identificador da tela de cotação no {@link CardLayout}. */
    public static final String TELA_COTACAO = ModoNavegacao.COTACAO.getIdentificadorCard();
    /** Identificador da tela de relatórios no {@link CardLayout}. */
    public static final String TELA_RELATORIOS = ModoNavegacao.RELATORIOS.getIdentificadorCard();

    /** Alterna entre as telas no painel de conteúdo. */
    private final CardLayout cardLayout = new CardLayout();
    /** Painel central que exibe a tela ativa. */
    private final JPanel pnlConteudo = new JPanel(cardLayout);

    /** Tela de dashboard. */
    private final DashboardPanel pnlDashboard;
    /** Tela de carteiras. */
    private final CarteirasPanel pnlCarteiras;
    /** Tela de compra e venda. */
    private final MovimentacoesPanel pnlMovimentacoes;
    /** Tela de cotação e oráculo. */
    private final CotacaoPanel pnlCotacao;
    /** Tela de relatórios. */
    private final RelatoriosPanel pnlRelatorios;

    /** Botões da barra de navegação, um por tela. */
    private final EnumMap<ModoNavegacao, ModernButton> botoesNavegacao = new EnumMap<>(ModoNavegacao.class);
    /** Botão que abre o manual. */
    private final ModernButton botaoAjuda = new ModernButton("", ModernButton.Variante.NAVEGACAO);
    /** Botão que abre os créditos. */
    private final ModernButton botaoSobre = new ModernButton("", ModernButton.Variante.NAVEGACAO);
    /** Botão que encerra o programa, fechando as conexões. */
    private final ModernButton botaoSair = new ModernButton("", ModernButton.Variante.PERIGO);
    /** Rótulo do seletor de idioma. */
    private final JLabel lblIdioma = new JLabel();
    /** Rótulo do seletor de banco de dados. */
    private final JLabel lblBanco = new JLabel();

    /** Rótulo da barra de status com a última mensagem. */
    private final JLabel lblStatusMensagem = new JLabel("");

    /** Seletor de idioma. */
    private final JComboBox<IdiomaSelector> cmbIdioma = new JComboBox<>(IdiomaSelector.values());
    /** Seletor do mecanismo de persistência. */
    private final JComboBox<DatabaseSelector> cmbBanco = new JComboBox<>(DatabaseSelector.values());

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(MainFrame.class.getName());

    private final transient ContextoAplicacao contexto;
    private final transient MensagensGui mensagens;

    /**
     * Monta a janela, as telas e os seletores de idioma e banco.
     *
     * @param contexto contexto da aplicação, com os serviços e as mensagens ativos
     */
    public MainFrame(ContextoAplicacao contexto) {
        super();
        this.contexto = contexto;
        this.mensagens = contexto.getMensagens();

        // O X da janela também pede confirmação, igual ao botão Sair
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evento) {
                sair();
            }
        });
        setMinimumSize(new Dimension(1100, 720));
        setSize(1240, 780);
        setLocationRelativeTo(null);

        CotacaoCacheService cotacaoService = new CotacaoCacheService(contexto);

        pnlDashboard = new DashboardPanel(this::navegarPara, contexto);
        pnlCarteiras = new CarteirasPanel(contexto);
        pnlMovimentacoes = new MovimentacoesPanel(cotacaoService, contexto);
        pnlCotacao = new CotacaoPanel(cotacaoService, contexto);
        pnlRelatorios = new RelatoriosPanel(cotacaoService, contexto);

        pnlConteudo.add(pnlDashboard, TELA_DASHBOARD);
        pnlConteudo.add(pnlCarteiras, TELA_CARTEIRAS);
        pnlConteudo.add(pnlMovimentacoes, TELA_MOVIMENTACOES);
        pnlConteudo.add(pnlCotacao, TELA_COTACAO);
        pnlConteudo.add(pnlRelatorios, TELA_RELATORIOS);

        JPanel pnlPrincipal = new JPanel(new BorderLayout());
        pnlPrincipal.add(criarBarraNavegacaoSuperior(), BorderLayout.NORTH);
        pnlPrincipal.add(pnlConteudo, BorderLayout.CENTER);
        pnlPrincipal.add(criarBarraStatus(), BorderLayout.SOUTH);
        setContentPane(pnlPrincipal);

        configurarSeletores();
        aplicarTextos();
        botoesNavegacao.get(ModoNavegacao.DASHBOARD).setAtivo(true);

        contexto.adicionarOuvintePersistencia(this::recarregarTodosPaineis);
        mensagens.adicionarOuvinte(() -> {
            aplicarTextos();
            // Os dados são recarregados para refazer valores formatados (moeda, datas) no novo idioma
            recarregarTodosPaineis();
        });

        recarregarTodosPaineis();
    }

    // Pergunta se o usuário quer mesmo sair; se sim, fecha as conexões e encerra o programa
    private void sair() {
        Object[] opcoes = { mensagens.get("gui.sair.sim"), mensagens.get("gui.sair.nao") };
        int escolha = JOptionPane.showOptionDialog(this, mensagens.get("gui.sair.mensagem"),
                mensagens.get("gui.sair.titulo"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opcoes, opcoes[1]);
        if (escolha == 0) {
            LOG.info("Encerrando FTCoin Studio");
            contexto.encerrar();
            System.exit(0);
        }
    }

    private JPanel criarBarraNavegacaoSuperior() {
        JPanel pnlBarra = new JPanel(new BorderLayout(15, 0));
        pnlBarra.setBackground(TemaCores.SUPERFICIE_CARD);
        pnlBarra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, TemaCores.BORDA_SUAVE),
                BorderFactory.createEmptyBorder(6, 20, 0, 20)
        ));

        // Esquerda: nome do sistema e as cinco telas
        JPanel pnlBotoesNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlBotoesNav.setOpaque(false);

        JLabel lblMarca = new JLabel("FTCoin");
        lblMarca.setFont(TemaCores.FONTE_TITULO_GRANDE);
        lblMarca.setForeground(TemaCores.PRIMARIA);
        lblMarca.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 24));
        pnlBotoesNav.add(lblMarca);

        for (ModoNavegacao modo : ModoNavegacao.values()) {
            ModernButton botao = new ModernButton("", ModernButton.Variante.NAVEGACAO);
            botao.addActionListener(e -> navegarPara(modo.getIdentificadorCard()));
            botoesNavegacao.put(modo, botao);
            pnlBotoesNav.add(botao);
        }
        pnlBarra.add(pnlBotoesNav, BorderLayout.WEST);

        // Direita: ajuda, sobre e sair
        botaoAjuda.addActionListener(e -> new AjudaDialog(this, mensagens).setVisible(true));
        botaoSobre.addActionListener(e -> new CreditosDialog(this, mensagens).setVisible(true));
        botaoSair.addActionListener(e -> sair());

        JPanel pnlAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlAcoes.setOpaque(false);
        pnlAcoes.add(botaoAjuda);
        pnlAcoes.add(botaoSobre);
        pnlAcoes.add(botaoSair);
        pnlBarra.add(pnlAcoes, BorderLayout.EAST);

        return pnlBarra;
    }

    // Rodapé: mensagem de estado à esquerda; banco e idioma, que mudam pouco, à direita
    private JPanel criarBarraStatus() {
        JPanel pnlStatus = new JPanel(new BorderLayout(15, 0));
        pnlStatus.setBackground(TemaCores.SUPERFICIE_CARD);
        pnlStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, TemaCores.BORDA_SUAVE),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));

        lblStatusMensagem.setFont(TemaCores.FONTE_SUBTITULO);
        lblStatusMensagem.setForeground(TemaCores.TEXTO_SECUNDARIO);
        pnlStatus.add(lblStatusMensagem, BorderLayout.WEST);

        lblBanco.setFont(TemaCores.FONTE_LABEL);
        lblBanco.setForeground(TemaCores.TEXTO_SECUNDARIO);
        cmbBanco.setPreferredSize(new Dimension(200, 28));
        lblIdioma.setFont(TemaCores.FONTE_LABEL);
        lblIdioma.setForeground(TemaCores.TEXTO_SECUNDARIO);
        cmbIdioma.setPreferredSize(new Dimension(130, 28));

        JPanel pnlSeletores = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlSeletores.setOpaque(false);
        pnlSeletores.add(lblBanco);
        pnlSeletores.add(cmbBanco);
        pnlSeletores.add(javax.swing.Box.createHorizontalStrut(12));
        pnlSeletores.add(lblIdioma);
        pnlSeletores.add(cmbIdioma);
        pnlStatus.add(pnlSeletores, BorderLayout.EAST);

        return pnlStatus;
    }

    private void configurarSeletores() {
        cmbIdioma.setRenderer(rendererTraduzido((IdiomaSelector opcao) -> opcao.getDescricaoLocalizada(mensagens)));
        cmbBanco.setRenderer(rendererTraduzido((DatabaseSelector opcao) -> opcao.getDescricaoLocalizada(mensagens)));
        cmbIdioma.setSelectedItem(IdiomaSelector.porLocale(mensagens.getLocaleAtual()));
        cmbBanco.setSelectedItem(DatabaseSelector.porTipo(contexto.getTipoPersistenciaAtual()));

        cmbIdioma.addActionListener(e -> {
            IdiomaSelector escolhido = (IdiomaSelector) cmbIdioma.getSelectedItem();
            if (escolhido != null && !escolhido.getLocale().equals(mensagens.getLocaleAtual())) {
                // A troca de idioma avisa os ouvintes, entre eles aplicarTextos()
                mensagens.setLocale(escolhido.getLocale());
            }
        });

        cmbBanco.addActionListener(e -> {
            DatabaseSelector escolhido = (DatabaseSelector) cmbBanco.getSelectedItem();
            if (escolhido == null || escolhido.getTipoPersistencia() == contexto.getTipoPersistenciaAtual()) {
                return;
            }
            try {
                contexto.alternarPersistencia(escolhido.getTipoPersistencia());
                lblStatusMensagem.setText(mensagens.get("gui.status.persistencia.alterada",
                        escolhido.getDescricaoLocalizada(mensagens)));
                    } catch (RuntimeException ex) {
                // Volta o seletor para o mecanismo que continua ativo
                cmbBanco.setSelectedItem(DatabaseSelector.porTipo(contexto.getTipoPersistenciaAtual()));
                JOptionPane.showMessageDialog(
                        this,
                        mensagens.get("gui.status.erro.conexao.msg", mensagens.descreverErro(ex)),
                        mensagens.get("gui.status.erro.conexao.titulo"),
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    // Os seletores guardam as opções do enum; o texto mostrado é traduzido a cada desenho da lista
    private static <T> DefaultListCellRenderer rendererTraduzido(Function<T, String> texto) {
        return new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean selecionado, boolean comFoco) {
                Object exibido = (valor == null) ? "" : texto.apply((T) valor);
                return super.getListCellRendererComponent(lista, exibido, indice, selecionado, comFoco);
            }
        };
    }

    // Reaplica os textos da janela; chamado na montagem e a cada troca de idioma
    private void aplicarTextos() {
        setTitle(mensagens.get("gui.app.titulo"));
        botoesNavegacao.forEach((modo, botao) -> botao.setText(modo.getTituloLocalizado(mensagens)));
        botaoAjuda.setText(mensagens.get("gui.nav.btn.ajuda"));
        botaoSobre.setText(mensagens.get("gui.nav.btn.sobre"));
        botaoSair.setText(mensagens.get("gui.nav.btn.sair"));
        lblIdioma.setText(mensagens.get("gui.nav.label.idioma"));
        lblBanco.setText(mensagens.get("gui.nav.label.banco"));
        cmbIdioma.repaint();
        cmbBanco.repaint();
        revalidate();
        repaint();
    }

    /**
     * Exibe uma tela e recarrega os dados dela.
     *
     * @param tela identificador da tela, uma das constantes {@code TELA_*}
     */
    public void navegarPara(String tela) {
        cardLayout.show(pnlConteudo, tela);
        botoesNavegacao.forEach((modo, botao) -> botao.setAtivo(modo.getIdentificadorCard().equals(tela)));
        if (TELA_DASHBOARD.equals(tela)) {
            pnlDashboard.inicializar();
        } else if (TELA_CARTEIRAS.equals(tela)) {
            pnlCarteiras.inicializar();
        } else if (TELA_MOVIMENTACOES.equals(tela)) {
            pnlMovimentacoes.inicializar();
        } else if (TELA_COTACAO.equals(tela)) {
            pnlCotacao.inicializar();
        } else if (TELA_RELATORIOS.equals(tela)) {
            pnlRelatorios.inicializar();
        }
    }

    /**
     * Exibe a tela de relatórios já na aba pedida.
     *
     * @param indiceAba posição da aba, de 0 a 4
     */
    public void navegarParaRelatorio(int indiceAba) {
        navegarPara(TELA_RELATORIOS);
        pnlRelatorios.selecionarAba(indiceAba);
    }

    private void recarregarTodosPaineis() {
        pnlDashboard.inicializar();
        pnlCarteiras.inicializar();
        pnlMovimentacoes.inicializar();
        pnlCotacao.inicializar();
        pnlRelatorios.inicializar();
    }
}
