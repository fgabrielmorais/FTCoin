package br.com.ftcoin.gui.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import br.com.ftcoin.gui.common.AppContext;
import br.com.ftcoin.gui.common.GuiFormatador;
import br.com.ftcoin.gui.common.TemaCores;
import br.com.ftcoin.gui.dto.CarteiraDTO;
import br.com.ftcoin.gui.dto.MovimentacaoDTO;
import br.com.ftcoin.gui.dto.PosicaoRelatorioDTO;
import br.com.ftcoin.gui.dto.RelatorioConsolidadoItemDTO;
import br.com.ftcoin.gui.dto.RelatorioDesempenhoDTO;
import br.com.ftcoin.gui.dto.RelatorioHistoricoCotacaoDTO;
import br.com.ftcoin.gui.presenters.RelatoriosPresenter;
import br.com.ftcoin.gui.services.RelatoriosService;
import br.com.ftcoin.gui.views.components.CurrencyTableCellRenderer;
import br.com.ftcoin.gui.views.components.ModernButton;
import br.com.ftcoin.gui.views.components.ModernCard;
import br.com.ftcoin.gui.views.contracts.IRelatoriosView;

/**
 * View do Módulo de Relatórios (Padrão MVP).
 * Apresenta tabelas e painéis analíticos com destaque visual de cores (verde para lucro e vermelho para prejuízo)
 * para os 5 relatórios do sistema FTCoin.
 */
public class RelatoriosPanel extends JPanel implements IRelatoriosView {

    private final RelatoriosPresenter presenter;

    private final JComboBox<ItemFiltroCarteira> cmbCarteiraFiltro = new JComboBox<>();
    private final JTabbedPane tabRelatorios = new JTabbedPane();
    private boolean carregandoFiltro = false;

    // Componentes do Relatório 1 (Cards de Posição CMP)
    private final ModernCard cardSaldo = new ModernCard("Saldo em Carteira", "0.000 FTC", TemaCores.PRIMARIA);
    private final ModernCard cardCustoMedio = new ModernCard("Custo Médio (CMP)", "R$ 0,00", TemaCores.TEXTO_PRINCIPAL);
    private final ModernCard cardValorCusto = new ModernCard("Total a Custo", "R$ 0,00", TemaCores.TEXTO_PRINCIPAL);
    private final ModernCard cardValorMercado = new ModernCard("Valor de Mercado", "R$ 0,00", TemaCores.SECUNDARIA);
    private final ModernCard cardGanhoRealizado = new ModernCard("Ganho Realizado", "R$ 0,00", TemaCores.POSITIVO_LUCRO);
    private final ModernCard cardGanhoNaoRealizado = new ModernCard("Ganho Não Realizado", "R$ 0,00", TemaCores.POSITIVO_LUCRO);
    private final ModernCard cardGanhoTotal = new ModernCard("Ganho Total Líquido", "R$ 0,00", TemaCores.POSITIVO_LUCRO);
    private final ModernCard cardRoi = new ModernCard("Retorno (ROI)", "0,00%", TemaCores.POSITIVO_LUCRO);

    // Componentes do Relatório 2 (Histórico)
    private final DefaultTableModel modelRel2;
    private final JTable tabelaRel2;

    // Componentes do Relatório 3 (Consolidado de Todas as Carteiras)
    private final DefaultTableModel modelRel3;
    private final JTable tabelaRel3;

    // Componentes do Relatório 4 (Desempenho e Lucratividade)
    private final DefaultTableModel modelRel4;
    private final JTable tabelaRel4;

    // Componentes do Relatório 5 (Histórico do Oráculo)
    private final DefaultTableModel modelRel5;
    private final JTable tabelaRel5;

    private record ItemFiltroCarteira(int id, String descricao) {
        @Override
        public String toString() {
            return descricao;
        }
    }

    public RelatoriosPanel(RelatoriosService relatoriosService) {
        this.presenter = new RelatoriosPresenter(this, relatoriosService);

        setLayout(new BorderLayout(15, 15));
        setBackground(TemaCores.FUNDO_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Topo com Título e Seletor de Carteira para Relatórios 1 e 2
        JPanel pnlTopo = new JPanel(new BorderLayout(10, 10));
        pnlTopo.setOpaque(false);

        JLabel lblTitulo = new JLabel("Painel Analítico de Relatórios Financeiros (5 Relatórios)");
        lblTitulo.setFont(TemaCores.FONTE_TITULO_GRANDE);
        lblTitulo.setForeground(TemaCores.TEXTO_PRINCIPAL);
        pnlTopo.add(lblTitulo, BorderLayout.NORTH);

        JPanel pnlFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlFiltro.setOpaque(false);

        JLabel lblFiltrar = new JLabel("Selecionar Carteira para Análise:");
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

        ModernButton btnRecarregar = new ModernButton("Atualizar Todos", ModernButton.Variante.NEUTRO);
        btnRecarregar.addActionListener(e -> presenter.inicializar());
        pnlFiltro.add(btnRecarregar);

        pnlTopo.add(pnlFiltro, BorderLayout.SOUTH);
        add(pnlTopo, BorderLayout.NORTH);

        // Abas para os 5 relatórios
        tabRelatorios.setFont(TemaCores.FONTE_LABEL);

        // Modelos e Tabelas
        GuiFormatador fmt = AppContext.getInstancia().getMensagens().getFormatador();

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
        tabRelatorios.addTab("  1. Posição Consolidada (CMP)  ", pnlRel1);

        // 2. Histórico da Carteira
        String[] colRel2 = {"Data", "Operação", "Quantidade", "Cotação Aplicada", "Valor Total"};
        modelRel2 = new DefaultTableModel(colRel2, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaRel2 = criarTabelaFormatada(modelRel2);
        tabelaRel2.getColumnModel().getColumn(1).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.TIPO_OPERACAO, fmt));
        tabelaRel2.getColumnModel().getColumn(2).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.QUANTIDADE, fmt));
        tabelaRel2.getColumnModel().getColumn(3).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, fmt));
        tabelaRel2.getColumnModel().getColumn(4).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, fmt));
        tabRelatorios.addTab("  2. Histórico de Operações  ", new JScrollPane(tabelaRel2));

        // 3. Consolidado de Todas as Carteiras
        String[] colRel3 = {"ID", "Titular", "Corretora", "Saldo FTC", "Valor de Mercado", "% Portfólio", "Ganho Total Líquido"};
        modelRel3 = new DefaultTableModel(colRel3, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaRel3 = criarTabelaFormatada(modelRel3);
        tabelaRel3.getColumnModel().getColumn(3).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.QUANTIDADE, fmt));
        tabelaRel3.getColumnModel().getColumn(4).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, fmt));
        tabelaRel3.getColumnModel().getColumn(5).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.PERCENTUAL_COM_COR, fmt));
        tabelaRel3.getColumnModel().getColumn(6).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA_COM_COR, fmt));
        tabRelatorios.addTab("  3. Consolidado do Portfólio  ", new JScrollPane(tabelaRel3));

        // 4. Desempenho e Lucratividade
        String[] colRel4 = {"ID", "Titular", "Corretora", "Montante Investido", "Valor Atual", "Lucro/Prejuízo Total", "ROI (%)"};
        modelRel4 = new DefaultTableModel(colRel4, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaRel4 = criarTabelaFormatada(modelRel4);
        tabelaRel4.getColumnModel().getColumn(3).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, fmt));
        tabelaRel4.getColumnModel().getColumn(4).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, fmt));
        tabelaRel4.getColumnModel().getColumn(5).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA_COM_COR, fmt));
        tabelaRel4.getColumnModel().getColumn(6).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.PERCENTUAL_COM_COR, fmt));
        tabRelatorios.addTab("  4. Desempenho & Lucratividade  ", new JScrollPane(tabelaRel4));

        // 5. Histórico do Oráculo
        String[] colRel5 = {"Data", "Cotação FTC", "Variação Diária (R$)", "Variação Diária (%)", "Fonte de Dados"};
        modelRel5 = new DefaultTableModel(colRel5, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaRel5 = criarTabelaFormatada(modelRel5);
        tabelaRel5.getColumnModel().getColumn(1).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, fmt));
        tabelaRel5.getColumnModel().getColumn(2).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA_COM_COR, fmt));
        tabelaRel5.getColumnModel().getColumn(3).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.PERCENTUAL_COM_COR, fmt));
        tabelaRel5.getColumnModel().getColumn(4).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.ORIGEM_BADGE, fmt));
        tabRelatorios.addTab("  5. Histórico do Oráculo  ", new JScrollPane(tabelaRel5));

        add(tabRelatorios, BorderLayout.CENTER);
    }

    private JTable criarTabelaFormatada(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(TemaCores.FONTE_PADRAO);
        table.getTableHeader().setFont(TemaCores.FONTE_LABEL);
        table.getTableHeader().setBackground(TemaCores.SUPERFICIE_HEADER);
        return table;
    }

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
    public void exibirCarteirasFiltro(List<CarteiraDTO> carteiras) {
        carregandoFiltro = true;
        try {
            cmbCarteiraFiltro.removeAllItems();
            for (CarteiraDTO c : carteiras) {
                cmbCarteiraFiltro.addItem(new ItemFiltroCarteira(
                        c.id(),
                        "#" + c.id() + " - " + c.nomeTitular() + " (" + c.corretora() + ")"
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
        cardSaldo.setValor("0.000 FTC", TemaCores.PRIMARIA);
        cardCustoMedio.setValor("R$ 0,00", TemaCores.TEXTO_PRINCIPAL);
        cardValorCusto.setValor("R$ 0,00", TemaCores.TEXTO_PRINCIPAL);
        cardValorMercado.setValor("R$ 0,00", TemaCores.SECUNDARIA);
        cardGanhoRealizado.setValor("R$ 0,00", TemaCores.POSITIVO_LUCRO);
        cardGanhoNaoRealizado.setValor("R$ 0,00", TemaCores.POSITIVO_LUCRO);
        cardGanhoTotal.setValor("R$ 0,00", TemaCores.POSITIVO_LUCRO);
        cardRoi.setValor("0,00%", TemaCores.POSITIVO_LUCRO);
        modelRel2.setRowCount(0);
    }

    @Override
    public void exibirRelatorio1Posicao(PosicaoRelatorioDTO posicao) {
        if (posicao == null) {
            limparRelatoriosIndividuais();
            return;
        }
        GuiFormatador fmt = AppContext.getInstancia().getMensagens().getFormatador();

        cardSaldo.setValor(fmt.quantidade(posicao.saldo()), TemaCores.PRIMARIA);
        cardCustoMedio.setValor(fmt.moeda(posicao.custoMedio()), TemaCores.TEXTO_PRINCIPAL);
        cardValorCusto.setValor(fmt.moeda(posicao.valorACusto()), TemaCores.TEXTO_PRINCIPAL);
        cardValorMercado.setValor(fmt.moeda(posicao.valorDeMercado()), TemaCores.SECUNDARIA);

        // Cores estratégicas: Verde para positivo/lucro, Vermelho para negativo/prejuízo
        cardGanhoRealizado.setValor(
                fmt.moeda(posicao.ganhoRealizado()),
                posicao.ganhoRealizado().signum() >= 0 ? TemaCores.POSITIVO_LUCRO : TemaCores.NEGATIVO_PREJUIZO
        );
        cardGanhoNaoRealizado.setValor(
                fmt.moeda(posicao.ganhoNaoRealizado()),
                posicao.ganhoNaoRealizado().signum() >= 0 ? TemaCores.POSITIVO_LUCRO : TemaCores.NEGATIVO_PREJUIZO
        );
        cardGanhoTotal.setValor(
                fmt.moeda(posicao.ganhoTotal()),
                posicao.ganhoTotal().signum() >= 0 ? TemaCores.POSITIVO_LUCRO : TemaCores.NEGATIVO_PREJUIZO
        );
        cardRoi.setValor(
                fmt.percentual(posicao.roiPercentual()),
                posicao.roiPercentual().signum() >= 0 ? TemaCores.POSITIVO_LUCRO : TemaCores.NEGATIVO_PREJUIZO
        );
    }

    @Override
    public void exibirRelatorio2Historico(List<MovimentacaoDTO> historico) {
        modelRel2.setRowCount(0);
        GuiFormatador fmt = AppContext.getInstancia().getMensagens().getFormatador();
        for (MovimentacaoDTO m : historico) {
            modelRel2.addRow(new Object[]{
                    fmt.data(m.dataOperacao()),
                    m.tipoOperacao(),
                    m.quantidade(),
                    m.cotacaoAplicada(),
                    m.valorTotalOperacao()
            });
        }
    }

    @Override
    public void exibirRelatorio3Consolidado(List<RelatorioConsolidadoItemDTO> consolidado) {
        modelRel3.setRowCount(0);
        for (RelatorioConsolidadoItemDTO item : consolidado) {
            modelRel3.addRow(new Object[]{
                    item.idCarteira(),
                    item.titular(),
                    item.corretora(),
                    item.saldo(),
                    item.valorMercado(),
                    item.participacaoPerc(),
                    item.ganhoTotal()
            });
        }
    }

    @Override
    public void exibirRelatorio4Desempenho(List<RelatorioDesempenhoDTO> desempenho) {
        modelRel4.setRowCount(0);
        for (RelatorioDesempenhoDTO d : desempenho) {
            modelRel4.addRow(new Object[]{
                    d.idCarteira(),
                    d.titular(),
                    d.corretora(),
                    d.valorInvestido(),
                    d.valorAtual(),
                    d.lucroPrejuizo(),
                    d.roiPercentual()
            });
        }
    }

    @Override
    public void exibirRelatorio5Cotacoes(List<RelatorioHistoricoCotacaoDTO> cotacoes) {
        modelRel5.setRowCount(0);
        GuiFormatador fmt = AppContext.getInstancia().getMensagens().getFormatador();
        for (RelatorioHistoricoCotacaoDTO c : cotacoes) {
            modelRel5.addRow(new Object[]{
                    fmt.data(c.data()),
                    c.valorCotacao(),
                    c.variacaoAbsoluta(),
                    c.variacaoPercentual(),
                    c.origem()
            });
        }
    }

    @Override
    public void exibirMensagemErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Erro no Relatório", JOptionPane.ERROR_MESSAGE);
    }
}
