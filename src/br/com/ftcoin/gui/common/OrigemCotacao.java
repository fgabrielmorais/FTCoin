package br.com.ftcoin.gui.common;

import java.awt.Color;

/**
 * Procedência de uma cotação exibida na interface: consultada no Oráculo ou recuperada do cache local.
 * O nome exibido vem dos arquivos de mensagens; o enum guarda só a chave e as cores.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public enum OrigemCotacao {

    /** Cotação consultada no Oráculo. */
    ORACULO("gui.cotacao.badge.oraculo", new Color(37, 99, 235), new Color(219, 234, 254)),

    /** Cotação recuperada do cache local. */
    CACHE("gui.cotacao.badge.cache", new Color(107, 114, 128), new Color(243, 244, 246));

    private final String chaveMensagem;
    private final Color corTexto;
    private final Color corFundo;

    OrigemCotacao(String chaveMensagem, Color corTexto, Color corFundo) {
        this.chaveMensagem = chaveMensagem;
        this.corTexto = corTexto;
        this.corFundo = corFundo;
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
     * Devolve o nome da origem no idioma ativo.
     *
     * @param mensagens textos do idioma ativo
     * @return o nome da origem
     */
    public String getDescricaoLocalizada(MensagensGui mensagens) {
        return mensagens.get(chaveMensagem);
    }
}
