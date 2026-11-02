package br.com.ftcoin.gui.common;

/**
 * <p>
 * Interface selada que padroniza o resultado de operações assíncronas ou síncronas na interface.
 * </p>
 *
 * <p>
 * Demonstra recursos da arquitetura moderna do Java para decomposição estrutural via
 * <i>pattern matching</i> e expressões de seleção.
 * </p>
 *
 * @param <T> Tipo de dado retornado em caso de conclusão bem-sucedida
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public sealed interface OperacaoResultado<T> permits OperacaoResultado.Sucesso, OperacaoResultado.Falha {

    /**
     * <p>
     * Registro imutável representativo do êxito na execução de uma operação.
     * </p>
     *
     * @param <T>      Tipo da carga útil transportada
     * @param mensagem Mensagem informativa ou de confirmação
     * @param dados    Carga útil dos dados resultantes
     * @author Equipe FTCoin
     * @version 1.0.0
     * @since 2026-10-20
     */
    record Sucesso<T>(String mensagem, T dados) implements OperacaoResultado<T> {

        /**
         * Cria uma instância de sucesso contendo mensagem e dados.
         *
         * @param <T>      Tipo do dado retornado
         * @param mensagem Texto informativo de confirmação
         * @param dados    Carga útil de retorno
         * @return Uma instância de {@code Sucesso<T>}
         */
        public static <T> Sucesso<T> of(String mensagem, T dados) {
            return new Sucesso<>(mensagem, dados);
        }

        /**
         * Cria uma instância de sucesso sem carga útil de retorno.
         *
         * @param mensagem Texto informativo de confirmação
         * @return Uma instância de {@code Sucesso<Void>}
         */
        public static Sucesso<Void> of(String mensagem) {
            return new Sucesso<>(mensagem, null);
        }
    }

    /**
     * <p>
     * Registro imutável representativo de falha na execução de uma operação.
     * </p>
     *
     * @param <T>          Tipo esperado do dado em caso de êxito
     * @param mensagemErro Texto descritivo da falha
     * @param causa        Exceção raiz causadora do problema (opcional)
     * @author Equipe FTCoin
     * @version 1.0.0
     * @since 2026-10-20
     */
    record Falha<T>(String mensagemErro, Throwable causa) implements OperacaoResultado<T> {

        /**
         * Cria uma instância de falha contendo apenas mensagem descritiva.
         *
         * @param <T>          Tipo esperado
         * @param mensagemErro Texto descritivo do erro
         * @return Uma instância de {@code Falha<T>}
         */
        public static <T> Falha<T> of(String mensagemErro) {
            return new Falha<>(mensagemErro, null);
        }

        /**
         * Cria uma instância de falha contendo mensagem e exceção causadora.
         *
         * @param <T>          Tipo esperado
         * @param mensagemErro Texto descritivo do erro
         * @param causa        Exceção original capturada
         * @return Uma instância de {@code Falha<T>}
         */
        public static <T> Falha<T> of(String mensagemErro, Throwable causa) {
            return new Falha<>(mensagemErro, causa);
        }
    }
}
