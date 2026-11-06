package br.com.ftcoin.daos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

// Decide qual oráculo usar a partir de oracle.provider, em oracle.properties:
//   simulado - oráculo da fábrica de persistência (padrão; funciona sem rede)
//   api      - servidor de cotações, com endereço e credenciais no mesmo arquivo
// Sem oracle.properties (ele não é versionado), o oráculo é o simulado.
final class FabricaOraculo {

	private static final String ARQUIVO_CONFIGURACAO = "oracle.properties";
	private static final String CHAVE_PROVEDOR = "oracle.provider";
	private static final String PROVEDOR_PADRAO = "simulado";
	private static final int TIMEOUT_PADRAO_MS = 5000;

	private static final Logger LOG = Logger.getLogger(FabricaOraculo.class.getName());

	private FabricaOraculo() {}

	static OraculoDAO criar(OraculoDAO simulado) {
		Properties configuracao = lerConfiguracao();
		String provedor = configuracao.getProperty(CHAVE_PROVEDOR, PROVEDOR_PADRAO).trim();
		LOG.log(Level.INFO, "Oráculo definido em {0}: {1}", new Object[] { ARQUIVO_CONFIGURACAO, provedor });

		return switch (provedor.toLowerCase()) {
			case "simulado" -> simulado;
			case "api" -> criarApi(configuracao);
			default -> throw new PersistenciaException("erro.configuracao.oraculoInvalido", provedor, "simulado, api");
		};
	}

	private static OraculoDAO criarApi(Properties p) {
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
			LOG.log(Level.SEVERE, "Chave {0} ausente em {1}", new Object[] { chave, ARQUIVO_CONFIGURACAO });
			throw new PersistenciaException("erro.configuracao.chaveAusente", chave, ARQUIVO_CONFIGURACAO);
		}
		return valor;
	}

	// Arquivo ausente não é erro: devolve propriedades vazias e o oráculo fica no padrão
	private static Properties lerConfiguracao() {
		Properties propriedades = new Properties();
		try (InputStream input = FabricaOraculo.class.getClassLoader().getResourceAsStream(ARQUIVO_CONFIGURACAO)) {
			if (input == null) {
				LOG.log(Level.INFO, "{0} não encontrado; usando o oráculo {1}",
						new Object[] { ARQUIVO_CONFIGURACAO, PROVEDOR_PADRAO });
				return propriedades;
			}
			propriedades.load(input);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Falha ao ler " + ARQUIVO_CONFIGURACAO, e);
			throw new PersistenciaException("erro.configuracao.leitura", e);
		}
		return propriedades;
	}
}
