package br.com.ftcoin.gui.views.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import br.com.ftcoin.gui.common.OrigemCotacao;
import br.com.ftcoin.gui.common.TemaCores;

/**
 * <p>
 * Componente visual de etiqueta (<i>badge</i>) arredondada para sinalização de estados e origens de dados.
 * </p>
 *
 * <p>
 * Utilizado primariamente na tela de cotações para diferenciar visualmente o <b>Oráculo Externo</b>
 * do <b>Cache Local</b> por meio de cores temáticas de alto contraste.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class BadgeLabel extends JLabel {

    /**
     * Cor de preenchimento do fundo da etiqueta.
     */
    private Color corFundo = new Color(243, 244, 246);

    /**
     * Cor da linha de borda perimetral.
     */
    private Color corBorda = new Color(209, 213, 219);

    /**
     * Raio de curvatura das pontas da etiqueta em pixels.
     */
    private int raioArredondamento = 14;

    /**
     * Constrói a etiqueta com o texto informado centralizado.
     *
     * @param texto Rótulo a ser exibido no badge
     */
    public BadgeLabel(String texto) {
        super(texto, SwingConstants.CENTER);
        setFont(TemaCores.FONTE_BADGE);
        setForeground(TemaCores.TEXTO_PRINCIPAL);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
    }

    /**
     * Configura dinamicamente o texto, ícone e esquema de cores conforme a procedência da cotação.
     *
     * @param origem Instância de {@link OrigemCotacao} que especifica a fonte do dado
     */
    public void configurarOrigem(OrigemCotacao origem) {
        if (origem == null) {
            setText("INDETERMINADO");
            setEstilo(new Color(243, 244, 246), new Color(107, 114, 128), new Color(209, 213, 219));
            return;
        }

        setText(origem.getIcone() + " " + origem.getDescricao());
        setEstilo(origem.getCorFundo(), origem.getCorTexto(), origem.getCorTexto());
    }

    /**
     * Define o estilo cromático do badge especificando cores de fundo, texto e contorno.
     *
     * @param fundo Cor de preenchimento
     * @param texto Cor da tipografia
     * @param borda Cor da borda perimetral
     */
    public void setEstilo(Color fundo, Color texto, Color borda) {
        this.corFundo = fundo;
        this.corBorda = borda;
        setForeground(texto);
        repaint();
    }

    /**
     * Renderiza o fundo com formato de pílula arredondada e contorno suavizado.
     *
     * @param g Contexto gráfico do componente
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(corFundo);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raioArredondamento, raioArredondamento);

        g2.setColor(corBorda);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raioArredondamento, raioArredondamento);

        g2.dispose();
        super.paintComponent(g);
    }
}
