package br.com.ftcoin.gui.views.contracts;

import java.util.Map;

import br.com.ftcoin.gui.common.OrigemCotacao;
import br.com.ftcoin.models.Cotacao;

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
 * @author Grupo B.05
 * @version 1.0.0
 * @since 2026-10-20
 */
public interface ICotacaoView {

    /**
     * Exibe a cotação consultada, de onde ela veio e quanto a consulta demorou.
     *
     * @param cotacao         cotação consultada
     * @param origem          se a cotação veio do Oráculo ou do cache
     * @param tempoRespostaMs duração da consulta, em milissegundos
     */
    void exibirCotacao(Cotacao cotacao, OrigemCotacao origem, long tempoRespostaMs);

    /**
     * Mostra ou esconde o indicador de consulta em andamento.
     *
     * @param carregando {@code true} enquanto a consulta não terminar
     */
    void exibirCarregando(boolean carregando);

    /**
     * Exibe as cotações dos últimos dias e a variação de cada dia em relação ao anterior.
     *
     * @param historico cotações da mais recente para a mais antiga, cada uma com a sua origem
     */
    void exibirHistoricoOscilacoes(Map<Cotacao, OrigemCotacao> historico);

    /**
     * Exibe uma mensagem de sucesso ao usuário.
     *
     * @param mensagem texto já traduzido
     */
    void exibirMensagemSucesso(String mensagem);

    /**
     * Exibe uma mensagem de falha na consulta ao usuário.
     *
     * @param mensagem texto já traduzido
     */
    void exibirMensagemErro(String mensagem);
}
