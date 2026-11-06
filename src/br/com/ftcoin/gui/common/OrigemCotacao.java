package br.com.ftcoin.gui.common;

import java.awt.Color;

/**
 * Procedência de uma cotação exibida na interface: consultada no Oráculo ou recuperada do cache local.
 * O nome exibido vem dos arquivos de mensagens; o enum guarda só a chave, as cores e o ícone.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public enum OrigemCotacao {

    /** Cotação consultada no Oráculo. */
    ORACULO("gui.cotacao.badge.oraculo", new Color(46, 125, 50), new Color(232, 245, 233), "🌐"),

    /** Cotação recuperada do cache local. */
    CACHE("gui.cotacao.badge.cache", new Color(21, 101, 192), new Color(227, 242, 253), "⚡");

    private final String chaveMensagem;
    private final Color corTexto;
    private final Color corFundo;
    private final String icone;

    OrigemCotacao(String chaveMensagem, Color corTexto, Color corFundo, String icone) {
        this.chaveMensagem = chaveMensagem;
        this.corTexto = corTexto;
        this.corFundo = corFundo;
        this.icone = icone;
    }

    /**
     * Devolve a cor do texto do selo de origem.
     *
     * @return a cor do texto
     */
    public Color getCorTexto() {
        return corTexto;
    }

    /**
     * Devolve a cor de fundo do selo de origem.
     *
     * @return a cor de fundo
     */
    public Color getCorFundo() {
        return corFundo;
    }

    /**
     * Devolve o ícone exibido antes do nome da origem.
     *
     * @return o ícone
     */
    public String getIcone() {
        return icone;
    }

    /**
     * Devolve o nome da origem no idioma ativo.
     *
     * @param mensagens textos do idioma ativo
     * @return o nome da origem
     */
    public String getDescricaoLocalizada(MensagensGui mensagens) {
        return mensagens.get(chaveMensagem);
    }
}
