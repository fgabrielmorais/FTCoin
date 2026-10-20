package br.com.ftcoin.gui.views.components;

import java.awt.Component;
import java.math.BigDecimal;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import br.com.ftcoin.gui.common.AppContext;
import br.com.ftcoin.gui.common.GuiFormatador;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.OrigemCotacao;
import br.com.ftcoin.gui.common.TemaCores;
import br.com.ftcoin.models.TipoOperacao;

/**
 * <p>
 * Renderizador especializado de células de tabelas ({@link JTable}) para a interface financeira.
 * </p>
 *
 * <p>
 * Formata valores monetários, percentuais e quantidades com alinhamento adequado à direita,
 * alternância de linhas em estilo zebrado e destaque cromático (verde para lucros e vermelho para prejuízos).
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class CurrencyTableCellRenderer extends DefaultTableCellRenderer {

    /**
     * <p>
     * Modos de exibição suportados pelo renderizador para células de dados.
     * </p>
     *
     * @author Equipe FTCoin
     * @version 1.0.0
     * @since 2026-10-20
     */
    public enum ModoRenderizacao {

        /**
         * Formatação monetária padrão sem destaque cromático.
         */
        MOEDA,

        /**
         * Formatação monetária com cores dinâmicas (verde para positivo, vermelho para negativo).
         */
        MOEDA_COM_COR,

        /**
         * Formatação de quantidade de moedas acompanhada do sufixo institucional FTC.
         */
        QUANTIDADE,

        /**
         * Formatação de percentual com cores dinâmicas (verde para ganho, vermelho para perda).
         */
        PERCENTUAL_COM_COR,

        /**
         * Formatação da fonte de cotação com badge e ícones característicos.
         */
        ORIGEM_BADGE,

        /**
         * Formatação de operações com destaque visual (Compra em verde, Venda em azul-marinho).
         */
        TIPO_OPERACAO
    }

    /**
     * Modo de renderização configurado para esta instância.
     */
    private final ModoRenderizacao modo;

    /**
     * Utilitário de formatação de valores respeitando o {@code Locale} ativo.
     */
    private final GuiFormatador formatador;

    /**
     * Constrói o renderizador de células especificando o modo e o formatador.
     *
     * @param modo       Modo de exibição pretendido; se {@code null}, adota {@code MOEDA}
     * @param formatador Utilitário gráfico de formatação
     */
    public CurrencyTableCellRenderer(ModoRenderizacao modo, GuiFormatador formatador) {
        this.modo = modo != null ? modo : ModoRenderizacao.MOEDA;
        this.formatador = formatador;
    }

    /**
     * Obtém o utilitário de formatação ativo, priorizando o formatador sincronizado
     * com o contexto global {@link AppContext} para suporte imediato à alternância
     * dinâmica de idioma em tempo de execução.
     *
     * @return O {@link GuiFormatador} configurado para a localidade corrente
     */
    private GuiFormatador obterFormatadorAtivo() {
        if (AppContext.getInstancia() != null && AppContext.getInstancia().getMensagens() != null) {
            GuiFormatador formatadorContexto = AppContext.getInstancia().getMensagens().getFormatador();
            if (formatadorContexto != null) {
                return formatadorContexto;
            }
        }
        return this.formatador;
    }

    /**
     * Retorna o componente configurado para renderizar o valor da célula especificada.
     *
     * @param table      A tabela onde a célula reside
     * @param value      O valor da célula a ser formatado
     * @param isSelected Se {@code true}, a célula está selecionada
     * @param hasFocus   Se {@code true}, a célula possui o foco do teclado
     * @param row        Índice da linha da célula
     * @param column     Índice da coluna da célula
     * @return O componente {@link Component} configurado
     */
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
    ) {
        JLabel rotulo = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Aplica padrão zebrado suave em linhas não selecionadas
        if (!isSelected) {
            rotulo.setBackground(row % 2 == 0 ? TemaCores.SUPERFICIE_CARD : TemaCores.SUPERFICIE_HEADER);
        }

        rotulo.setFont(TemaCores.FONTE_PADRAO);
        rotulo.setForeground(TemaCores.TEXTO_PRINCIPAL);

        if (value == null) {
            rotulo.setText("-");
            rotulo.setHorizontalAlignment(SwingConstants.CENTER);
            return rotulo;
        }

        GuiFormatador fmt = obterFormatadorAtivo();

        // Formatação contextual e alinhamento conforme o modo do renderizador
        switch (modo) {
            case MOEDA -> {
                rotulo.setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof BigDecimal bd) {
                    rotulo.setText(fmt != null ? fmt.moeda(bd) : value.toString());
                } else {
                    rotulo.setText(value.toString());
                }
            }
            case MOEDA_COM_COR -> {
                rotulo.setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof BigDecimal bd) {
                    rotulo.setText(fmt != null ? fmt.moeda(bd) : value.toString());
                    if (!isSelected) {
                        if (bd.signum() > 0) {
                            rotulo.setForeground(TemaCores.POSITIVO_LUCRO);
                        } else if (bd.signum() < 0) {
                            rotulo.setForeground(TemaCores.NEGATIVO_PREJUIZO);
                        } else {
                            rotulo.setForeground(TemaCores.TEXTO_SECUNDARIO);
                        }
                    }
                } else {
                    rotulo.setText(value.toString());
                }
            }
            case QUANTIDADE -> {
                rotulo.setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof BigDecimal bd) {
                    rotulo.setText(fmt != null ? fmt.quantidade(bd) : value.toString());
                } else {
                    rotulo.setText(value.toString());
                }
            }
            case PERCENTUAL_COM_COR -> {
                rotulo.setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof BigDecimal bd) {
                    rotulo.setText(fmt != null ? fmt.percentual(bd) : value.toString());
                    if (!isSelected) {
                        if (bd.signum() > 0) {
                            rotulo.setForeground(TemaCores.POSITIVO_LUCRO);
                        } else if (bd.signum() < 0) {
                            rotulo.setForeground(TemaCores.NEGATIVO_PREJUIZO);
                        } else {
                            rotulo.setForeground(TemaCores.TEXTO_SECUNDARIO);
                        }
                    }
                } else {
                    rotulo.setText(value.toString());
                }
            }
            case ORIGEM_BADGE -> {
                rotulo.setHorizontalAlignment(SwingConstants.CENTER);
                if (value instanceof OrigemCotacao origem) {
                    rotulo.setText(origem.getIcone() + " " + origem.getDescricaoLocalizada());
                    if (!isSelected) {
                        rotulo.setForeground(origem.getCorTexto());
                    }
                } else {
                    rotulo.setText(value.toString());
                }
            }
            case TIPO_OPERACAO -> {
                rotulo.setHorizontalAlignment(SwingConstants.CENTER);
                if (value instanceof TipoOperacao tipo) {
                    MensagensGui msg = AppContext.getInstancia().getMensagens();
                    if (tipo == TipoOperacao.COMPRA) {
                        String textoCompra = (msg != null) ? msg.get("operacao.compra").toUpperCase() : "COMPRA";
                        rotulo.setText("🛒 " + textoCompra);
                        if (!isSelected) {
                            rotulo.setForeground(TemaCores.POSITIVO_LUCRO);
                        }
                    } else {
                        String textoVenda = (msg != null) ? msg.get("operacao.venda").toUpperCase() : "VENDA";
                        rotulo.setText("💰 " + textoVenda);
                        if (!isSelected) {
                            rotulo.setForeground(TemaCores.PRIMARIA);
                        }
                    }
                } else {
                    rotulo.setText(value.toString());
                }
            }
        }

        return rotulo;
    }
}
