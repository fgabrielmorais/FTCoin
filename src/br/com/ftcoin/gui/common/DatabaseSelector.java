package br.com.ftcoin.gui.common;

import br.com.ftcoin.daos.TipoPersistencia;

/**
 * Opções do seletor de banco de dados da barra superior, ligadas ao mecanismo de persistência correspondente.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public enum DatabaseSelector {

    /** Dados em memória. */
    MEMORIA(TipoPersistencia.MEMORIA, "gui.menu.banco.memoria"),

    /** Dados no MariaDB. */
    MARIADB(TipoPersistencia.MARIADB, "gui.menu.banco.mariadb");

    private final TipoPersistencia tipoPersistencia;
    private final String chaveMensagem;

    DatabaseSelector(TipoPersistencia tipoPersistencia, String chaveMensagem) {
        this.tipoPersistencia = tipoPersistencia;
        this.chaveMensagem = chaveMensagem;
    }

    /**
     * Devolve o mecanismo de persistência da opção.
     *
     * @return o tipo de persistência
     */
    public TipoPersistencia getTipoPersistencia() {
        return tipoPersistencia;
    }

    /**
     * Devolve o nome da opção no idioma ativo.
     *
     * @param mensagens textos do idioma ativo
     * @return o nome da opção
     */
    public String getDescricaoLocalizada(MensagensGui mensagens) {
        return mensagens.get(chaveMensagem);
    }

    /**
     * Devolve a opção correspondente a um mecanismo de persistência.
     *
     * @param tipo mecanismo de persistência
     * @return a opção do seletor; memória para qualquer valor que não seja MariaDB
     */
    public static DatabaseSelector porTipo(TipoPersistencia tipo) {
        return (tipo == TipoPersistencia.MARIADB) ? MARIADB : MEMORIA;
    }
}
