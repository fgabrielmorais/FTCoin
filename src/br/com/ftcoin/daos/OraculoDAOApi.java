package br.com.ftcoin.daos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import network.ExchangeRate;
import network.NetworkRequest;
import network.NetworkResponse;

/**
 * Oráculo que consulta o servidor de cotações do professor.
 * <p>
 * Protocolo: abre um {@link Socket}, envia um {@link NetworkRequest} (grupo, senha e faixa de datas) por um
 * {@link ObjectOutputStream} e recebe um {@link NetworkResponse} por um {@link ObjectInputStream}, com a lista
 * de cotações da faixa (que pode vir vazia). Cada consulta usa uma conexão nova, com limite de tempo para
 * conectar e para esperar a resposta.
 * </p>
 * <p>
 * São erros: faixa de datas invertida, data futura, credenciais recusadas pelo servidor, rede fora do ar e
 * resposta de classe inesperada. <strong>Não ter cotação não é erro</strong>: o método devolve {@code null}
 * ou uma lista vazia.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class OraculoDAOApi extends OraculoDAO {

	private static final Logger LOG = Logger.getLogger(OraculoDAOApi.class.getName());

	private static final int CASAS_DECIMAIS = 2;

	private final String host;
	private final int porta;
	private final String grupo;
	private final String senha;
	private final int timeoutMs;

	/**
	 * Cria o cliente do servidor de cotações. Nenhuma conexão é aberta aqui.
	 *
	 * @param host endereço do servidor
	 * @param porta porta do servidor
	 * @param grupo nome do grupo, usado na autenticação
	 * @param senha senha do grupo; nunca é escrita nos logs
	 * @param timeoutMs tempo máximo, em milissegundos, para conectar e para esperar a resposta
	 */
	public OraculoDAOApi(String host, int porta, String grupo, String senha, int timeoutMs) {
		this.host = host;
		this.porta = porta;
		this.grupo = grupo;
		this.senha = senha;
		this.timeoutMs = timeoutMs;
	}

	/**
	 * Consulta a faixa {@code [data, data]} e só usa a cotação se a resposta trouxer exatamente essa data.
	 *
	 * @param data dia da cotação, que não pode estar no futuro
	 * @return a cotação com 2 casas decimais, ou {@code null} se o servidor não tiver cotação para o dia
	 * @throws OraculoException se a data for futura, o servidor recusar a consulta ou houver falha de rede
	 */
	@Override
	public BigDecimal buscarCotacaoPorData(LocalDate data) {
		if (data.isAfter(LocalDate.now())) {
			throw new OraculoException("erro.oraculo.dataFutura", data);
		}

		for (ExchangeRate cotacao : buscarCotacoesPorPeriodo(data, data)) {
			if (data.equals(cotacao.getDate()) && cotacao.getValue() != null) {
				return cotacao.getValue().setScale(CASAS_DECIMAIS, RoundingMode.HALF_EVEN);
			}
		}
		LOG.log(Level.FINE, "Oráculo sem cotação para {0}", data);
		return null;
	}

	/**
	 * Pede ao servidor todas as cotações de um período, em uma única requisição.
	 *
	 * @param inicio primeiro dia do período
	 * @param fim último dia do período, que não pode ser anterior ao primeiro
	 * @return as cotações recebidas; lista vazia quando o servidor não tem nenhuma no período
	 * @throws OraculoException se a faixa estiver invertida, o servidor recusar a consulta (por exemplo, por
	 *         credenciais inválidas), a resposta for inválida ou houver falha de rede
	 */
	public List<ExchangeRate> buscarCotacoesPorPeriodo(LocalDate inicio, LocalDate fim) {
		if (inicio.isAfter(fim)) {
			throw new OraculoException("erro.oraculo.faixaInvertida", inicio, fim);
		}

		NetworkResponse resposta = enviar(new NetworkRequest(grupo, senha, inicio, fim));

		if (resposta == null) {
			throw new OraculoException("erro.oraculo.respostaInvalida", "null");
		}
		if (resposta.getErrorMessage() != null) {
			LOG.log(Level.WARNING, "Oráculo recusou a consulta: {0}", resposta.getErrorMessage());
			throw new OraculoException("erro.oraculo.servidor", resposta.getErrorMessage());
		}
		List<ExchangeRate> cotacoes = resposta.getExchangeRates();
		return cotacoes == null ? List.of() : cotacoes;
	}

	/**
	 * Envia a requisição ao servidor e lê a resposta, fechando o socket ao terminar.
	 *
	 * @param requisicao requisição a enviar
	 * @return a resposta do servidor
	 * @throws OraculoException se a rede falhar ou o servidor responder um objeto que não é {@link NetworkResponse}
	 */
	private NetworkResponse enviar(NetworkRequest requisicao) {
		LOG.log(Level.FINE, "Consultando oráculo {0}:{1} de {2} a {3}",
				new Object[] { host, porta, requisicao.getFirstDate(), requisicao.getLastDate() });

		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, porta), timeoutMs);
			socket.setSoTimeout(timeoutMs);

			ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
			saida.writeObject(requisicao);
			saida.flush();

			ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
			Object objeto = entrada.readObject();
			if (!(objeto instanceof NetworkResponse resposta)) {
				throw new OraculoException("erro.oraculo.respostaInvalida",
						objeto == null ? "null" : objeto.getClass().getName());
			}
			return resposta;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Falha de rede ao consultar o oráculo", e);
			throw new OraculoException("erro.oraculo.rede", e);
		} catch (ClassNotFoundException e) {
			LOG.log(Level.SEVERE, "Oráculo enviou objeto de classe desconhecida", e);
			throw new OraculoException("erro.oraculo.respostaInvalida", e);
		}
	}
}
