package br.com.ftcoin.gui.common;

import br.com.ftcoin.daos.TipoPersistencia;

/**
 * <p>
 * Enumeração para o seletor de mecanismos de banco de dados e persistência na interface gráfica.
 * </p>
 *
 * <p>
 * Representa os estados fixos de armazenamento configuráveis pelo usuário no componente
 * {@code JComboBox} da barra de navegação da {@code GUI}.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public enum DatabaseSelector {

    /**
     * Armazenamento volátil em memória para simulações e testes rápidos.
     */
    MEMORIA(TipoPersistencia.MEMORIA, "gui.menu.banco.memoria", "💾 Memória Volátil"),

    /**
     * Armazenamento relacional persistente via servidor MariaDB.
     */
    MARIADB(TipoPersistencia.MARIADB, "gui.menu.banco.mariadb", "🗄️ MariaDB (SQL)");

    /**
     * Tipo de persistência de domínio associado à opção.
     */
    private final TipoPersistencia tipoPersistencia;

    /**
     * Chave do arquivo de propriedades para internacionalização.
     */
    private final String chaveMensagem;

    /**
     * Rótulo descritivo padrão de exibição visual.
     */
    private final String descricaoPadrao;

    /**
     * Construtor da enumeração do seletor de banco de dados.
     *
     * @param tipoPersistencia Tipo de persistência do domínio subjacente
     * @param chaveMensagem   Chave de mensagem do bundle para internacionalização
     * @param descricaoPadrao Texto de fallback para exibição no componente visual
     */
    DatabaseSelector(TipoPersistencia tipoPersistencia, String chaveMensagem, String descricaoPadrao) {
        this.tipoPersistencia = tipoPersistencia;
        this.chaveMensagem = chaveMensagem;
        this.descricaoPadrao = descricaoPadrao;
    }

    /**
     * Retorna o tipo de persistência associado a este seletor.
     *
     * @return O tipo de persistência {@code TipoPersistencia}
     */
    public TipoPersistencia getTipoPersistencia() {
        return tipoPersistencia;
    }

    /**
     * Retorna a chave do bundle de mensagens para este seletor.
     *
     * @return A chave de internacionalização {@code String}
     */
    public String getChaveMensagem() {
        return chaveMensagem;
    }

    /**
     * Retorna a descrição padrão de fallback para este seletor.
     *
     * @return A descrição padrão {@code String}
     */
    public String getDescricaoPadrao() {
        return descricaoPadrao;
    }

    /**
     * Retorna o texto formatado e internacionalizado para o seletor.
     *
     * @return O rótulo localizado do seletor {@code String}
     */
    public String getDescricaoLocalizada() {
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();
        if (mensagens != null) {
            return mensagens.get(chaveMensagem);
        }
        return descricaoPadrao;
    }

    /**
     * Retorna a representação textual para renderização em componentes de lista da GUI.
     *
     * @return O texto de apresentação no {@code JComboBox}
     */
    @Override
    public String toString() {
        return getDescricaoLocalizada();
    }

    /**
     * Converte um {@code TipoPersistencia} de domínio no seletor correspondente da interface.
     *
     * @param tipo Tipo de persistência a ser localizado
     * @return A instância de {@code DatabaseSelector} correspondente, ou {@code MEMORIA} caso {@code tipo} seja {@code null}
     */
    public static DatabaseSelector porTipo(TipoPersistencia tipo) {
        if (tipo == TipoPersistencia.MARIADB) {
            return MARIADB;
        }
        return MEMORIA;
    }
}
