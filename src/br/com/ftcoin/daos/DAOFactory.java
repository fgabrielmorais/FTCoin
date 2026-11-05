package br.com.ftcoin.daos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DAOFactory {

	private static final String ARQUIVO_CONFIGURACAO = "application.properties";
	private static final String CHAVE_PERSISTENCIA = "storage.provider";

	private static final Logger LOG = Logger.getLogger(DAOFactory.class.getName());

	private static DAOFactory instancia;

	public abstract CarteiraDAO criarCarteiraDAO();
	public abstract MovimentacaoDAO criarMovimentacaoDAO();
	//Oráculo usado quando oracle.provider=simulado
	protected abstract OraculoDAO criarOraculoSimulado();

	//Simulado ou API, conforme oracle.provider em application.properties, sempre com cache
	public final OraculoDAOCache criarOraculoDAO() {
		return new OraculoDAOCache(FabricaOraculo.criar(criarOraculoSimulado()));
	}

	//Único ponto do sistema que decide entre memória e banco de dados
	public static DAOFactory getInstancia() {
		if (instancia == null) {
			TipoPersistencia tipo = lerTipoPersistencia();
			LOG.log(Level.INFO, "Persistência definida em {0}: {1}", new Object[] { ARQUIVO_CONFIGURACAO, tipo.getValorConfiguracao() });
			instancia = criarFabrica(tipo);
		}
		return instancia;
	}

	private static DAOFactory criarFabrica(TipoPersistencia tipo) {
		return switch (tipo) {
			case MEMORIA -> new DAOFactoryMemoria();
			case MARIADB -> new DAOFactoryMariaDB();
		};
	}

	private static TipoPersistencia lerTipoPersistencia() {
		Properties propriedades = new Properties();

		try (InputStream input = DAOFactory.class.getClassLoader().getResourceAsStream(ARQUIVO_CONFIGURACAO)) {
			if (input == null) {
				LOG.log(Level.SEVERE, "Arquivo {0} não encontrado no classpath", ARQUIVO_CONFIGURACAO);
				throw new PersistenciaException("erro.configuracao.arquivoAusente", ARQUIVO_CONFIGURACAO);
			}
			propriedades.load(input);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Falha ao ler " + ARQUIVO_CONFIGURACAO, e);
			throw new PersistenciaException("erro.configuracao.leitura", e);
		}
		
		String valor = propriedades.getProperty(CHAVE_PERSISTENCIA);
		try {
			return TipoPersistencia.fromString(valor);
		} catch (PersistenciaException e) {
			LOG.log(Level.SEVERE, "Valor inválido para {0} em {1}: [{2}]", new Object[] { CHAVE_PERSISTENCIA, ARQUIVO_CONFIGURACAO, valor });
			throw e;
		}
	}
}
