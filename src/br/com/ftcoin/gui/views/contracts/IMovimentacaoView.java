package br.com.ftcoin.gui.views.contracts;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.Posicao;
import br.com.ftcoin.models.TipoOperacao;

/**
 * <p>
 * Contrato de interface da camada de visão ({@code View}) para o Módulo de Movimentações no padrão <b>MVP</b>.
 * </p>
 *
 * <p>
 * Define as operações de renderização de seletores de carteiras, estimativas financeiras em tempo real,
 * histórico de ordens executadas e limpeza de formulários.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public interface IMovimentacaoView {

    /**
     * Atualiza as caixas de seleção de carteiras dos formulários de compra e venda.
     *
     * @param posicoes posição de cada carteira; o saldo aparece no formulário de venda
     */
    void exibirCarteirasDisponiveis(Map<Carteira, Posicao> posicoes);

    /**
     * Exibe as movimentações mais recentes de todas as carteiras.
     *
     * @param movimentacoes movimentações da mais recente para a mais antiga
     * @param carteiras     carteiras indexadas pelo identificador, para mostrar o titular de cada operação
     * @param cotacoes      cotação de cada data das movimentações, usada no valor de cada operação
     */
    void exibirHistoricoMovimentacoes(List<Movimentacao> movimentacoes, Map<Integer, Carteira> carteiras,
            Map<LocalDate, BigDecimal> cotacoes);

    /**
     * Atualiza a prévia financeira de um dos formulários com a cotação estimada e o montante total.
     *
     * @param tipo       operação cujo formulário deve ser atualizado
     * @param cotacao    Valor unitário da cotação aplicada
     * @param valorTotal Valor financeiro total resultante do produto entre quantidade e cotação
     */
    void atualizarEstimativas(TipoOperacao tipo, BigDecimal cotacao, BigDecimal valorTotal);

    /**
     * Mostra nos rótulos de prévia da operação que não há cotação disponível, com o motivo
     * (data sem cotação ou falha ao consultar o oráculo).
     *
     * @param tipo   Operação cuja prévia é atualizada ({@code COMPRA} ou {@code VENDA})
     * @param motivo Texto já traduzido explicando por que não há estimativa
     */
    void informarCotacaoIndisponivel(TipoOperacao tipo, String motivo);

    /**
     * Exibe um diálogo modal com mensagem de confirmação de compra ou venda registrada.
     *
     * @param mensagem Texto formatado com o sucesso da transação
     */
    void exibirMensagemSucesso(String mensagem);

    /**
     * Exibe um diálogo modal com alerta de inconsistência ou falha na validação de campos.
     *
     * @param mensagem Texto explicativo da validação não atendida
     */
    void exibirMensagemErro(String mensagem);

    /**
     * Restaura os campos de texto dos formulários de compra e venda para seus valores padrão.
     */
    void limparFormulario();
}
