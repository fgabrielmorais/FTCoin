package br.com.ftcoin.gui.services;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.List;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes da {@link ImpressaoRelatorio}. Cobrem a cópia da tabela da tela (todas as linhas, cabeçalhos
 * traduzidos, texto dos renderizadores e dimensionamento sem janela) e a montagem do livro de páginas
 * com vários relatórios. Não abrem o diálogo de impressão.
 */
class ImpressaoRelatorioTest {

    private static final int LARGURA_PAGINA = 595;
    private static final int ALTURA_PAGINA = 842;
    private static final int MARGEM_PAGINA = 36;
    private static final int LINHAS_RELATORIO_LONGO = 200;

    @Test
    @DisplayName("Cópia dimensiona colunas e cabeçalho mesmo sem janela")
    void dimensionaColunasECabecalhoMesmoSemJanela() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JTable origem = new JTable(new Object[][] {
                    { "GANHO NÃO REALIZADO", "R$ 1.018,30" },
                    { "RETORNO SOBRE O INVESTIMENTO", "0,00%" }
            }, new String[] { "Indicador", "Valor" });

            JTable copia = ImpressaoRelatorio.copiarTabela(origem);

            assertTrue(copia.getTableHeader().getHeight() >= 28);
            assertEquals(copia.getWidth(), copia.getTableHeader().getWidth());
            int total = 0;
            for (int c = 0; c < copia.getColumnCount(); c++) {
                TableColumn coluna = copia.getColumnModel().getColumn(c);
                total += coluna.getWidth();
                assertEquals(coluna.getPreferredWidth(), coluna.getWidth());
                for (int r = 0; r < copia.getRowCount(); r++) {
                    var celula = copia.prepareRenderer(copia.getCellRenderer(r, c), r, c);
                    assertTrue(coluna.getWidth() >= celula.getPreferredSize().width + 20);
                }
            }
            assertEquals(total, copia.getWidth());
        });
    }

    @Test
    @DisplayName("Cópia traz todas as linhas na ordem da tela e não muda se o modelo mudar depois")
    void copiaTodasAsLinhasNaOrdemDaTelaSemDependerDeAlteracoesPosteriores() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DefaultTableModel modelo = new DefaultTableModel(new String[] { "Valor" }, 0);
            for (int i = 0; i < LINHAS_RELATORIO_LONGO; i++) {
                modelo.addRow(new Object[] { i });
            }
            JTable origem = new JTable(modelo);
            origem.setAutoCreateRowSorter(true);
            origem.getRowSorter().toggleSortOrder(0);
            origem.getRowSorter().toggleSortOrder(0);
            origem.getColumnModel().getColumn(0).setHeaderValue("Valor traduzido");
            origem.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                private static final long serialVersionUID = 1L;

                @Override
                protected void setValue(Object value) {
                    setText("R$ " + value);
                }
            });

            JTable copia = ImpressaoRelatorio.copiarTabela(origem);

            assertEquals(LINHAS_RELATORIO_LONGO, copia.getRowCount());
            assertEquals("Valor traduzido", copia.getColumnName(0));
            for (int i = 0; i < LINHAS_RELATORIO_LONGO; i++) {
                assertEquals("R$ " + origem.getValueAt(i, 0), copia.getValueAt(i, 0));
            }
            modelo.setRowCount(0);
            assertEquals(LINHAS_RELATORIO_LONGO, copia.getRowCount());
        });
    }

    @Test
    @DisplayName("Livro soma as páginas dos relatórios e permite reimprimir uma página anterior")
    void combinaRelatoriosEPermiteReimprimirPaginaAnterior() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                PageFormat formato = formatoA4();
                JTable curta = ImpressaoRelatorio.copiarTabela(new JTable(
                        new Object[][] { { "Saldo", "170 FTC" } }, new String[] { "Indicador", "Valor" }));
                Object[][] linhas = new Object[LINHAS_RELATORIO_LONGO][2];
                for (int i = 0; i < LINHAS_RELATORIO_LONGO; i++) {
                    linhas[i] = new Object[] { "Movimento " + i, "R$ 10,00" };
                }
                JTable longa = ImpressaoRelatorio.copiarTabela(new JTable(linhas, new String[] { "Movimento", "Valor" }));
                var r1 = new ImpressaoRelatorio.Relatorio("Posição", curta);
                var r2 = new ImpressaoRelatorio.Relatorio("Movimentações", longa);

                Book livro = ImpressaoRelatorio.prepararLivro(List.of(r1, r2), formato, "Página {0}");
                int paginasLonga = ImpressaoRelatorio.prepararLivro(List.of(r2), formato, "Página {0}")
                        .getNumberOfPages();

                assertTrue(paginasLonga > 1);
                assertEquals(1 + paginasLonga, livro.getNumberOfPages());
                int[] primeira = renderizar(livro, 1);
                renderizar(livro, livro.getNumberOfPages() - 1);
                assertArrayEquals(primeira, renderizar(livro, 1));
            } catch (PrinterException e) {
                throw new AssertionError(e);
            }
        });
    }

    @Test
    @DisplayName("Relatório sem linhas não gera páginas")
    void relatorioVazioNaoGeraPaginas() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JTable vazia = ImpressaoRelatorio.copiarTabela(new JTable(new DefaultTableModel(new String[] { "A" }, 0)));

                Book livro = ImpressaoRelatorio.prepararLivro(
                        List.of(new ImpressaoRelatorio.Relatorio("Vazio", vazia)), formatoA4(), "Página {0}");

                assertEquals(0, livro.getNumberOfPages());
            } catch (PrinterException e) {
                throw new AssertionError(e);
            }
        });
    }

    @Test
    @DisplayName("Imprimir sem nenhum relatório é recusado")
    void imprimirSemRelatoriosELancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> ImpressaoRelatorio.imprimir(List.of(), "Página {0}"));
    }

    private static PageFormat formatoA4() {
        PageFormat formato = new PageFormat();
        Paper papel = new Paper();
        papel.setSize(LARGURA_PAGINA, ALTURA_PAGINA);
        papel.setImageableArea(MARGEM_PAGINA, MARGEM_PAGINA,
                LARGURA_PAGINA - 2 * MARGEM_PAGINA, ALTURA_PAGINA - 2 * MARGEM_PAGINA);
        formato.setPaper(papel);
        return formato;
    }

    private static int[] renderizar(Book livro, int pagina) throws PrinterException {
        BufferedImage imagem = new BufferedImage(LARGURA_PAGINA, ALTURA_PAGINA, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagem.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, LARGURA_PAGINA, ALTURA_PAGINA);
            assertEquals(Printable.PAGE_EXISTS,
                    livro.getPrintable(pagina).print(g, livro.getPageFormat(pagina), pagina));
        } finally {
            g.dispose();
        }
        return imagem.getRGB(0, 0, LARGURA_PAGINA, ALTURA_PAGINA, null, 0, LARGURA_PAGINA);
    }
}
