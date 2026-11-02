package br.com.ftcoin.gui.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import br.com.ftcoin.gui.common.AppContext;
import br.com.ftcoin.gui.common.Info;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.TemaCores;
import br.com.ftcoin.gui.views.components.ModernButton;

/**
 * <p>
 * Diálogo modal de Ajuda contendo o Manual Estendido do Usuário do <b>FTCoin Studio</b>.
 * </p>
 *
 * <p>
 * Documenta os casos de uso do sistema, regras contábeis do Custo Médio Ponderado (CMP),
 * integração com o Oráculo de cotações e modos de persistência.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class AjudaDialog extends JDialog {

    /**
     * Componente para exibição de texto com formatação HTML.
     */
    private final JEditorPane editorTexto;

    /**
     * Botão para fechamento da janela de manual.
     */
    private final ModernButton botaoFechar;
    private final JScrollPane painelRolagem;

    private final Runnable ouvinteIdioma = this::atualizarTextosInterface;

    /**
     * Constrói o diálogo modal do manual do sistema.
     *
     * @param janelaPai Janela principal proprietária deste diálogo
     */
    public AjudaDialog(Frame janelaPai) {
        super(janelaPai, AppContext.getInstancia().getMensagens().get("gui.ajuda.dialog.titulo"), true);
        setSize(720, 560);
        setLocationRelativeTo(janelaPai);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        editorTexto = new JEditorPane();
        editorTexto.setEditable(false);
        editorTexto.setContentType("text/html");
        editorTexto.setBackground(TemaCores.SUPERFICIE_CARD);
        editorTexto.setText(gerarConteudoManualHtml());
        editorTexto.setCaretPosition(0);

        painelRolagem = new JScrollPane(editorTexto);
        painelRolagem.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(painelRolagem, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        painelBotoes.setBackground(TemaCores.SUPERFICIE_HEADER);
        painelBotoes.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TemaCores.BORDA_SUAVE));

        botaoFechar = new ModernButton(mensagens.get("gui.ajuda.btn.fechar"), ModernButton.Variante.PRIMARIA);
        botaoFechar.setPreferredSize(new Dimension(140, 34));
        botaoFechar.addActionListener(e -> dispose());
        painelBotoes.add(botaoFechar);

        add(painelBotoes, BorderLayout.SOUTH);
    }

        @Override
    public void addNotify() {
        super.addNotify();

        AppContext.getInstancia().getMensagens()
                .adicionarOuvinte(ouvinteIdioma);

        atualizarTextosInterface();
    }

    @Override
    public void removeNotify() {
        AppContext.getInstancia().getMensagens()
                .removerOuvinte(ouvinteIdioma);

        super.removeNotify();
    }

    private void atualizarTextosInterface() {
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(
                    this::atualizarTextosInterface
            );
            return;
        }

        MensagensGui mensagens =
                AppContext.getInstancia().getMensagens();

        javax.swing.JScrollBar barraVertical =
                painelRolagem.getVerticalScrollBar();

        int alcanceAnterior = Math.max(
                0,
                barraVertical.getMaximum()
                        - barraVertical.getVisibleAmount()
        );

        double proporcaoLeitura = alcanceAnterior == 0
                ? 0.0
                : (double) barraVertical.getValue() / alcanceAnterior;

        setTitle(mensagens.get("gui.ajuda.dialog.titulo"));
        botaoFechar.setText(mensagens.get("gui.ajuda.btn.fechar"));

        botaoFechar.setPreferredSize(null);
        Dimension tamanhoNatural = botaoFechar.getPreferredSize();

        botaoFechar.setPreferredSize(
                new Dimension(
                        Math.max(140, tamanhoNatural.width),
                        Math.max(34, tamanhoNatural.height)
                )
        );

        editorTexto.setText(gerarConteudoManualHtml());
        editorTexto.setCaretPosition(0);

        revalidate();
        repaint();

        javax.swing.SwingUtilities.invokeLater(() -> {
            if (!isDisplayable()) {
                return;
            }

            int novoAlcance = Math.max(
                    0,
                    barraVertical.getMaximum()
                            - barraVertical.getVisibleAmount()
            );

            barraVertical.setValue(
                    (int) Math.round(proporcaoLeitura * novoAlcance)
            );
        });
    }

    /**
     * Monta o manual com os textos do idioma ativo.
     *
     * @return Conteúdo do manual em HTML
     */
    private String gerarConteudoManualHtml() {
        MensagensGui mensagens =
                AppContext.getInstancia().getMensagens();

        StringBuilder html = new StringBuilder();

        html.append("""
                <html>
                <body style="font-family: sans-serif;
                             padding: 15px;
                             color: #1f2937;">
                <h1 style="color: #1a2b4c;">
                """);

        html.append(escaparHtml(
                mensagens.get("gui.ajuda.manual.titulo")
        ));

        html.append("</h1>");
        html.append("<p style=\"color: #6b7280;\">");
        html.append(escaparHtml(Info.NOME_SISTEMA));
        html.append(" — ");
        html.append(escaparHtml(
                mensagens.get("gui.ajuda.manual.versao")
        ));
        html.append(" ");
        html.append(escaparHtml(Info.VERSAO));
        html.append("</p><hr>");

        for (int numero = 1; numero <= 8; numero++) {
            String prefixo = "gui.ajuda.manual.secao" + numero;

            html.append("<h2 style=\"color: #2563eb;\">");
            html.append(escaparHtml(
                    mensagens.get(prefixo + ".titulo")
            ));
            html.append("</h2>");

            html.append(mensagens.get(prefixo + ".html"));
        }

        html.append("</body></html>");
        return html.toString();
    }

    private static String escaparHtml(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
