package br.com.ftcoin.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes de {@link Posicao} e {@link TipoOperacao}: custo médio ponderado, ganho realizado e não realizado,
 * condições de contorno e recusa de venda acima do saldo.
 */
class PosicaoTest {

    private static BigDecimal n(String valor) {
        return new BigDecimal(valor);
    }

    private static void assertValor(String esperado, BigDecimal obtido) {
        assertEquals(0, n(esperado).compareTo(obtido), "esperado " + esperado + " mas foi " + obtido);
    }

    @Test
    @DisplayName("Posição nova está zerada")
    void posicaoNovaEstaZerada() {
        Posicao posicao = new Posicao();
        assertValor("0", posicao.getSaldo());
        assertValor("0", posicao.getCustoMedio());
        assertValor("0", posicao.getGanhoRealizado());
    }

    @Test
    @DisplayName("Uma compra define saldo e custo médio")
    void compraUnica() {
        Posicao posicao = new Posicao();
        posicao.comprar(n("10"), n("5.00"));
        assertValor("10", posicao.getSaldo());
        assertValor("5", posicao.getCustoMedio());
        assertValor("50", posicao.getValorACusto());
    }

    @Test
    @DisplayName("Duas compras geram o custo médio ponderado")
    void duasComprasCustoMedioPonderado() {
        Posicao posicao = new Posicao();
        posicao.comprar(n("10"), n("10"));
        posicao.comprar(n("30"), n("20"));
        assertValor("17.5", posicao.getCustoMedio());
    }

    @Test
    @DisplayName("Venda parcial mantém o custo médio e realiza o resultado")
    void vendaParcialMantemCustoMedio() {
        Posicao posicao = new Posicao();
        posicao.comprar(n("10"), n("10"));
        posicao.vender(n("4"), n("15"));
        assertValor("6", posicao.getSaldo());
        assertValor("10", posicao.getCustoMedio());
        assertValor("20", posicao.getGanhoRealizado());
    }

    @Test
    @DisplayName("Venda com prejuízo gera ganho realizado negativo")
    void vendaComPrejuizo() {
        Posicao posicao = new Posicao();
        posicao.comprar(n("10"), n("10"));
        posicao.vender(n("5"), n("8"));
        assertValor("-10", posicao.getGanhoRealizado());
    }

    @Test
    @DisplayName("Limite: vender exatamente o saldo zera a posição")
    void vendaDoSaldoInteiro() {
        Posicao posicao = new Posicao();
        posicao.comprar(n("3"), n("7"));
        posicao.vender(n("3"), n("7"));
        assertValor("0", posicao.getSaldo());
        assertValor("0", posicao.getCustoMedio());
        assertValor("0", posicao.getValorACusto());
    }

    @Test
    @DisplayName("Exceção: venda acima do saldo é recusada e não altera a posição")
    void vendaAcimaDoSaldoLancaExcecao() {
        Posicao posicao = new Posicao();
        posicao.comprar(n("2"), n("10"));
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> posicao.vender(n("2.001"), n("10")));
        assertEquals("erro.movimentacao.saldoInsuficiente", erro.getChave());
        assertValor("2", posicao.getSaldo());
        assertValor("0", posicao.getGanhoRealizado());
    }

    @Test
    @DisplayName("Venda sem nenhuma compra é recusada")
    void vendaSemSaldo() {
        assertThrows(RegraNegocioException.class, () -> new Posicao().vender(BigDecimal.ONE, BigDecimal.TEN));
    }

    @Test
    @DisplayName("Ganho não realizado e total pela cotação atual")
    void ganhoNaoRealizadoETotal() {
        Posicao posicao = new Posicao();
        posicao.comprar(n("10"), n("10"));
        posicao.vender(n("4"), n("15"));
        assertValor("72", posicao.calcularValorDeMercado(n("12")));
        assertValor("12", posicao.calcularGanhoNaoRealizado(n("12")));
        assertValor("32", posicao.calcularGanhoTotal(n("12")));
    }

    @Test
    @DisplayName("ROI é zero quando não há valor a custo")
    void roiSemCustoEZero() {
        assertValor("0", new Posicao().calcularRoi(n("10")));
    }

    @Test
    @DisplayName("ROI em porcentagem com 2 casas")
    void roiComDuasCasas() {
        Posicao posicao = new Posicao();
        posicao.comprar(n("3"), n("10"));
        assertValor("10.00", posicao.calcularRoi(n("11")));
    }

    @Test
    @DisplayName("Consolidar soma saldo, custo e ganho realizado das posições")
    void consolidarSomaPosicoes() {
        Posicao a = new Posicao();
        a.comprar(n("10"), n("10"));
        Posicao b = new Posicao();
        b.comprar(n("10"), n("20"));
        Posicao total = Posicao.consolidar(List.of(a, b));
        assertValor("20", total.getSaldo());
        assertValor("15", total.getCustoMedio());
        assertValor("0", Posicao.consolidar(List.of()).getSaldo());
    }

    @Test
    @DisplayName("Participação no portfólio, e zero se o portfólio não tem valor")
    void participacaoNoPortfolio() {
        Posicao a = new Posicao();
        a.comprar(n("10"), n("10"));
        Posicao b = new Posicao();
        b.comprar(n("30"), n("10"));
        Posicao total = Posicao.consolidar(List.of(a, b));
        assertValor("25.00", a.calcularParticipacao(total, n("10")));
        assertValor("0", a.calcularParticipacao(new Posicao(), n("10")));
    }

    @Test
    @DisplayName("TipoOperacao converte o código C/V sem diferenciar maiúsculas")
    void tipoOperacaoDoCodigo() {
        assertEquals(TipoOperacao.COMPRA, TipoOperacao.fromCodigo("C"));
        assertEquals(TipoOperacao.VENDA, TipoOperacao.fromCodigo(" v "));
        assertEquals('C', TipoOperacao.COMPRA.getOperacao());
        assertEquals('V', TipoOperacao.VENDA.getOperacao());
    }

    @Test
    @DisplayName("Exceção: código de operação desconhecido ou nulo")
    void tipoOperacaoDesconhecido() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> TipoOperacao.fromCodigo("X"));
        assertEquals("erro.operacao.codigoDesconhecido", erro.getChave());
        assertThrows(RegraNegocioException.class, () -> TipoOperacao.fromCodigo(null));
    }
}
