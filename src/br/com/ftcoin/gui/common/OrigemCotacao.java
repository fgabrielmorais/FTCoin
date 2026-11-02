package br.com.ftcoin.gui.common;

import java.awt.Color;

/**
 * <p>
 * Enumeração que identifica a procedência dos dados de cotação obtidos pelo sistema.
 * </p>
 *
 * <p>
 * Diferencia se o preço foi consultado diretamente no <b>Oráculo Externo</b> ou recuperado
 * a partir do <b>Cache Local</b> em memória de alta velocidade.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public enum OrigemCotacao {

    /**
     * Dado recuperado diretamente através de chamada simulada ao serviço do oráculo.
     */
    ORACULO("ORÁCULO EXTERNO", new Color(46, 125, 50), new Color(232, 245, 233), "🌐"),

    /**
     * Dado obtido instantaneamente a partir da memória cache local.
     */
    CACHE("CACHE LOCAL", new Color(21, 101, 192), new Color(227, 242, 253), "⚡");

    /**
     * Descrição textual de apresentação da origem.
     */
    private final String descricao;

    /**
     * Cor primária de texto para contraste visual no badge.
     */
    private final Color corTexto;

    /**
     * Cor suave de fundo do componente visual de identificação.
     */
    private final Color corFundo;

    /**
     * Emoji ou símbolo gráfico representativo da fonte.
     */
    private final String icone;

    /**
     * Constrói uma opção da enumeração de origem da cotação.
     *
     * @param descricao Rótulo textual da fonte
     * @param corTexto  Cor de contraste da tipografia
     * @param corFundo  Cor de preenchimento do componente
     * @param icone     Símbolo representativo da origem
     */
    OrigemCotacao(String descricao, Color corTexto, Color corFundo, String icone) {
        this.descricao = descricao;
        this.corTexto = corTexto;
        this.corFundo = corFundo;
        this.icone = icone;
    }

    /**
     * Retorna a descrição textual desta origem de dados.
     *
     * @return O texto descritivo {@code String}
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Retorna a cor de texto recomendada para renderização do rótulo.
     *
     * @return A cor {@code Color} da tipografia
     */
    public Color getCorTexto() {
        return corTexto;
    }

    /**
     * Retorna a cor de fundo recomendada para o componente de destaque.
     *
     * @return A cor {@code Color} de superfície
     */
    public Color getCorFundo() {
        return corFundo;
    }

    /**
     * Retorna o ícone gráfico associado a esta origem de dados.
     *
     * @return O símbolo textual {@code String}
     */
    public String getIcone() {
        return icone;
    }

    /**
     * Retorna a descrição devidamente internacionalizada conforme o {@link java.util.Locale}
     * ativo no {@link AppContext} da aplicação.
     *
     * @return A descrição localizada correspondente a esta origem de cotação
     */
    public String getDescricaoLocalizada() {
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();
        if (mensagens != null) {
            String chave = (this == ORACULO) ? "gui.cotacao.badge.oraculo" : "gui.cotacao.badge.cache";
            return mensagens.get(chave);
        }
        return descricao;
    }
}
