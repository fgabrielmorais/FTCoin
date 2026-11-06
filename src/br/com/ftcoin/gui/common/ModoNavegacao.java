package br.com.ftcoin.gui.common;

/**
 * Telas principais da interface: o identificador de cada uma no {@code CardLayout} e a chave do texto
 * do botão de navegação.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public enum ModoNavegacao {

    /** Tela de dashboard. */
    DASHBOARD("DASHBOARD", "gui.nav.btn.dashboard"),

    /** Tela de carteiras. */
    CARTEIRAS("CARTEIRAS", "gui.nav.btn.carteiras"),

    /** Tela de compra e venda. */
    MOVIMENTACOES("MOVIMENTACOES", "gui.nav.btn.movimentacoes"),

    /** Tela de cotação e oráculo. */
    COTACAO("COTACAO", "gui.nav.btn.cotacao"),

    /** Tela de relatórios. */
    RELATORIOS("RELATORIOS", "gui.nav.btn.relatorios");

    private final String identificadorCard;
    private final String chaveMensagem;

    ModoNavegacao(String identificadorCard, String chaveMensagem) {
        this.identificadorCard = identificadorCard;
        this.chaveMensagem = chaveMensagem;
    }

    /**
     * Devolve o identificador da tela no {@code CardLayout}.
     *
     * @return o identificador da tela
     */
    public String getIdentificadorCard() {
        return identificadorCard;
    }

    /**
     * Devolve o texto do botão de navegação no idioma ativo.
     *
     * @param mensagens textos do idioma ativo
     * @return o texto do botão
     */
    public String getTituloLocalizado(MensagensGui mensagens) {
        return mensagens.get(chaveMensagem);
    }
}
