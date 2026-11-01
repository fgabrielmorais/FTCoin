package br.com.ftcoin.gui.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import br.com.ftcoin.gui.common.AppContext;
import br.com.ftcoin.gui.common.GuiFormatador;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.TemaCores;
import br.com.ftcoin.gui.dto.CarteiraDTO;
import br.com.ftcoin.gui.presenters.CarteiraPresenter;
import br.com.ftcoin.gui.services.RelatoriosService;
import br.com.ftcoin.gui.views.components.CurrencyTableCellRenderer;
import br.com.ftcoin.gui.views.components.ModernButton;
import br.com.ftcoin.gui.views.contracts.ICarteiraView;

/**
 * <p>
 * Visão gráfica do Módulo de Carteiras no padrão arquitetural <b>MVP</b>.
 * </p>
 *
 * <p>
 * Renderiza a listagem de carteiras ativas em uma {@link JTable} zebrada e provê
 * diálogos modais para criação e edição com validações rigorosas de preenchimento.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class CarteirasPanel extends JPanel implements ICarteiraView {

    /**
     * Apresentador responsável pelas regras de apresentação do módulo.
     */
    private final CarteiraPresenter presenter;

    /**
     * Modelo tabular que gerencia os dados de exibição da tabela de carteiras.
     */
    private final DefaultTableModel modeloTabelaCarteiras;

    /**
     * Tabela visual para renderização da listagem de carteiras.
     */
    private final JTable tabelaCarteiras;

    /**
     * Lista em memória das carteiras atualmente exibidas.
     */
    private List<CarteiraDTO> carteirasCarregadas = List.of();

    /**
     * Referência para a janela modal de formulário ativa.
     */
    private JDialog modalFormularioAtivo;
    private final JLabel rotuloTitulo;

    private final ModernButton botaoNovaCarteira;
    private final ModernButton botaoEditarCarteira;
    private final ModernButton botaoExcluirCarteira;
    private final ModernButton botaoAtualizarLista;

    private final Runnable ouvinteMudancaIdioma = this::atualizarTextos;

    /**
     * Constrói o painel de carteiras inicializando componentes visuais e apresentador.
     *
     * @param relatoriosService Serviço de dados e relatórios da aplicação
     */
    public CarteirasPanel(RelatoriosService relatoriosService) {
        this.presenter = new CarteiraPresenter(this, relatoriosService);
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        setLayout(new BorderLayout(15, 15));
        setBackground(TemaCores.FUNDO_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Topo com Título e Botões de Ação
        JPanel painelTopo = new JPanel(new BorderLayout());
        painelTopo.setOpaque(false);

        rotuloTitulo = new JLabel(mensagens.get("gui.carteira.titulo"));
        rotuloTitulo.setFont(TemaCores.FONTE_TITULO_GRANDE);
        rotuloTitulo.setForeground(TemaCores.TEXTO_PRINCIPAL);
        painelTopo.add(rotuloTitulo, BorderLayout.NORTH);

        JPanel painelBotoesAcao = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        painelBotoesAcao.setOpaque(false);

                botaoNovaCarteira = new ModernButton(
                mensagens.get("gui.carteira.btn.novo"),
                ModernButton.Variante.SUCESSO
        );

        botaoEditarCarteira = new ModernButton(
                mensagens.get("gui.carteira.btn.editar"),
                ModernButton.Variante.PRIMARIA
        );

        botaoExcluirCarteira = new ModernButton(
                mensagens.get("gui.carteira.btn.excluir"),
                ModernButton.Variante.PERIGO
        );

        botaoAtualizarLista = new ModernButton(
                mensagens.get("gui.carteira.btn.atualizar"),
                ModernButton.Variante.NEUTRO
        );

        botaoNovaCarteira.addActionListener(e -> abrirModalCadastro());
        botaoEditarCarteira.addActionListener(e -> tratarAcaoEditar());
        botaoExcluirCarteira.addActionListener(e -> tratarAcaoExcluir());
        botaoAtualizarLista.addActionListener(e -> presenter.carregarCarteiras());

        painelBotoesAcao.add(botaoNovaCarteira);
        painelBotoesAcao.add(botaoEditarCarteira);
        painelBotoesAcao.add(botaoExcluirCarteira);
        painelBotoesAcao.add(botaoAtualizarLista);
        painelTopo.add(painelBotoesAcao, BorderLayout.SOUTH);

        add(painelTopo, BorderLayout.NORTH);

        // Configuração dos cabeçalhos da tabela via arquivo de propriedades
        String[] colunas = {
                mensagens.get("gui.carteira.col.id"),
                mensagens.get("gui.carteira.col.titular"),
                mensagens.get("gui.carteira.col.corretora"),
                mensagens.get("gui.carteira.col.saldo"),
                mensagens.get("gui.carteira.col.valorMercado")
        };

        modeloTabelaCarteiras = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaCarteiras = new JTable(modeloTabelaCarteiras);
        tabelaCarteiras.setRowHeight(34);
        tabelaCarteiras.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaCarteiras.setFont(TemaCores.FONTE_PADRAO);
        tabelaCarteiras.getTableHeader().setFont(TemaCores.FONTE_LABEL);
        tabelaCarteiras.getTableHeader().setBackground(TemaCores.SUPERFICIE_HEADER);
        tabelaCarteiras.getTableHeader().setForeground(TemaCores.TEXTO_PRINCIPAL);

        GuiFormatador formatador = mensagens.getFormatador();
        tabelaCarteiras.getColumnModel().getColumn(3).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.QUANTIDADE, formatador));
        tabelaCarteiras.getColumnModel().getColumn(4).setCellRenderer(
                new CurrencyTableCellRenderer(CurrencyTableCellRenderer.ModoRenderizacao.MOEDA, formatador));

        JScrollPane painelRolagemTabela = new JScrollPane(tabelaCarteiras);
        painelRolagemTabela.setBorder(BorderFactory.createLineBorder(TemaCores.BORDA_SUAVE));
        painelRolagemTabela.getViewport().setBackground(TemaCores.SUPERFICIE_CARD);

        add(painelRolagemTabela, BorderLayout.CENTER);
    }

        /**
     * Registra a atualização de idioma quando o painel entra na janela.
     */
    @Override
    public void addNotify() {
        super.addNotify();

        AppContext.getInstancia().getMensagens()
                .adicionarOuvinte(ouvinteMudancaIdioma);

        atualizarTextos();
    }

    /**
     * Remove o registro quando o painel sai da janela.
     */
    @Override
    public void removeNotify() {
        AppContext.getInstancia().getMensagens()
                .removerOuvinte(ouvinteMudancaIdioma);

        super.removeNotify();
    }

    /**
     * Atualiza os textos sem reconstruir a tabela nem alterar seus dados.
     */
    private void atualizarTextos() {
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        rotuloTitulo.setText(mensagens.get("gui.carteira.titulo"));

        botaoNovaCarteira.setText(
                mensagens.get("gui.carteira.btn.novo")
        );
        botaoEditarCarteira.setText(
                mensagens.get("gui.carteira.btn.editar")
        );
        botaoExcluirCarteira.setText(
                mensagens.get("gui.carteira.btn.excluir")
        );
        botaoAtualizarLista.setText(
                mensagens.get("gui.carteira.btn.atualizar")
        );

        String[] chavesColunas = {
                "gui.carteira.col.id",
                "gui.carteira.col.titular",
                "gui.carteira.col.corretora",
                "gui.carteira.col.saldo",
                "gui.carteira.col.valorMercado"
        };

        for (int i = 0; i < tabelaCarteiras.getColumnCount(); i++) {
            javax.swing.table.TableColumn coluna =
                    tabelaCarteiras.getColumnModel().getColumn(i);

            coluna.setHeaderValue(
                    mensagens.get(chavesColunas[coluna.getModelIndex()])
            );
        }

        tabelaCarteiras.getTableHeader().repaint();
        tabelaCarteiras.repaint();
        revalidate();
        repaint();
    }

    /**
     * Inicializa a tela disparando o carregamento dos registros pelo apresentador.
     */
    public void inicializar() {
        presenter.carregarCarteiras();
    }

    /**
     * Trata o evento de clique no botão de edição de carteira.
     */
    private void tratarAcaoEditar() {
        CarteiraDTO selecionada = getCarteiraSelecionada();
        if (selecionada != null) {
            abrirModalEdicao(selecionada);
        } else {
            exibirMensagemErro(AppContext.getInstancia().getMensagens().get("gui.carteira.erro.selecione"));
        }
    }

    /**
     * Trata o evento de clique no botão de exclusão de carteira solicitando confirmação.
     */
        private void tratarAcaoExcluir() {
        CarteiraDTO selecionada = getCarteiraSelecionada();
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        if (selecionada == null) {
            exibirMensagemErro(
                    mensagens.get("gui.carteira.erro.selecione")
            );
            return;
        }

        Object[] opcoes = {
                mensagens.get("gui.carteira.btn.excluir"),
                mensagens.get("gui.comum.cancelar")
        };

        int confirmacao = JOptionPane.showOptionDialog(
                this,
                mensagens.get(
                        "gui.carteira.confirm.excluir",
                        selecionada.id(),
                        selecionada.nomeTitular()
                ),
                mensagens.get("gui.comum.atencao"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                opcoes,
                opcoes[1]
        );

        if (confirmacao == 0) {
            presenter.excluirCarteira();
        }
    }

    /**
     * Renderiza as carteiras na tabela visual da tela.
     *
     * @param carteiras Lista contendo os dados imutáveis de carteiras em formato {@link CarteiraDTO}
     */
    @Override
    public void exibirCarteiras(List<CarteiraDTO> carteiras) {
        this.carteirasCarregadas = carteiras;
        modeloTabelaCarteiras.setRowCount(0);
        for (CarteiraDTO carteira : carteiras) {
            modeloTabelaCarteiras.addRow(new Object[]{
                    carteira.id(),
                    carteira.nomeTitular(),
                    carteira.corretora(),
                    carteira.saldo(),
                    carteira.valorMercado()
            });
        }
    }

    /**
     * Exibe um diálogo modal com mensagem de confirmação e êxito operacional.
     *
     * @param mensagem Texto localizado contendo os detalhes do sucesso
     */
    @Override
    public void exibirMensagemSucesso(String mensagem) {
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();
        JOptionPane.showMessageDialog(this, mensagem, mensagens.get("gui.comum.sucesso"), JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Exibe um diálogo modal com mensagem de erro ou validação.
     *
     * @param mensagem Texto localizado contendo os detalhes do problema
     */
    @Override
    public void exibirMensagemErro(String mensagem) {
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();
        JOptionPane.showMessageDialog(this, mensagem, mensagens.get("gui.comum.erro"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Retorna a carteira atualmente selecionada pelo usuário na tabela visual.
     *
     * @return O objeto {@link CarteiraDTO} selecionado, ou {@code null} se não houver linha ativa
     */
    @Override
    public CarteiraDTO getCarteiraSelecionada() {
        int linhaSelecionada = tabelaCarteiras.getSelectedRow();
        if (linhaSelecionada >= 0 && linhaSelecionada < carteirasCarregadas.size()) {
            return carteirasCarregadas.get(linhaSelecionada);
        }
        return null;
    }

    /**
     * Abre a janela modal com formulário limpo para inclusão de nova carteira.
     */
    @Override
    public void abrirModalCadastro() {
        abrirDialogFormulario(null);
    }

    /**
     * Abre a janela modal com os dados cadastrais preenchidos para alteração.
     *
     * @param carteira Dados da carteira a ser modificada
     */
    @Override
    public void abrirModalEdicao(CarteiraDTO carteira) {
        abrirDialogFormulario(carteira);
    }

    /**
     * Fecha e libera a janela modal de formulário atualmente em exibição.
     */
    @Override
    public void fecharModal() {
        if (modalFormularioAtivo != null) {
            modalFormularioAtivo.dispose();
            modalFormularioAtivo = null;
        }
    }

    /**
     * Constrói e exibe a janela modal com formulário de cadastro ou edição.
     *
     * @param existente Carteira para edição, ou {@code null} em caso de novo registro
     */
    private void abrirDialogFormulario(CarteiraDTO existente) {
        Frame janelaMae = (Frame) SwingUtilities.getWindowAncestor(this);
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        String tituloModal = (existente == null)
                ? mensagens.get("gui.carteira.dialog.novoTitulo")
                : mensagens.get("gui.carteira.dialog.editarTitulo");

        modalFormularioAtivo = new JDialog(janelaMae, tituloModal, true);
        modalFormularioAtivo.setLayout(new BorderLayout());
        modalFormularioAtivo.setSize(440, 260);
        modalFormularioAtivo.setLocationRelativeTo(this);
        modalFormularioAtivo.setResizable(false);

        JPanel painelFormulario = new JPanel(new GridBagLayout());
        painelFormulario.setBackground(TemaCores.SUPERFICIE_CARD);
        painelFormulario.setBorder(BorderFactory.createEmptyBorder(20, 24, 15, 24));

        GridBagConstraints restricoes = new GridBagConstraints();
        restricoes.insets = new Insets(6, 6, 6, 6);
        restricoes.fill = GridBagConstraints.HORIZONTAL;
        restricoes.gridx = 0;

        JLabel rotuloTitular = new JLabel(mensagens.get("gui.carteira.form.titular"));
        rotuloTitular.setFont(TemaCores.FONTE_LABEL);
        JTextField campoTextoTitular = new JTextField(existente != null ? existente.nomeTitular() : "", 20);
        campoTextoTitular.setFont(TemaCores.FONTE_PADRAO);
        campoTextoTitular.setPreferredSize(new Dimension(280, 32));

        JLabel rotuloCorretora = new JLabel(mensagens.get("gui.carteira.form.corretora"));
        rotuloCorretora.setFont(TemaCores.FONTE_LABEL);
        JTextField campoTextoCorretora = new JTextField(existente != null ? existente.corretora() : "", 20);
        campoTextoCorretora.setFont(TemaCores.FONTE_PADRAO);
        campoTextoCorretora.setPreferredSize(new Dimension(280, 32));

        restricoes.gridy = 0;
        painelFormulario.add(rotuloTitular, restricoes);
        restricoes.gridy = 1;
        painelFormulario.add(campoTextoTitular, restricoes);
        restricoes.gridy = 2;
        painelFormulario.add(rotuloCorretora, restricoes);
        restricoes.gridy = 3;
        painelFormulario.add(campoTextoCorretora, restricoes);

        JPanel painelBotoesModal = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        painelBotoesModal.setBackground(TemaCores.SUPERFICIE_HEADER);
        painelBotoesModal.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TemaCores.BORDA_SUAVE));

        ModernButton botaoCancelar = new ModernButton(mensagens.get("gui.comum.cancelar"), ModernButton.Variante.NEUTRO);
        botaoCancelar.addActionListener(e -> fecharModal());

        ModernButton botaoSalvar = new ModernButton(mensagens.get("gui.comum.salvar"), ModernButton.Variante.PRIMARIA);
        botaoSalvar.addActionListener(e -> {
            if (existente == null) {
                presenter.salvarNovaCarteira(campoTextoTitular.getText(), campoTextoCorretora.getText());
            } else {
                presenter.atualizarCarteira(existente.id(), campoTextoTitular.getText(), campoTextoCorretora.getText());
            }
        });

        painelBotoesModal.add(botaoCancelar);
        painelBotoesModal.add(botaoSalvar);

                modalFormularioAtivo.add(painelFormulario, BorderLayout.CENTER);
        modalFormularioAtivo.add(painelBotoesModal, BorderLayout.SOUTH);

        JDialog dialogo = modalFormularioAtivo;

        Runnable atualizarFormulario = () -> {
            String chaveTitulo = existente == null
                    ? "gui.carteira.dialog.novoTitulo"
                    : "gui.carteira.dialog.editarTitulo";

            dialogo.setTitle(mensagens.get(chaveTitulo));

            rotuloTitular.setText(
                    mensagens.get("gui.carteira.form.titular")
            );
            rotuloCorretora.setText(
                    mensagens.get("gui.carteira.form.corretora")
            );
            botaoCancelar.setText(
                    mensagens.get("gui.comum.cancelar")
            );
            botaoSalvar.setText(
                    mensagens.get("gui.comum.salvar")
            );

            dialogo.pack();

            dialogo.setSize(
                    Math.max(460, dialogo.getWidth()),
                    Math.max(280, dialogo.getHeight())
            );

            dialogo.revalidate();
            dialogo.repaint();
        };

        mensagens.adicionarOuvinte(atualizarFormulario);

        try {
            atualizarFormulario.run();
            dialogo.setLocationRelativeTo(this);
            dialogo.setVisible(true);
        } finally {
            mensagens.removerOuvinte(atualizarFormulario);

            if (modalFormularioAtivo == dialogo) {
                modalFormularioAtivo = null;
            }
        }
    }
}
