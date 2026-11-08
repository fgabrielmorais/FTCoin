package br.com.ftcoin.gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import br.com.ftcoin.gui.common.ContextoAplicacao;
import br.com.ftcoin.gui.common.GuiFormatador;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.OrigemCotacao;
import br.com.ftcoin.gui.common.TemaCores;
import br.com.ftcoin.gui.common.TextosTraduzidos;
import br.com.ftcoin.gui.presenters.RelatoriosPresenter;
import br.com.ftcoin.gui.services.CotacaoCacheService;
import br.com.ftcoin.gui.views.components.CurrencyTableCellRenderer;
import br.com.ftcoin.gui.views.components.ModernButton;
import br.com.ftcoin.gui.views.components.ModernCard;
import br.com.ftcoin.gui.views.contracts.IRelatoriosView;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Cotacao;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.Posicao;

/**
 * View do Módulo de Relatórios (Padrão MVP).
 * Apresenta tabelas e painéis analíticos com destaque visual de cores (verde para lucro e vermelho para prejuízo)
 * para os 5 relatórios do sistema FTCoin.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class RelatoriosPanel extends JPanel implements IRelatoriosView {

    private static final long serialVersionUID = 1L;

    private final transient RelatoriosPresenter presenter;

    /** Seletor da carteira dos relatórios por carteira. */
    private final JComboBox<ItemFiltroCarteira> cmbCarteiraFiltro = new JComboBox<>();
    /** Abas dos cinco relatórios. */
    private final JTabbedPane tabRelatorios = new JTabbedPane();
    /** Evita gerar relatório enquanto o seletor de carteiras é preenchido. */
    private boolean carregandoFiltro = false;

    // Componentes do Relatório 1 (Cards de Posição CMP)
    /** Cartão do saldo de FTCoins. */
    private final ModernCard cardSaldo;
    /** Cartão do custo médio ponderado. */
    private final ModernCard cardCustoMedio;
    /** Cartão do valor a custo. */
    private final ModernCard cardValorCusto;
    /** Cartão do valor de mercado. */
    private final ModernCard cardValorMercado;
    /** Cartão do ganho realizado. */
    private final ModernCard cardGanhoRealizado;
    /** Cartão do ganho não realizado. */
    private final ModernCard cardGanhoNaoRealizado;
    /** Cartão do ganho total. */
    private final ModernCard cardGanhoTotal;
    /** Cartão do retorno sobre o investimento. */
    private final ModernCard cardRoi;

    // Componentes do Relatório 2 (Histórico)
    /** Dados do relatório 2 (histórico). */
    private final DefaultTableModel modelRel2;
    /** Tabela do relatório 2 (histórico). */
    private final JTable tabelaRel2;

    // Componentes do Relatório 3 (Consolidado de Todas as Carteiras)
    /** Dados do relatório 3 (consolidado). */
    private final DefaultTableModel modelRel3;
    /** Tabela do relatório 3 (consolidado). */
    private final JTable tabelaRel3;

    // Componentes do Relatório 4 (Desempenho e Lucratividade)
    /** Dados do relatório 4 (desempenho). */
    private final DefaultTableModel modelRel4;
    /** Tabela do relatório 4 (desempenho). */
    private final JTable tabelaRel4;

    // Componentes do Relatório 5 (Histórico do Oráculo)
    /** Dados do relatório 5 (oráculo). */
    private final DefaultTableModel modelRel5;
    /** Tabela do relatório 5 (oráculo). */
    private final JTable tabelaRel5;

    // Componentes das listagens de carteiras
    /** Dados da listagem de carteiras por identificador. */
    private final DefaultTableModel modelRel6;
    /** Tabela da listagem de carteiras por identificador. */
    private final JTable tabelaRel6;
    /** Dados da listagem de carteiras por titular. */
    private final DefaultTableModel modelRel7;
    /** Tabela da listagem de carteiras por titular. */
    private final JTable tabelaRel7;

    private record ItemFiltroCarteira(int id, String descricao) {
        @Override
        public String toString() {
            return descricao;
        }
    }

    private final transient ContextoAplicacao contexto;
    private final transient TextosTraduzidos textos;

    /**
     * Monta as abas dos relatórios e o presenter da tela.
     *
     * @param cotacaoService serviço de cotações com cache, usado no relatório 5
     * @param contexto contexto da aplicação, com os serviços e as mensagens ativos
     */
    public RelatoriosPanel(CotacaoCacheService cotacaoService, ContextoAplicacao contexto) {
        this.contexto = contexto;
        this.presenter = new RelatoriosPresenter(this, cotacaoService, contexto);
        MensagensGui mensagens = contexto.getMensagens();
        this.textos = new TextosTraduzidos(mensagens);
        GuiFormatador fmt = mensagens.getFormatador();
        cardSaldo = new ModernCard(null, fmt.quantidade(BigDecimal.ZERO), TemaCores.PRIMARIA);
        textos.vincular(cardSaldo::setTitulo, "gui.rel.card.saldoCarteira");
        cardCustoMedio = new ModernCard(null, fmt.moeda(BigDecimal.ZERO), TemaCores.TEXTO_PRINCIPAL);
        textos.vincular(cardCustoMedio::setTitulo, "gui.rel.card.custoMedioCMP");
        cardValorCusto = new ModernCard(null, fmt.moeda(BigDecimal.ZERO), TemaCores.TEXTO_PRINCIPAL);
        textos.vincular(cardValorCusto::setTitulo, "gui.rel.card.totalACusto");
        cardValorMercado = new ModernCard(null, fmt.moeda(BigDecimal.ZERO), TemaCores.SECUNDARIA);
        textos.vincular(cardValorMercado::setTitulo, "gui.rel.card.valorMercadoAtual");
        cardGanhoRealizado = new ModernCard(null, fmt.moeda(BigDecimal.ZERO), TemaCores.POSITIVO_LUCRO);
        textos.vincular(cardGanhoRealizado::setTitulo, "gui.rel.card.ganhoRealizado");
        cardGanhoNaoRealizado = new ModernCard(null, fmt.moeda(BigDecimal.ZERO), TemaCores.POSITIVO_LUCRO);
        textos.vincular(cardGanhoNaoRealizado::setTitulo, "gui.rel.card.ganhoNaoRealizado");
        cardGanhoTotal = new ModernCard(null, fmt.moeda(BigDecimal.ZERO), TemaCores.POSITIVO_LUCRO);
        textos.vincular(cardGanhoTotal::setTitulo, "gui.rel.card.ganhoTotalLiquido");
        cardRoi = new ModernCard(null, fmt.percentual(BigDecimal.ZERO), TemaCores.POSITIVO_LUCRO);
        textos.vincular(cardRoi::setTitulo, "gui.rel.card.retornoRoi");

        setLayout(new BorderLayout(15, 15));
        setBackground(TemaCores.FUNDO_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Topo com Título e Seletor de Carteira para Relatórios 1 e 2
        JPanel pnlTopo = new JPanel(new BorderLayout(10, 10));
        pnlTopo.setOpaque(false);

        JLabel lblTitulo = new JLabel();
        textos.vincular(lblTitulo::setText, "gui.rel.painel.titulo");
        lblTitulo.setFont(TemaCores.FONTE_TITULO_GRANDE);
        lblTitulo.setForeground(TemaCores.TEXTO_PRINCIPAL);
        pnlTopo.add(lblTitulo, BorderLayout.NORTH);

        JPanel pnlFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlFiltro.setOpaque(false);

        JLabel lblFiltrar = new JLabel();
        textos.vincular(lblFiltrar::setText, "gui.rel.filtro.label");
        lblFiltrar.setFont(TemaCores.FONTE_LABEL);
        pnlFiltro.add(lblFiltrar);

        cmbCarteiraFiltro.setPreferredSize(new Dimension(320, 32));
        cmbCarteiraFiltro.setFont(TemaCores.FONTE_PADRAO);
        cmbCarteiraFiltro.addActionListener(e -> {
            if (carregandoFiltro) {
                return;
            }
            ItemFiltroCarteira sel = (ItemFiltroCarteira) cmbCarteiraFiltro.getSelectedItem();
            if (sel != null) {
                presenter.gerarRelatoriosCarteiraEspecifica(sel.id());
            }
        });
        pnlFiltro.add(cmbCarteiraFiltro);

        ModernButton btnRecarregar = new ModernButton("", ModernButton.Variante.NEUTRO);
        textos.vincular(btnRecarregar::setText, "gui.rel.btn.atualizarTodos");
        btnRecarregar.addActionListener(e -> presenter.inicializar());
        pnlFiltro.add(btnRecarregar);

        pnlTopo.add(pnlFiltro, BorderLayout.SOUTH);
        add(pnlTopo, BorderLayout.NORTH);

        // Abas para os 5 relatórios
        tabRelatorios.setFont(TemaCores.FONTE_LABEL);

        // Modelos e Tabelas

        // 1. Posição da Carteira
        JPanel pnlRel1 = new JPanel(new GridLayout(2, 4, 14, 14));
        pnlRel1.setOpaque(false);
        pnlRel1.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        pnlRel1.add(cardSaldo);
        pnlRel1.add(cardCustoMedio);
        pnlRel1.add(cardValorCusto);
        pnlRel1.add(cardValorMercado);
        pnlRel1.add(cardGanhoRealizado);
        pnlRel1.add(cardGanhoNaoRealizado);
        pnlRel1.add(cardGanhoTotal);
        pnlRel1.add(cardRoi);
        tabRelatorios.addTab("", pnlRel1);
        textos.vincular(titulo -> tabRelatorios.setTitleAt(0, titulo), "gui.rel.tab1.titulo");

        // 2. Histórico da Carteira
        String[] colRel2 = chaves("gui.rel.tab2.col.", "data", "operacao", "quantidade", "cotacao", "total");
        modelRel2 = new DefaultTableModel(colRel2, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaRel2 = criarTabelaFormatada(modelRel2);
        textos.vincularColunas(tabelaRel2, colRel2);
        tabelaRel2.getColumnModel().getColumn(1).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.TIPO_OPERACAO, mensagens));
        tabelaRel2.getColumnModel().getColumn(2).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.QUANTIDADE, mensagens));
        tabelaRel2.getColumnModel().getColumn(3).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, mensagens));
        tabelaRel2.getColumnModel().getColumn(4).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, mensagens));
        tabRelatorios.addTab("", new JScrollPane(tabelaRel2));
        textos.vincular(titulo -> tabRelatorios.setTitleAt(1, titulo), "gui.rel.tab2.titulo");

        // 3. Consolidado de Todas as Carteiras
        String[] colRel3 = chaves("gui.rel.tab3.col.", "id", "titular", "corretora", "saldo", "valorMercado", "participacao", "ganhoTotal");
        modelRel3 = new DefaultTableModel(colRel3, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaRel3 = criarTabelaFormatada(modelRel3);
        textos.vincularColunas(tabelaRel3, colRel3);
        tabelaRel3.getColumnModel().getColumn(3).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.QUANTIDADE, mensagens));
        tabelaRel3.getColumnModel().getColumn(4).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, mensagens));
        tabelaRel3.getColumnModel().getColumn(5).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.PERCENTUAL_COM_COR, mensagens));
        tabelaRel3.getColumnModel().getColumn(6).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA_COM_COR, mensagens));
        tabRelatorios.addTab("", new JScrollPane(tabelaRel3));
        textos.vincular(titulo -> tabRelatorios.setTitleAt(2, titulo), "gui.rel.tab3.titulo");

        // 4. Desempenho e Lucratividade
        String[] colRel4 = chaves("gui.rel.tab4.col.", "id", "titular", "corretora", "investido", "valorAtual", "lucroPrejuizo", "roi");
        modelRel4 = new DefaultTableModel(colRel4, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaRel4 = criarTabelaFormatada(modelRel4);
        textos.vincularColunas(tabelaRel4, colRel4);
        tabelaRel4.getColumnModel().getColumn(3).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, mensagens));
        tabelaRel4.getColumnModel().getColumn(4).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, mensagens));
        tabelaRel4.getColumnModel().getColumn(5).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA_COM_COR, mensagens));
        tabelaRel4.getColumnModel().getColumn(6).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.PERCENTUAL_COM_COR, mensagens));
        tabRelatorios.addTab("", new JScrollPane(tabelaRel4));
        textos.vincular(titulo -> tabRelatorios.setTitleAt(3, titulo), "gui.rel.tab4.titulo");

        // 5. Histórico do Oráculo
        String[] colRel5 = chaves("gui.rel.tab5.col.", "data", "cotacao", "varAbs", "varPerc", "fonte");
        modelRel5 = new DefaultTableModel(colRel5, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaRel5 = criarTabelaFormatada(modelRel5);
        textos.vincularColunas(tabelaRel5, colRel5);
        tabelaRel5.getColumnModel().getColumn(1).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, mensagens));
        tabelaRel5.getColumnModel().getColumn(2).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA_COM_COR, mensagens));
        tabelaRel5.getColumnModel().getColumn(3).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.PERCENTUAL_COM_COR, mensagens));
        tabelaRel5.getColumnModel().getColumn(4).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.ORIGEM_BADGE, mensagens));
        tabRelatorios.addTab("", new JScrollPane(tabelaRel5));
        textos.vincular(titulo -> tabRelatorios.setTitleAt(4, titulo), "gui.rel.tab5.titulo");

        // 6 e 7. Listagens de carteiras ordenadas por identificador e por nome do titular
        String[] colRel6 = chaves("gui.rel.tab6.col.", "id", "titular", "corretora");
        modelRel6 = new DefaultTableModel(colRel6, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaRel6 = criarTabelaFormatada(modelRel6);
        textos.vincularColunas(tabelaRel6, colRel6);
        tabRelatorios.addTab("", new JScrollPane(tabelaRel6));
        textos.vincular(titulo -> tabRelatorios.setTitleAt(5, titulo), "gui.rel.tab6.titulo");

        String[] colRel7 = chaves("gui.rel.tab7.col.", "id", "titular", "corretora");
        modelRel7 = new DefaultTableModel(colRel7, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaRel7 = criarTabelaFormatada(modelRel7);
        textos.vincularColunas(tabelaRel7, colRel7);
        tabRelatorios.addTab("", new JScrollPane(tabelaRel7));
        textos.vincular(titulo -> tabRelatorios.setTitleAt(6, titulo), "gui.rel.tab7.titulo");

        add(tabRelatorios, BorderLayout.CENTER);
    }

    // Monta as chaves das colunas de uma tabela a partir de um prefixo comum
    private static String[] chaves(String prefixo, String... sufixos) {
        String[] chaves = new String[sufixos.length];
        for (int i = 0; i < sufixos.length; i++) {
            chaves[i] = prefixo + sufixos[i];
        }
        return chaves;
    }

    private JTable criarTabelaFormatada(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(TemaCores.FONTE_PADRAO);
        table.getTableHeader().setFont(TemaCores.FONTE_LABEL);
        table.getTableHeader().setBackground(TemaCores.SUPERFICIE_HEADER);
        return table;
    }

    /**
     * Recarrega o seletor de carteiras e o relatório da aba ativa.
     */
    public void inicializar() {
        presenter.inicializar();
    }

    /**
     * Alterna programaticamente para a aba de relatório especificada.
     *
     * @param indiceAba Índice numérico da aba (0 a 4)
     */
    public void selecionarAba(int indiceAba) {
        if (indiceAba >= 0 && indiceAba < tabRelatorios.getTabCount()) {
            tabRelatorios.setSelectedIndex(indiceAba);
        }
    }

    @Override
    public void exibirCarteirasFiltro(List<Carteira> carteiras) {
        carregandoFiltro = true;
        try {
            cmbCarteiraFiltro.removeAllItems();
            for (Carteira c : carteiras) {
                cmbCarteiraFiltro.addItem(new ItemFiltroCarteira(
                        c.getId(),
                        "#" + c.getId() + " - " + c.getNomeTitular() + " (" + c.getCorretora() + ")"
                ));
            }
            if (carteiras.isEmpty()) {
                limparRelatoriosIndividuais();
            }
        } finally {
            carregandoFiltro = false;
        }
    }

    private void limparRelatoriosIndividuais() {
        exibirRelatorio1Posicao(new Posicao(), BigDecimal.ZERO);
        modelRel2.setRowCount(0);
    }

    @Override
    public void exibirRelatorio1Posicao(Posicao posicao, BigDecimal cotacaoAtual) {
        GuiFormatador fmt = contexto.getMensagens().getFormatador();

        cardSaldo.setValor(fmt.quantidade(posicao.getSaldo()), TemaCores.PRIMARIA);
        cardCustoMedio.setValor(fmt.moeda(posicao.getCustoMedio()), TemaCores.TEXTO_PRINCIPAL);
        cardValorCusto.setValor(fmt.moeda(posicao.getValorACusto()), TemaCores.TEXTO_PRINCIPAL);
        cardValorMercado.setValor(fmt.moeda(posicao.calcularValorDeMercado(cotacaoAtual)), TemaCores.SECUNDARIA);

        // Verde para lucro, vermelho para prejuízo
        BigDecimal ganhoRealizado = posicao.getGanhoRealizado();
        BigDecimal ganhoNaoRealizado = posicao.calcularGanhoNaoRealizado(cotacaoAtual);
        BigDecimal ganhoTotal = posicao.calcularGanhoTotal(cotacaoAtual);
        BigDecimal roi = posicao.calcularRoi(cotacaoAtual);
        cardGanhoRealizado.setValor(fmt.moeda(ganhoRealizado), corDoResultado(ganhoRealizado));
        cardGanhoNaoRealizado.setValor(fmt.moeda(ganhoNaoRealizado), corDoResultado(ganhoNaoRealizado));
        cardGanhoTotal.setValor(fmt.moeda(ganhoTotal), corDoResultado(ganhoTotal));
        cardRoi.setValor(fmt.percentual(roi), corDoResultado(roi));
    }

    private static Color corDoResultado(BigDecimal valor) {
        return valor.signum() >= 0 ? TemaCores.POSITIVO_LUCRO : TemaCores.NEGATIVO_PREJUIZO;
    }

    @Override
    public void exibirRelatorio2Historico(List<Movimentacao> historico, Map<LocalDate, BigDecimal> cotacoes) {
        modelRel2.setRowCount(0);
        GuiFormatador fmt = contexto.getMensagens().getFormatador();
        for (Movimentacao m : historico) {
            BigDecimal cotacao = cotacoes.get(m.getDataOperacao());
            modelRel2.addRow(new Object[]{
                    fmt.data(m.getDataOperacao()),
                    m.getTipoOperacao(),
                    m.getQuantidadeMovimentada(),
                    cotacao,
                    m.calcularValor(cotacao)
            });
        }
    }

    @Override
    public void exibirRelatorio3Consolidado(Map<Carteira, Posicao> posicoes, Posicao portfolio, BigDecimal cotacaoAtual) {
        modelRel3.setRowCount(0);
        posicoes.forEach((carteira, posicao) -> modelRel3.addRow(new Object[]{
                carteira.getId(),
                carteira.getNomeTitular(),
                carteira.getCorretora(),
                posicao.getSaldo(),
                posicao.calcularValorDeMercado(cotacaoAtual),
                posicao.calcularParticipacao(portfolio, cotacaoAtual),
                posicao.calcularGanhoTotal(cotacaoAtual)
        }));
    }

    @Override
    public void exibirRelatorio4Desempenho(Map<Carteira, Posicao> posicoes, BigDecimal cotacaoAtual) {
        modelRel4.setRowCount(0);
        posicoes.forEach((carteira, posicao) -> modelRel4.addRow(new Object[]{
                carteira.getId(),
                carteira.getNomeTitular(),
                carteira.getCorretora(),
                posicao.getValorACusto(),
                posicao.calcularValorDeMercado(cotacaoAtual),
                posicao.calcularGanhoTotal(cotacaoAtual),
                posicao.calcularRoi(cotacaoAtual)
        }));
    }

    @Override
    public void exibirRelatorio5Cotacoes(Map<Cotacao, OrigemCotacao> historico) {
        CotacaoPanel.preencherHistorico(modelRel5, historico, contexto.getMensagens().getFormatador());
    }

    @Override
    public void exibirCarteirasPorIdentificador(List<Carteira> carteiras) {
        preencherListagem(modelRel6, carteiras);
    }

    @Override
    public void exibirCarteirasPorTitular(List<Carteira> carteiras) {
        preencherListagem(modelRel7, carteiras);
    }

    // Uma linha por carteira: identificador, titular e corretora
    private static void preencherListagem(DefaultTableModel modelo, List<Carteira> carteiras) {
        modelo.setRowCount(0);
        for (Carteira c : carteiras) {
            modelo.addRow(new Object[]{ c.getId(), c.getNomeTitular(), c.getCorretora() });
        }
    }

    @Override
    public void exibirMensagemErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, contexto.getMensagens().get("gui.rel.erro.titulo"), JOptionPane.ERROR_MESSAGE);
    }
}
