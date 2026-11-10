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
 * Botão de visual minimalista, com cantos suaves e efeito de passagem do mouse.
 * </p>
 *
 * <p>
 * A cor indica o tipo da ação: azul-marinho para a ação principal, verde para comprar, âmbar para vender,
 * vermelho para sair ou excluir, e só contorno ou texto para as ações secundárias.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.1.0
 * @since 2026-10-20
 */
public class ModernButton extends JButton {

    private static final long serialVersionUID = 1L;

    /**
     * Estilos visuais disponíveis para o botão.
     */
    public enum Variante {

        /** Ação principal da tela: fundo azul-marinho e texto branco. */
        PRIMARIA(TemaCores.PRIMARIA, TemaCores.PRIMARIA_HOVER, Color.WHITE, null),

        /** Ação positiva, como comprar: fundo verde e texto branco. */
        SUCESSO(TemaCores.POSITIVO_LUCRO, new Color(13, 126, 78), Color.WHITE, null),

        /** Ação de atenção, como vender: fundo âmbar e texto branco. */
        ALERTA(TemaCores.ALERTA, TemaCores.ALERTA_HOVER, Color.WHITE, null),

        /** Ação que encerra ou apaga, como sair: fundo vermelho e texto branco. */
        PERIGO(TemaCores.NEGATIVO_PREJUIZO, new Color(185, 28, 28), Color.WHITE, null),

        /** Ação destrutiva secundária, como excluir: fundo branco, texto e contorno vermelhos. */
        PERIGO_SUAVE(Color.WHITE, TemaCores.NEGATIVO_FUNDO, TemaCores.NEGATIVO_PREJUIZO, TemaCores.NEGATIVO_PREJUIZO),

        /** Ação secundária: fundo branco, contorno cinza e texto escuro. */
        NEUTRO(Color.WHITE, TemaCores.SUPERFICIE_HEADER, TemaCores.TEXTO_PRINCIPAL, TemaCores.BORDA_DIVISOR),

        /** Menu de navegação: sem fundo nem contorno, só texto; o item ativo recebe um sublinhado. */
        NAVEGACAO(null, TemaCores.BORDA_SUAVE, TemaCores.TEXTO_SECUNDARIO, null);

        final Color fundoPadrao;

        final Color fundoHover;

        final Color texto;

        final Color contorno;

        Variante(Color fundoPadrao, Color fundoHover, Color texto, Color contorno) {
            this.fundoPadrao = fundoPadrao;
            this.fundoHover = fundoHover;
            this.texto = texto;
            this.contorno = contorno;
        }

        /**
         * Devolve a cor de fundo do botão em repouso.
         *
         * @return a cor de fundo, ou {@code null} se o botão não tem fundo
         */
        public Color getFundoPadrao() {
            return fundoPadrao;
        }

        /**
         * Devolve a cor de fundo com o mouse sobre o botão.
         *
         * @return a cor de fundo ao passar o mouse
         */
        public Color getFundoHover() {
            return fundoHover;
        }

        /**
         * Devolve a cor do texto.
         *
         * @return a cor do texto
         */
        public Color getTexto() {
            return texto;
        }
    }

    /** Estilo visual do botão. */
    private final Variante variante;

    /** Indica se o mouse está sobre o botão. */
    private boolean mouseOver = false;

    /** Indica se é o item ativo do menu de navegação. */
    private boolean ativo = false;

    /** Raio dos cantos, em pixels. */
    private int raioArredondamento = 8;

    /**
     * Cria o botão com o texto e o estilo informados.
     *
     * @param texto texto exibido no botão
     * @param variante estilo visual; {@code null} equivale a {@link Variante#PRIMARIA}
     */
    public ModernButton(String texto, Variante variante) {
        super(texto);
        this.variante = variante != null ? variante : Variante.PRIMARIA;

        setFont(TemaCores.FONTE_LABEL);
        setForeground(this.variante.texto);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
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
     * Devolve o estilo do botão.
     *
     * @return a variante
     */
    public Variante getVariante() {
        return variante;
    }

    /**
     * Marca o botão como o item ativo do menu de navegação: texto escuro e sublinhado.
     *
     * @param ativo {@code true} para o item da tela que está aberta
     */
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
        setForeground(ativo ? TemaCores.ACCENT : variante.texto);
        repaint();
    }

    /**
     * Devolve o raio dos cantos.
     *
     * @return o raio, em pixels
     */
    public int getRaioArredondamento() {
        return raioArredondamento;
    }

    /**
     * Define o raio dos cantos.
     *
     * @param raioArredondamento raio, em pixels
     */
    public void setRaioArredondamento(int raioArredondamento) {
        this.raioArredondamento = raioArredondamento;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fundo: muda ao passar o mouse; o menu de navegação só tem fundo no hover
        Color corFundo = mouseOver ? variante.fundoHover : variante.fundoPadrao;
        if (corFundo != null && !(variante == Variante.NAVEGACAO && ativo)) {
            g2.setColor(corFundo);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raioArredondamento, raioArredondamento);
        }

        // Contorno fino nos botões secundários
        if (variante.contorno != null) {
            g2.setColor(variante.contorno);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raioArredondamento, raioArredondamento);
        }

        // Sublinhado do item ativo do menu
        if (variante == Variante.NAVEGACAO && ativo) {
            g2.setColor(TemaCores.ACCENT);
            g2.fillRect(14, getHeight() - 3, getWidth() - 28, 2);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
