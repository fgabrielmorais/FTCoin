package br.com.ftcoin.gui.views.contracts;

import java.math.BigDecimal;
import java.util.List;
import br.com.ftcoin.gui.dto.CarteiraDTO;
import br.com.ftcoin.gui.dto.MovimentacaoDTO;

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
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public interface IMovimentacaoView {

    /**
     * Atualiza as caixas de seleção de carteiras disponíveis para operações de compra e venda.
     *
     * @param carteiras Lista de carteiras ativas para preenchimento dos componentes
     */
    void exibirCarteirasDisponiveis(List<CarteiraDTO> carteiras);

    /**
     * Renderiza o histórico de transações na tabela da tela de movimentações.
     *
     * @param movimentacoes Lista contendo as movimentações mais recentes em formato {@link MovimentacaoDTO}
     */
    void exibirHistoricoMovimentacoes(List<MovimentacaoDTO> movimentacoes);

    /**
     * Atualiza os rótulos de prévia financeira com a cotação estimada e o montante total calculado.
     *
     * @param cotacao    Valor unitário da cotação aplicada
     * @param valorTotal Valor financeiro total resultante do produto entre quantidade e cotação
     */
    void atualizarEstimativas(BigDecimal cotacao, BigDecimal valorTotal);

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
