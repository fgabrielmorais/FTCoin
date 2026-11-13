package br.com.ftcoin.gui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import br.com.ftcoin.config.RegistroDeFalhas;
import br.com.ftcoin.gui.common.ContextoAplicacao;
import br.com.ftcoin.gui.common.GuiFormatador;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.OrigemCotacao;
import br.com.ftcoin.gui.common.TemaCores;
import br.com.ftcoin.gui.common.TextosTraduzidos;
import br.com.ftcoin.gui.presenters.RelatoriosPresenter;
import br.com.ftcoin.gui.services.CotacaoCacheService;
import br.com.ftcoin.gui.services.ImpressaoRelatorio;
import br.com.ftcoin.gui.views.components.CurrencyTableCellRenderer;
import br.com.ftcoin.gui.views.components.EstiloTabela;
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
 * para os relatórios do sistema FTCoin. Permite imprimir os relatórios escolhidos, ou salvá-los em PDF.
 *
 * @author Grupo B.05
 * @version 1.1.0
 */
public class RelatoriosPanel extends JPanel implements IRelatoriosView {

    private static final long serialVersionUID = 1L;

    /** Quantidade de abas, a partir da primeira, que dependem da carteira selecionada (Posição e Histórico). */
    private static final int ABAS_POR_CARTEIRA = 2;

    /** Posição da aba "Todas as movimentações" entre as abas dos relatórios. */
    private static final int ABA_TODAS_MOVIMENTACOES = 7;

    /** Coluna da operação (compra ou venda) na tabela de todas as movimentações. */
    private static final int COLUNA_TODAS_OPERACAO = 5;

    /** Coluna da quantidade na tabela de todas as movimentações. */
    private static final int COLUNA_TODAS_QUANTIDADE = 6;

    /** Coluna da cotação na tabela de todas as movimentações. */
    private static final int COLUNA_TODAS_COTACAO = 7;

    /** Coluna do valor total na tabela de todas as movimentações. */
    private static final int COLUNA_TODAS_TOTAL = 8;

    /** Largura da coluna de indicadores na impressão do relatório 1, em pontos. */
    private static final int LARGURA_INDICADOR_IMPRESSAO = 360;

    /** Largura da coluna de valores na impressão do relatório 1, em pontos. */
    private static final int LARGURA_VALOR_IMPRESSAO = 220;

    /** Registrador de eventos desta tela. */
    private static final Logger LOG = Logger.getLogger(RelatoriosPanel.class.getName());

    private final transient RelatoriosPresenter presenter;

    /** Seletor da carteira dos relatórios por carteira. */
    private final JComboBox<ItemFiltroCarteira> cmbCarteiraFiltro = new JComboBox<>();
    /** Rótulo e seletor de carteira, visíveis só nas abas que dependem da carteira. */
    private final JPanel grupoCarteira = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
    /** Abas dos relatórios. */
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

    // Componentes do Relatório 8 (todas as movimentações)
    /** Dados do relatório 8 (todas as movimentações). */
    private final DefaultTableModel modelTodas;
    /** Tabela do relatório 8 (todas as movimentações). */
    private final JTable tabelaTodas;

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

        // Grupo do seletor de carteira: só aparece nas abas Posição e Histórico, as únicas que dependem dele
        grupoCarteira.setOpaque(false);

        JLabel lblFiltrar = new JLabel();
        textos.vincular(lblFiltrar::setText, "gui.rel.filtro.label");
        lblFiltrar.setFont(TemaCores.FONTE_LABEL);
        lblFiltrar.setForeground(TemaCores.TEXTO_SECUNDARIO);
        grupoCarteira.add(lblFiltrar);

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
        grupoCarteira.add(cmbCarteiraFiltro);
        pnlFiltro.add(grupoCarteira);

        ModernButton btnRecarregar = new ModernButton("", ModernButton.Variante.NEUTRO);
        textos.vincular(btnRecarregar::setText, "gui.rel.btn.atualizarTodos");
        btnRecarregar.addActionListener(e -> presenter.inicializar());
        pnlFiltro.add(btnRecarregar);

        ModernButton btnImprimir = new ModernButton("", ModernButton.Variante.NEUTRO);
        textos.vincular(btnImprimir::setText, "gui.rel.btn.imprimir");
        btnImprimir.addActionListener(e -> imprimirRelatorio());
        pnlFiltro.add(btnImprimir);

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
        // Cartões no topo, com altura própria, para não esticarem até o rodapé
        JPanel envoltorioRel1 = new JPanel(new BorderLayout());
        envoltorioRel1.setOpaque(false);
        envoltorioRel1.add(pnlRel1, BorderLayout.NORTH);
        tabRelatorios.addTab("", envoltorioRel1);
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
        tabRelatorios.addTab("", EstiloTabela.comRolagemSemBorda(tabelaRel2));
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
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.PERCENTUAL_SIMPLES, mensagens));
        tabelaRel3.getColumnModel().getColumn(6).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA_COM_COR, mensagens));
        tabRelatorios.addTab("", EstiloTabela.comRolagemSemBorda(tabelaRel3));
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
        tabRelatorios.addTab("", EstiloTabela.comRolagemSemBorda(tabelaRel4));
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
        tabRelatorios.addTab("", EstiloTabela.comRolagemSemBorda(tabelaRel5));
        textos.vincular(titulo -> tabRelatorios.setTitleAt(4, titulo), "gui.rel.tab5.titulo");

        // 6 e 7. Listagens de carteiras ordenadas por identificador e por nome do titular
        String[] colRel6 = chaves("gui.rel.tab6.col.", "id", "titular", "corretora");
        modelRel6 = new DefaultTableModel(colRel6, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaRel6 = criarTabelaFormatada(modelRel6);
        textos.vincularColunas(tabelaRel6, colRel6);
        tabRelatorios.addTab("", EstiloTabela.comRolagemSemBorda(tabelaRel6));
        textos.vincular(titulo -> tabRelatorios.setTitleAt(5, titulo), "gui.rel.tab6.titulo");

        String[] colRel7 = chaves("gui.rel.tab7.col.", "id", "titular", "corretora");
        modelRel7 = new DefaultTableModel(colRel7, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaRel7 = criarTabelaFormatada(modelRel7);
        textos.vincularColunas(tabelaRel7, colRel7);
        tabRelatorios.addTab("", EstiloTabela.comRolagemSemBorda(tabelaRel7));
        textos.vincular(titulo -> tabRelatorios.setTitleAt(6, titulo), "gui.rel.tab7.titulo");

        // 8. Todas as operações, sem limite de linhas
        String[] colTodas = chaves("gui.rel.todas.col.", "id", "carteira", "titular", "corretora",
                "data", "operacao", "quantidade", "cotacao", "total");
        modelTodas = new DefaultTableModel(colTodas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaTodas = criarTabelaFormatada(modelTodas);
        textos.vincularColunas(tabelaTodas, colTodas);
        tabelaTodas.getColumnModel().getColumn(COLUNA_TODAS_OPERACAO).setCellRenderer(new CurrencyTableCellRenderer(
                CurrencyTableCellRenderer.ModoRenderizacao.TIPO_OPERACAO, mensagens));
        tabelaTodas.getColumnModel().getColumn(COLUNA_TODAS_QUANTIDADE).setCellRenderer(new CurrencyTableCellRenderer(
                CurrencyTableCellRenderer.ModoRenderizacao.QUANTIDADE, mensagens));
        for (int coluna : new int[] { COLUNA_TODAS_COTACAO, COLUNA_TODAS_TOTAL }) {
            tabelaTodas.getColumnModel().getColumn(coluna).setCellRenderer(new CurrencyTableCellRenderer(
                    CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, mensagens));
        }
        tabRelatorios.addTab("", EstiloTabela.comRolagemSemBorda(tabelaTodas));
        textos.vincular(titulo -> tabRelatorios.setTitleAt(ABA_TODAS_MOVIMENTACOES, titulo), "gui.rel.todas.titulo");

        tabRelatorios.addChangeListener(e -> grupoCarteira.setVisible(tabRelatorios.getSelectedIndex() < ABAS_POR_CARTEIRA));
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
        EstiloTabela.aplicar(table);
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
        if (valor.signum() == 0) {
            return TemaCores.TEXTO_PRINCIPAL;
        }
        return valor.signum() > 0 ? TemaCores.POSITIVO_LUCRO : TemaCores.NEGATIVO_PREJUIZO;
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
    public void exibirTodasMovimentacoes(List<Movimentacao> historico, List<Carteira> carteiras,
            Map<LocalDate, BigDecimal> cotacoes) {
        modelTodas.setRowCount(0);
        Map<Integer, Carteira> carteirasPorId = new HashMap<>();
        carteiras.forEach(c -> carteirasPorId.put(c.getId(), c));
        GuiFormatador formatador = contexto.getMensagens().getFormatador();
        for (Movimentacao m : historico) {
            Carteira carteira = carteirasPorId.get(m.getIdCarteira());
            BigDecimal cotacao = cotacoes.get(m.getDataOperacao());
            modelTodas.addRow(new Object[]{
                    m.getIdMovimento(),
                    m.getIdCarteira(),
                    carteira == null ? "-" : carteira.getNomeTitular(),
                    carteira == null ? "-" : carteira.getCorretora(),
                    formatador.data(m.getDataOperacao()),
                    m.getTipoOperacao(),
                    m.getQuantidadeMovimentada(),
                    cotacao,
                    m.calcularValor(cotacao)
            });
        }
    }

    /**
     * Pergunta quais relatórios imprimir e envia a seleção a um único trabalho de impressão (ou PDF).
     * Só podem ser marcados os relatórios que têm dados; o da aba atual já vem marcado.
     * O diálogo volta a aparecer enquanto o usuário confirmar sem marcar nenhum relatório.
     */
    private void imprimirRelatorio() {
        MensagensGui mensagens = contexto.getMensagens();
        ItemFiltroCarteira carteira = (ItemFiltroCarteira) cmbCarteiraFiltro.getSelectedItem();

        JPanel escolhas = new JPanel(new GridLayout(0, 1, 4, 4));
        escolhas.add(new JLabel(mensagens.get("gui.rel.impressao.escolha")));
        escolhas.add(new JLabel(mensagens.get("gui.rel.impressao.carteira") + " "
                + (carteira == null ? "-" : carteira.descricao())));

        JCheckBox[] opcoes = new JCheckBox[tabRelatorios.getTabCount()];
        for (int i = 0; i < opcoes.length; i++) {
            opcoes[i] = new JCheckBox(tabRelatorios.getTitleAt(i));
            boolean disponivel = (i >= ABAS_POR_CARTEIRA || carteira != null) && tabelaDoRelatorio(i).getRowCount() > 0;
            opcoes[i].setEnabled(disponivel);
            opcoes[i].setSelected(disponivel && i == tabRelatorios.getSelectedIndex());
            if (!disponivel) {
                opcoes[i].setToolTipText(mensagens.get("gui.rel.impressao.vazio"));
            }
            escolhas.add(opcoes[i]);
        }
        JCheckBox todas = new JCheckBox(mensagens.get("gui.rel.impressao.selecionarTodos"));
        todas.addActionListener(e -> {
            for (JCheckBox opcao : opcoes) {
                if (opcao.isEnabled()) {
                    opcao.setSelected(todas.isSelected());
                }
            }
        });
        escolhas.add(todas);

        while (JOptionPane.showConfirmDialog(this, escolhas, mensagens.get("gui.rel.btn.imprimir"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            List<ImpressaoRelatorio.Relatorio> selecionados = new ArrayList<>();
            for (int i = 0; i < opcoes.length; i++) {
                if (!opcoes[i].isSelected() || !opcoes[i].isEnabled()) {
                    continue;
                }
                String titulo = "FTCoin - " + tabRelatorios.getTitleAt(i);
                if (i < ABAS_POR_CARTEIRA) {
                    titulo += " (#" + carteira.id() + ")";
                }
                selecionados.add(new ImpressaoRelatorio.Relatorio(titulo,
                        ImpressaoRelatorio.copiarTabela(tabelaDoRelatorio(i))));
            }
            if (selecionados.isEmpty()) {
                exibirMensagemErro(mensagens.get("gui.rel.impressao.nenhum"));
                continue;
            }
            try {
                ImpressaoRelatorio.imprimir(selecionados,
                        mensagens.getFormatador().data(LocalDate.now()) + " - "
                                + mensagens.get("gui.rel.impressao.pagina"));
            } catch (PrinterException | SecurityException e) {
                RegistroDeFalhas.registrar(LOG, "imprimir os relatórios", e);
                exibirMensagemErro(mensagens.get("gui.rel.impressao.erro") + " " + e.getMessage());
            }
            return;
        }
    }

    /**
     * Devolve a tabela com os dados de um relatório, na forma em que ele é impresso.
     * O relatório 1 mostra cartões na tela e, por isso, vira uma tabela de indicador e valor.
     *
     * @param aba posição da aba do relatório, a partir de zero
     * @return tabela com os dados do relatório
     */
    private JTable tabelaDoRelatorio(int aba) {
        if (aba != 0) {
            return new JTable[] { tabelaRel2, tabelaRel3, tabelaRel4, tabelaRel5,
                    tabelaRel6, tabelaRel7, tabelaTodas }[aba - 1];
        }
        MensagensGui mensagens = contexto.getMensagens();
        DefaultTableModel modelo = new DefaultTableModel(new String[] {
                mensagens.get("gui.rel.impressao.indicador"), mensagens.get("gui.rel.impressao.valor") }, 0);
        for (ModernCard card : new ModernCard[] { cardSaldo, cardCustoMedio, cardValorCusto, cardValorMercado,
                cardGanhoRealizado, cardGanhoNaoRealizado, cardGanhoTotal, cardRoi }) {
            modelo.addRow(new Object[] { card.getTitulo(), card.getValor() });
        }
        JTable tabela = new JTable(modelo);
        tabela.getColumnModel().getColumn(0).setWidth(LARGURA_INDICADOR_IMPRESSAO);
        tabela.getColumnModel().getColumn(1).setWidth(LARGURA_VALOR_IMPRESSAO);
        return tabela;
    }

    @Override
    public void exibirMensagemErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, contexto.getMensagens().get("gui.rel.erro.titulo"), JOptionPane.ERROR_MESSAGE);
    }
}
