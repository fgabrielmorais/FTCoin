package br.com.ftcoin.gui.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import br.com.ftcoin.gui.common.ContextoAplicacao;
import br.com.ftcoin.gui.common.GuiFormatador;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.OrigemCotacao;
import br.com.ftcoin.gui.common.TemaCores;
import br.com.ftcoin.gui.common.TextosTraduzidos;
import br.com.ftcoin.gui.presenters.CotacaoPresenter;
import br.com.ftcoin.gui.services.CotacaoCacheService;
import br.com.ftcoin.gui.views.components.BadgeLabel;
import br.com.ftcoin.gui.views.components.CurrencyTableCellRenderer;
import br.com.ftcoin.gui.views.components.EstiloTabela;
import br.com.ftcoin.gui.views.components.ModernButton;
import br.com.ftcoin.gui.views.contracts.ICotacaoView;
import br.com.ftcoin.models.Cotacao;

/**
 * <p>
 * Visão gráfica da Tela de Cotações de Moedas e Oráculo no padrão arquitetural <b>MVP</b>.
 * </p>
 *
 * <p>
 * Exibe cotações apuradas assincronamente via <i>Virtual Threads</i> com indicadores visuais de status,
 * etiqueta colorida ({@link BadgeLabel}) identificando a procedência (Oráculo versus Cache) e telemetria de rede.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class CotacaoPanel extends JPanel implements ICotacaoView {

    private static final long serialVersionUID = 1L;

    /** Formato do horário da consulta, igual em todos os idiomas. */
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Apresentador responsável pelas regras de apresentação da tela de cotações.
     */
    private final transient CotacaoPresenter presenter;

    /**
     * Campo de entrada de texto para a data cronológica da cotação desejada.
     */
    private final JTextField campoDataConsulta;

    /**
     * Botão para disparar a consulta de preço junto ao apresentador.
     */
    private final ModernButton botaoConsultarCotacao;

    /**
     * Botão para expurgar as entradas temporárias salvas no cache em memória.
     */
    private final ModernButton botaoLimparCacheOraculo;

    /**
     * Barra de progresso indeterminada para feedback visual durante requisições assíncronas.
     */
    private final JProgressBar barraProgressoRequisicao = new JProgressBar();

    /**
     * Rótulo de destaque tipográfico para o preço da moeda.
     */
    private final JLabel rotuloValorMoeda = new JLabel("R$ --,--");

    /**
     * Etiqueta com cantos arredondados indicando a procedência dos dados (Oráculo ou Cache).
     */
    private final BadgeLabel etiquetaOrigemDado;

    /**
     * Rótulo para telemetria de latência da requisição em milissegundos.
     */
    private final JLabel rotuloLatenciaResposta;

    /**
     * Rótulo para o carimbo de data e hora em que a consulta foi processada.
     */
    private final JLabel rotuloHorarioConsulta;

    /**
     * Modelo de tabela para a série temporal de oscilações do oráculo.
     */
    private final DefaultTableModel modeloTabelaHistorico;

    /**
     * Tabela visual para renderização das oscilações recentes.
     */
    private final JTable tabelaHistoricoOscilacoes;

    private final transient ContextoAplicacao contexto;
    private final transient TextosTraduzidos textos;

    /**
     * Constrói o painel de cotações inicializando componentes gráficos e o apresentador.
     *
     * @param cotacaoService Serviço de cache e cotações da aplicação
     * @param contexto contexto da aplicação, com os serviços e as mensagens ativos
     */
    public CotacaoPanel(CotacaoCacheService cotacaoService, ContextoAplicacao contexto) {
        this.contexto = contexto;
        this.presenter = new CotacaoPresenter(this, cotacaoService, contexto);
        MensagensGui mensagens = contexto.getMensagens();
        this.textos = new TextosTraduzidos(mensagens);

        this.campoDataConsulta = new JTextField(contexto.getMensagens().getFormatador().data(LocalDate.now()), 10);
        this.botaoConsultarCotacao = new ModernButton("", ModernButton.Variante.PRIMARIA);
        this.botaoLimparCacheOraculo = new ModernButton("", ModernButton.Variante.NEUTRO);
        textos.vincular(botaoConsultarCotacao::setText, "gui.cotacao.btn.consultar");
        textos.vincular(botaoLimparCacheOraculo::setText, "gui.cotacao.btn.limparCache");
        this.etiquetaOrigemDado = new BadgeLabel(mensagens.get("gui.cotacao.aguardando"));
        this.rotuloLatenciaResposta = new JLabel(mensagens.get("gui.cotacao.latencia", "-"));
        this.rotuloHorarioConsulta = new JLabel(mensagens.get("gui.cotacao.horario", "--:--:--"));

        setLayout(new BorderLayout(15, 15));
        setBackground(TemaCores.FUNDO_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Topo: Título da tela e Filtro de Consulta
        JPanel painelTopo = new JPanel(new BorderLayout(10, 10));
        painelTopo.setOpaque(false);

        JLabel rotuloTitulo = new JLabel();
        textos.vincular(rotuloTitulo::setText, "gui.cotacao.painel.titulo");
        rotuloTitulo.setFont(TemaCores.FONTE_TITULO_GRANDE);
        rotuloTitulo.setForeground(TemaCores.TEXTO_PRINCIPAL);
        painelTopo.add(rotuloTitulo, BorderLayout.NORTH);

        JPanel painelBarraConsulta = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        painelBarraConsulta.setOpaque(false);

        JLabel rotuloData = new JLabel();
        textos.vincular(rotuloData::setText, "gui.cotacao.label.data");
        rotuloData.setFont(TemaCores.FONTE_LABEL);
        rotuloData.setForeground(TemaCores.TEXTO_SECUNDARIO);
        painelBarraConsulta.add(rotuloData);

        campoDataConsulta.setFont(TemaCores.FONTE_PADRAO);
        campoDataConsulta.setPreferredSize(new Dimension(130, 34));
        painelBarraConsulta.add(campoDataConsulta);

        botaoConsultarCotacao.addActionListener(e -> executarConsultaCotacao(false));
        botaoLimparCacheOraculo.addActionListener(e -> presenter.limparCache());

        painelBarraConsulta.add(botaoConsultarCotacao);
        painelBarraConsulta.add(botaoLimparCacheOraculo);

        barraProgressoRequisicao.setIndeterminate(true);
        barraProgressoRequisicao.setVisible(false);
        barraProgressoRequisicao.setPreferredSize(new Dimension(100, 20));
        painelBarraConsulta.add(barraProgressoRequisicao);

        painelTopo.add(painelBarraConsulta, BorderLayout.SOUTH);
        add(painelTopo, BorderLayout.NORTH);

        // Centro: Card Principal com Badge e Indicadores
        JPanel painelCentro = new JPanel(new BorderLayout(15, 15));
        painelCentro.setOpaque(false);

        JPanel cardCotacaoDestaque = new JPanel(new GridBagLayout());
        cardCotacaoDestaque.setBackground(TemaCores.SUPERFICIE_CARD);
        cardCotacaoDestaque.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaCores.BORDA_SUAVE, 1),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));

        GridBagConstraints restricoes = new GridBagConstraints();
        restricoes.insets = new Insets(6, 10, 6, 10);
        restricoes.gridx = 0;

        JLabel rotuloFTC = new JLabel();
        textos.vincular(rotuloFTC::setText, "gui.cotacao.rotuloFTC");
        rotuloFTC.setFont(TemaCores.FONTE_LABEL);
        rotuloFTC.setForeground(TemaCores.TEXTO_SECUNDARIO);
        restricoes.gridy = 0;
        cardCotacaoDestaque.add(rotuloFTC, restricoes);

        rotuloValorMoeda.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 36));
        rotuloValorMoeda.setForeground(TemaCores.PRIMARIA);
        restricoes.gridy = 1;
        cardCotacaoDestaque.add(rotuloValorMoeda, restricoes);

        etiquetaOrigemDado.setPreferredSize(new Dimension(190, 32));
        restricoes.gridy = 2;
        cardCotacaoDestaque.add(etiquetaOrigemDado, restricoes);

        JPanel painelTelemetria = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        painelTelemetria.setOpaque(false);
        rotuloLatenciaResposta.setFont(TemaCores.FONTE_SUBTITULO);
        rotuloLatenciaResposta.setForeground(TemaCores.TEXTO_SECUNDARIO);
        rotuloHorarioConsulta.setFont(TemaCores.FONTE_SUBTITULO);
        rotuloHorarioConsulta.setForeground(TemaCores.TEXTO_SECUNDARIO);
        // Largura folgada: o texto muda a cada consulta e não pode ser cortado
        rotuloLatenciaResposta.setPreferredSize(new Dimension(210, 22));
        rotuloHorarioConsulta.setPreferredSize(new Dimension(230, 22));
        painelTelemetria.add(rotuloLatenciaResposta);
        painelTelemetria.add(rotuloHorarioConsulta);

        restricoes.gridy = 3;
        cardCotacaoDestaque.add(painelTelemetria, restricoes);

        painelCentro.add(cardCotacaoDestaque, BorderLayout.NORTH);

        // Histórico de Oscilações Recentes do Oráculo
        JPanel painelHistorico = new JPanel(new BorderLayout(8, 8));
        painelHistorico.setOpaque(false);

        JLabel rotuloHistTitulo = new JLabel();
        textos.vincular(rotuloHistTitulo::setText, "gui.cotacao.historico.titulo");
        rotuloHistTitulo.setFont(TemaCores.FONTE_TITULO_MEDIO);
        rotuloHistTitulo.setForeground(TemaCores.TEXTO_PRINCIPAL);
        painelHistorico.add(rotuloHistTitulo, BorderLayout.NORTH);

        String[] chavesColunas = {
                "gui.cotacao.col.data",
                "gui.cotacao.col.cotacao",
                "gui.cotacao.col.varAbs",
                "gui.cotacao.col.varPerc",
                "gui.cotacao.col.fonte"
        };

        modeloTabelaHistorico = new DefaultTableModel(chavesColunas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tabelaHistoricoOscilacoes = new JTable(modeloTabelaHistorico);
        textos.vincularColunas(tabelaHistoricoOscilacoes, chavesColunas);
        EstiloTabela.aplicar(tabelaHistoricoOscilacoes);

        tabelaHistoricoOscilacoes.getColumnModel().getColumn(1).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, contexto.getMensagens()));
        tabelaHistoricoOscilacoes.getColumnModel().getColumn(2).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA_COM_COR, contexto.getMensagens()));
        tabelaHistoricoOscilacoes.getColumnModel().getColumn(3).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.PERCENTUAL_COM_COR, contexto.getMensagens()));
        tabelaHistoricoOscilacoes.getColumnModel().getColumn(4).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.ORIGEM_BADGE, contexto.getMensagens()));

        JScrollPane painelRolagemHistorico = EstiloTabela.comRolagem(tabelaHistoricoOscilacoes);
        painelHistorico.add(painelRolagemHistorico, BorderLayout.CENTER);

        painelCentro.add(painelHistorico, BorderLayout.CENTER);
        add(painelCentro, BorderLayout.CENTER);
    }

    /**
     * Valida a data informada e dispara a rotina de busca de cotação junto ao apresentador.
     *
     * @param forcarOraculo Se {@code true}, ignora o cache e obriga consulta remota
     */
    private void executarConsultaCotacao(boolean forcarOraculo) {
        MensagensGui mensagens = contexto.getMensagens();
        LocalDate data;
        try {
            data = GuiFormatador.lerData(campoDataConsulta.getText());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    mensagens.get("gui.cotacao.dataInvalida"),
                    mensagens.get("gui.cotacao.dataInvalidaTitulo"),
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        presenter.consultarCotacao(data, forcarOraculo);
    }

    /**
     * Inicializa a tela disparando a consulta padrão do dia atual.
     */
    public void inicializar() {
        presenter.inicializar();
    }

    /**
     * Atualiza a tela com a cotação consultada, a origem dela e o tempo da consulta.
     *
     * @param cotacao         cotação consultada
     * @param origem          se veio do Oráculo ou do cache
     * @param tempoRespostaMs duração da consulta, em milissegundos
     */
    @Override
    public void exibirCotacao(Cotacao cotacao, OrigemCotacao origem, long tempoRespostaMs) {
        MensagensGui mensagens = contexto.getMensagens();
        GuiFormatador formatador = mensagens.getFormatador();

        rotuloValorMoeda.setText(formatador.moeda(cotacao.getValor()));
        etiquetaOrigemDado.configurarOrigem(origem, mensagens);
        rotuloLatenciaResposta.setText(mensagens.get("gui.cotacao.latencia", tempoRespostaMs));
        rotuloHorarioConsulta.setText(mensagens.get("gui.cotacao.horario", LocalTime.now().format(FORMATO_HORA)));
    }

    /**
     * Alterna o estado visual de carregamento da interface.
     *
     * @param carregando {@code true} para indicar carregamento em andamento; {@code false} para repouso
     */
    @Override
    public void exibirCarregando(boolean carregando) {
        barraProgressoRequisicao.setVisible(carregando);
        botaoConsultarCotacao.setEnabled(!carregando);
    }

    /**
     * Preenche a tabela com as cotações dos últimos dias e a variação de cada dia.
     *
     * @param historico cotações da mais recente para a mais antiga, cada uma com a sua origem
     */
    @Override
    public void exibirHistoricoOscilacoes(Map<Cotacao, OrigemCotacao> historico) {
        preencherHistorico(modeloTabelaHistorico, historico, contexto.getMensagens().getFormatador());
    }

    // Tabela de cotações (aqui e no relatório 5): data, valor, variação em reais e em %, origem.
    // O histórico vem do dia mais recente para o mais antigo, então a cotação anterior é a da linha seguinte.
    static void preencherHistorico(DefaultTableModel modelo, Map<Cotacao, OrigemCotacao> historico,
            GuiFormatador formatador) {
        modelo.setRowCount(0);
        List<Cotacao> cotacoes = List.copyOf(historico.keySet());
        for (int i = 0; i < cotacoes.size(); i++) {
            Cotacao cotacao = cotacoes.get(i);
            Cotacao anterior = (i + 1 < cotacoes.size()) ? cotacoes.get(i + 1) : null;
            modelo.addRow(new Object[]{
                    formatador.data(cotacao.getData()),
                    cotacao.getValor(),
                    cotacao.calcularVariacao(anterior),
                    cotacao.calcularVariacaoPercentual(anterior),
                    historico.get(cotacao)
            });
        }
    }

    /**
     * Exibe um diálogo modal com mensagem de confirmação e êxito operacional.
     *
     * @param mensagem Texto com os detalhes do sucesso
     */
    @Override
    public void exibirMensagemSucesso(String mensagem) {
        MensagensGui mensagens = contexto.getMensagens();
        JOptionPane.showMessageDialog(this, mensagem, mensagens.get("gui.comum.sucesso"), JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Exibe um diálogo modal com mensagem de falha ou indisponibilidade na consulta.
     *
     * @param mensagem Texto explicativo da falha ocorrida
     */
    @Override
    public void exibirMensagemErro(String mensagem) {
        MensagensGui mensagens = contexto.getMensagens();
        JOptionPane.showMessageDialog(this, mensagem, mensagens.get("gui.cotacao.erroTitulo"), JOptionPane.ERROR_MESSAGE);
    }
}
