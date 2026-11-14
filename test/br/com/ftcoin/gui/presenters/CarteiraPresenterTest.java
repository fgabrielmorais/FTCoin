package br.com.ftcoin.gui.presenters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.ftcoin.daos.TipoPersistencia;
import br.com.ftcoin.gui.common.ContextoAplicacao;
import br.com.ftcoin.gui.common.MensagensGui;
import br.com.ftcoin.gui.views.contracts.ICarteiraView;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Posicao;
import br.com.ftcoin.models.RegraNegocioException;

/**
 * Testes do {@link CarteiraPresenter} com uma view falsa, escrita à mão: consulta, edição e exclusão por ID,
 * inclusive de carteiras que não existem (mais).
 */
class CarteiraPresenterTest {

    /** View falsa: guarda o que o presenter mandou mostrar e responde à confirmação como configurado. */
    private static final class ViewFalsa implements ICarteiraView {
        final List<String> erros = new ArrayList<>();
        final List<String> sucessos = new ArrayList<>();
        Carteira selecionada;
        Carteira detalhes;
        Carteira emEdicao;
        boolean confirmar;
        int confirmacoesPedidas;

        @Override
        public void exibirCarteiras(Map<Carteira, Posicao> posicoes, BigDecimal cotacaoAtual) {
            // Não é necessário guardar a lista
        }

        @Override
        public void exibirMensagemSucesso(String mensagem) {
            sucessos.add(mensagem);
        }

        @Override
        public void exibirMensagemErro(String mensagem) {
            erros.add(mensagem);
        }

        @Override
        public Carteira getCarteiraSelecionada() {
            return selecionada;
        }

        @Override
        public void exibirDetalhesCarteira(Carteira carteira, Posicao posicao) {
            detalhes = carteira;
        }

        @Override
        public boolean confirmarExclusao(Carteira carteira) {
            confirmacoesPedidas++;
            return confirmar;
        }

        @Override
        public void abrirModalCadastro() {
            // Não usado nestes testes
        }

        @Override
        public void abrirModalEdicao(Carteira carteira) {
            emEdicao = carteira;
        }

        @Override
        public void fecharModal() {
            // Não usado nestes testes
        }
    }

    private ContextoAplicacao contexto;
    private ViewFalsa view;
    private CarteiraPresenter presenter;
    private Carteira ana;

    @BeforeEach
    void preparar() {
        contexto = new ContextoAplicacao(new MensagensGui(MensagensGui.LOCALE_PT), TipoPersistencia.MEMORIA);
        view = new ViewFalsa();
        presenter = new CarteiraPresenter(view, contexto);
        ana = contexto.getCarteiraService().criarCarteira("Ana Souza", "Binance");
    }

    @AfterEach
    void encerrar() {
        contexto.encerrar();
    }

    private String mensagemNaoEncontrada(int id) {
        return contexto.getMensagens().get("erro.carteira.naoEncontrada", id);
    }

    @Test
    @DisplayName("Consultar pelo ID mostra os dados da carteira")
    void consultarExistente() {
        presenter.consultarCarteira(String.valueOf(ana.getId()));

        assertEquals(ana, view.detalhes);
        assertTrue(view.erros.isEmpty());
    }

    @Test
    @DisplayName("Consultar com ID vazio usa a linha selecionada")
    void consultarPelaSelecao() {
        view.selecionada = ana;
        presenter.consultarCarteira("  ");
        assertEquals(ana, view.detalhes);
    }

    @Test
    @DisplayName("Consultar um ID que não existe mostra a mensagem de carteira não encontrada")
    void consultarInexistente() {
        presenter.consultarCarteira("5");

        assertNull(view.detalhes);
        assertEquals(List.of(mensagemNaoEncontrada(5)), view.erros);
        assertEquals("Nenhuma carteira encontrada com o ID 5.", view.erros.get(0));
    }

    @Test
    @DisplayName("ID que não é número, e ID vazio sem linha selecionada, são recusados")
    void idInvalidoOuAusente() {
        presenter.consultarCarteira("abc");
        presenter.consultarCarteira("");

        assertEquals(List.of(contexto.getMensagens().get("gui.carteira.erro.idInvalido"),
                contexto.getMensagens().get("gui.carteira.erro.selecione")), view.erros);
        assertNull(view.detalhes);
    }

    @Test
    @DisplayName("Linha selecionada de uma carteira já excluída é recusada como não encontrada")
    void selecaoDeCarteiraExcluida() {
        view.selecionada = ana;
        contexto.getCarteiraService().excluirCarteira(ana.getId());

        presenter.iniciarEdicao("");

        assertNull(view.emEdicao);
        assertEquals(List.of(mensagemNaoEncontrada(ana.getId())), view.erros);
    }

    @Test
    @DisplayName("Editar abre o formulário só para carteira que existe")
    void iniciarEdicao() {
        presenter.iniciarEdicao(String.valueOf(ana.getId()));
        assertEquals(ana, view.emEdicao);

        view.emEdicao = null;
        presenter.iniciarEdicao("10");
        assertNull(view.emEdicao);
        assertEquals(List.of(mensagemNaoEncontrada(10)), view.erros);
    }

    @Test
    @DisplayName("Excluir um ID que não existe mostra o erro e nem pede confirmação")
    void excluirInexistente() {
        presenter.excluirCarteira("5");

        assertEquals(0, view.confirmacoesPedidas);
        assertEquals(List.of(mensagemNaoEncontrada(5)), view.erros);
    }

    @Test
    @DisplayName("Excluir só acontece depois da confirmação")
    void excluirComConfirmacao() {
        view.confirmar = false;
        presenter.excluirCarteira(String.valueOf(ana.getId()));
        assertEquals(1, view.confirmacoesPedidas);
        assertNotNull(contexto.getCarteiraService().buscarCarteira(ana.getId()));

        view.confirmar = true;
        presenter.excluirCarteira(String.valueOf(ana.getId()));
        assertThrows(RegraNegocioException.class, () -> contexto.getCarteiraService().buscarCarteira(ana.getId()));
        assertTrue(view.sucessos.stream().anyMatch(m -> m.contains(String.valueOf(ana.getId()))));

        // Uma segunda exclusão do mesmo ID já não encontra a carteira
        view.erros.clear();
        presenter.excluirCarteira(String.valueOf(ana.getId()));
        assertEquals(List.of(mensagemNaoEncontrada(ana.getId())), view.erros);
    }
}
