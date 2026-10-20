package br.com.ftcoin.gui.common;

import java.awt.Color;
import java.awt.Font;

/**
 * <p>
 * Catálogo unificado de paleta de cores, tipografia e diretrizes de estilo visual para a interface.
 * </p>
 *
 * <p>
 * Define tokens de <i>design system</i> focados em consistência, legibilidade e conformidade
 * com os padrões de experiência do usuário ({@code UI/UX}).
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public final class TemaCores {

    /**
     * Construtor privado para impedir instanciação da classe utilitária de estilos visuais.
     */
    private TemaCores() {
        // Construtor privado para classe utilitária
    }

    /**
     * Tom azul-marinho profundo utilizado em cabeçalhos, títulos e elementos de destaque primário.
     */
    public static final Color PRIMARIA = new Color(26, 43, 76);

    /**
     * Tom alternativo de azul acionado durante o estado de foco e sobreposição do cursor (hover).
     */
    public static final Color PRIMARIA_HOVER = new Color(38, 64, 114);

    /**
     * Tom índigo moderno para botões secundários e seletores de relevância média.
     */
    public static final Color SECUNDARIA = new Color(79, 70, 229);

    /**
     * Tom ciano para acentuações visuais e indicadores numéricos especiais.
     */
    public static final Color ACCENT = new Color(14, 165, 233);

    /**
     * Cor neutra suave aplicada ao fundo de janelas e painéis principais.
     */
    public static final Color FUNDO_APP = new Color(243, 244, 246);

    /**
     * Cor branca pura para preenchimento de cartões, formulários e células de tabelas.
     */
    public static final Color SUPERFICIE_CARD = new Color(255, 255, 255);

    /**
     * Cor cinza muito clara para cabeçalhos de tabela, barras de menu e rodapés de status.
     */
    public static final Color SUPERFICIE_HEADER = new Color(249, 250, 251);

    /**
     * Tom cinza claro para linhas divisórias sutis e contornos de cartões.
     */
    public static final Color BORDA_SUAVE = new Color(229, 231, 235);

    /**
     * Tom cinza médio para divisores estruturais e separadores de seções.
     */
    public static final Color BORDA_DIVISOR = new Color(209, 213, 219);

    /**
     * Tom verde esmeralda para representação explícita de lucros, variações positivas e compras.
     */
    public static final Color POSITIVO_LUCRO = new Color(16, 149, 93);

    /**
     * Fundo verde sutil para badges e células com resultado lucrativo.
     */
    public static final Color POSITIVO_FUNDO = new Color(236, 253, 245);

    /**
     * Tom vermelho carmesim para representação explícita de prejuízos, perdas e alertas de exclusão.
     */
    public static final Color NEGATIVO_PREJUIZO = new Color(220, 38, 38);

    /**
     * Fundo vermelho sutil para badges e células com resultado deficitário.
     */
    public static final Color NEGATIVO_FUNDO = new Color(254, 242, 242);

    /**
     * Cor de alto contraste para leitura de textos principais e títulos da interface.
     */
    public static final Color TEXTO_PRINCIPAL = new Color(17, 24, 39);

    /**
     * Cor atenuada para legendas, metadados secundários e rótulos auxiliares.
     */
    public static final Color TEXTO_SECUNDARIO = new Color(107, 114, 128);

    /**
     * Cor branca para tipografia sobreposta a botões e cabeçalhos escuros.
     */
    public static final Color TEXTO_CLARO = new Color(255, 255, 255);

    /**
     * Fonte em negrito de tamanho 22 para títulos principais de telas.
     */
    public static final Font FONTE_TITULO_GRANDE = new Font("Segoe UI", Font.BOLD, 22);

    /**
     * Fonte em negrito de tamanho 16 para títulos de seções internas e tabelas.
     */
    public static final Font FONTE_TITULO_MEDIO = new Font("Segoe UI", Font.BOLD, 16);

    /**
     * Fonte regular de tamanho 13 para subtítulos e mensagens da barra de status.
     */
    public static final Font FONTE_SUBTITULO = new Font("Segoe UI", Font.PLAIN, 13);

    /**
     * Fonte em negrito de tamanho 12 para rótulos de campos e cabeçalhos de colunas.
     */
    public static final Font FONTE_LABEL = new Font("Segoe UI", Font.BOLD, 12);

    /**
     * Fonte regular de tamanho 13 para campos de entrada e texto corrido.
     */
    public static final Font FONTE_PADRAO = new Font("Segoe UI", Font.PLAIN, 13);

    /**
     * Fonte em negrito de tamanho 20 para destaque de valores numéricos e saldos em cartões.
     */
    public static final Font FONTE_DESTAQUE_NUMERO = new Font("Segoe UI", Font.BOLD, 20);

    /**
     * Fonte em negrito de tamanho 11 para badges e etiquetas de estado.
     */
    public static final Font FONTE_BADGE = new Font("Segoe UI", Font.BOLD, 11);
}
