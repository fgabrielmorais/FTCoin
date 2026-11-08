package br.com.ftcoin.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
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
    @DisplayName("Compra é registrada hoje, com o tipo e a quantidade pedidos")
    void compra() {
        Movimentacao mov = servico.registrarOperacao(idCarteira, TipoOperacao.COMPRA, n("5"));

        assertEquals(HOJE, mov.getDataOperacao());
        assertEquals(TipoOperacao.COMPRA, mov.getTipoOperacao());
        assertEquals(1, servico.buscarHistorico(idCarteira).size());
        assertEquals(0, n("5").compareTo(servico.apurarPosicao(idCarteira).getSaldo()));
    }

    @Test
    @DisplayName("Limite: vender exatamente o saldo é aceito")
    void vendaDoSaldoInteiro() {
        servico.registrarOperacao(idCarteira, TipoOperacao.COMPRA, n("5"));
        servico.registrarOperacao(idCarteira, TipoOperacao.VENDA, n("5"));
        assertEquals(0, servico.apurarPosicao(idCarteira).getSaldo().signum());
    }

    @Test
    @DisplayName("Exceção: venda acima do saldo é recusada e nada é gravado")
    void vendaAcimaDoSaldo() {
        servico.registrarOperacao(idCarteira, TipoOperacao.COMPRA, n("5"));

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> servico.registrarOperacao(idCarteira, TipoOperacao.VENDA, n("5.001")));

        assertEquals("erro.movimentacao.saldoInsuficiente", erro.getChave());
        assertEquals(1, servico.buscarHistorico(idCarteira).size());
    }

    @Test
    @DisplayName("Exceção: quantidade zero ou negativa")
    void quantidadeInvalida() {
        assertThrows(RegraNegocioException.class, () -> servico.registrarOperacao(idCarteira, TipoOperacao.COMPRA, BigDecimal.ZERO));
        assertThrows(RegraNegocioException.class, () -> servico.registrarOperacao(idCarteira, TipoOperacao.COMPRA, n("-1")));
        assertTrue(servico.buscarHistorico(idCarteira).isEmpty());
    }

    @Test
    @DisplayName("Exceção: carteira inexistente")
    void carteiraInexistente() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> servico.registrarOperacao(99, TipoOperacao.COMPRA, BigDecimal.ONE));
        assertEquals("erro.carteira.naoEncontrada", erro.getChave());
    }

    @Test
    @DisplayName("Exceção: sem cotação para hoje não se registra operação")
    void semCotacaoHoje() {
        cotacoes.clear();
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> servico.registrarOperacao(idCarteira, TipoOperacao.COMPRA, BigDecimal.ONE));
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
}
