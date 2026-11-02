package br.com.ftcoin.gui.common;

/**
 * <p>
 * Enumeração para os modos de exibição e identificadores das telas principais da interface gráfica.
 * </p>
 *
 * <p>
 * Define as chaves de cartões consumidas pelo layout de navegação ({@code CardLayout}) do
 * {@code MainFrame} e os rótulos de navegação internacionalizados.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public enum ModoNavegacao {

    /**
     * Tela inicial com indicadores consolidados (KPIs) e feed de operações recentes.
     */
    DASHBOARD("DASHBOARD", "gui.nav.btn.dashboard", "📊 Dashboard"),

    /**
     * Tela de gestão, listagem e cadastro de carteiras de custódia.
     */
    CARTEIRAS("CARTEIRAS", "gui.nav.btn.carteiras", "💼 Carteiras"),

    /**
     * Tela de registro e histórico de operações financeiras de compra e venda.
     */
    MOVIMENTACOES("MOVIMENTACOES", "gui.nav.btn.movimentacoes", "🔄 Movimentações"),

    /**
     * Tela de telemetria e consulta assíncrona de preços junto ao Oráculo e Cache.
     */
    COTACAO("COTACAO", "gui.nav.btn.cotacao", "🌐 Cotação & Oráculo"),

    /**
     * Tela analítica com os cinco relatórios financeiros especializados do sistema.
     */
    RELATORIOS("RELATORIOS", "gui.nav.btn.relatorios", "📈 Relatórios");

    /**
     * Identificador do cartão no {@code CardLayout}.
     */
    private final String identificadorCard;

    /**
     * Chave do arquivo de propriedades para o texto do botão.
     */
    private final String chaveMensagem;

    /**
     * Texto padrão de fallback para o botão de navegação.
     */
    private final String tituloPadrao;

    /**
     * Construtor da enumeração do modo de navegação.
     *
     * @param identificadorCard Nome identificador da tela para chave no {@code CardLayout}
     * @param chaveMensagem     Chave no arquivo de propriedades
     * @param tituloPadrao      Texto padrão com ícone para exibição
     */
    ModoNavegacao(String identificadorCard, String chaveMensagem, String tituloPadrao) {
        this.identificadorCard = identificadorCard;
        this.chaveMensagem = chaveMensagem;
        this.tituloPadrao = tituloPadrao;
    }

    /**
     * Retorna o identificador único do cartão para alternância no {@code CardLayout}.
     *
     * @return O identificador da tela {@code String}
     */
    public String getIdentificadorCard() {
        return identificadorCard;
    }

    /**
     * Retorna a chave do bundle de mensagens associada a este modo.
     *
     * @return A chave de mensagem {@code String}
     */
    public String getChaveMensagem() {
        return chaveMensagem;
    }

    /**
     * Retorna o título padrão de fallback deste modo de navegação.
     *
     * @return O título padrão {@code String}
     */
    public String getTituloPadrao() {
        return tituloPadrao;
    }

    /**
     * Retorna o título localizado obtido a partir do provedor central de mensagens da GUI.
     *
     * @return O título internacionalizado {@code String}
     */
    public String getTituloLocalizado() {
        MensagensGui mensagens = AppContext.getInstancia().getMensagens();
        if (mensagens != null) {
            return mensagens.get(chaveMensagem);
        }
        return tituloPadrao;
    }
}
