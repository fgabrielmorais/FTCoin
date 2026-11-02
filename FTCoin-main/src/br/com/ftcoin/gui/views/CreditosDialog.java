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
import javax.swing.SwingUtilities;

import br.com.ftcoin.gui.common.AppContext;
import br.com.ftcoin.gui.common.Info;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.TemaCores;
import br.com.ftcoin.gui.views.components.ModernButton;

/**
 * Diálogo modal de créditos e informações do sistema.
 * Apresenta os metadados e textos no idioma ativo.
 *
 * @author Equipe FTCoin
 */
public class CreditosDialog extends JDialog {

    private final JEditorPane editorTexto;
    private final ModernButton botaoFechar;
    private final JScrollPane painelRolagem;

    private final Runnable ouvinteIdioma = this::atualizarTextosInterface;

    public CreditosDialog(Frame janelaPai) {
        super(
                janelaPai,
                AppContext.getInstancia().getMensagens()
                        .get("gui.creditos.dialog.titulo"),
                true
        );

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(560, 480);
        setLocationRelativeTo(janelaPai);
        setLayout(new BorderLayout());

        MensagensGui mensagens =
                AppContext.getInstancia().getMensagens();

        editorTexto = new JEditorPane();
        editorTexto.setEditable(false);
        editorTexto.setContentType("text/html");
        editorTexto.setBackground(TemaCores.SUPERFICIE_CARD);
        editorTexto.setText(gerarConteudoCreditosHtml());
        editorTexto.setCaretPosition(0);

        painelRolagem = new JScrollPane(editorTexto);
        painelRolagem.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
        add(painelRolagem, BorderLayout.CENTER);

        JPanel painelBotoes =
                new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));

        painelBotoes.setBackground(TemaCores.SUPERFICIE_HEADER);
        painelBotoes.setBorder(
                BorderFactory.createMatteBorder(
                        1, 0, 0, 0, TemaCores.BORDA_SUAVE
                )
        );

        botaoFechar = new ModernButton(
                mensagens.get("gui.creditos.btn.fechar"),
                ModernButton.Variante.PRIMARIA
        );

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
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::atualizarTextosInterface);
            return;
        }

        MensagensGui mensagens =
                AppContext.getInstancia().getMensagens();

        javax.swing.JScrollBar barra =
                painelRolagem.getVerticalScrollBar();

        int alcanceAnterior = Math.max(
                0,
                barra.getMaximum() - barra.getVisibleAmount()
        );

        double proporcao = alcanceAnterior == 0
                ? 0.0
                : (double) barra.getValue() / alcanceAnterior;

        setTitle(mensagens.get("gui.creditos.dialog.titulo"));
        botaoFechar.setText(mensagens.get("gui.creditos.btn.fechar"));

        botaoFechar.setPreferredSize(null);
        Dimension tamanhoNatural = botaoFechar.getPreferredSize();

        botaoFechar.setPreferredSize(
                new Dimension(
                        Math.max(100, tamanhoNatural.width),
                        Math.max(32, tamanhoNatural.height)
                )
        );

        editorTexto.setText(gerarConteudoCreditosHtml());
        editorTexto.setCaretPosition(0);

        revalidate();
        repaint();

        SwingUtilities.invokeLater(() -> {
            if (!isDisplayable()) {
                return;
            }

            int novoAlcance = Math.max(
                    0,
                    barra.getMaximum() - barra.getVisibleAmount()
            );

            barra.setValue((int) Math.round(proporcao * novoAlcance));
        });
    }

    private String gerarConteudoCreditosHtml() {
        MensagensGui mensagens =
                AppContext.getInstancia().getMensagens();

        StringBuilder html = new StringBuilder();

        html.append("""
                <html>
                <body style="font-family: sans-serif;
                             padding: 15px;
                             color: #1f2937;">
                <h2 style="color: #1a2b4c;">
                """);

        html.append(escaparHtml(Info.NOME_SISTEMA));
        html.append("</h2><h4 style=\"color: #4f46e5;\">");
        html.append(escaparHtml(
                mensagens.get("gui.creditos.apresentacao")
        ));
        html.append("</h4>");

        acrescentarCampo(
                html,
                mensagens.get("gui.creditos.versao"),
                Info.VERSAO
        );

        acrescentarCampo(
                html,
                mensagens.get("gui.creditos.tecnologia"),
                Info.PLATAFORMA_JAVA
        );

        acrescentarCampo(
                html,
                mensagens.get("gui.creditos.data"),
                Info.getDataLancamentoLocalizada(
                        mensagens.getLocaleAtual()
                )
        );

        html.append("<hr><h3>");
        html.append(escaparHtml(
                mensagens.get("gui.creditos.equipe")
        ));
        html.append("</h3><ul>");

        for (String autor : Info.AUTORES.split(",")) {
            html.append("<li><b>");
            html.append(escaparHtml(autor.trim()));
            html.append("</b></li>");
        }

        html.append("</ul><hr>");

        acrescentarCampo(
                html,
                mensagens.get("gui.creditos.instituicao"),
                Info.INSTITUICAO
        );

        html.append("<p>");
        html.append(escaparHtml(
                mensagens.get("gui.creditos.curso")
        ));
        html.append("<br>");
        html.append(escaparHtml(
                mensagens.get("gui.creditos.disciplina")
        ));
        html.append("</p>");

        html.append("<p style=\"font-size: 11px; color: #6b7280;\">");
        html.append("© 2026 ");
        html.append(escaparHtml(Info.NOME_SISTEMA));
        html.append(". ");
        html.append(escaparHtml(
                mensagens.get("gui.creditos.direitos")
        ));
        html.append("<br>");
        html.append(escaparHtml(
                mensagens.get("gui.creditos.finalidade")
        ));
        html.append("</p></body></html>");

        return html.toString();
    }

    private static void acrescentarCampo(
            StringBuilder html,
            String rotulo,
            String valor
    ) {
        html.append("<p><b>");
        html.append(escaparHtml(rotulo));
        html.append(":</b> ");
        html.append(escaparHtml(valor));
        html.append("</p>");
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