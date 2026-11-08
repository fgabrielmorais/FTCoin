package br.com.ftcoin.config;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * <p>
 * Define o layout de cada linha de log: data e hora, nível, thread, logger e mensagem.
 * </p>
 *
 * <p>
 * Substitui o {@code SimpleFormatter} por dois motivos: o nível sai sempre com o nome padrão
 * ({@code SEVERE}, {@code WARNING}, {@code INFO}, {@code FINE}, {@code FINEST}), sem a tradução
 * automática do JDK (que em português escreveria "GRAVE", "ADVERTÊNCIA"...), e a linha mostra a
 * thread de origem. Como o {@code java.util.logging} não tem MDC, a thread ajuda a separar as
 * consultas de cotação feitas em paralelo.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-11-02
 */
public final class FormatadorLog extends Formatter {

    /**
     * Layout da linha: data, hora, [nível], [thread], logger - mensagem, exceção.
     */
    private static final String FORMATO = "%1$tF %1$tT.%1$tL [%2$s] [%3$s] %4$s - %5$s%6$s%n";

    /**
     * Cria o formatador de linhas de log.
     */
    public FormatadorLog() {
        // Sem estado: o layout é fixo
    }

    @Override
    public String format(LogRecord registro) {
        ZonedDateTime momento = ZonedDateTime.ofInstant(registro.getInstant(), ZoneId.systemDefault());
        return String.format(FORMATO,
                momento,
                registro.getLevel().getName(),
                nomeDaThreadAtual(),
                registro.getLoggerName(),
                montarMensagem(registro),
                pilhaDaExcecao(registro.getThrown()));
    }

    /**
     * Substitui os {@code {0}}, {@code {1}}... da mensagem pelos parâmetros.
     * Números saem sem separador de milhar e com ponto decimal, iguais em qualquer idioma:
     * um ID 1000 continua "1000" e uma cotação sai "5.79", nunca "1.000" ou "5,79".
     *
     * @param registro registro de log com a mensagem e seus parâmetros
     * @return a mensagem pronta, ou o texto original se o modelo for inválido
     */
    private static String montarMensagem(LogRecord registro) {
        String mensagem = registro.getMessage();
        Object[] parametros = registro.getParameters();
        if (mensagem == null || parametros == null || parametros.length == 0) {
            return mensagem;
        }

        Object[] textos = new Object[parametros.length];
        for (int i = 0; i < parametros.length; i++) {
            textos[i] = paraTexto(parametros[i]);
        }
        try {
            return MessageFormat.format(mensagem, textos);
        } catch (IllegalArgumentException e) {
            return mensagem;
        }
    }

    /**
     * Converte números em texto neutro; os demais objetos mantêm o próprio {@code toString}.
     *
     * @param parametro valor de um parâmetro da mensagem
     * @return o texto neutro de um número, ou o próprio parâmetro
     */
    private static Object paraTexto(Object parametro) {
        if (parametro instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (parametro instanceof Number numero) {
            return numero.toString();
        }
        return parametro;
    }

    /**
     * O formatador roda na mesma thread que chamou o log; threads virtuais não têm nome,
     * então recebem um identificador.
     *
     * @return o nome da thread, ou {@code virtual-<id>} para threads virtuais
     */
    private static String nomeDaThreadAtual() {
        Thread atual = Thread.currentThread();
        return atual.getName().isEmpty() ? "virtual-" + atual.threadId() : atual.getName();
    }

    /**
     * Converte a exceção em texto com o stack trace completo, em uma nova linha.
     *
     * @param excecao exceção registrada, ou {@code null}
     * @return o stack trace, ou texto vazio se não houver exceção
     */
    private static String pilhaDaExcecao(Throwable excecao) {
        if (excecao == null) {
            return "";
        }
        StringWriter texto = new StringWriter();
        texto.append(System.lineSeparator());
        excecao.printStackTrace(new PrintWriter(texto));
        return texto.toString().stripTrailing();
    }
}
