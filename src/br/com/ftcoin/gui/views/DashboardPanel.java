package br.com.ftcoin.gui.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import br.com.ftcoin.gui.common.ContextoAplicacao;
import br.com.ftcoin.gui.common.GuiFormatador;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.ModoNavegacao;
import br.com.ftcoin.gui.common.TemaCores;
import br.com.ftcoin.gui.common.TextosTraduzidos;
import br.com.ftcoin.gui.presenters.DashboardPresenter;
import br.com.ftcoin.gui.views.components.CurrencyTableCellRenderer;
import br.com.ftcoin.gui.views.components.ModernButton;
import br.com.ftcoin.gui.views.components.ModernCard;
import br.com.ftcoin.gui.views.contracts.IDashboardView;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.Posicao;

/**
 * <p>
 * Visão gráfica do Dashboard Principal no padrão arquitetural <b>MVP</b>.
 * </p>
 *
 * <p>
 * Apresenta a visão geral consolidada da carteira global com cartões de indicadores (KPIs),
 * atalhos rápidos para execução de ordens e feed das operações registradas recentemente.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class DashboardPanel extends JPanel implements IDashboardView {

    private static final long serialVersionUID = 1L;

    /**
     * Apresentador responsável pelas regras de apresentação do dashboard.
     */
    private final transient DashboardPresenter presenter;

    /**
     * Função consumidora para solicitação de transição de abas na janela principal.
     */
    private final transient Consumer<String> consumidorNavegacao;

    /**
     * Cartão de KPI para exibição do patrimônio financeiro total consolidado.
     */
    private final ModernCard cardPatrimonioTotal;

    /**
     * Cartão de KPI para exibição do saldo total de moedas em custódia.
     */
    private final ModernCard cardSaldoConsolidado;

    /**
     * Cartão de KPI para exibição da cotação unitária do dia.
     */
    private final ModernCard cardCotacaoHoje;

    /**
     * Cartão de KPI para contagem de carteiras ativas no sistema.
     */
    private final ModernCard cardCarteirasAtivas;

    /**
     * Cartão de KPI para exibição do lucro ou prejuízo líquido acumulado.
     */
    private final ModernCard cardResultadoTotal;

    /**
     * Modelo de tabela para o feed de últimas movimentações financeiras.
     */
    private final DefaultTableModel modeloTabelaFeed;

    /**
     * Tabela visual para renderização do feed de últimas movimentações.
     */
    private final JTable tabelaFeedOperacoes;

    /**
     * Rótulo de título principal do painel do dashboard.
     */
    private final JLabel rotuloTitulo;

    /**
     * Botão de atalho rápido para criação de nova carteira.
     */
    private final ModernButton botaoNovaCarteira;

    /**
     * Botão de atalho rápido para aquisição de FTCoins.
     */
    private final ModernButton botaoComprarMoedas;

    /**
     * Botão de atalho rápido para alienação de FTCoins.
     */
    private final ModernButton botaoVenderMoedas;

    /**
     * Botão de atalho rápido para navegação ao módulo de relatórios.
     */
    private final ModernButton botaoAbrirRelatorios;

    /**
     * Rótulo descritivo do cabeçalho da tabela de transações recentes.
     */
    private final JLabel rotuloFeedTitulo;

    private final transient ContextoAplicacao contexto;
    private final transient TextosTraduzidos textos;

    /**
     * Constrói o painel de dashboard inicializando cartões, tabelas e botões de atalho.
     *
     * @param consumidorNavegacao Ação consumidora para troca de telas na janela principal
     * @param contexto contexto da aplicação, com os serviços e as mensagens ativos
     */
    public DashboardPanel(Consumer<String> consumidorNavegacao, ContextoAplicacao contexto) {
        this.contexto = contexto;
        this.presenter = new DashboardPresenter(this, contexto);
        this.consumidorNavegacao = consumidorNavegacao;
        MensagensGui mensagens = contexto.getMensagens();
        this.textos = new TextosTraduzidos(mensagens);

        setLayout(new BorderLayout(15, 15));
        setBackground(TemaCores.FUNDO_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Inicialização dos cartões com textos internacionalizados
        GuiFormatador formatadorInicial = mensagens.getFormatador();
        cardPatrimonioTotal = new ModernCard(null, formatadorInicial.moeda(BigDecimal.ZERO), TemaCores.PRIMARIA);
        textos.vincular(cardPatrimonioTotal::setTitulo, "gui.dash.card.patrimonio");
        cardSaldoConsolidado = new ModernCard(null, formatadorInicial.quantidade(BigDecimal.ZERO), TemaCores.TEXTO_PRINCIPAL);
        textos.vincular(cardSaldoConsolidado::setTitulo, "gui.dash.card.saldoConsolidado");
        cardCotacaoHoje = new ModernCard(null, formatadorInicial.moeda(BigDecimal.ZERO), TemaCores.SECUNDARIA);
        textos.vincular(cardCotacaoHoje::setTitulo, "gui.dash.card.cotacaoHoje");
        cardCarteirasAtivas = new ModernCard(null, "0", TemaCores.ACCENT);
        textos.vincular(cardCarteirasAtivas::setTitulo, "gui.dash.card.carteirasAtivas");
        cardResultadoTotal = new ModernCard(null, formatadorInicial.moeda(BigDecimal.ZERO), TemaCores.POSITIVO_LUCRO);
        textos.vincular(cardResultadoTotal::setTitulo, "gui.dash.card.resultadoTotal");

        // Topo: Título da tela e Ações Rápidas
        JPanel painelTopo = new JPanel(new BorderLayout());
        painelTopo.setOpaque(false);

        rotuloTitulo = new JLabel();
        textos.vincular(rotuloTitulo::setText, "gui.dash.titulo");
        rotuloTitulo.setFont(TemaCores.FONTE_TITULO_GRANDE);
        rotuloTitulo.setForeground(TemaCores.TEXTO_PRINCIPAL);
        painelTopo.add(rotuloTitulo, BorderLayout.WEST);

        JPanel painelAcoesRapidas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        painelAcoesRapidas.setOpaque(false);

        botaoNovaCarteira = new ModernButton("", ModernButton.Variante.SUCESSO);
        textos.vincular(botaoNovaCarteira::setText, "gui.dash.btn.novaCarteira");
        botaoComprarMoedas = new ModernButton("", ModernButton.Variante.PRIMARIA);
        textos.vincular(botaoComprarMoedas::setText, "gui.dash.btn.comprarFTC");
        botaoVenderMoedas = new ModernButton("", ModernButton.Variante.NEUTRO);
        textos.vincular(botaoVenderMoedas::setText, "gui.dash.btn.venderFTC");
        botaoAbrirRelatorios = new ModernButton("", ModernButton.Variante.NEUTRO);
        textos.vincular(botaoAbrirRelatorios::setText, "gui.dash.btn.relatorios");

        botaoNovaCarteira.addActionListener(e -> navegarPara(ModoNavegacao.CARTEIRAS.getIdentificadorCard()));
        botaoComprarMoedas.addActionListener(e -> navegarPara(ModoNavegacao.MOVIMENTACOES.getIdentificadorCard()));
        botaoVenderMoedas.addActionListener(e -> navegarPara(ModoNavegacao.MOVIMENTACOES.getIdentificadorCard()));
        botaoAbrirRelatorios.addActionListener(e -> navegarPara(ModoNavegacao.RELATORIOS.getIdentificadorCard()));

        painelAcoesRapidas.add(botaoNovaCarteira);
        painelAcoesRapidas.add(botaoComprarMoedas);
        painelAcoesRapidas.add(botaoVenderMoedas);
        painelAcoesRapidas.add(botaoAbrirRelatorios);
        painelTopo.add(painelAcoesRapidas, BorderLayout.EAST);

        add(painelTopo, BorderLayout.NORTH);

        // Corpo central: Linha de cartões e Feed de operações recentes
        JPanel painelCorpoCentral = new JPanel(new BorderLayout(15, 15));
        painelCorpoCentral.setOpaque(false);

        JPanel painelGridCards = new JPanel(new GridLayout(1, 5, 12, 0));
        painelGridCards.setOpaque(false);
        painelGridCards.add(cardPatrimonioTotal);
        painelGridCards.add(cardSaldoConsolidado);
        painelGridCards.add(cardCotacaoHoje);
        painelGridCards.add(cardCarteirasAtivas);
        painelGridCards.add(cardResultadoTotal);

        painelCorpoCentral.add(painelGridCards, BorderLayout.NORTH);

        // Painel do Feed de Operações Recentes
        JPanel painelFeedRecente = new JPanel(new BorderLayout(8, 8));
        painelFeedRecente.setOpaque(false);

        rotuloFeedTitulo = new JLabel();
        textos.vincular(rotuloFeedTitulo::setText, "gui.dash.feed.titulo");
        rotuloFeedTitulo.setFont(TemaCores.FONTE_TITULO_MEDIO);
        rotuloFeedTitulo.setForeground(TemaCores.TEXTO_PRINCIPAL);
        painelFeedRecente.add(rotuloFeedTitulo, BorderLayout.NORTH);

        String[] chavesColunas = {
                "gui.dash.feed.col.data",
                "gui.dash.feed.col.carteira",
                "gui.dash.feed.col.tipo",
                "gui.dash.feed.col.quantidade",
                "gui.dash.feed.col.cotacao",
                "gui.dash.feed.col.total"
        };

        modeloTabelaFeed = new DefaultTableModel(chavesColunas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tabelaFeedOperacoes = new JTable(modeloTabelaFeed);
        textos.vincularColunas(tabelaFeedOperacoes, chavesColunas);
        tabelaFeedOperacoes.setRowHeight(32);
        tabelaFeedOperacoes.setFont(TemaCores.FONTE_PADRAO);
        tabelaFeedOperacoes.getTableHeader().setFont(TemaCores.FONTE_LABEL);
        tabelaFeedOperacoes.getTableHeader().setBackground(TemaCores.SUPERFICIE_HEADER);

        tabelaFeedOperacoes.getColumnModel().getColumn(2).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.TIPO_OPERACAO, contexto.getMensagens()));
        tabelaFeedOperacoes.getColumnModel().getColumn(3).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.QUANTIDADE, contexto.getMensagens()));
        tabelaFeedOperacoes.getColumnModel().getColumn(4).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, contexto.getMensagens()));
        tabelaFeedOperacoes.getColumnModel().getColumn(5).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, contexto.getMensagens()));

        JScrollPane painelRolagemFeed = new JScrollPane(tabelaFeedOperacoes);
        painelRolagemFeed.setBorder(BorderFactory.createLineBorder(TemaCores.BORDA_SUAVE));
        painelFeedRecente.add(painelRolagemFeed, BorderLayout.CENTER);

        painelCorpoCentral.add(painelFeedRecente, BorderLayout.CENTER);
        add(painelCorpoCentral, BorderLayout.CENTER);
    }

    /**
     * Solicita a transição para a tela identificada pelo argumento fornecido.
     *
     * @param destino Identificador de cartão da tela de destino
     */
    private void navegarPara(String destino) {
        if (consumidorNavegacao != null) {
            consumidorNavegacao.accept(destino);
        }
    }

    /**
     * Inicializa o painel solicitando os dados analíticos mais recentes ao apresentador.
     */
    public void inicializar() {
        presenter.carregarDadosDashboard();
    }

    /**
     * Atualiza os cartões de indicadores do portfólio.
     *
     * @param portfolio           soma das posições de todas as carteiras
     * @param quantidadeCarteiras número de carteiras cadastradas
     * @param cotacaoAtual        cotação de hoje
     */
    @Override
    public void exibirIndicadores(Posicao portfolio, int quantidadeCarteiras, BigDecimal cotacaoAtual) {
        GuiFormatador formatador = contexto.getMensagens().getFormatador();
        BigDecimal ganhoTotal = portfolio.calcularGanhoTotal(cotacaoAtual);

        cardPatrimonioTotal.setValor(formatador.moeda(portfolio.calcularValorDeMercado(cotacaoAtual)), TemaCores.PRIMARIA);
        cardSaldoConsolidado.setValor(formatador.quantidade(portfolio.getSaldo()), TemaCores.TEXTO_PRINCIPAL);
        cardCotacaoHoje.setValor(formatador.moeda(cotacaoAtual), TemaCores.SECUNDARIA);
        cardCarteirasAtivas.setValor(String.valueOf(quantidadeCarteiras), TemaCores.ACCENT);
        // Lucro em verde e prejuízo em vermelho
        cardResultadoTotal.setValor(formatador.moeda(ganhoTotal),
                ganhoTotal.signum() >= 0 ? TemaCores.POSITIVO_LUCRO : TemaCores.NEGATIVO_PREJUIZO);
    }

    /**
     * Preenche a tabela com as movimentações mais recentes.
     *
     * @param movimentacoes movimentações da mais recente para a mais antiga
     * @param carteiras     carteiras indexadas pelo identificador
     * @param cotacoes      cotação de cada data das movimentações
     */
    @Override
    public void exibirUltimasMovimentacoes(List<Movimentacao> movimentacoes, Map<Integer, Carteira> carteiras,
            Map<LocalDate, BigDecimal> cotacoes) {
        GuiFormatador formatador = contexto.getMensagens().getFormatador();
        modeloTabelaFeed.setRowCount(0);
        for (Movimentacao movimentacao : movimentacoes) {
            modeloTabelaFeed.addRow(linhaMovimentacao(movimentacao, carteiras, cotacoes, formatador));
        }
    }

    // Linha das tabelas de movimentações (aqui e na tela de movimentações): data, carteira, tipo, quantidade,
    // cotação e valor da operação
    static Object[] linhaMovimentacao(Movimentacao movimentacao, Map<Integer, Carteira> carteiras,
            Map<LocalDate, BigDecimal> cotacoes, GuiFormatador formatador) {
        Carteira carteira = carteiras.get(movimentacao.getIdCarteira());
        String titular = (carteira != null) ? carteira.getNomeTitular() : "";
        BigDecimal cotacao = cotacoes.get(movimentacao.getDataOperacao());
        return new Object[]{
                formatador.data(movimentacao.getDataOperacao()),
                "#" + movimentacao.getIdCarteira() + " " + titular,
                movimentacao.getTipoOperacao(),
                movimentacao.getQuantidadeMovimentada(),
                cotacao,
                movimentacao.calcularValor(cotacao)
        };
    }

    /**
     * Exibe um diálogo modal com mensagem de erro informada pelo apresentador.
     *
     * @param mensagem Texto com os detalhes da falha
     */
    @Override
    public void exibirMensagemErro(String mensagem) {
        MensagensGui mensagens = contexto.getMensagens();
        JOptionPane.showMessageDialog(this, mensagem, mensagens.get("gui.dash.erro.titulo"), JOptionPane.ERROR_MESSAGE);
    }
}
