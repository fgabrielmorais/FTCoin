package br.com.ftcoin.daos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.RegraNegocioException;

/**
 * Testes do {@link CarteiraDAOMemoria}: identificadores sequenciais, busca, atualização e exclusão.
 */
class CarteiraDAOMemoriaTest {

    private CarteiraDAOMemoria dao;

    @BeforeEach
    void preparar() {
        dao = new CarteiraDAOMemoria();
    }

    @Test
    @DisplayName("Inserir gera os identificadores 1, 2, 3... em sequência")
    void inserirGeraIdsSequenciais() {
        for (int esperado = 1; esperado <= 10; esperado++) {
            Carteira carteira = new Carteira(0, "Titular " + esperado, "Corretora");
            dao.inserir(carteira);
            assertEquals(esperado, carteira.getId());
        }
        assertEquals(10, dao.listarTodas().size());
    }

    @Test
    @DisplayName("Buscar devolve a carteira pelo id, ou null se não existir")
    void buscar() {
        Carteira ana = new Carteira(0, "Ana", "XP");
        dao.inserir(ana);

        assertSame(ana, dao.buscarPorId(ana.getId()));
        assertNull(dao.buscarPorId(99));
    }

    @Test
    @DisplayName("buscarExistente lança erro de negócio quando a carteira não existe")
    void buscarExistente() {
        Carteira ana = new Carteira(0, "Ana", "XP");
        dao.inserir(ana);

        assertSame(ana, dao.buscarExistente(ana.getId()));
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> dao.buscarExistente(99));
        assertEquals("erro.carteira.naoEncontrada", erro.getChave());
    }

    @Test
    @DisplayName("Atualizar troca os dados da carteira; id inexistente é ignorado")
    void atualizar() {
        Carteira ana = new Carteira(0, "Ana", "XP");
        dao.inserir(ana);

        Carteira alterada = new Carteira(ana.getId(), "Ana Maria", "Binance");
        dao.atualizar(alterada);
        assertEquals("Ana Maria", dao.buscarPorId(ana.getId()).getNomeTitular());
        assertEquals("Binance", dao.buscarPorId(ana.getId()).getCorretora());

        dao.atualizar(new Carteira(99, "Ninguém", "X"));
        assertEquals(1, dao.listarTodas().size());
    }

    @Test
    @DisplayName("Excluir remove a carteira; excluir id inexistente não altera nada")
    void excluir() {
        Carteira ana = new Carteira(0, "Ana", "XP");
        Carteira bia = new Carteira(0, "Bia", "XP");
        dao.inserir(ana);
        dao.inserir(bia);

        dao.excluir(ana.getId());
        assertNull(dao.buscarPorId(ana.getId()));
        assertEquals(1, dao.listarTodas().size());

        dao.excluir(99);
        assertEquals(1, dao.listarTodas().size());
    }

    @Test
    @DisplayName("Os ids não são reaproveitados depois de uma exclusão")
    void idsNaoReaproveitados() {
        Carteira ana = new Carteira(0, "Ana", "XP");
        dao.inserir(ana);
        dao.excluir(ana.getId());

        Carteira bia = new Carteira(0, "Bia", "XP");
        dao.inserir(bia);
        assertEquals(2, bia.getId());
    }

    @Test
    @DisplayName("A lista devolvida não permite alterar a tabela interna")
    void listaDevolvidaEhImutavel() {
        dao.inserir(new Carteira(0, "Ana", "XP"));
        assertThrows(UnsupportedOperationException.class, () -> dao.listarTodas().clear());
        assertTrue(dao.listarTodas().size() == 1);
    }
}
