package br.com.ftcoin.daos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Decide qual oráculo usar a partir da chave {@code oracle.provider}, em {@code oracle.properties}.
 * <ul>
 * <li>{@code simulado}: oráculo calculado localmente (padrão; funciona sem rede e sem banco);</li>
 * <li>{@code api}: servidor de cotações, com endereço e credenciais no mesmo arquivo.</li>
 * </ul>
 * Sem o arquivo {@code oracle.properties} (que não é versionado), o oráculo é o simulado.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
final class FabricaOraculo {

	private static final String ARQUIVO_CONFIGURACAO = "oracle.properties";
	private static final String CHAVE_PROVEDOR = "oracle.provider";
	private static final String PROVEDOR_PADRAO = "simulado";
	private static final int TIMEOUT_PADRAO_MS = 5000;

	private static final Logger LOG = Logger.getLogger(FabricaOraculo.class.getName());

	private FabricaOraculo() {
		// Classe utilitária: não deve ser instanciada
	}

	/**
	 * Cria o oráculo configurado.
	 *
	 * @param simulado oráculo simulado, usado quando o provedor é {@code simulado} ou não há configuração
	 * @return o oráculo simulado ou o cliente do servidor de cotações
	 * @throws PersistenciaException se o provedor for desconhecido, faltar uma chave obrigatória da API
	 *         ou o arquivo não puder ser lido
	 */
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

	/**
	 * Monta o cliente da API com host, porta, grupo, senha e tempo limite lidos da configuração.
	 *
	 * @param p configuração lida de {@code oracle.properties}
	 * @return o cliente do servidor de cotações
	 */
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

	/**
	 * Lê uma chave que não pode faltar nem estar em branco. Os espaços das pontas são removidos, porque um
	 * espaço sobrando no arquivo (por exemplo, depois do nome do grupo) faria o servidor recusar a autenticação.
	 *
	 * @param p configuração lida de {@code oracle.properties}
	 * @param chave nome da chave
	 * @return o valor da chave, sem espaços nas pontas
	 */
	private static String obrigatoria(Properties p, String chave) {
		String valor = p.getProperty(chave);
		if (valor == null || valor.isBlank()) {
			LOG.log(Level.SEVERE, "Chave {0} ausente em {1}", new Object[] { chave, ARQUIVO_CONFIGURACAO });
			throw new PersistenciaException("erro.configuracao.chaveAusente", chave, ARQUIVO_CONFIGURACAO);
		}
		return valor.trim();
	}

	/**
	 * Lê {@code oracle.properties} do classpath; arquivo ausente não é erro e devolve propriedades vazias.
	 *
	 * @return a configuração lida, possivelmente vazia
	 */
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
