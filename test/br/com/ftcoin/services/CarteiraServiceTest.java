package br.com.ftcoin.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.ftcoin.daos.CarteiraDAO;
import br.com.ftcoin.daos.DAOFactory;
import br.com.ftcoin.daos.MovimentacaoDAO;
import br.com.ftcoin.daos.TipoPersistencia;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.RegraNegocioException;
import br.com.ftcoin.models.TipoOperacao;

/**
 * Testes do {@link CarteiraService} com os DAOs em memória: o CRUD de carteiras e a regra de que uma
 * carteira com movimentações não pode ser excluída.
 */
class CarteiraServiceTest {

    private CarteiraService servico;
    private MovimentacaoDAO movimentacaoDAO;

    @BeforeEach
    void preparar() {
        DAOFactory fabrica = DAOFactory.criar(TipoPersistencia.MEMORIA);
        CarteiraDAO carteiraDAO = fabrica.criarCarteiraDAO();
        movimentacaoDAO = fabrica.criarMovimentacaoDAO();
        servico = new CarteiraService(carteiraDAO, movimentacaoDAO);
    }

    @Test
    @DisplayName("Incluir devolve a carteira com identificador e ela passa a ser listada")
    void incluirEListar() {
        Carteira criada = servico.criarCarteira("Ana", "XP");
        servico.criarCarteira("Bia", "Rico");

        assertEquals(1, criada.getId());
        assertEquals(2, servico.listarTodasCarteiras().size());
        assertEquals("Ana", servico.buscarCarteira(1).getNomeTitular());
    }

    @Test
    @DisplayName("Editar altera titular e corretora")
    void editar() {
        Carteira criada = servico.criarCarteira("Ana", "XP");
        servico.editarCarteira(criada.getId(), "Ana Maria", "Clear");

        Carteira lida = servico.buscarCarteira(criada.getId());
        assertEquals("Ana Maria", lida.getNomeTitular());
        assertEquals("Clear", lida.getCorretora());
    }

    @Test
    @DisplayName("Excluir carteira sem movimentações a remove")
    void excluirSemMovimentacoes() {
        Carteira criada = servico.criarCarteira("Ana", "XP");
        servico.excluirCarteira(criada.getId());
        assertTrue(servico.listarTodasCarteiras().isEmpty());
    }

    @Test
    @DisplayName("Exceção: carteira com movimentações não pode ser excluída")
    void excluirComMovimentacoesRecusado() {
        Carteira criada = servico.criarCarteira("Ana", "XP");
        movimentacaoDAO.inserir(new Movimentacao(0, criada.getId(), LocalDate.now(), TipoOperacao.COMPRA, BigDecimal.ONE));

        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> servico.excluirCarteira(criada.getId()));
        assertEquals("erro.carteira.possuiMovimentacoes", erro.getChave());
        assertEquals(1, servico.listarTodasCarteiras().size());
    }

    @Test
    @DisplayName("Exceção: carteira inexistente na consulta, edição e exclusão")
    void carteiraInexistente() {
        assertEquals("erro.carteira.naoEncontrada",
                assertThrows(RegraNegocioException.class, () -> servico.buscarCarteira(99)).getChave());
        assertThrows(RegraNegocioException.class, () -> servico.editarCarteira(99, "A", "B"));
        assertThrows(RegraNegocioException.class, () -> servico.excluirCarteira(99));
    }

    @Test
    @DisplayName("Relatório: carteiras ordenadas por identificador, sem alterar a lista do DAO")
    void listarPorIdentificador() {
        servico.criarCarteira("Carlos", "XP");
        servico.criarCarteira("ana", "Rico");
        servico.criarCarteira("Bia", "Clear");

        List<Carteira> ordenadas = servico.listarPorIdentificador();

        assertEquals(1, ordenadas.get(0).getId());
        assertEquals(2, ordenadas.get(1).getId());
        assertEquals(3, ordenadas.get(2).getId());
        ordenadas.clear();
        assertEquals(3, servico.listarTodasCarteiras().size());
    }

    @Test
    @DisplayName("Relatório: carteiras ordenadas por titular, sem diferenciar maiúsculas, empate por identificador")
    void listarPorTitular() {
        servico.criarCarteira("Carlos", "XP");
        servico.criarCarteira("ana", "Rico");
        servico.criarCarteira("Bia", "Clear");
        servico.criarCarteira("Ana", "Inter");

        List<Carteira> ordenadas = servico.listarPorTitular();

        assertEquals("ana", ordenadas.get(0).getNomeTitular());
        assertEquals("Ana", ordenadas.get(1).getNomeTitular());
        assertEquals("Bia", ordenadas.get(2).getNomeTitular());
        assertEquals("Carlos", ordenadas.get(3).getNomeTitular());
        // Os dois "ana" empatam no nome e ficam em ordem de identificador (2 antes de 4)
        assertEquals(2, ordenadas.get(0).getId());
        assertEquals(4, ordenadas.get(1).getId());
    }

    @Test
    @DisplayName("Limite: sem carteiras as listagens vêm vazias")
    void listagensVazias() {
        assertTrue(servico.listarPorIdentificador().isEmpty());
        assertTrue(servico.listarPorTitular().isEmpty());
    }

    @Test
    @DisplayName("Exceção: titular ou corretora vazios ao incluir")
    void camposObrigatorios() {
        assertThrows(RegraNegocioException.class, () -> servico.criarCarteira("", "XP"));
        assertThrows(RegraNegocioException.class, () -> servico.criarCarteira("Ana", "  "));
        assertTrue(servico.listarTodasCarteiras().isEmpty());
    }
}
