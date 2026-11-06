package br.com.ftcoin.gui.views.contracts;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.Posicao;

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
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public interface IDashboardView {

    /**
     * Exibe os indicadores do portfólio inteiro.
     *
     * @param portfolio           soma das posições de todas as carteiras
     * @param quantidadeCarteiras número de carteiras cadastradas
     * @param cotacaoAtual        cotação de hoje
     */
    void exibirIndicadores(Posicao portfolio, int quantidadeCarteiras, BigDecimal cotacaoAtual);

    /**
     * Exibe as movimentações mais recentes de todas as carteiras.
     *
     * @param movimentacoes movimentações da mais recente para a mais antiga
     * @param carteiras     carteiras indexadas pelo identificador, para mostrar o titular de cada operação
     * @param cotacoes      cotação de cada data das movimentações, usada no valor de cada operação
     */
    void exibirUltimasMovimentacoes(List<Movimentacao> movimentacoes, Map<Integer, Carteira> carteiras,
            Map<LocalDate, BigDecimal> cotacoes);

    /**
     * Exibe uma mensagem de erro ao usuário.
     *
     * @param mensagem texto já traduzido
     */
    void exibirMensagemErro(String mensagem);
}
