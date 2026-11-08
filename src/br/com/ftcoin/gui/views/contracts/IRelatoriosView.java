package br.com.ftcoin.gui.views.contracts;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import br.com.ftcoin.gui.common.OrigemCotacao;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Cotacao;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.Posicao;

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
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public interface IRelatoriosView {

    /**
     * Preenche o seletor de carteira usado pelos relatórios 1 e 2.
     *
     * @param carteiras carteiras cadastradas
     */
    void exibirCarteirasFiltro(List<Carteira> carteiras);

    /**
     * Exibe o relatório 1: a posição da carteira selecionada pelo custo médio.
     *
     * @param posicao      posição apurada da carteira
     * @param cotacaoAtual cotação de hoje, usada no valor de mercado, nos ganhos e no ROI
     */
    void exibirRelatorio1Posicao(Posicao posicao, BigDecimal cotacaoAtual);

    /**
     * Exibe o relatório 2: o histórico de operações da carteira selecionada.
     *
     * @param historico movimentações da mais recente para a mais antiga
     * @param cotacoes  cotação de cada data das movimentações, usada no valor de cada operação
     */
    void exibirRelatorio2Historico(List<Movimentacao> historico, Map<LocalDate, BigDecimal> cotacoes);

    /**
     * Exibe o relatório 3: todas as carteiras e quanto cada uma representa do portfólio.
     *
     * @param posicoes     posição de cada carteira
     * @param portfolio    soma de todas as posições
     * @param cotacaoAtual cotação de hoje
     */
    void exibirRelatorio3Consolidado(Map<Carteira, Posicao> posicoes, Posicao portfolio, BigDecimal cotacaoAtual);

    /**
     * Exibe o relatório 4: valor investido, valor atual, resultado e ROI de cada carteira.
     *
     * @param posicoes     posição de cada carteira
     * @param cotacaoAtual cotação de hoje
     */
    void exibirRelatorio4Desempenho(Map<Carteira, Posicao> posicoes, BigDecimal cotacaoAtual);

    /**
     * Exibe o relatório 5: as cotações dos últimos dias e a variação diária.
     *
     * @param historico cotações da mais recente para a mais antiga, cada uma com a sua origem
     */
    void exibirRelatorio5Cotacoes(Map<Cotacao, OrigemCotacao> historico);

    /**
     * Exibe a listagem de carteiras ordenadas pelo identificador.
     *
     * @param carteiras carteiras do menor para o maior identificador
     */
    void exibirCarteirasPorIdentificador(List<Carteira> carteiras);

    /**
     * Exibe a listagem de carteiras ordenadas pelo nome do titular.
     *
     * @param carteiras carteiras em ordem alfabética do titular
     */
    void exibirCarteirasPorTitular(List<Carteira> carteiras);

    /**
     * Exibe uma mensagem de erro ao usuário.
     *
     * @param mensagem texto já traduzido
     */
    void exibirMensagemErro(String mensagem);
}
