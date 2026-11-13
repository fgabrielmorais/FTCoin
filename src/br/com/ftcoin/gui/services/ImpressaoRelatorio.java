package br.com.ftcoin.gui.services;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.MessageFormat;
import java.util.List;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * <p>
 * Impressão dos relatórios da interface gráfica, incluindo a geração de PDF por uma impressora virtual.
 * </p>
 *
 * <p>
 * Os relatórios escolhidos formam um único trabalho de impressão: cada um começa em uma nova página e
 * a numeração é reiniciada a cada relatório. A impressão usa uma cópia da tabela, que reúne todas as
 * linhas do modelo, inclusive as que estão fora da área de rolagem da tela.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-11-13
 */
public final class ImpressaoRelatorio {

    /** Nome do trabalho exibido na fila de impressão. */
    private static final String NOME_TRABALHO = "FTCoin - Relatórios";

    /** Quantidade de colunas a partir da qual a página sai na horizontal (paisagem). */
    private static final int COLUNAS_PARA_PAISAGEM = 5;

    /** Largura mínima de cada coluna impressa, em pontos. */
    private static final int LARGURA_MINIMA_COLUNA = 100;

    /** Folga somada ao conteúdo de cada coluna, em pontos. */
    private static final int FOLGA_COLUNA = 20;

    /** Altura mínima de cada linha impressa, em pontos. */
    private static final int ALTURA_MINIMA_LINHA = 24;

    /** Altura mínima do cabeçalho impresso, em pontos. */
    private static final int ALTURA_MINIMA_CABECALHO = 28;

    /** Folga somada à altura do texto do cabeçalho, em pontos. */
    private static final int FOLGA_CABECALHO = 8;

    /** Construtor privado: classe utilitária, sem instâncias. */
    private ImpressaoRelatorio() {
    }

    /**
     * Relatório a imprimir.
     *
     * @param titulo título impresso no alto de cada página do relatório
     * @param tabela tabela com os dados, em geral uma cópia feita por {@link #copiarTabela(JTable)}
     */
    public record Relatorio(String titulo, JTable tabela) {
    }

    /**
     * Abre um único diálogo de impressão e envia a seleção em um único trabalho (impressora ou PDF).
     * Se o usuário cancelar o diálogo, nada é impresso. A página fica na horizontal quando algum
     * relatório tem mais de {@value #COLUNAS_PARA_PAISAGEM} colunas.
     *
     * @param relatorios relatórios a imprimir, na ordem da impressão; não pode ser vazio
     * @param rodape     rodapé das páginas, em formato {@link MessageFormat}, em que {0} é o número da página
     * @throws PrinterException         se a impressão falhar
     * @throws IllegalArgumentException se a lista de relatórios estiver vazia
     */
    public static void imprimir(List<Relatorio> relatorios, String rodape) throws PrinterException {
        if (relatorios.isEmpty()) {
            throw new IllegalArgumentException("Nenhum relatório selecionado");
        }
        PrinterJob trabalho = PrinterJob.getPrinterJob();
        trabalho.setJobName(NOME_TRABALHO);
        HashPrintRequestAttributeSet atributos = new HashPrintRequestAttributeSet();
        if (relatorios.stream().anyMatch(r -> r.tabela().getColumnCount() > COLUNAS_PARA_PAISAGEM)) {
            atributos.add(OrientationRequested.LANDSCAPE);
        }
        if (!trabalho.printDialog(atributos)) {
            return;
        }
        PageFormat formato = trabalho.getPageFormat(atributos);
        trabalho.setPageable(prepararLivro(relatorios, formato, rodape));
        trabalho.print(atributos);
    }

    /**
     * Monta o livro de páginas do trabalho. Cada relatório começa em uma nova página e a numeração é
     * local ao relatório. Relatórios sem linhas são ignorados.
     *
     * @param relatorios relatórios a imprimir, na ordem da impressão
     * @param formato    formato da página escolhido pelo usuário
     * @param rodape     rodapé das páginas, em formato {@link MessageFormat}, em que {0} é o número da página
     * @return livro com todas as páginas dos relatórios
     * @throws PrinterException se o cálculo das páginas falhar
     */
    public static Book prepararLivro(List<Relatorio> relatorios, PageFormat formato, String rodape)
            throws PrinterException {
        Book livro = new Book();
        BufferedImage imagem = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        for (Relatorio relatorio : relatorios) {
            if (relatorio.tabela().getRowCount() == 0) {
                continue;
            }
            MessageFormat titulo = new MessageFormat("'" + relatorio.titulo().replace("'", "''") + "'");
            Printable paginador = relatorio.tabela().getPrintable(JTable.PrintMode.FIT_WIDTH,
                    titulo, new MessageFormat(rodape));
            int paginas = contarPaginas(paginador, imagem, formato);
            final int inicio = livro.getNumberOfPages();
            // Drivers podem solicitar páginas repetidas ou fora de ordem.
            // A JTable mantém estado de paginação: recria-se o paginador a cada solicitação.
            if (paginas > 0) {
                livro.append((g, pf, pagina) -> {
                    Printable impressao = relatorio.tabela().getPrintable(JTable.PrintMode.FIT_WIDTH,
                            titulo, new MessageFormat(rodape));
                    Graphics2D copia = (Graphics2D) g.create();
                    try {
                        copia.setColor(Color.BLACK);
                        return impressao.print(copia, pf, pagina - inicio);
                    } finally {
                        copia.dispose();
                    }
                }, formato, paginas);
            }
        }
        return livro;
    }

    /**
     * Conta quantas páginas o paginador produz, desenhando numa imagem descartável.
     *
     * @param paginador paginador do relatório
     * @param imagem    imagem usada como superfície de desenho
     * @param formato   formato da página
     * @return quantidade de páginas
     * @throws PrinterException se o paginador falhar
     */
    private static int contarPaginas(Printable paginador, BufferedImage imagem, PageFormat formato)
            throws PrinterException {
        int paginas = 0;
        while (true) {
            Graphics2D g = imagem.createGraphics();
            try {
                if (paginador.print(g, formato, paginas) == Printable.NO_SUCH_PAGE) {
                    return paginas;
                }
            } finally {
                g.dispose();
            }
            paginas++;
        }
    }

    /**
     * Copia a tabela da tela para uma tabela própria da impressão, com todas as linhas na ordem
     * exibida, os cabeçalhos traduzidos e o texto formatado pelos renderizadores da tela.
     * Deve ser chamado na thread do Swing.
     *
     * @param origem tabela exibida na tela
     * @return tabela em preto e branco, com colunas e cabeçalho já dimensionados
     */
    public static JTable copiarTabela(JTable origem) {
        DefaultTableModel modelo = new DefaultTableModel();
        for (int c = 0; c < origem.getColumnCount(); c++) {
            modelo.addColumn(origem.getColumnModel().getColumn(c).getHeaderValue());
        }
        for (int r = 0; r < origem.getRowCount(); r++) {
            Object[] linha = new Object[origem.getColumnCount()];
            for (int c = 0; c < linha.length; c++) {
                Component renderer = origem.prepareRenderer(origem.getCellRenderer(r, c), r, c);
                linha[c] = renderer instanceof JLabel label ? label.getText() : origem.getValueAt(r, c);
            }
            modelo.addRow(linha);
        }
        JTable copia = new JTable(modelo);
        copia.setFont(origem.getFont());
        copia.setForeground(Color.BLACK);
        copia.setBackground(Color.WHITE);
        // A tabela de impressão não pertence a uma janela: o Swing não fará seu layout.
        // Definir só preferredWidth deixa as colunas com a largura padrão de 75 pixels.
        copia.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        copia.setRowHeight(Math.max(ALTURA_MINIMA_LINHA, origem.getRowHeight()));
        JTableHeader cabecalho = copia.getTableHeader();
        cabecalho.setFont(copia.getFont().deriveFont(Font.BOLD));
        cabecalho.setForeground(Color.BLACK);
        cabecalho.setBackground(Color.WHITE);
        int larguraTotal = 0;
        int alturaCabecalho = ALTURA_MINIMA_CABECALHO;
        for (int c = 0; c < copia.getColumnCount(); c++) {
            TableColumn coluna = copia.getColumnModel().getColumn(c);
            Component titulo = cabecalho.getDefaultRenderer().getTableCellRendererComponent(
                    copia, coluna.getHeaderValue(), false, false, -1, c);
            int largura = Math.max(LARGURA_MINIMA_COLUNA, titulo.getPreferredSize().width + FOLGA_COLUNA);
            alturaCabecalho = Math.max(alturaCabecalho, titulo.getPreferredSize().height + FOLGA_CABECALHO);
            for (int r = 0; r < copia.getRowCount(); r++) {
                Component celula = copia.prepareRenderer(copia.getCellRenderer(r, c), r, c);
                largura = Math.max(largura, celula.getPreferredSize().width + FOLGA_COLUNA);
            }
            // Respeita larguras já definidas para os indicadores e mede o conteúdo completo.
            largura = Math.max(largura, origem.getColumnModel().getColumn(c).getWidth());
            coluna.setPreferredWidth(largura);
            coluna.setWidth(largura);
            larguraTotal += largura;
        }
        copia.setSize(larguraTotal, copia.getPreferredSize().height);
        copia.doLayout();
        // Sem uma altura explícita, JTable.print omite o cabeçalho desta tabela avulsa.
        cabecalho.setPreferredSize(new Dimension(larguraTotal, alturaCabecalho));
        cabecalho.setSize(larguraTotal, alturaCabecalho);
        cabecalho.doLayout();
        return copia;
    }
}
