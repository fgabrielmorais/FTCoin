package br.com.ftcoin.gui.views.contracts;

import java.util.List;
import br.com.ftcoin.gui.dto.CarteiraDTO;

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
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public interface ICarteiraView {

    /**
     * Renderiza a listagem de carteiras ativas na tabela visual da interface.
     *
     * @param carteiras Lista contendo os dados imutáveis de carteiras em formato {@link CarteiraDTO}
     */
    void exibirCarteiras(List<CarteiraDTO> carteiras);

    /**
     * Exibe um diálogo modal com mensagem de confirmação e êxito operacional.
     *
     * @param mensagem Texto localizado contendo os detalhes do sucesso
     */
    void exibirMensagemSucesso(String mensagem);

    /**
     * Exibe um diálogo modal com mensagem informativa de falha ou erro de validação.
     *
     * @param mensagem Texto localizado contendo os detalhes do problema
     */
    void exibirMensagemErro(String mensagem);

    /**
     * Retorna a carteira atualmente selecionada pelo usuário na tabela visual.
     *
     * @return O objeto {@link CarteiraDTO} selecionado, ou {@code null} se não houver seleção ativa
     */
    CarteiraDTO getCarteiraSelecionada();

    /**
     * Abre a janela modal com formulário limpo para inclusão de nova carteira.
     */
    void abrirModalCadastro();

    /**
     * Abre a janela modal com os dados cadastrais preenchidos para alteração.
     *
     * @param carteira Dados da carteira a ser modificada
     */
    void abrirModalEdicao(CarteiraDTO carteira);

    /**
     * Fecha e libera a janela modal de formulário atualmente em exibição.
     */
    void fecharModal();
}
