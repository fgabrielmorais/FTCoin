package br.com.ftcoin.gui.views.contracts;

import br.com.ftcoin.gui.dto.ResumoDashboardDTO;

/**
 * <p>
 * Contrato de interface da camada de visão ({@code View}) para a Tela Principal do Dashboard no padrão <b>MVP</b>.
 * </p>
 *
 * <p>
 * Estabelece a assinatura para recepção de métricas patrimoniais agregadas, montantes em custódia
 * e histórico das movimentações recentes do sistema.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public interface IDashboardView {

    /**
     * Atualiza os cartões de KPI e a tabela de histórico recente com os dados resumidos consolidados.
     *
     * @param resumo Objeto imutável contendo os indicadores de desempenho e a lista de transações recentes
     */
    void exibirResumo(ResumoDashboardDTO resumo);

    /**
     * Exibe um diálogo modal com mensagem de falha ou erro ao carregar os indicadores do dashboard.
     *
     * @param mensagem Texto localizado contendo os detalhes do problema
     */
    void exibirMensagemErro(String mensagem);
}
