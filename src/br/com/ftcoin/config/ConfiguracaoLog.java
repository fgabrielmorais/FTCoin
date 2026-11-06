package br.com.ftcoin.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * <p>
 * Ativa os logs da aplicação a partir do arquivo {@code logging.properties}.
 * </p>
 *
 * <p>
 * Deve ser chamada na primeira linha de cada {@code main}, antes de qualquer registro de log.
 * Lê a configuração do classpath, cria a pasta {@code logs/} usada pelo arquivo de rotação e
 * aplica o {@link FormatadorLog} a todos os destinos.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-11-02
 */
public final class ConfiguracaoLog {

    private static final Logger LOG = Logger.getLogger(ConfiguracaoLog.class.getName());

    /**
     * Arquivo de configuração procurado no classpath.
     */
    private static final String ARQUIVO_CONFIGURACAO = "logging.properties";

    /**
     * Pasta dos arquivos de log, a mesma do {@code FileHandler.pattern}.
     */
    private static final Path PASTA_LOGS = Path.of("logs");

    /**
     * Nome do logger pai de todas as classes do projeto, configurado por {@code br.com.ftcoin.level}.
     */
    private static final String LOGGER_DO_PROJETO = "br.com.ftcoin";

    /**
     * Mantém o logger pai do projeto vivo (o JUL guarda loggers por referência fraca).
     */
    private static Logger loggerDoProjeto;

    /**
     * Impede que a configuração seja carregada duas vezes.
     */
    private static boolean iniciada;

    private ConfiguracaoLog() {
        // Classe utilitária: não deve ser instanciada
    }

    /**
     * Carrega a configuração de logs. Se o arquivo não existir ou não puder ser lido,
     * a aplicação continua com a configuração padrão do JDK (só console, nível INFO).
     */
    public static synchronized void iniciar() {
        if (iniciada) {
            return;
        }
        iniciada = true;

        try {
            Files.createDirectories(PASTA_LOGS);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Não foi possível criar a pasta de logs " + PASTA_LOGS.toAbsolutePath(), e);
        }

        try (InputStream entrada = ConfiguracaoLog.class.getClassLoader().getResourceAsStream(ARQUIVO_CONFIGURACAO)) {
            if (entrada == null) {
                LOG.log(Level.WARNING, "Arquivo {0} não encontrado; usando a configuração padrão do JDK", ARQUIVO_CONFIGURACAO);
                return;
            }
            LogManager.getLogManager().readConfiguration(entrada);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Falha ao ler " + ARQUIVO_CONFIGURACAO + "; usando a configuração padrão do JDK", e);
            return;
        }

        // Loggers estáticos criados antes desta leitura (ex.: o da classe com o main) só herdam
        // o nível do projeto depois que o logger pai existe
        loggerDoProjeto = Logger.getLogger(LOGGER_DO_PROJETO);

        aplicarFormatador();
        LOG.fine(() -> "Logs configurados; arquivos em " + PASTA_LOGS.toAbsolutePath());
    }

    /**
     * Aplica o mesmo layout de linha a todos os destinos do logger raiz.
     */
    private static void aplicarFormatador() {
        FormatadorLog formatador = new FormatadorLog();
        for (Handler destino : Logger.getLogger("").getHandlers()) {
            destino.setFormatter(formatador);
        }
    }
}
