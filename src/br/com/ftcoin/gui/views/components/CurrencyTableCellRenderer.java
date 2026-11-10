package br.com.ftcoin.gui.views.components;

import java.awt.Component;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
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
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public class CurrencyTableCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Modos de exibição suportados pelo renderizador para células de dados.
     * </p>
     *
     * @author Grupo B.05
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

        /** Percentual sem sinal nem cor, como a participação no portfólio. */
        PERCENTUAL_SIMPLES,

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

    // Transiente: o renderizador é serializável, mas as mensagens não precisam ser
    private final transient MensagensGui mensagens;

    /**
     * Constrói o renderizador de células especificando o modo e as mensagens do idioma ativo.
     *
     * @param modo      Modo de exibição pretendido; se {@code null}, adota {@code MOEDA}
     * @param mensagens Textos e formatador do idioma ativo
     */
    public CurrencyTableCellRenderer(ModoRenderizacao modo, MensagensGui mensagens) {
        this.modo = modo != null ? modo : ModoRenderizacao.MOEDA;
        this.mensagens = mensagens;
    }

    /**
     * Devolve o alinhamento do texto deste renderizador, usado também pelo cabeçalho da coluna.
     *
     * @return {@code SwingConstants.RIGHT} para valores e quantidades, {@code SwingConstants.CENTER} para
     *         origem e tipo de operação
     */
    public int getAlinhamento() {
        return (modo == ModoRenderizacao.ORIGEM_BADGE || modo == ModoRenderizacao.TIPO_OPERACAO)
                ? SwingConstants.CENTER : SwingConstants.RIGHT;
    }

    // Consultado a cada célula, para acompanhar a troca de idioma em tempo de execução
    private GuiFormatador obterFormatadorAtivo() {
        return mensagens.getFormatador();
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

        // Respiro interno; o fundo fica branco (minimalismo: sem zebra)
        rotulo.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        if (!isSelected) {
            rotulo.setBackground(TemaCores.SUPERFICIE_CARD);
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
                            rotulo.setForeground(TemaCores.TEXTO_PRINCIPAL);
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
                            rotulo.setForeground(TemaCores.TEXTO_PRINCIPAL);
                        }
                    }
                } else {
                    rotulo.setText(value.toString());
                }
            }
            case PERCENTUAL_SIMPLES -> {
                rotulo.setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof BigDecimal bd) {
                    rotulo.setText(fmt != null ? fmt.percentualSimples(bd) : value.toString());
                } else {
                    rotulo.setText(value.toString());
                }
            }
            case ORIGEM_BADGE -> {
                rotulo.setHorizontalAlignment(SwingConstants.CENTER);
                if (value instanceof OrigemCotacao origem) {
                    rotulo.setText(origem.getDescricaoLocalizada(mensagens));
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
                    if (tipo == TipoOperacao.COMPRA) {
                        String textoCompra = mensagens.get("operacao.compra").toUpperCase();
                        rotulo.setText(textoCompra);
                        if (!isSelected) {
                            rotulo.setForeground(TemaCores.POSITIVO_LUCRO);
                        }
                    } else {
                        String textoVenda = mensagens.get("operacao.venda").toUpperCase();
                        rotulo.setText(textoVenda);
                        if (!isSelected) {
                            rotulo.setForeground(TemaCores.ALERTA);
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
