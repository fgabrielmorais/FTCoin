package br.com.ftcoin.gui.views.contracts;

import java.util.List;
import br.com.ftcoin.gui.dto.CarteiraDTO;
import br.com.ftcoin.gui.dto.MovimentacaoDTO;
import br.com.ftcoin.gui.dto.PosicaoRelatorioDTO;
import br.com.ftcoin.gui.dto.RelatorioConsolidadoItemDTO;
import br.com.ftcoin.gui.dto.RelatorioDesempenhoDTO;
import br.com.ftcoin.gui.dto.RelatorioHistoricoCotacaoDTO;

/**
 * <p>
 * Contrato de interface da camada de visão ({@code View}) para o Módulo de Relatórios no padrão <b>MVP</b>.
 * </p>
 *
 * <p>
 * Especifica os métodos de apresentação dos cinco relatórios financeiros do sistema FTCoin:
 * </p>
 * <ul>
 *     <li><b>Relatório 1:</b> Posição Consolidada da Carteira (CMP).</li>
 *     <li><b>Relatório 2:</b> Histórico e Extrato de Operações da Carteira.</li>
 *     <li><b>Relatório 3:</b> Consolidado Geral do Portfólio.</li>
 *     <li><b>Relatório 4:</b> Análise de Desempenho e Lucratividade (ROI).</li>
 *     <li><b>Relatório 5:</b> Histórico de Oscilação do Oráculo de Cotações.</li>
 * </ul>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public interface IRelatoriosView {

    /**
     * Atualiza a caixa de seleção com as carteiras disponíveis para filtragem nos relatórios individuais.
     *
     * @param carteiras Lista contendo os dados básicos das carteiras cadastradas
     */
    void exibirCarteirasFiltro(List<CarteiraDTO> carteiras);

    /**
     * Atualiza os cartões analíticos do Relatório 1 com os indicadores de posição e custo médio ponderado.
     *
     * @param posicao Objeto imutável contendo os cálculos de saldo, CMP, valor de mercado e resultados
     */
    void exibirRelatorio1Posicao(PosicaoRelatorioDTO posicao);

    /**
     * Renderiza na tabela do Relatório 2 o extrato cronológico de operações da carteira selecionada.
     *
     * @param historico Lista com as movimentações filtradas da carteira
     */
    void exibirRelatorio2Historico(List<MovimentacaoDTO> historico);

    /**
     * Renderiza na tabela do Relatório 3 a visão horizontal de todas as carteiras e participações percentuais.
     *
     * @param consolidado Lista de itens consolidados do portfólio
     */
    void exibirRelatorio3Consolidado(List<RelatorioConsolidadoItemDTO> consolidado);

    /**
     * Renderiza na tabela do Relatório 4 a avaliação de retorno financeiro e taxa de rentabilidade (ROI).
     *
     * @param desempenho Lista com as métricas de lucratividade calculadas por carteira
     */
    void exibirRelatorio4Desempenho(List<RelatorioDesempenhoDTO> desempenho);

    /**
     * Renderiza na tabela do Relatório 5 a série temporal de cotações emitidas pelo oráculo.
     *
     * @param cotacoes Lista contendo o histórico diário de oscilações e fontes
     */
    void exibirRelatorio5Cotacoes(List<RelatorioHistoricoCotacaoDTO> cotacoes);

    /**
     * Exibe um diálogo modal com mensagem de falha na geração ou atualização dos relatórios.
     *
     * @param mensagem Texto explicativo da falha ocorrida
     */
    void exibirMensagemErro(String mensagem);
}
