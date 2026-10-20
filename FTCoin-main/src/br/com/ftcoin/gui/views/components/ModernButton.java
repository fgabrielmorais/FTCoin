package br.com.ftcoin.gui.views.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import br.com.ftcoin.gui.common.TemaCores;

/**
 * <p>
 * Componente visual de botão com acabamento moderno, cantos arredondados e microinteração hover.
 * </p>
 *
 * <p>
 * Estende {@link JButton} para fornecer estilização consistente conforme a paleta de cores
 * do sistema, dispensando bordas quadradas nativas e aplicando antialiasing gráfico.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class ModernButton extends JButton {

    /**
     * <p>
     * Enumeração das variantes semânticas de estilo visual do botão moderno.
     * </p>
     *
     * @author Equipe FTCoin
     * @version 1.0.0
     * @since 2026-10-20
     */
    public enum Variante {

        /**
         * Estilo primário com fundo azul-marinho para ações de destaque padrão.
         */
        PRIMARIA(TemaCores.PRIMARIA, TemaCores.PRIMARIA_HOVER, Color.WHITE),

        /**
         * Estilo positivo com fundo verde esmeralda para confirmações e inclusões.
         */
        SUCESSO(TemaCores.POSITIVO_LUCRO, new Color(13, 126, 78), Color.WHITE),

        /**
         * Estilo de alerta com fundo vermelho carmesim para exclusões e cancelamentos.
         */
        PERIGO(TemaCores.NEGATIVO_PREJUIZO, new Color(185, 28, 28), Color.WHITE),

        /**
         * Estilo neutro com fundo cinza suave para botões secundários e filtros.
         */
        NEUTRO(new Color(243, 244, 246), new Color(229, 231, 235), TemaCores.TEXTO_PRINCIPAL);

        /**
         * Cor de preenchimento padrão em repouso.
         */
        final Color fundoPadrao;

        /**
         * Cor de preenchimento acionada sob o ponteiro do mouse (hover).
         */
        final Color fundoHover;

        /**
         * Cor da tipografia textual.
         */
        final Color texto;

        /**
         * Construtor da variante semântica do botão.
         *
         * @param fundoPadrao Cor de repouso
         * @param fundoHover  Cor de sobreposição do cursor
         * @param texto       Cor do texto
         */
        Variante(Color fundoPadrao, Color fundoHover, Color texto) {
            this.fundoPadrao = fundoPadrao;
            this.fundoHover = fundoHover;
            this.texto = texto;
        }

        /**
         * Retorna a cor de fundo padrão da variante.
         *
         * @return A cor {@code Color} de repouso
         */
        public Color getFundoPadrao() {
            return fundoPadrao;
        }

        /**
         * Retorna a cor de fundo para o estado de hover.
         *
         * @return A cor {@code Color} ativada no foco
         */
        public Color getFundoHover() {
            return fundoHover;
        }

        /**
         * Retorna a cor do texto para contraste adequado.
         *
         * @return A cor {@code Color} do texto
         */
        public Color getTexto() {
            return texto;
        }
    }

    /**
     * Variante de estilização visual associada a este botão.
     */
    private final Variante variante;

    /**
     * Estado que indica se o cursor do mouse está atualmente posicionado sobre o botão.
     */
    private boolean mouseOver = false;

    /**
     * Raio de curvatura dos cantos arredondados em pixels.
     */
    private int raioArredondamento = 10;

    /**
     * Constrói um botão moderno com o texto informado e a variante visual escolhida.
     *
     * @param texto    Rótulo textual a ser exibido
     * @param variante Variante semântica de cores; se {@code null}, adota {@code Variante.PRIMARIA}
     */
    public ModernButton(String texto, Variante variante) {
        super(texto);
        this.variante = variante != null ? variante : Variante.PRIMARIA;

        setFont(TemaCores.FONTE_LABEL);
        setForeground(this.variante.texto);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseOver = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseOver = false;
                repaint();
            }
        });
    }

    /**
     * Retorna a variante de estilo aplicada a este botão.
     *
     * @return A {@link Variante} em uso
     */
    public Variante getVariante() {
        return variante;
    }

    /**
     * Retorna o raio de arredondamento das bordas em pixels.
     *
     * @return O valor do raio {@code int}
     */
    public int getRaioArredondamento() {
        return raioArredondamento;
    }

    /**
     * Define o raio de arredondamento das bordas do botão.
     *
     * @param raioArredondamento Novo raio em pixels
     */
    public void setRaioArredondamento(int raioArredondamento) {
        this.raioArredondamento = raioArredondamento;
        repaint();
    }

    /**
     * Renderiza o fundo arredondado e efeitos de transição cromática suave.
     *
     * @param g Contexto gráfico do componente
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Renderiza fundo dinâmico conforme estado do cursor
        Color corFundo = mouseOver ? variante.fundoHover : variante.fundoPadrao;
        g2.setColor(corFundo);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), raioArredondamento, raioArredondamento);

        g2.dispose();
        super.paintComponent(g);
    }
}
