package br.com.ftcoin.daos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.TipoOperacao;

/**
 * Testes do {@link MovimentacaoDAOMemoria}: inserção com id sequencial, busca por carteira e listagem geral.
 */
class MovimentacaoDAOMemoriaTest {

    private static final LocalDate DIA = LocalDate.of(2026, 2, 1);

    private MovimentacaoDAOMemoria dao;

    @BeforeEach
    void preparar() {
        dao = new MovimentacaoDAOMemoria();
    }

    private Movimentacao inserir(int idCarteira, TipoOperacao tipo, String quantidade) {
        Movimentacao mov = new Movimentacao(0, idCarteira, DIA, tipo, new BigDecimal(quantidade));
        dao.inserir(mov);
        return mov;
    }

    @Test
    @DisplayName("Inserir gera os identificadores 1, 2, 3... em sequência")
    void inserirGeraIdsSequenciais() {
        assertEquals(1, inserir(1, TipoOperacao.COMPRA, "10").getIdMovimento());
        assertEquals(2, inserir(1, TipoOperacao.VENDA, "5").getIdMovimento());
        assertEquals(3, inserir(2, TipoOperacao.COMPRA, "1").getIdMovimento());
    }

    @Test
    @DisplayName("Buscar por carteira devolve só as movimentações dela, na ordem de inserção")
    void buscarPorCarteira() {
        Movimentacao a = inserir(1, TipoOperacao.COMPRA, "10");
        inserir(2, TipoOperacao.COMPRA, "99");
        Movimentacao c = inserir(1, TipoOperacao.VENDA, "5");

        List<Movimentacao> daCarteira1 = dao.buscarPorCarteira(1);

        assertEquals(List.of(a, c), daCarteira1);
        assertTrue(dao.buscarPorCarteira(3).isEmpty());
    }

    @Test
    @DisplayName("Listar todas devolve as movimentações de todas as carteiras")
    void listarTodas() {
        assertTrue(dao.listarTodas().isEmpty());
        inserir(1, TipoOperacao.COMPRA, "10");
        inserir(2, TipoOperacao.COMPRA, "20");
        assertEquals(2, dao.listarTodas().size());
    }

    @Test
    @DisplayName("A lista devolvida não permite alterar a tabela interna")
    void listaDevolvidaEhImutavel() {
        inserir(1, TipoOperacao.COMPRA, "10");
        assertThrows(UnsupportedOperationException.class, () -> dao.listarTodas().clear());
        assertEquals(1, dao.listarTodas().size());
    }
}
