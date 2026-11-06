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
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class CreditosDialog extends JDialog {

    private static final long serialVersionUID = 1L;

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
     * @param mensagens textos da interface no idioma ativo
     */
    public CreditosDialog(Frame janelaPai, MensagensGui mensagens) {
        super(janelaPai, mensagens.get("gui.creditos.dialog.titulo"), true);
        setSize(560, 480);
        setLocationRelativeTo(janelaPai);
        setLayout(new BorderLayout());

        editorTexto = new JEditorPane();
        editorTexto.setEditable(false);
        editorTexto.setContentType("text/html");
        editorTexto.setBackground(TemaCores.SUPERFICIE_CARD);
        editorTexto.setText(mensagens.carregarDocumento("creditos", Info.valoresDocumentos()));
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
}
