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

// Oráculo que consulta o servidor de cotações do professor.
// Protocolo: abre um socket, envia um NetworkRequest (grupo, senha e faixa de datas)
// e recebe um NetworkResponse com a lista de cotações da faixa (pode vir vazia).
public class OraculoDAOApi extends OraculoDAO {

	private static final Logger LOG = Logger.getLogger(OraculoDAOApi.class.getName());

	private final String host;
	private final int porta;
	private final String grupo;
	private final String senha;
	private final int timeoutMs;

	public OraculoDAOApi(String host, int porta, String grupo, String senha, int timeoutMs) {
		this.host = host;
		this.porta = porta;
		this.grupo = grupo;
		this.senha = senha;
		this.timeoutMs = timeoutMs;
	}

	//Consulta a faixa [data, data] e só usa a cotação se a resposta trouxer exatamente essa data.
	//Devolve null quando não há cotação (não é erro).
	@Override
	public BigDecimal buscarCotacaoPorData(LocalDate data) {
		if (data.isAfter(LocalDate.now())) {
			throw new OraculoException("erro.oraculo.dataFutura", data);
		}

		for (ExchangeRate cotacao : buscarCotacoesPorPeriodo(data, data)) {
			if (data.equals(cotacao.getDate()) && cotacao.getValue() != null) {
				return cotacao.getValue().setScale(2, RoundingMode.HALF_EVEN);
			}
		}
		LOG.log(Level.FINE, "Oráculo sem cotação para {0}", data);
		return null;
	}

	//Todas as cotações do período informado; lista vazia quando não há nenhuma
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
