package br.com.ftcoin.gui.views.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import br.com.ftcoin.gui.common.TemaCores;

/**
 * <p>
 * Componente visual de cartão moderno para apresentação de indicadores-chave (KPIs).
 * </p>
 *
 * <p>
 * Apresenta cantos arredondados, fundo de superfície limpo, cabeçalho atenuado
 * e valores financeiros com destaque tipográfico em negrito.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class ModernCard extends JPanel {

    /**
     * Rótulo de texto para exibição do título superior do cartão.
     */
    private final JLabel lblTitulo;

    /**
     * Rótulo em destaque tipográfico para o valor numérico ou monetário do indicador.
     */
    private final JLabel lblValor;

    /**
     * Rótulo inferior opcional para informações complementares ou taxas percentuais.
     */
    private final JLabel lblSubtitulo;

    /**
     * Raio de curvatura dos cantos arredondados do cartão em pixels.
     */
    private int raioArredondamento = 12;

    /**
     * Constrói um cartão visual inicializado com título, valor inicial e cor de destaque.
     *
     * @param titulo       Texto do título do indicador
     * @param valorInicial Texto com o montante ou métrica inicial
     * @param corValor     Cor aplicada ao valor em destaque
     */
    public ModernCard(String titulo, String valorInicial, Color corValor) {
        setLayout(new BorderLayout(5, 5));
        setOpaque(false);
        setBackground(TemaCores.SUPERFICIE_CARD);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        lblTitulo = new JLabel(titulo != null ? titulo.toUpperCase() : "");
        lblTitulo.setFont(TemaCores.FONTE_LABEL);
        lblTitulo.setForeground(TemaCores.TEXTO_SECUNDARIO);

        lblValor = new JLabel(valorInicial != null ? valorInicial : "-");
        lblValor.setFont(TemaCores.FONTE_DESTAQUE_NUMERO);
        lblValor.setForeground(corValor != null ? corValor : TemaCores.TEXTO_PRINCIPAL);

        lblSubtitulo = new JLabel("");
        lblSubtitulo.setFont(TemaCores.FONTE_BADGE);
        lblSubtitulo.setForeground(TemaCores.TEXTO_SECUNDARIO);

        JPanel pnlCentro = new JPanel(new BorderLayout(2, 2));
        pnlCentro.setOpaque(false);
        pnlCentro.add(lblTitulo, BorderLayout.NORTH);
        pnlCentro.add(lblValor, BorderLayout.CENTER);
        pnlCentro.add(lblSubtitulo, BorderLayout.SOUTH);

        add(pnlCentro, BorderLayout.CENTER);
        setPreferredSize(new Dimension(190, 85));
    }

    /**
     * Define o título superior do cartão.
     *
     * @param titulo Novo texto do título
     */
    public void setTitulo(String titulo) {
        lblTitulo.setText(titulo != null ? titulo.toUpperCase() : "");
    }

    /**
     * Define o valor monetário ou quantitativo exibido no cartão acompanhado de sua cor.
     *
     * @param valor Texto formatado a ser exibido no centro do cartão
     * @param cor   Cor aplicada ao valor textual
     */
    public void setValor(String valor, Color cor) {
        lblValor.setText(valor);
        if (cor != null) {
            lblValor.setForeground(cor);
        }
    }

    /**
     * Define o texto informativo secundário na base do cartão.
     *
     * @param texto Informação complementar a ser exibida
     */
    public void setSubtitulo(String texto) {
        lblSubtitulo.setText(texto);
    }

    /**
     * Renderiza o fundo arredondado e contornos com suavização de bordas.
     *
     * @param g Contexto gráfico do componente
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Preenchimento do cartão
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raioArredondamento, raioArredondamento);

        // Borda suave externa
        g2.setColor(TemaCores.BORDA_SUAVE);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, raioArredondamento, raioArredondamento);

        g2.dispose();
        super.paintComponent(g);
    }
}
