package br.com.ftcoin.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Confere {@link Posicao} contra a planilha de simulação de apuração de resultados do professor
 * ({@code FTCoinApuracaoResultado.ods}): 30 dias de cotações, compras e vendas, com saldo, custo médio,
 * resultado acumulado e saldo a mercado esperados em cada dia.
 */
class PosicaoPlanilhaTest {

    /** Diferença aceita: a planilha usa ponto flutuante e o custo médio do projeto tem 8 casas. */
    private static final BigDecimal TOLERANCIA = new BigDecimal("0.0001");

    /** Colunas: cotação, compra, venda, saldo, custo médio, resultado acumulado, saldo a mercado. */
    private static final String DIAS = """
            12.287940008672,1000,0,1000,12.287940008672,0,12287.940008672
            13.868970004336,0,0,1000,12.287940008672,0,13868.970004336
            12.1650612633917,0,0,1000,12.287940008672,0,12165.0612633917
            12.1,0,0,1000,12.287940008672,0,12100
            10.5569100130081,0,500,500,12.287940008672,-865.51499783199,5278.45500650403
            13.6060912590557,1000,0,1500,13.1667075089278,-865.51499783199,20409.1368885835
            14.0130380486863,0,0,1500,13.1667075089278,-865.51499783199,21019.5570730294
            11.121029995664,0,0,1500,13.1667075089278,-865.51499783199,16681.544993496
            12.4321825181114,0,500,1000,13.1667075089278,-1232.77749324021,12432.1825181114
            13.837940008672,0,500,500,13.1667075089278,-897.16124336809,6918.97000433602
            14.2093326938303,1000,0,1500,13.8617909655294,-897.16124336809,21313.9990407454
            13.4371212547197,0,0,1500,13.8617909655294,-897.16124336809,20155.6818820795
            11.4718833609789,0,500,1000,13.8617909655294,-2092.11504564337,11471.8833609789
            14.2240680443503,0,0,1000,13.8617909655294,-2092.11504564337,14224.0680443503
            12.5640312677277,0,500,500,13.8617909655294,-2740.99489454423,6282.01563386386
            12.602059991328,1000,0,1500,13.0219703160618,-2740.99489454423,18903.0899869919
            12.9583889300503,0,0,1500,13.0219703160618,-2740.99489454423,19437.5833950755
            14.6132125137753,0,500,1000,13.0219703160618,-1945.37379568746,14613.2125137753
            14.6066936096249,0,0,1000,13.0219703160618,-1945.37379568746,14606.6936096249
            11.558970004336,0,500,500,13.0219703160618,-2676.87395155034,5779.48500216801
            12.060159303406,1000,0,1500,12.3807629742912,-2676.87395155034,18090.2389551089
            14.1603626894942,0,0,1500,12.3807629742912,-2676.87395155034,21240.5440342414
            10.9196678446896,0,500,1000,12.3807629742912,-3407.42151635114,10919.6678446896
            11.7981512503836,0,0,1000,12.3807629742912,-3407.42151635114,11798.1512503836
            13.0258800173441,0,500,500,12.3807629742912,-3084.86299482472,6512.94000867204
            12.0329133566429,1000,0,1500,12.1488632291923,-3084.86299482472,18049.3700349643
            10.849303772831,0,0,1500,12.1488632291923,-3084.86299482472,16273.9556592465
            12.0350980400143,0,500,1000,12.1488632291923,-3141.74558941375,12035.0980400143
            11.830338006571,0,0,1000,12.1488632291923,-3141.74558941375,11830.338006571
            14.6550612633917,0,0,1000,12.1488632291923,-3141.74558941375,14655.0612633917
            """;

    @Test
    @DisplayName("Os 30 dias da planilha batem com a apuração por custo médio")
    void apuracaoDiaADiaBateComAPlanilha() {
        Posicao posicao = new Posicao();
        int dia = 0;
        for (String linha : DIAS.strip().split("\\R")) {
            dia++;
            String[] c = linha.strip().split(",");
            BigDecimal cotacao = new BigDecimal(c[0]);
            BigDecimal compra = new BigDecimal(c[1]);
            BigDecimal venda = new BigDecimal(c[2]);

            if (compra.signum() > 0) {
                TipoOperacao.COMPRA.aplicar(posicao, compra, cotacao);
            }
            if (venda.signum() > 0) {
                TipoOperacao.VENDA.aplicar(posicao, venda, cotacao);
            }

            confere("saldo", dia, c[3], posicao.getSaldo());
            confere("custo médio", dia, c[4], posicao.getCustoMedio());
            confere("resultado acumulado", dia, c[5], posicao.getGanhoRealizado());
            confere("saldo a mercado", dia, c[6], posicao.calcularValorDeMercado(cotacao));
        }
        assertEquals(30, dia);
    }

    @Test
    @DisplayName("Totais finais da planilha: valor a custo e ganho total a mercado")
    void totaisFinaisBatemComAPlanilha() {
        Posicao posicao = new Posicao();
        BigDecimal ultimaCotacao = BigDecimal.ZERO;
        for (String linha : DIAS.strip().split("\\R")) {
            String[] c = linha.strip().split(",");
            ultimaCotacao = new BigDecimal(c[0]);
            if (new BigDecimal(c[1]).signum() > 0) {
                TipoOperacao.COMPRA.aplicar(posicao, new BigDecimal(c[1]), ultimaCotacao);
            }
            if (new BigDecimal(c[2]).signum() > 0) {
                TipoOperacao.VENDA.aplicar(posicao, new BigDecimal(c[2]), ultimaCotacao);
            }
        }
        confere("valor a custo", 30, "12148.8632291923", posicao.getValorACusto());
        confere("ganho total a mercado", 30, "-635.547555214367", posicao.calcularGanhoTotal(ultimaCotacao));
    }

    private static void confere(String campo, int dia, String esperado, BigDecimal obtido) {
        BigDecimal diferenca = obtido.subtract(new BigDecimal(esperado)).abs();
        assertTrue(diferenca.compareTo(TOLERANCIA) <= 0,
                "Dia " + dia + ", " + campo + ": esperado " + esperado + " mas foi " + obtido);
    }
}
