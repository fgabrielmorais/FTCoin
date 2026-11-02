package br.com.ftcoin.gui.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import br.com.ftcoin.gui.common.AppContext;
import br.com.ftcoin.gui.common.GuiFormatador;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.TemaCores;
import br.com.ftcoin.gui.dto.CarteiraDTO;
import br.com.ftcoin.gui.dto.MovimentacaoDTO;
import br.com.ftcoin.gui.presenters.MovimentacaoPresenter;
import br.com.ftcoin.gui.services.CotacaoCacheService;
import br.com.ftcoin.gui.services.RelatoriosService;
import br.com.ftcoin.gui.views.components.CurrencyTableCellRenderer;
import br.com.ftcoin.gui.views.components.ModernButton;
import br.com.ftcoin.gui.views.contracts.IMovimentacaoView;
import br.com.ftcoin.models.TipoOperacao;

/**
 * <p>
 * Visão gráfica do Módulo de Movimentações no padrão arquitetural <b>MVP</b>.
 * </p>
 *
 * <p>
 * Permite registrar operações de Compra e Venda de FTCoins através de formulários dedicados com cálculo
 * prévio de estimativa financeira em tempo real e visualização tabular do histórico geral de transações.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class MovimentacoesPanel extends JPanel implements IMovimentacaoView {

    /**
     * Apresentador responsável pelas regras de apresentação do módulo.
     */
    private final MovimentacaoPresenter presenter;

    /**
     * Caixa de seleção de carteiras para ordens de compra.
     */
    private final JComboBox<ItemCarteiraCombo> comboCarteiraCompra = new JComboBox<>();

    /**
     * Caixa de seleção de carteiras para ordens de venda.
     */
    private final JComboBox<ItemCarteiraCombo> comboCarteiraVenda = new JComboBox<>();

    /**
     * Campo de texto para data da operação de compra.
     */
    private final JTextField campoDataCompra = new JTextField(LocalDate.now().toString(), 12);

    /**
     * Campo de texto para data da operação de venda.
     */
    private final JTextField campoDataVenda = new JTextField(LocalDate.now().toString(), 12);

    /**
     * Campo de texto para quantidade de moedas a comprar.
     */
    private final JTextField campoQuantidadeCompra = new JTextField("10.000", 12);

    /**
     * Campo de texto para quantidade de moedas a vender.
     */
    private final JTextField campoQuantidadeVenda = new JTextField("5.000", 12);

    /**
     * Rótulo de exibição da cotação unitária estimada para compra.
     */
    private final JLabel rotuloCotacaoEstimadaCompra = new JLabel("-");

    /**
     * Rótulo de destaque para o montante total financeiro estimado na compra.
     */
    private final JLabel rotuloTotalEstimadoCompra = new JLabel("-");

    /**
     * Rótulo com destaque positivo exibindo o saldo disponível para venda.
     */
    private final JLabel rotuloSaldoDisponivelVenda = new JLabel("-");

    /**
     * Rótulo de exibição da cotação unitária estimada para venda.
     */
    private final JLabel rotuloCotacaoEstimadaVenda = new JLabel("-");

    /**
     * Rótulo de destaque para o montante total financeiro estimado na venda.
     */
    private final JLabel rotuloTotalEstimadoVenda = new JLabel("-");

    /**
     * Modelo de tabela para o histórico de operações.
     */
    private final DefaultTableModel modeloTabelaHistorico;

    /**
     * Tabela visual para renderização do histórico de operações.
     */
    private final JTable tabelaHistoricoOperacoes;

    /**
     * <p>
     * Registro imutável auxiliar para renderização e retenção de estado de carteiras no {@link JComboBox}.
     * </p>
     *
     * @param id        Identificador numérico da carteira
     * @param descricao Texto formatado para exibição visual
     * @param saldo     Saldo quantitativo de moedas disponível
     * @author Equipe FTCoin
     * @version 1.0.0
     * @since 2026-10-20
     */
    private record ItemCarteiraCombo(int id, String descricao, BigDecimal saldo) {

        /**
         * Retorna a descrição formatada para exibição no componente visual.
         *
         * @return O texto descritivo {@code String}
         */
        @Override
        public String toString() {
            return descricao;
        }
    }

    /**
     * Constrói o painel de movimentações inicializando formulários e o apresentador.
     *
     * @param relatoriosService Serviço analítico de relatórios e carteiras
     * @param cotacaoService   Serviço de cotações para estimativas imediatas
     */
    public MovimentacoesPanel(RelatoriosService relatoriosService, CotacaoCacheService cotacaoService) {
        this.presenter = new MovimentacaoPresenter(this, relatoriosService, cotacaoService);
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        setLayout(new BorderLayout(15, 15));
        setBackground(TemaCores.FUNDO_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título Superior
        JLabel rotuloTituloSuperior = new JLabel(mensagens.get("gui.mov.tituloSuperior"));
        rotuloTituloSuperior.setFont(TemaCores.FONTE_TITULO_GRANDE);
        rotuloTituloSuperior.setForeground(TemaCores.TEXTO_PRINCIPAL);
        add(rotuloTituloSuperior, BorderLayout.NORTH);

        // Painel Dividido: Abas de Operação (Compra e Venda) no Topo e Histórico no Centro
        JPanel painelConteudoCentral = new JPanel(new BorderLayout(15, 15));
        painelConteudoCentral.setOpaque(false);

        JTabbedPane abasOperacoes = new JTabbedPane();
        abasOperacoes.setFont(TemaCores.FONTE_LABEL);
        abasOperacoes.addTab(mensagens.get("gui.mov.tab.compra"), criarPainelFormulario(TipoOperacao.COMPRA));
        abasOperacoes.addTab(mensagens.get("gui.mov.tab.venda"), criarPainelFormulario(TipoOperacao.VENDA));

        painelConteudoCentral.add(abasOperacoes, BorderLayout.NORTH);

        // Painel de Histórico Recente de Operações
        JPanel painelHistorico = new JPanel(new BorderLayout(8, 8));
        painelHistorico.setOpaque(false);

        JLabel rotuloHistoricoTitulo = new JLabel(mensagens.get("gui.mov.historico.titulo"));
        rotuloHistoricoTitulo.setFont(TemaCores.FONTE_TITULO_MEDIO);
        rotuloHistoricoTitulo.setForeground(TemaCores.TEXTO_PRINCIPAL);
        painelHistorico.add(rotuloHistoricoTitulo, BorderLayout.NORTH);

        String[] colunas = {
                mensagens.get("gui.dash.feed.col.data"),
                mensagens.get("gui.dash.feed.col.carteira"),
                mensagens.get("gui.dash.feed.col.tipo"),
                mensagens.get("gui.dash.feed.col.quantidade"),
                mensagens.get("gui.dash.feed.col.cotacao"),
                mensagens.get("gui.dash.feed.col.total")
        };

        modeloTabelaHistorico = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tabelaHistoricoOperacoes = new JTable(modeloTabelaHistorico);
        tabelaHistoricoOperacoes.setRowHeight(32);
        tabelaHistoricoOperacoes.setFont(TemaCores.FONTE_PADRAO);
        tabelaHistoricoOperacoes.getTableHeader().setFont(TemaCores.FONTE_LABEL);
        tabelaHistoricoOperacoes.getTableHeader().setBackground(TemaCores.SUPERFICIE_HEADER);

        GuiFormatador formatador = mensagens.getFormatador();
        tabelaHistoricoOperacoes.getColumnModel().getColumn(2).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.TIPO_OPERACAO, formatador));
        tabelaHistoricoOperacoes.getColumnModel().getColumn(3).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.QUANTIDADE, formatador));
        tabelaHistoricoOperacoes.getColumnModel().getColumn(4).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, formatador));
        tabelaHistoricoOperacoes.getColumnModel().getColumn(5).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, formatador));

        JScrollPane painelRolagemHistorico = new JScrollPane(tabelaHistoricoOperacoes);
        painelRolagemHistorico.setBorder(BorderFactory.createLineBorder(TemaCores.BORDA_SUAVE));
        painelRolagemHistorico.setPreferredSize(new Dimension(800, 220));
        painelHistorico.add(painelRolagemHistorico, BorderLayout.CENTER);

        painelConteudoCentral.add(painelHistorico, BorderLayout.CENTER);
        add(painelConteudoCentral, BorderLayout.CENTER);

        // Configuração dos ouvintes de recálculo dinâmico
        configurarListenersRecalculo();
    }

    /**
     * Constrói o formulário de entrada para a operação de compra ou venda especificada.
     *
     * @param tipo Classificação da operação ({@code TipoOperacao.COMPRA} ou {@code TipoOperacao.VENDA})
     * @return O painel {@link JPanel} configurado
     */
    private JPanel criarPainelFormulario(TipoOperacao tipo) {
        boolean ehCompra = (tipo == TipoOperacao.COMPRA);
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        JPanel painelForm = new JPanel(new GridBagLayout());
        painelForm.setBackground(TemaCores.SUPERFICIE_CARD);
        painelForm.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        GridBagConstraints restricoes = new GridBagConstraints();
        restricoes.insets = new Insets(8, 8, 8, 8);
        restricoes.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<ItemCarteiraCombo> comboCarteira = ehCompra ? comboCarteiraCompra : comboCarteiraVenda;
        JTextField campoData = ehCompra ? campoDataCompra : campoDataVenda;
        JTextField campoQuantidade = ehCompra ? campoQuantidadeCompra : campoQuantidadeVenda;
        JLabel rotuloCotacao = ehCompra ? rotuloCotacaoEstimadaCompra : rotuloCotacaoEstimadaVenda;
        JLabel rotuloTotal = ehCompra ? rotuloTotalEstimadoCompra : rotuloTotalEstimadoVenda;

        rotuloCotacao.setFont(TemaCores.FONTE_LABEL);
        rotuloTotal.setFont(TemaCores.FONTE_DESTAQUE_NUMERO);
        rotuloTotal.setForeground(TemaCores.SECUNDARIA);

        // Linha 0: Seleção de Carteira
        restricoes.gridx = 0; restricoes.gridy = 0;
        painelForm.add(new JLabel(mensagens.get("gui.mov.label.carteiraDestinoOrigem")), restricoes);
        restricoes.gridx = 1; restricoes.gridy = 0; restricoes.gridwidth = 2;
        painelForm.add(comboCarteira, restricoes);
        restricoes.gridwidth = 1;

        // Se for Venda, exibe saldo disponível para alienação
        if (!ehCompra) {
            restricoes.gridx = 3; restricoes.gridy = 0;
            painelForm.add(new JLabel(mensagens.get("gui.mov.label.saldoDisponivel")), restricoes);
            restricoes.gridx = 4; restricoes.gridy = 0;
            rotuloSaldoDisponivelVenda.setFont(TemaCores.FONTE_LABEL);
            rotuloSaldoDisponivelVenda.setForeground(TemaCores.POSITIVO_LUCRO);
            painelForm.add(rotuloSaldoDisponivelVenda, restricoes);
        }

        // Linha 1: Data e Quantidade
        restricoes.gridx = 0; restricoes.gridy = 1;
        painelForm.add(new JLabel(mensagens.get("gui.mov.label.dataOperacao")), restricoes);
        restricoes.gridx = 1; restricoes.gridy = 1;
        painelForm.add(campoData, restricoes);

        restricoes.gridx = 2; restricoes.gridy = 1;
        painelForm.add(new JLabel(mensagens.get("gui.mov.label.quantidadeFTC")), restricoes);
        restricoes.gridx = 3; restricoes.gridy = 1;
        painelForm.add(campoQuantidade, restricoes);

        // Linha 2: Cotação e Total Estimado
        restricoes.gridx = 0; restricoes.gridy = 2;
        painelForm.add(new JLabel(mensagens.get("gui.mov.label.cotacaoAplicada")), restricoes);
        restricoes.gridx = 1; restricoes.gridy = 2;
        painelForm.add(rotuloCotacao, restricoes);

        restricoes.gridx = 2; restricoes.gridy = 2;
        painelForm.add(new JLabel(mensagens.get("gui.mov.label.totalEstimado")), restricoes);
        restricoes.gridx = 3; restricoes.gridy = 2;
        painelForm.add(rotuloTotal, restricoes);

        // Linha 3: Botão de Confirmação da Ordem
        restricoes.gridx = 3; restricoes.gridy = 3;
        String textoBotao = ehCompra ? mensagens.get("gui.mov.btn.confirmarCompra") : mensagens.get("gui.mov.btn.confirmarVenda");
        ModernButton botaoConfirmar = new ModernButton(
                textoBotao,
                ehCompra ? ModernButton.Variante.SUCESSO : ModernButton.Variante.PRIMARIA
        );
        botaoConfirmar.setPreferredSize(new Dimension(230, 38));
        botaoConfirmar.addActionListener(e -> {
            ItemCarteiraCombo item = (ItemCarteiraCombo) comboCarteira.getSelectedItem();
            if (item == null) {
                exibirMensagemErro(mensagens.get("gui.mov.erro.carteiraObrigatoria"));
                return;
            }
            LocalDate dataOperacao;
            try {
                dataOperacao = LocalDate.parse(campoData.getText().trim());
            } catch (DateTimeParseException ex) {
                exibirMensagemErro(mensagens.get("gui.mov.erro.dataInvalida"));
                return;
            }

            presenter.registrarOperacao(item.id(), tipo, dataOperacao, campoQuantidade.getText());
        });

        painelForm.add(botaoConfirmar, restricoes);
        return painelForm;
    }

    /**
     * Configura ouvintes de alteração nos campos de texto para recálculo automático de totais.
     */
    private void configurarListenersRecalculo() {
        DocumentListener ouvinteDocCompra = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalcular(); }
            @Override public void removeUpdate(DocumentEvent e) { recalcular(); }
            @Override public void changedUpdate(DocumentEvent e) { recalcular(); }

            private void recalcular() {
                LocalDate data = converterTextoEmData(campoDataCompra.getText());
                presenter.calcularEstimativas(data, campoQuantidadeCompra.getText());
            }
        };
        campoQuantidadeCompra.getDocument().addDocumentListener(ouvinteDocCompra);

        DocumentListener ouvinteDocVenda = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalcular(); }
            @Override public void removeUpdate(DocumentEvent e) { recalcular(); }
            @Override public void changedUpdate(DocumentEvent e) { recalcular(); }

            private void recalcular() {
                LocalDate data = converterTextoEmData(campoDataVenda.getText());
                presenter.calcularEstimativas(data, campoQuantidadeVenda.getText());
            }
        };
        campoQuantidadeVenda.getDocument().addDocumentListener(ouvinteDocVenda);

        comboCarteiraVenda.addActionListener(e -> {
            ItemCarteiraCombo selecionado = (ItemCarteiraCombo) comboCarteiraVenda.getSelectedItem();
            if (selecionado != null) {
                GuiFormatador formatador = AppContext.getInstancia().getMensagens().getFormatador();
                rotuloSaldoDisponivelVenda.setText(formatador.quantidade(selecionado.saldo()));
            }
        });
    }

    /**
     * Converte o texto fornecido em data cronológica segura com fallback para hoje.
     *
     * @param texto String com a data digitada
     * @return A data {@link LocalDate} resultante
     */
    private LocalDate converterTextoEmData(String texto) {
        try {
            return LocalDate.parse(texto.trim());
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    /**
     * Inicializa a tela disparando a carga de carteiras e a estimativa inicial.
     */
    public void inicializar() {
        presenter.inicializar();
        presenter.calcularEstimativas(LocalDate.now(), campoQuantidadeCompra.getText());
    }

    /**
     * Atualiza as caixas de seleção de carteiras disponíveis para compra e venda.
     *
     * @param carteiras Lista contendo as carteiras ativas
     */
    @Override
    public void exibirCarteirasDisponiveis(List<CarteiraDTO> carteiras) {
        comboCarteiraCompra.removeAllItems();
        comboCarteiraVenda.removeAllItems();

        GuiFormatador formatador = AppContext.getInstancia().getMensagens().getFormatador();
        for (CarteiraDTO carteira : carteiras) {
            String rotulo = "#" + carteira.id() + " - " + carteira.nomeTitular() + " (" + carteira.corretora() + ")";
            ItemCarteiraCombo item = new ItemCarteiraCombo(carteira.id(), rotulo, carteira.saldo());
            comboCarteiraCompra.addItem(item);
            comboCarteiraVenda.addItem(item);
        }

        if (comboCarteiraVenda.getItemCount() > 0) {
            ItemCarteiraCombo primeiro = comboCarteiraVenda.getItemAt(0);
            rotuloSaldoDisponivelVenda.setText(formatador.quantidade(primeiro.saldo()));
        }
    }

    /**
     * Renderiza o histórico de transações na tabela visual da tela.
     *
     * @param movimentacoes Lista com os registros recentes
     */
    @Override
    public void exibirHistoricoMovimentacoes(List<MovimentacaoDTO> movimentacoes) {
        modeloTabelaHistorico.setRowCount(0);
        GuiFormatador formatador = AppContext.getInstancia().getMensagens().getFormatador();
        for (MovimentacaoDTO movimentacao : movimentacoes) {
            modeloTabelaHistorico.addRow(new Object[]{
                    formatador.data(movimentacao.dataOperacao()),
                    "#" + movimentacao.idCarteira() + " " + movimentacao.nomeTitular(),
                    movimentacao.tipoOperacao(),
                    movimentacao.quantidade(),
                    movimentacao.cotacaoAplicada(),
                    movimentacao.valorTotalOperacao()
            });
        }
    }

    /**
     * Atualiza os rótulos de prévia financeira com os valores estimados calculados.
     *
     * @param cotacao    Valor da cotação
     * @param valorTotal Valor financeiro total calculado
     */
    @Override
    public void atualizarEstimativas(BigDecimal cotacao, BigDecimal valorTotal) {
        GuiFormatador formatador = AppContext.getInstancia().getMensagens().getFormatador();
        rotuloCotacaoEstimadaCompra.setText(formatador.moeda(cotacao));
        rotuloTotalEstimadoCompra.setText(formatador.moeda(valorTotal));
        rotuloCotacaoEstimadaVenda.setText(formatador.moeda(cotacao));
        rotuloTotalEstimadoVenda.setText(formatador.moeda(valorTotal));
    }

    /**
     * Exibe um diálogo modal com mensagem de confirmação da transação.
     *
     * @param mensagem Texto com os detalhes do sucesso
     */
    @Override
    public void exibirMensagemSucesso(String mensagem) {
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();
        JOptionPane.showMessageDialog(this, mensagem, mensagens.get("gui.comum.sucesso"), JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Exibe um diálogo modal com alerta de inconsistência ou falha na validação.
     *
     * @param mensagem Texto explicativo da falha ocorrida
     */
    @Override
    public void exibirMensagemErro(String mensagem) {
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();
        JOptionPane.showMessageDialog(this, mensagem, mensagens.get("gui.mov.avisoErroTitulo"), JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Restaura os campos de quantidade dos formulários para seus valores padrão.
     */
    @Override
    public void limparFormulario() {
        campoQuantidadeCompra.setText("10.000");
        campoQuantidadeVenda.setText("5.000");
    }
}
