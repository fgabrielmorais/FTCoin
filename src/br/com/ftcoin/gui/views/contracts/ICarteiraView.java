package br.com.ftcoin.gui.views.contracts;

import java.math.BigDecimal;
import java.util.Map;

import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Posicao;

/**
 * <p>
 * Contrato de interface da camada de visão ({@code View}) para o Módulo de Carteiras no padrão <b>MVP</b>.
 * </p>
 *
 * <p>
 * Define as operações de renderização de carteiras cadastradas, abertura e fechamento de diálogos modais,
 * recuperação da linha selecionada na tabela e disparo de notificações ao usuário.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public interface ICarteiraView {

    /**
     * Exibe as carteiras cadastradas com o saldo e o valor de mercado de cada uma.
     *
     * @param posicoes     posição de cada carteira, na ordem em que devem aparecer
     * @param cotacaoAtual cotação de hoje, usada no valor de mercado
     */
    void exibirCarteiras(Map<Carteira, Posicao> posicoes, BigDecimal cotacaoAtual);

    /**
     * Exibe uma mensagem de sucesso ao usuário.
     *
     * @param mensagem texto já traduzido
     */
    void exibirMensagemSucesso(String mensagem);

    /**
     * Exibe uma mensagem de erro ou de validação ao usuário.
     *
     * @param mensagem texto já traduzido
     */
    void exibirMensagemErro(String mensagem);

    /**
     * Devolve a carteira selecionada na tabela.
     *
     * @return a carteira selecionada, ou {@code null} se nenhuma linha estiver selecionada
     */
    Carteira getCarteiraSelecionada();

    /**
     * Abre o formulário vazio de inclusão de carteira.
     */
    void abrirModalCadastro();

    /**
     * Abre o formulário de edição já preenchido com os dados da carteira.
     *
     * @param carteira carteira a editar
     */
    void abrirModalEdicao(Carteira carteira);

    /**
     * Fecha o formulário de carteira aberto, se houver.
     */
    void fecharModal();
}
