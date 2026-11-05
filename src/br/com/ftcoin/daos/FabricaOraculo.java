package br.com.ftcoin.daos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

// Decide qual oráculo usar a partir de oracle.provider (application.properties):
//   simulado - oráculo da fábrica de persistência (padrão; funciona sem rede)
//   api      - servidor de cotações, com endereço e credenciais em oracle.properties
final class FabricaOraculo {

	private static final String ARQUIVO_CONFIGURACAO = "application.properties";
	private static final String ARQUIVO_API = "oracle.properties";
	private static final String CHAVE_PROVEDOR = "oracle.provider";
	private static final int TIMEOUT_PADRAO_MS = 5000;

	private static final Logger LOG = Logger.getLogger(FabricaOraculo.class.getName());

	private FabricaOraculo() {}

	static OraculoDAO criar(OraculoDAO simulado) {
		String provedor = ler(ARQUIVO_CONFIGURACAO, true).getProperty(CHAVE_PROVEDOR, "simulado").trim();
		LOG.log(Level.INFO, "Oráculo definido em {0}: {1}", new Object[] { ARQUIVO_CONFIGURACAO, provedor });

		return switch (provedor.toLowerCase()) {
			case "simulado" -> simulado;
			case "api" -> criarApi();
			default -> throw new PersistenciaException("erro.configuracao.oraculoInvalido", provedor, "simulado, api");
		};
	}

	private static OraculoDAO criarApi() {
		Properties p = ler(ARQUIVO_API, true);
		try {
			return new OraculoDAOApi(
					obrigatoria(p, "oracle.host"),
					Integer.parseInt(obrigatoria(p, "oracle.port").trim()),
					obrigatoria(p, "oracle.group"),
					obrigatoria(p, "oracle.password"),
					Integer.parseInt(p.getProperty("oracle.timeoutMs", String.valueOf(TIMEOUT_PADRAO_MS)).trim()));
		} catch (NumberFormatException e) {
			throw new PersistenciaException("erro.configuracao.leitura", e);
		}
	}

	private static String obrigatoria(Properties p, String chave) {
		String valor = p.getProperty(chave);
		if (valor == null || valor.isBlank()) {
			LOG.log(Level.SEVERE, "Chave {0} ausente em {1}", new Object[] { chave, ARQUIVO_API });
			throw new PersistenciaException("erro.configuracao.chaveAusente", chave, ARQUIVO_API);
		}
		return valor;
	}

	private static Properties ler(String arquivo, boolean obrigatorio) {
		Properties propriedades = new Properties();
		try (InputStream input = FabricaOraculo.class.getClassLoader().getResourceAsStream(arquivo)) {
			if (input == null) {
				if (obrigatorio) {
					LOG.log(Level.SEVERE, "Arquivo {0} não encontrado no classpath", arquivo);
					throw new PersistenciaException("erro.configuracao.arquivoAusente", arquivo);
				}
				return propriedades;
			}
			propriedades.load(input);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Falha ao ler " + arquivo, e);
			throw new PersistenciaException("erro.configuracao.leitura", e);
		}
		return propriedades;
	}
}
