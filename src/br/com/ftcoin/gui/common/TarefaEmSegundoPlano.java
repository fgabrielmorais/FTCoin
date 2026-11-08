package br.com.ftcoin.gui.common;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;

/**
 * Roda um trabalho demorado (ex.: consulta ao oráculo pela rede) numa virtual thread
 * e entrega o resultado, ou o erro, na thread do Swing (EDT). Assim a tela não trava.
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-11-05
 */
public final class TarefaEmSegundoPlano {

    private TarefaEmSegundoPlano() {}

    /**
     * Executa o trabalho numa virtual thread e devolve o resultado, ou a falha, na thread do Swing.
     *
     * @param trabalho  o que roda fora da EDT
     * @param onSucesso recebe o resultado na EDT
     * @param onErro    recebe a exceção na EDT
     * @param <T>       tipo do resultado
     */
    public static <T> void executar(Supplier<T> trabalho, Consumer<T> onSucesso, Consumer<Throwable> onErro) {
        Thread.ofVirtual().start(() -> {
            try {
                T resultado = trabalho.get();
                SwingUtilities.invokeLater(() -> onSucesso.accept(resultado));
            } catch (Throwable e) {
                SwingUtilities.invokeLater(() -> onErro.accept(e));
            }
        });
    }
}
