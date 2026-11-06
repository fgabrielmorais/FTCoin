package br.com.ftcoin.config;

import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.ftcoin.models.FTCoinException;

/**
 * <p>
 * Regra única para registrar falhas na camada de apresentação (presenters e menu do modo texto).
 * </p>
 *
 * <p>
 * Uma {@link FTCoinException} é prevista: uma regra de negócio violada (ex.: saldo insuficiente)
 * ou uma falha técnica que o DAO já registrou com stack trace. Por isso vira {@code WARNING} só com
 * a chave do erro. Qualquer outra exceção é inesperada e vira {@code SEVERE} com o stack trace completo.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-11-02
 */
public final class RegistroDeFalhas {

    private RegistroDeFalhas() {
        // Classe utilitária: não deve ser instanciada
    }

    /**
     * Registra a falha no logger de quem a tratou.
     *
     * @param log  Logger da classe que capturou a exceção
     * @param acao O que o usuário tentou fazer, no infinitivo (ex.: "criar carteira")
     * @param erro Exceção capturada
     */
    public static void registrar(Logger log, String acao, Throwable erro) {
        if (erro instanceof FTCoinException falhaPrevista) {
            log.log(Level.WARNING, "Não foi possível {0}: {1}", new Object[] { acao, falhaPrevista.getChave() });
        } else {
            log.log(Level.SEVERE, "Falha inesperada ao " + acao, erro);
        }
    }
}
