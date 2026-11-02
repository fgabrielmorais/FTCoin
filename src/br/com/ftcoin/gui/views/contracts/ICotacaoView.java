package br.com.ftcoin.gui.views.contracts;

import java.util.List;
import br.com.ftcoin.gui.dto.CotacaoDTO;
import br.com.ftcoin.gui.dto.RelatorioHistoricoCotacaoDTO;

/**
 * <p>
 * Contrato de interface da camada de visão ({@code View}) para o Módulo de Cotações no padrão <b>MVP</b>.
 * </p>
 *
 * <p>
 * Declara os métodos de atualização dos indicadores de preço, badge visual colorido de origem
 * (Oráculo versus Cache), controle da barra de carregamento assíncrono e histórico de oscilações.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public interface ICotacaoView {

    /**
     * Atualiza os componentes visuais com os dados da cotação consultada.
     *
     * @param cotacao Objeto imutável contendo preço, origem e métricas de telemetria
     */
    void exibirCotacao(CotacaoDTO cotacao);

    /**
     * Alterna o estado visual de processamento da tela (barra de progresso e botões).
     *
     * @param carregando {@code true} para indicar carregamento em andamento; {@code false} para repouso
     */
    void exibirCarregando(boolean carregando);

    /**
     * Renderiza o histórico de oscilações diárias recentes na tabela da tela.
     *
     * @param historico Lista de registros de cotação dos últimos períodos
     */
    void exibirHistoricoOscilacoes(List<RelatorioHistoricoCotacaoDTO> historico);

    /**
     * Exibe um diálogo modal com mensagem de confirmação e êxito operacional.
     *
     * @param mensagem Texto localizado contendo os detalhes do sucesso
     */
    void exibirMensagemSucesso(String mensagem);

    /**
     * Exibe um diálogo modal com mensagem de falha ou indisponibilidade na consulta.
     *
     * @param mensagem Texto explicativo da falha ocorrida
     */
    void exibirMensagemErro(String mensagem);
}
