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
 * Diálogo modal de Créditos e Informações Institucionais do sistema <b>FTCoin Studio</b>.
 * </p>
 *
 * <p>
 * Recupera os metadados centralizados a partir de {@link Info} para exibir versões, autores,
 * papéis da equipe e copyright institucional da aplicação.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class CreditosDialog extends JDialog {

    /**
     * Painel de exibição de texto formatado com tecnologia HTML.
     */
    private final JEditorPane editorTexto;

    /**
     * Botão de fechamento da janela modal.
     */
    private final ModernButton botaoFechar;

    /**
     * Constrói o diálogo modal de créditos e dados de autoria.
     *
     * @param janelaPai Janela principal ({@link Frame}) proprietária deste diálogo
     */
    public CreditosDialog(Frame janelaPai) {
        super(janelaPai, AppContext.getInstancia().getMensagens().get("gui.creditos.dialog.titulo"), true);
        setSize(560, 480);
        setLocationRelativeTo(janelaPai);
        setLayout(new BorderLayout());

        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        editorTexto = new JEditorPane();
        editorTexto.setEditable(false);
        editorTexto.setContentType("text/html");
        editorTexto.setBackground(TemaCores.SUPERFICIE_CARD);
        editorTexto.setText(gerarConteudoCreditosHtml());
        editorTexto.setCaretPosition(0);

        JScrollPane painelRolagem = new JScrollPane(editorTexto);
        painelRolagem.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(painelRolagem, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        painelBotoes.setBackground(TemaCores.SUPERFICIE_HEADER);
        painelBotoes.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TemaCores.BORDA_SUAVE));

        botaoFechar = new ModernButton(mensagens.get("gui.creditos.btn.fechar"), ModernButton.Variante.PRIMARIA);
        botaoFechar.setPreferredSize(new Dimension(100, 32));
        botaoFechar.addActionListener(e -> dispose());
        painelBotoes.add(botaoFechar);

        add(painelBotoes, BorderLayout.SOUTH);
    }

    /**
     * Retorna a estrutura HTML com os dados de versão e autoria estruturados via {@link Info}.
     *
     * @return O conteúdo formatado em HTML {@code String}
     */
    private String gerarConteudoCreditosHtml() {
        return """
            <html>
            <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; padding: 15px; color: #1f2937; text-align: center;">
                <h2 style="color: #1a2b4c; margin-bottom: 2px;">""" + Info.NOME_SISTEMA + """
                </h2>
                <h4 style="color: #4f46e5; margin-top: 0;">Sistema de Gestão Financeira de Criptomoedas</h4>
                <p style="font-size: 13px; color: #6b7280;"><b>Versão:</b> """ + Info.VERSAO + """
                 &nbsp;|&nbsp; <b>Tecnologia:</b> """ + Info.PLATAFORMA_JAVA + """
                </p>
                <p style="font-size: 12px; color: #6b7280;"><b>Data de Lançamento:</b> """ + Info.DATA_LANCAMENTO + """
                </p>
                <hr style="border: 0; border-top: 1px solid #e5e7eb; margin: 15px 0;">

                <h3 style="color: #1a2b4c; text-align: left;">Equipe de Desenvolvimento:</h3>
                <ul style="text-align: left; font-size: 13px; line-height: 1.8;">
                    <li><b>Ana</b></li>
                    <li><b>Arthur</b></li>
                    <li><b>Erik</b></li>
                    <li><b>Gabriel Morais Felix</b></li>
                    <li><b>Hyago</b></li>
                    <li><b>Isabela</b></li>
                    <li><b>Pedro</b></li>
                    <li><b>Vitor</b></li>
                </ul>

                <hr style="border: 0; border-top: 1px solid #e5e7eb; margin: 15px 0;">
                <p style="font-size: 12px; color: #4b5563;"><b>Instituição:</b> """ + Info.INSTITUICAO + """
                <br>
                """ + Info.CURSO + """
                 - """ + Info.DISCIPLINA + """
                </p>
                <p style="font-size: 11px; color: #9ca3af; margin-top: 15px;">
                    """ + Info.COPYRIGHT + """
                    <br>
                    Desenvolvido com fins estritamente acadêmicos e profissionais.
                </p>
            </body>
            </html>
            """;
    }
}
