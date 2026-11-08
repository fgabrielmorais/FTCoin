package br.com.ftcoin.daos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import network.ExchangeRate;
import network.NetworkRequest;
import network.NetworkResponse;

/**
 * Testes de integração do {@link OraculoDAOApi} contra um servidor local que fala o mesmo protocolo do
 * servidor do professor (objeto Java em um canal de saída e outro de entrada). Cobrem sucesso, ausência de
 * cotação (que não é erro) e os erros: faixa invertida, servidor recusando, resposta inválida, rede e timeout.
 */
class OraculoDAOApiTest {

    private static final String GRUPO = "GRUPO_TESTE";
    private static final String SENHA = "SENHA_TESTE";
    private static final LocalDate DIA = LocalDate.now().minusDays(3);

    private ServidorFalso servidor;

    @BeforeEach
    void iniciarServidor() throws IOException {
        servidor = new ServidorFalso();
    }

    @AfterEach
    void fecharServidor() throws IOException {
        servidor.fechar();
    }

    private OraculoDAOApi cliente(int timeoutMs) {
        return new OraculoDAOApi("localhost", servidor.porta(), GRUPO, SENHA, timeoutMs);
    }

    private static NetworkResponse respostaCom(ExchangeRate... taxas) {
        return new NetworkResponse(List.of(taxas), null);
    }

    @Test
    @DisplayName("Sucesso: devolve o valor da data pedida com 2 casas, enviando grupo, senha e as duas datas")
    void sucesso() {
        servidor.responder(respostaCom(new ExchangeRate(DIA, new BigDecimal("12.3456"))));

        BigDecimal valor = cliente(2000).buscarCotacaoPorData(DIA);

        assertEquals(new BigDecimal("12.35"), valor);
        NetworkRequest enviada = servidor.recebidas().get(0);
        assertEquals(GRUPO, enviada.getGroupName());
        assertEquals(SENHA, enviada.getGroupPassword());
        assertEquals(DIA, enviada.getFirstDate());
        assertEquals(DIA, enviada.getLastDate());
    }

    @Test
    @DisplayName("Lista de um período: devolve todas as cotações recebidas")
    void periodo() {
        servidor.responder(respostaCom(
                new ExchangeRate(DIA, new BigDecimal("1.00")),
                new ExchangeRate(DIA.plusDays(1), new BigDecimal("2.00"))));

        List<ExchangeRate> lista = cliente(2000).buscarCotacoesPorPeriodo(DIA, DIA.plusDays(1));

        assertEquals(2, lista.size());
        assertEquals(DIA.plusDays(1), servidor.recebidas().get(0).getLastDate());
    }

    @Test
    @DisplayName("Sem cotação não é erro: lista vazia devolve null")
    void listaVaziaNaoEErro() {
        servidor.responder(respostaCom());
        assertNull(cliente(2000).buscarCotacaoPorData(DIA));
    }

    @Test
    @DisplayName("Antes de usar, confere a data recebida: cotação de outra data é ignorada")
    void ignoraCotacaoDeOutraData() {
        servidor.responder(respostaCom(new ExchangeRate(DIA.minusDays(1), BigDecimal.TEN)));
        assertNull(cliente(2000).buscarCotacaoPorData(DIA));
    }

    @Test
    @DisplayName("Exceção: servidor responde com mensagem de erro (ex.: não autenticado)")
    void servidorRecusa() {
        servidor.responder(new NetworkResponse(null, "Grupo ou senha inválidos"));
        OraculoDAOApi cliente = cliente(2000);

        OraculoException erro = assertThrows(OraculoException.class, () -> cliente.buscarCotacaoPorData(DIA));

        assertEquals("erro.oraculo.servidor", erro.getChave());
    }

    @Test
    @DisplayName("Exceção: servidor responde um objeto que não é NetworkResponse")
    void respostaInvalida() {
        servidor.responder("isto não é uma resposta");
        OraculoDAOApi cliente = cliente(2000);

        OraculoException erro = assertThrows(OraculoException.class, () -> cliente.buscarCotacaoPorData(DIA));

        assertEquals("erro.oraculo.respostaInvalida", erro.getChave());
    }

    @Test
    @DisplayName("Exceção: ninguém escutando na porta (conexão recusada)")
    void conexaoRecusada() throws IOException {
        // Descobre uma porta livre e a libera: nada escuta nela
        ServerSocket temporario = new ServerSocket(0);
        int portaLivre = temporario.getLocalPort();
        temporario.close();
        OraculoDAOApi cliente = new OraculoDAOApi("localhost", portaLivre, GRUPO, SENHA, 1000);

        OraculoException erro = assertThrows(OraculoException.class, () -> cliente.buscarCotacaoPorData(DIA));

        assertEquals("erro.oraculo.rede", erro.getChave());
    }

    @Test
    @DisplayName("Exceção: servidor aceita e não responde (timeout)")
    void timeout() {
        servidor.responder(null);
        OraculoDAOApi cliente = cliente(300);

        OraculoException erro = assertThrows(OraculoException.class, () -> cliente.buscarCotacaoPorData(DIA));

        assertEquals("erro.oraculo.rede", erro.getChave());
    }

    @Test
    @DisplayName("Exceção: faixa invertida é recusada antes de ir à rede")
    void faixaInvertida() {
        servidor.responder(respostaCom());
        OraculoDAOApi cliente = cliente(2000);

        OraculoException erro = assertThrows(OraculoException.class,
                () -> cliente.buscarCotacoesPorPeriodo(DIA, DIA.minusDays(1)));

        assertEquals("erro.oraculo.faixaInvertida", erro.getChave());
        assertTrue(servidor.recebidas().isEmpty());
    }

    @Test
    @DisplayName("Exceção: data futura é recusada antes de ir à rede")
    void dataFutura() {
        servidor.responder(respostaCom());
        OraculoDAOApi cliente = cliente(2000);

        OraculoException erro = assertThrows(OraculoException.class,
                () -> cliente.buscarCotacaoPorData(LocalDate.now().plusDays(1)));

        assertEquals("erro.oraculo.dataFutura", erro.getChave());
        assertTrue(servidor.recebidas().isEmpty());
    }

    @Test
    @DisplayName("Cache + API: um período inteiro vai ao servidor em uma única requisição")
    void periodoComCacheFazUmaRequisicao() {
        LocalDate inicio = LocalDate.now().minusDays(5);
        servidor.responder(respostaCom(
                new ExchangeRate(inicio, new BigDecimal("1.00")),
                new ExchangeRate(inicio.plusDays(2), new BigDecimal("3.00"))));
        OraculoDAOCache cache = new OraculoDAOCache(cliente(2000));

        Map<LocalDate, OraculoDAOCache.Consulta> periodo = cache.consultarPeriodo(inicio, inicio.plusDays(2));

        assertEquals(3, periodo.size());
        assertEquals(1, servidor.recebidas().size());
        assertNull(periodo.get(inicio.plusDays(1)).valor());

        // Segunda chamada: tudo já está no cache, sem nova requisição
        cache.consultarPeriodo(inicio, inicio.plusDays(2));
        assertEquals(1, servidor.recebidas().size());
    }
}
