package br.com.ftcoin.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Confere o cálculo de {@link Posicao} contra o gabarito da planilha oficial (arquivo
 * {@code planilha_apuracao.csv}): saldo, custo médio e resultado acumulado a cada linha, mais os totais.
 */
class PlanilhaApuracaoTest {

    private static final String ARQUIVO = "/br/com/ftcoin/models/planilha_apuracao.csv";
    private static final BigDecimal TOLERANCIA = new BigDecimal("0.0001");
    private static final int COLUNA_COTACAO = 1;
    private static final int COLUNA_COMPRA = 2;
    private static final int COLUNA_VENDA = 3;
    private static final int COLUNA_SALDO = 4;
    private static final int COLUNA_CUSTO_MEDIO = 5;
    private static final int COLUNA_RESULTADO = 6;
    private static final int LINHAS_ESPERADAS = 30;

    private static void assertProximo(String esperado, BigDecimal obtido, String contexto) {
        BigDecimal diferenca = new BigDecimal(esperado).subtract(obtido).abs();
        assertTrue(diferenca.compareTo(TOLERANCIA) <= 0,
                contexto + ": esperado " + esperado + " mas foi " + obtido);
    }

    @Test
    @DisplayName("Saldo, custo médio e resultado batem com a planilha oficial em todas as linhas")
    void bateComAPlanilha() throws IOException {
        Posicao posicao = new Posicao();
        BigDecimal comprado = BigDecimal.ZERO;
        BigDecimal vendido = BigDecimal.ZERO;
        BigDecimal ultimaCotacao = null;
        int linhas = 0;

        try (InputStream entrada = getClass().getResourceAsStream(ARQUIVO);
                BufferedReader leitor = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                if (linha.isBlank() || linha.startsWith("#")) {
                    continue;
                }
                String[] c = linha.split(";");
                BigDecimal cotacao = new BigDecimal(c[COLUNA_COTACAO]);
                BigDecimal compra = new BigDecimal(c[COLUNA_COMPRA]);
                BigDecimal venda = new BigDecimal(c[COLUNA_VENDA]);
                if (compra.signum() > 0) {
                    TipoOperacao.COMPRA.aplicar(posicao, compra, cotacao);
                    comprado = comprado.add(compra.multiply(cotacao));
                }
                if (venda.signum() > 0) {
                    TipoOperacao.VENDA.aplicar(posicao, venda, cotacao);
                    vendido = vendido.add(venda.multiply(cotacao));
                }
                ultimaCotacao = cotacao;
                linhas++;

                String contexto = "linha " + c[0];
                assertProximo(c[COLUNA_SALDO], posicao.getSaldo(), contexto + " saldo");
                assertProximo(c[COLUNA_CUSTO_MEDIO], posicao.getCustoMedio(), contexto + " custo médio");
                assertProximo(c[COLUNA_RESULTADO], posicao.getGanhoRealizado(), contexto + " resultado");
            }
        }

        assertEquals(LINHAS_ESPERADAS, linhas);
        assertProximo("76798.4966", comprado, "total comprado");
        assertProximo("61507.8878", vendido, "total vendido");
        assertProximo("-3141.7456", posicao.getGanhoRealizado(), "resultado realizado");
        assertProximo("1000", posicao.getSaldo(), "saldo final");
        // Vendendo tudo pela última cotação (14,6550612633917), o resultado total da planilha é -635,5476
        assertProximo("-635.5476", posicao.calcularGanhoTotal(ultimaCotacao), "ganho total");
    }
}
