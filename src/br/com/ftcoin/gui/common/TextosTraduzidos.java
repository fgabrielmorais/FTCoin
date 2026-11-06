package br.com.ftcoin.gui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

/**
 * Liga componentes de uma tela às chaves dos arquivos de mensagens. Cada texto vinculado é aplicado
 * na hora e reaplicado sempre que o usuário troca o idioma, sem que a tela precise se reconstruir.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public final class TextosTraduzidos {

    private final MensagensGui mensagens;
    private final List<Runnable> aplicacoes = new ArrayList<>();

    /**
     * Cria o vinculador e passa a escutar as trocas de idioma.
     *
     * @param mensagens textos do idioma ativo
     */
    public TextosTraduzidos(MensagensGui mensagens) {
        this.mensagens = mensagens;
        mensagens.adicionarOuvinte(this::aplicarTodos);
    }

    /**
     * Vincula um destino de texto a uma chave de mensagem e aplica o texto imediatamente.
     *
     * @param destino quem recebe o texto, como {@code rotulo::setText}
     * @param chave   chave da mensagem
     */
    public void vincular(Consumer<String> destino, String chave) {
        registrar(() -> destino.accept(mensagens.get(chave)));
    }

    /**
     * Vincula os títulos das colunas de uma tabela, na ordem das colunas.
     *
     * @param tabela tabela cujos cabeçalhos serão traduzidos
     * @param chaves chave de cada coluna, da primeira à última
     */
    public void vincularColunas(JTable tabela, String... chaves) {
        registrar(() -> {
            TableColumnModel colunas = tabela.getColumnModel();
            for (int i = 0; i < chaves.length && i < colunas.getColumnCount(); i++) {
                colunas.getColumn(i).setHeaderValue(mensagens.get(chaves[i]));
            }
            if (tabela.getTableHeader() != null) {
                tabela.getTableHeader().repaint();
            }
        });
    }

    private void registrar(Runnable aplicacao) {
        aplicacoes.add(aplicacao);
        aplicacao.run();
    }

    private void aplicarTodos() {
        aplicacoes.forEach(Runnable::run);
    }
}
