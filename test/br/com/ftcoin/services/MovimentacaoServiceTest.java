package br.com.ftcoin.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.ftcoin.daos.CarteiraDAO;
import br.com.ftcoin.daos.DAOFactory;
import br.com.ftcoin.daos.MovimentacaoDAO;
import br.com.ftcoin.daos.OraculoDAO;
import br.com.ftcoin.daos.TipoPersistencia;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.Posicao;
import br.com.ftcoin.models.RegraNegocioException;
import br.com.ftcoin.models.TipoOperacao;

/**
 * Testes do {@link MovimentacaoService} com DAOs em memória e um oráculo de teste. Cobrem compra, venda,
 * a recusa de venda acima do saldo, quantidade inválida, falta de cotação e a apuração com datas diferentes.
 */
class MovimentacaoServiceTest {

    private static final LocalDate HOJE = LocalDate.now();

    private final Map<LocalDate, BigDecimal> cotacoes = new HashMap<>();
    private MovimentacaoService servico;
    private MovimentacaoDAO movimentacaoDAO;
    private int idCarteira;

    @BeforeEach
    void preparar() {
        OraculoDAO oraculo = new OraculoDAO() {
            @Override
            public BigDecimal buscarCotacaoPorData(LocalDate data) {
                return cotacoes.get(data);
            }
        };
        DAOFactory fabrica = DAOFactory.criar(TipoPersistencia.MEMORIA);
        CarteiraDAO carteiraDAO = fabrica.criarCarteiraDAO();
        movimentacaoDAO = fabrica.criarMovimentacaoDAO();
        servico = new MovimentacaoService(movimentacaoDAO, carteiraDAO, oraculo);

        Carteira carteira = new Carteira(0, "Ana", "XP");
        carteiraDAO.inserir(carteira);
        idCarteira = carteira.getId();
        cotacoes.put(HOJE, new BigDecimal("10.00"));
    }

    private static BigDecimal n(String valor) {
        return new BigDecimal(valor);
    }

    @Test
    @DisplayName("Compra é registrada na data pedida, com o tipo e a quantidade pedidos")
    void compra() {
        Movimentacao mov = servico.registrarOperacao(idCarteira, TipoOperacao.COMPRA, HOJE, n("5"));

        assertEquals(HOJE, mov.getDataOperacao());
        assertEquals(TipoOperacao.COMPRA, mov.getTipoOperacao());
        assertEquals(1, servico.buscarHistorico(idCarteira).size());
        assertEquals(0, n("5").compareTo(servico.apurarPosicao(idCarteira).getSaldo()));
    }

    @Test
    @DisplayName("Limite: vender exatamente o saldo é aceito")
    void vendaDoSaldoInteiro() {
        servico.registrarOperacao(idCarteira, TipoOperacao.COMPRA, HOJE, n("5"));
        servico.registrarOperacao(idCarteira, TipoOperacao.VENDA, HOJE, n("5"));
        assertEquals(0, servico.apurarPosicao(idCarteira).getSaldo().signum());
    }

    @Test
    @DisplayName("Exceção: venda acima do saldo é recusada e nada é gravado")
    void vendaAcimaDoSaldo() {
        servico.registrarOperacao(idCarteira, TipoOperacao.COMPRA, HOJE, n("5"));

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> servico.registrarOperacao(idCarteira, TipoOperacao.VENDA, HOJE, n("5.001")));

        assertEquals("erro.movimentacao.saldoNegativoNaData", erro.getChave());
        assertEquals(1, servico.buscarHistorico(idCarteira).size());
    }

    @Test
    @DisplayName("Exceção: quantidade zero ou negativa")
    void quantidadeInvalida() {
        assertThrows(RegraNegocioException.class, () -> servico.registrarOperacao(idCarteira, TipoOperacao.COMPRA, HOJE, BigDecimal.ZERO));
        assertThrows(RegraNegocioException.class, () -> servico.registrarOperacao(idCarteira, TipoOperacao.COMPRA, HOJE, n("-1")));
        assertTrue(servico.buscarHistorico(idCarteira).isEmpty());
    }

    @Test
    @DisplayName("Exceção: carteira inexistente")
    void carteiraInexistente() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> servico.registrarOperacao(99, TipoOperacao.COMPRA, HOJE, BigDecimal.ONE));
        assertEquals("erro.carteira.naoEncontrada", erro.getChave());
    }

    @Test
    @DisplayName("Exceção: sem cotação para hoje não se registra operação")
    void semCotacaoHoje() {
        cotacoes.clear();
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> servico.registrarOperacao(idCarteira, TipoOperacao.COMPRA, HOJE, BigDecimal.ONE));
        assertEquals("erro.oraculo.semCotacao", erro.getChave());
        assertTrue(servico.buscarHistorico(idCarteira).isEmpty());
    }

    private Movimentacao operar(TipoOperacao tipo, LocalDate data, String quantidade) {
        return servico.registrarOperacao(idCarteira, tipo, data, n(quantidade));
    }

    private void cotarDias(LocalDate... dias) {
        for (LocalDate dia : dias) {
            cotacoes.put(dia, n("10"));
        }
    }

    @Test
    @DisplayName("Venda de 500 em carteira vazia é recusada")
    void vendaEmCarteiraVazia() {
        LocalDate dia = LocalDate.of(2026, 1, 1);
        cotarDias(dia);
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> operar(TipoOperacao.VENDA, dia, "500"));
        assertEquals("erro.movimentacao.saldoNegativoNaData", erro.getChave());
        assertTrue(servico.buscarHistorico(idCarteira).isEmpty());
    }

    @Test
    @DisplayName("Compra e venda no mesmo dia: a venda vem depois e é aceita")
    void compraEVendaNoMesmoDia() {
        LocalDate dia = LocalDate.of(2026, 2, 1);
        cotarDias(dia);
        operar(TipoOperacao.COMPRA, dia, "1000");
        operar(TipoOperacao.VENDA, dia, "500");
        assertEquals(0, n("500").compareTo(servico.apurarPosicao(idCarteira).getSaldo()));
    }

    @Test
    @DisplayName("Venda em data anterior com saldo menor que a venda é recusada")
    void vendaRetroativaSemSaldoNaData() {
        LocalDate d1 = LocalDate.of(2026, 2, 1);
        LocalDate d2 = LocalDate.of(2026, 2, 2);
        cotarDias(d1, d2);
        operar(TipoOperacao.COMPRA, d1, "100");
        operar(TipoOperacao.VENDA, d2, "50");
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> operar(TipoOperacao.VENDA, d1, "101"));
        assertEquals("erro.movimentacao.saldoNegativoNaData", erro.getChave());
        assertEquals(2, servico.buscarHistorico(idCarteira).size());
    }

    @Test
    @DisplayName("Operação retroativa que deixaria venda posterior sem saldo é recusada e nada é gravado")
    void retroativaPrejudicaVendaPosterior() {
        LocalDate d1 = LocalDate.of(2026, 2, 1);
        LocalDate d2 = LocalDate.of(2026, 2, 2);
        LocalDate d3 = LocalDate.of(2026, 2, 3);
        cotarDias(d1, d2, d3);
        operar(TipoOperacao.COMPRA, d1, "100");
        operar(TipoOperacao.VENDA, d3, "100");
        // Vender 60 em 02/02 deixaria a venda de 03/02 sem saldo
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> operar(TipoOperacao.VENDA, d2, "60"));
        assertEquals("erro.movimentacao.saldoNegativoNaData", erro.getChave());
        assertEquals(d3, erro.getArgumentos()[0]);
        assertEquals(2, servico.buscarHistorico(idCarteira).size());
    }

    @Test
    @DisplayName("Compra retroativa que cobre vendas posteriores é aceita")
    void compraRetroativaAceita() {
        LocalDate d1 = LocalDate.of(2026, 2, 1);
        LocalDate d2 = LocalDate.of(2026, 2, 2);
        cotarDias(d1, d2);
        operar(TipoOperacao.COMPRA, d2, "10");
        operar(TipoOperacao.COMPRA, d1, "5");
        operar(TipoOperacao.VENDA, d2, "15");
        assertEquals(0, servico.apurarPosicao(idCarteira).getSaldo().signum());
    }

    @Test
    @DisplayName("Exceção: data futura é recusada")
    void dataFutura() {
        LocalDate amanha = HOJE.plusDays(1);
        cotarDias(amanha);
        RegraNegocioException erro = assertThrows(RegraNegocioException.class, () -> operar(TipoOperacao.COMPRA, amanha, "1"));
        assertEquals("erro.movimentacao.dataFutura", erro.getChave());
        assertTrue(servico.buscarHistorico(idCarteira).isEmpty());
    }

    @Test
    @DisplayName("Exceção: data sem cotação")
    void dataSemCotacao() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> operar(TipoOperacao.COMPRA, LocalDate.of(2025, 12, 31), "1"));
        assertEquals("erro.oraculo.semCotacao", erro.getChave());
        assertTrue(servico.buscarHistorico(idCarteira).isEmpty());
    }

    @Test
    @DisplayName("Apuração valoriza cada operação pela cotação do seu dia")
    void apuracaoComDatasDiferentes() {
        LocalDate d1 = HOJE.minusDays(2);
        LocalDate d2 = HOJE.minusDays(1);
        cotacoes.put(d1, n("10"));
        cotacoes.put(d2, n("20"));
        movimentacaoDAO.inserir(new Movimentacao(0, idCarteira, d1, TipoOperacao.COMPRA, n("10")));
        movimentacaoDAO.inserir(new Movimentacao(0, idCarteira, d2, TipoOperacao.VENDA, n("4")));

        Posicao posicao = servico.apurarPosicao(idCarteira);

        assertEquals(0, n("6").compareTo(posicao.getSaldo()));
        assertEquals(0, n("10").compareTo(posicao.getCustoMedio()));
        assertEquals(0, n("40").compareTo(posicao.getGanhoRealizado()));
    }

    @Test
    @DisplayName("Histórico sai em ordem cronológica mesmo se foi gravado fora de ordem")
    void historicoEmOrdemCronologica() {
        cotacoes.put(HOJE.minusDays(3), n("10"));
        cotacoes.put(HOJE.minusDays(1), n("10"));
        movimentacaoDAO.inserir(new Movimentacao(0, idCarteira, HOJE.minusDays(1), TipoOperacao.COMPRA, n("1")));
        movimentacaoDAO.inserir(new Movimentacao(0, idCarteira, HOJE.minusDays(3), TipoOperacao.COMPRA, n("1")));

        assertEquals(HOJE.minusDays(3), servico.buscarHistorico(idCarteira).get(0).getDataOperacao());
    }

    @Test
    @DisplayName("Histórico geral não tem limite e sai da operação mais recente para a mais antiga")
    void historicoGeralSemLimiteEmOrdemDecrescente() {
        int outraCarteira = idCarteira + 1; // o DAO em memória não exige que a carteira exista
        for (int i = 0; i < 25; i++) {
            movimentacaoDAO.inserir(new Movimentacao(0, i % 2 == 0 ? idCarteira : outraCarteira,
                    i < 10 ? HOJE : HOJE.minusDays(1),
                    i % 2 == 0 ? TipoOperacao.COMPRA : TipoOperacao.VENDA, n("1")));
        }

        List<Movimentacao> todas = servico.buscarTodasMovimentacoes();

        assertEquals(25, todas.size());
        assertEquals(2, todas.stream().map(Movimentacao::getIdCarteira).distinct().count());
        for (int i = 1; i < todas.size(); i++) {
            Movimentacao anterior = todas.get(i - 1);
            Movimentacao atual = todas.get(i);
            assertTrue(anterior.getDataOperacao().isAfter(atual.getDataOperacao())
                    || (anterior.getDataOperacao().equals(atual.getDataOperacao())
                    && anterior.getIdMovimento() > atual.getIdMovimento()));
        }
        assertEquals(todas.subList(0, 5), servico.buscarUltimasMovimentacoes(5));
    }
}
