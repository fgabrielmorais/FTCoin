package br.com.ftcoin.daos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes do {@link OraculoDAOCache}: cada data é buscada uma vez, a ausência de cotação de datas passadas
 * é guardada, e nem "hoje sem cotação" nem falhas ficam no cache.
 */
class OraculoDAOCacheTest {

    private static final LocalDate PASSADO = LocalDate.now().minusDays(10);
    private static final LocalDate HOJE = LocalDate.now();

    @Test
    @DisplayName("A segunda consulta da mesma data vem do cache")
    void segundaConsultaVemDoCache() {
        OraculoFalso oraculo = new OraculoFalso().com(PASSADO, "5.50");
        OraculoDAOCache cache = new OraculoDAOCache(oraculo);

        OraculoDAOCache.Consulta primeira = cache.consultar(PASSADO);
        OraculoDAOCache.Consulta segunda = cache.consultar(PASSADO);

        assertFalse(primeira.doCache());
        assertTrue(segunda.doCache());
        assertEquals(new BigDecimal("5.50"), segunda.valor());
        assertEquals(1, oraculo.consultas());
    }

    @Test
    @DisplayName("Datas diferentes são buscadas cada uma uma vez")
    void datasDiferentes() {
        OraculoFalso oraculo = new OraculoFalso().com(PASSADO, "5.50").com(PASSADO.plusDays(1), "6.00");
        OraculoDAOCache cache = new OraculoDAOCache(oraculo);
        cache.buscarCotacaoPorData(PASSADO);
        cache.buscarCotacaoPorData(PASSADO.plusDays(1));
        cache.buscarCotacaoPorData(PASSADO);
        assertEquals(2, oraculo.consultas());
    }

    @Test
    @DisplayName("Data passada sem cotação (não é erro) fica guardada")
    void passadoSemCotacaoFicaNoCache() {
        OraculoFalso oraculo = new OraculoFalso();
        OraculoDAOCache cache = new OraculoDAOCache(oraculo);

        assertNull(cache.consultar(PASSADO).valor());
        OraculoDAOCache.Consulta segunda = cache.consultar(PASSADO);

        assertNull(segunda.valor());
        assertTrue(segunda.doCache());
        assertEquals(1, oraculo.consultas());
    }

    @Test
    @DisplayName("Hoje sem cotação NÃO fica no cache: o servidor pode publicá-la depois")
    void hojeSemCotacaoNaoFicaNoCache() {
        OraculoFalso oraculo = new OraculoFalso();
        OraculoDAOCache cache = new OraculoDAOCache(oraculo);

        assertNull(cache.consultar(HOJE).valor());
        oraculo.com(HOJE, "7.00");
        OraculoDAOCache.Consulta depois = cache.consultar(HOJE);

        assertEquals(new BigDecimal("7.00"), depois.valor());
        assertFalse(depois.doCache());
        assertEquals(2, oraculo.consultas());
    }

    @Test
    @DisplayName("Exceção: falha do oráculo não fica no cache e a próxima consulta tenta de novo")
    void falhaNaoFicaNoCache() {
        OraculoFalso oraculo = new OraculoFalso().com(PASSADO, "5.50");
        OraculoDAOCache cache = new OraculoDAOCache(oraculo);

        oraculo.falhar(true);
        assertThrows(OraculoException.class, () -> cache.consultar(PASSADO));

        oraculo.falhar(false);
        OraculoDAOCache.Consulta recuperada = cache.consultar(PASSADO);

        assertEquals(new BigDecimal("5.50"), recuperada.valor());
        assertFalse(recuperada.doCache());
    }

    @Test
    @DisplayName("descartar e limpar forçam nova consulta ao oráculo")
    void descartarELimpar() {
        OraculoFalso oraculo = new OraculoFalso().com(PASSADO, "5.50");
        OraculoDAOCache cache = new OraculoDAOCache(oraculo);

        cache.consultar(PASSADO);
        cache.descartar(PASSADO);
        assertFalse(cache.consultar(PASSADO).doCache());

        cache.limpar();
        assertFalse(cache.consultar(PASSADO).doCache());
        assertEquals(3, oraculo.consultas());
    }

    @Test
    @DisplayName("consultarPeriodo devolve um item por dia, em ordem, aproveitando o cache")
    void consultarPeriodoDiaADia() {
        LocalDate d1 = PASSADO;
        LocalDate d2 = PASSADO.plusDays(1);
        LocalDate d3 = PASSADO.plusDays(2);
        OraculoFalso oraculo = new OraculoFalso().com(d1, "1.00").com(d3, "3.00");
        OraculoDAOCache cache = new OraculoDAOCache(oraculo);
        cache.consultar(d1);

        Map<LocalDate, OraculoDAOCache.Consulta> periodo = cache.consultarPeriodo(d1, d3);

        assertEquals(List.of(d1, d2, d3), List.copyOf(periodo.keySet()));
        assertTrue(periodo.get(d1).doCache());
        assertNull(periodo.get(d2).valor());
        assertEquals(new BigDecimal("3.00"), periodo.get(d3).valor());
        assertEquals(3, oraculo.consultas());
    }
}
