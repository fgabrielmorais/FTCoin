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
 * Diálogo modal de Ajuda contendo o Manual Estendido do Usuário do <b>FTCoin Studio</b>.
 * </p>
 *
 * <p>
 * Documenta os casos de uso do sistema, regras contábeis do Custo Médio Ponderado (CMP),
 * integração com o Oráculo de cotações e modos de persistência.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class AjudaDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    /**
     * Componente para exibição de texto com formatação HTML.
     */
    private final JEditorPane editorTexto;

    /**
     * Botão para fechamento da janela de manual.
     */
    private final ModernButton botaoFechar;

    /**
     * Constrói o diálogo modal do manual do sistema.
     *
     * @param janelaPai Janela principal proprietária deste diálogo
     * @param mensagens textos da interface no idioma ativo
     */
    public AjudaDialog(Frame janelaPai, MensagensGui mensagens) {
        super(janelaPai, mensagens.get("gui.ajuda.dialog.titulo"), true);
        setSize(720, 560);
        setLocationRelativeTo(janelaPai);
        setLayout(new BorderLayout());

        editorTexto = new JEditorPane();
        editorTexto.setEditable(false);
        editorTexto.setContentType("text/html");
        editorTexto.setBackground(TemaCores.SUPERFICIE_CARD);
        editorTexto.setText(mensagens.carregarDocumento("ajuda", Info.valoresDocumentos()));
        editorTexto.setCaretPosition(0);

        JScrollPane painelRolagem = new JScrollPane(editorTexto);
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
}
