package br.com.ftcoin.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.ftcoin.daos.CarteiraDAO;
import br.com.ftcoin.daos.DAOFactory;
import br.com.ftcoin.daos.OraculoDAOMemoria;
import br.com.ftcoin.daos.TipoPersistencia;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.RegraNegocioException;
import br.com.ftcoin.models.TipoOperacao;

/**
 * Executa, numa só carteira, a sequência de movimentações do roteiro do vídeo (item 3.11), com os DAOs em memória
 * e o oráculo simulado real, e confere saldos, número de movimentações e ganho realizado.
 */
class CenarioRoteiroTest {

    private static final double TOLERANCIA_GANHO = 0.01;
    private static final int MINIMO_SETEMBRO = 40;
    private static final int SALDO_FINAL = 540;
    private static final int SETEMBRO = 9;

    /** Uma operação do roteiro: tipo, data e quantidade. */
    private record Op(TipoOperacao tipo, LocalDate data, int quantidade) {
    }

    private final OraculoDAOMemoria oraculo = new OraculoDAOMemoria();
    private MovimentacaoService servico;
    private int idCarteira;

    @BeforeEach
    void preparar() {
        DAOFactory fabrica = DAOFactory.criar(TipoPersistencia.MEMORIA);
        CarteiraDAO carteiraDAO = fabrica.criarCarteiraDAO();
        Carteira carteira = new Carteira(0, "Roteiro", "XP");
        carteiraDAO.inserir(carteira);
        idCarteira = carteira.getId();
        servico = new MovimentacaoService(fabrica.criarMovimentacaoDAO(), carteiraDAO, oraculo);
    }

    private static LocalDate d(int mes, int dia) {
        return LocalDate.of(2026, mes, dia);
    }

    private static Op c(int mes, int dia, int quantidade) {
        return new Op(TipoOperacao.COMPRA, d(mes, dia), quantidade);
    }

    private static Op v(int mes, int dia, int quantidade) {
        return new Op(TipoOperacao.VENDA, d(mes, dia), quantidade);
    }

    private void executar(Op op) {
        servico.registrarOperacao(idCarteira, op.tipo(), op.data(), BigDecimal.valueOf(op.quantidade()));
    }

    private void assertSaldo(int esperado, String momento) {
        assertEquals(0, BigDecimal.valueOf(esperado).compareTo(servico.apurarPosicao(idCarteira).getSaldo()), momento);
    }

    /** Recalcula o ganho realizado por fora, em BigDecimal, sem usar a classe Posicao. */
    private BigDecimal ganhoManual(List<Op> operacoes) {
        BigDecimal saldo = BigDecimal.ZERO;
        BigDecimal custoTotal = BigDecimal.ZERO;
        BigDecimal ganho = BigDecimal.ZERO;
        for (Op op : operacoes) {
            BigDecimal quantidade = BigDecimal.valueOf(op.quantidade());
            BigDecimal cotacao = oraculo.buscarCotacaoPorData(op.data());
            if (op.tipo() == TipoOperacao.COMPRA) {
                saldo = saldo.add(quantidade);
                custoTotal = custoTotal.add(quantidade.multiply(cotacao));
            } else {
                BigDecimal custoMedio = custoTotal.divide(saldo, MathContext.DECIMAL128);
                ganho = ganho.add(quantidade.multiply(cotacao.subtract(custoMedio)));
                saldo = saldo.subtract(quantidade);
                custoTotal = custoTotal.subtract(quantidade.multiply(custoMedio));
            }
        }
        return ganho;
    }

    @Test
    @DisplayName("Primeira venda de 500 em 01/01/2026, com a carteira vazia, é recusada")
    void vendaInicialRecusada() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> executar(v(1, 1, 500)));
        assertEquals("erro.movimentacao.saldoNegativoNaData", erro.getChave());
        assertTrue(servico.buscarHistorico(idCarteira).isEmpty());
    }

    @Test
    @DisplayName("Cenário completo do roteiro: fevereiro a abril e as 18 operações de setembro")
    void cenarioCompleto() {
        assertThrows(RegraNegocioException.class, () -> executar(v(1, 1, 500)));

        List<Op> fase1 = List.of(c(2, 1, 1000), v(2, 1, 500), v(2, 2, 500));
        fase1.forEach(this::executar);
        assertSaldo(0, "após 02/02");

        List<Op> fase2 = List.of(c(2, 25, 1000), c(3, 2, 2000), v(3, 10, 400));
        fase2.forEach(this::executar);
        assertSaldo(2600, "após 10/03");

        List<Op> fase3 = List.of(v(4, 1, 2600));
        fase3.forEach(this::executar);
        assertSaldo(0, "após 01/04");

        List<Op> setembro = List.of(c(9, 1, 1000), c(9, 4, 120), v(9, 5, 500), c(9, 6, 500), v(9, 8, 80),
                v(9, 9, 500), v(9, 10, 500), c(9, 11, 1000), v(9, 13, 500), v(9, 15, 500), c(9, 16, 1000),
                v(9, 18, 500), v(9, 20, 500), c(9, 21, 1000), v(9, 23, 500), v(9, 25, 500), c(9, 26, 1000),
                v(9, 28, 500));
        assertEquals(18, setembro.size());
        setembro.forEach(this::executar);

        assertSaldo(SALDO_FINAL, "saldo final");
        assertEquals(fase1.size() + fase2.size() + fase3.size() + setembro.size(),
                servico.buscarHistorico(idCarteira).size());

        // O saldo nunca fica negativo e o mínimo de setembro é 40
        BigDecimal saldo = BigDecimal.ZERO;
        BigDecimal minimoSetembro = null;
        for (Movimentacao mov : servico.buscarHistorico(idCarteira)) {
            BigDecimal q = mov.getQuantidadeMovimentada();
            saldo = mov.getTipoOperacao() == TipoOperacao.COMPRA ? saldo.add(q) : saldo.subtract(q);
            assertTrue(saldo.signum() >= 0, "saldo negativo em " + mov.getDataOperacao());
            if (mov.getDataOperacao().getMonthValue() == SETEMBRO) {
                minimoSetembro = (minimoSetembro == null || saldo.compareTo(minimoSetembro) < 0) ? saldo : minimoSetembro;
            }
        }
        assertEquals(0, BigDecimal.valueOf(MINIMO_SETEMBRO).compareTo(minimoSetembro));

        List<Op> todas = new ArrayList<>(fase1);
        todas.addAll(fase2);
        todas.addAll(fase3);
        todas.addAll(setembro);
        BigDecimal esperado = ganhoManual(todas);
        BigDecimal obtido = servico.apurarPosicao(idCarteira).getGanhoRealizado();
        assertEquals(esperado.doubleValue(), obtido.doubleValue(), TOLERANCIA_GANHO);
    }
}
