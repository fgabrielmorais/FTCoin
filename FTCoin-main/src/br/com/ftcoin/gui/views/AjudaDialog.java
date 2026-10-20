package br.com.ftcoin.gui.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import br.com.ftcoin.gui.common.AppContext;
import br.com.ftcoin.gui.common.Info;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.common.TemaCores;
import br.com.ftcoin.gui.views.components.ModernButton;

/**
 * <p>
 * Diálogo modal de Ajuda contendo o Manual Estendido do Usuário do <b>FTCoin Studio</b>.
 * </p>
 *
 * <p>
 * Documenta os casos de uso do sistema, regras contábeis do Custo Médio Ponderado (CMP),
 * integração com o Oráculo de cotações e modos de persistência.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class AjudaDialog extends JDialog {

    /**
     * Componente para exibição de texto com formatação HTML.
     */
    private final JEditorPane editorTexto;

    /**
     * Botão para fechamento da janela de manual.
     */
    private final ModernButton botaoFechar;

    /**
     * Constrói o diálogo modal do manual do sistema.
     *
     * @param janelaPai Janela principal proprietária deste diálogo
     */
    public AjudaDialog(Frame janelaPai) {
        super(janelaPai, AppContext.getInstancia().getMensagens().get("gui.ajuda.dialog.titulo"), true);
        setSize(720, 560);
        setLocationRelativeTo(janelaPai);
        setLayout(new BorderLayout());

        MensagensGui mensagens = AppContext.getInstancia().getMensagens();

        editorTexto = new JEditorPane();
        editorTexto.setEditable(false);
        editorTexto.setContentType("text/html");
        editorTexto.setBackground(TemaCores.SUPERFICIE_CARD);
        editorTexto.setText(gerarConteudoManualHtml());
        editorTexto.setCaretPosition(0);

        JScrollPane painelRolagem = new JScrollPane(editorTexto);
        painelRolagem.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(painelRolagem, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        painelBotoes.setBackground(TemaCores.SUPERFICIE_HEADER);
        painelBotoes.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TemaCores.BORDA_SUAVE));

        botaoFechar = new ModernButton(mensagens.get("gui.ajuda.btn.fechar"), ModernButton.Variante.PRIMARIA);
        botaoFechar.setPreferredSize(new Dimension(140, 34));
        botaoFechar.addActionListener(e -> dispose());
        painelBotoes.add(botaoFechar);

        add(painelBotoes, BorderLayout.SOUTH);
    }

    /**
     * Retorna a documentação operacional e arquitetural estruturada em formato HTML.
     *
     * @return O conteúdo textual formatado {@code String}
     */
    private String gerarConteudoManualHtml() {
        return """
            <html>
            <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; padding: 15px; color: #1f2937;">
                <h1 style="color: #1a2b4c; margin-bottom: 5px;">Manual Estendido do Usuário</h1>
                <p style="color: #6b7280; font-size: 13px;">""" + Info.NOME_SISTEMA + " - Versão " + Info.VERSAO + " | Arquitetura POO & MVP</p>" + """
                <hr style="border: 0; border-top: 1px solid #e5e7eb; margin: 15px 0;">

                <h3 style="color: #2563eb;">1. Visão Geral e Inicialização</h3>
                <p>O <b>FTCoin Studio</b> é uma plataforma profissional para gestão de carteiras de criptomoedas, registro de operações e apuração de resultados financeiros.
                O sistema permite operar em modo de <b>Memória Volátil</b> (ideal para simulações e testes rápidos) ou com <b>Banco de Dados MariaDB</b> para persistência definitiva.</p>

                <h3 style="color: #2563eb;">2. Módulo de Carteiras (View + Presenter)</h3>
                <ul>
                    <li><b>Inclusão de Carteira:</b> No menu ou na aba <i>Carteiras</i>, clique em <i>+ Nova Carteira</i> e informe o Nome do Titular e a Corretora. Ambos os campos são validados quanto a preenchimento e tamanho.</li>
                    <li><b>Edição:</b> Selecione a carteira na tabela e clique em <i>Editar</i> para atualizar dados cadastrais.</li>
                    <li><b>Exclusão:</b> Uma carteira que possua movimentações registradas não pode ser excluída, garantindo a integridade dos livros contábeis.</li>
                </ul>

                <h3 style="color: #2563eb;">3. Módulo de Movimentações (Compra e Venda)</h3>
                <ul>
                    <li><b>Compra:</b> Aumenta o saldo sob custódia e o custo total investido, recalculando automaticamente o Custo Médio Ponderado.</li>
                    <li><b>Venda:</b> Mantém inalterado o Custo Médio e realiza imediatamente o lucro ou prejuízo da quantidade alienada: <i>Qtd × (Cotação - Custo Médio)</i>. O sistema bloqueia vendas que excedam o saldo disponível.</li>
                </ul>

                <h3 style="color: #2563eb;">4. Oráculo de Cotações e Cache Inteligente</h3>
                <p>As cotações de moedas são processadas em <b>Virtual Threads</b>. A tela de cotação exibe um badge visual colorido indicando:</p>
                <ul>
                    <li><span style="color: #15803d; font-weight: bold;">[ORÁCULO EXTERNO]</span>: Consulta direta efetuada ao provedor com round-trip simulado.</li>
                    <li><span style="color: #1d4ed8; font-weight: bold;">[CACHE LOCAL]</span>: Resposta instantânea recuperada da memória de alta velocidade (TTL configurado).</li>
                </ul>

                <h3 style="color: #2563eb;">5. Módulo de Relatórios Financeiros (5 Relatórios)</h3>
                <ol>
                    <li><b>Posição Consolidada (CMP):</b> Saldo, Custo Médio, Valor a Custo, Valor de Mercado e Ganhos Realizado, Não Realizado e Total.</li>
                    <li><b>Histórico de Operações:</b> Extrato cronológico com tipo de operação, datas e valores totais.</li>
                    <li><b>Consolidado do Portfólio:</b> Visão horizontal de todas as carteiras com percentual de participação patrimonial.</li>
                    <li><b>Desempenho & Lucratividade:</b> Ranking de rentabilidade com ROI (%) e saldo líquido em cores (verde para lucro, vermelho para prejuízo).</li>
                    <li><b>Histórico do Oráculo:</b> Análise da curva diária de oscilação de preço da FTCoins.</li>
                </ol>

                <h3 style="color: #2563eb;">6. Seletor de Idioma e Banco de Dados</h3>
                <p>Na barra superior, o usuário pode alternar a qualquer momento entre <b>Português (Brasil)</b> e <b>English (US)</b>, ou trocar a camada de persistência entre <b>Memória</b> e <b>MariaDB</b> em tempo de execução sem reiniciar o programa.</p>

                <h3 style="color: #2563eb;">7. Contato e Suporte da Equipe de Desenvolvimento</h3>
                <p>Para dúvidas técnicas, suporte operacional ou informações acadêmicas sobre o <b>FTCoin Studio</b>, entre em contato com os membros da equipe de desenvolvimento:</p>
                <ul style="font-size: 13px; line-height: 1.8;">
                    <li><b>Ana:</b> ana@dac.unicamp.br</li>
                    <li><b>Arthur:</b> arthur@dac.unicamp.br</li>
                    <li><b>Erik:</b> erik@dac.unicamp.br</li>
                    <li><b>Gabriel Morais Felix:</b> fgabrielmorais@gmail.com</li>
                    <li><b>Hyago:</b> hyago@dac.unicamp.br</li>
                    <li><b>Isabela Dumas:</b> isabeladumas12@gmail.com</li>
                    <li><b>Pedro:</b> pedro@dac.unicamp.br</li>
                    <li><b>Vitor:</b> vitor@dac.unicamp.br</li>
                </ul>
            </body>
            </html>
            """;
    }
}
