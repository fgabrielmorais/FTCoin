package br.com.ftcoin.gui.views.components;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import br.com.ftcoin.gui.common.TemaCores;

/**
 * <p>
 * Aplica o visual minimalista a uma tabela: sem grade, só uma linha fina entre as linhas, cabeçalho
 * claro em cinza e células com respiro interno.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-11-09
 */
public final class EstiloTabela {

    private static final int ALTURA_LINHA = 40;

    private static final Color FUNDO_SELECAO = new Color(232, 236, 244);

    private EstiloTabela() {
        // Classe utilitária: não deve ser instanciada
    }

    /**
     * Aplica o estilo à tabela.
     *
     * @param tabela tabela a estilizar
     */
    public static void aplicar(JTable tabela) {
        tabela.setRowHeight(ALTURA_LINHA);
        tabela.setShowVerticalLines(false);
        tabela.setShowHorizontalLines(true);
        tabela.setGridColor(TemaCores.BORDA_SUAVE);
        tabela.setIntercellSpacing(new java.awt.Dimension(0, 1));
        tabela.setBackground(TemaCores.SUPERFICIE_CARD);
        tabela.setSelectionBackground(FUNDO_SELECAO);
        tabela.setSelectionForeground(TemaCores.TEXTO_PRINCIPAL);
        tabela.setFont(TemaCores.FONTE_PADRAO);
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFocusable(false);
        tabela.setDefaultRenderer(Object.class, new CelulaTexto());

        JTableHeader cabecalho = tabela.getTableHeader();
        cabecalho.setDefaultRenderer(new Cabecalho());
        cabecalho.setReorderingAllowed(false);
        cabecalho.setPreferredSize(new java.awt.Dimension(0, 38));
    }

    /**
     * Cria a área de rolagem de uma tabela, com borda fina e fundo branco.
     *
     * @param tabela tabela que será exibida
     * @return a área de rolagem pronta para ser adicionada à tela
     */
    public static JScrollPane comRolagem(JTable tabela) {
        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.setBorder(BorderFactory.createLineBorder(TemaCores.BORDA_SUAVE));
        rolagem.getViewport().setBackground(TemaCores.SUPERFICIE_CARD);
        return rolagem;
    }

    /**
     * Cria a área de rolagem de uma tabela que fica dentro de uma aba, sem borda própria (a aba já tem uma).
     *
     * @param tabela tabela que será exibida
     * @return a área de rolagem pronta para ser adicionada à aba
     */
    public static JScrollPane comRolagemSemBorda(JTable tabela) {
        JScrollPane rolagem = comRolagem(tabela);
        rolagem.setBorder(BorderFactory.createEmptyBorder());
        return rolagem;
    }

    /**
     * Célula de texto padrão: alinhada à esquerda, com respiro interno.
     */
    private static final class CelulaTexto extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                boolean comFoco, int linha, int coluna) {
            JLabel rotulo = (JLabel) super.getTableCellRendererComponent(tabela, valor, selecionada, false, linha, coluna);
            rotulo.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            rotulo.setForeground(TemaCores.TEXTO_PRINCIPAL);
            return rotulo;
        }
    }

    /**
     * Cabeçalho claro, com o texto em cinza e uma linha fina embaixo. O texto acompanha o alinhamento da coluna.
     */
    private static final class Cabecalho extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable tabela, Object valor, boolean selecionada,
                boolean comFoco, int linha, int coluna) {
            JLabel rotulo = (JLabel) super.getTableCellRendererComponent(tabela, valor, false, false, linha, coluna);
            rotulo.setBackground(TemaCores.SUPERFICIE_CARD);
            rotulo.setForeground(TemaCores.TEXTO_SECUNDARIO);
            rotulo.setFont(TemaCores.FONTE_LABEL);
            rotulo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, TemaCores.BORDA_DIVISOR),
                    BorderFactory.createEmptyBorder(0, 14, 0, 14)));

            int alinhamento = SwingConstants.LEFT;
            Object renderizador = tabela.getColumnModel().getColumn(coluna).getCellRenderer();
            if (renderizador instanceof CurrencyTableCellRenderer celula) {
                alinhamento = celula.getAlinhamento();
            }
            rotulo.setHorizontalAlignment(alinhamento);
            return rotulo;
        }
    }
}
