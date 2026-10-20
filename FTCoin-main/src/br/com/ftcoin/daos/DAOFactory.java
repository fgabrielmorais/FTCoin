package br.com.ftcoin.daos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class DAOFactory {

	private static final String ARQUIVO_CONFIGURACAO = "application.properties";
	private static final String CHAVE_PERSISTENCIA = "storage.provider";

	private static DAOFactory instancia;

	public abstract CarteiraDAO criarCarteiraDAO();
	public abstract MovimentacaoDAO criarMovimentacaoDAO();
	public abstract OraculoDAO criarOraculoDAO();

	//Único ponto do sistema que decide entre memória e banco de dados
	public static DAOFactory getInstancia() {
		if (instancia == null) {
			instancia = criarFabrica(lerTipoPersistencia());
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
				throw new PersistenciaException("erro.configuracao.arquivoAusente", ARQUIVO_CONFIGURACAO);
			}
			propriedades.load(input);
		} catch (IOException e) {
			throw new PersistenciaException("erro.configuracao.leitura", e);
		}
		
		return TipoPersistencia.fromString(propriedades.getProperty(CHAVE_PERSISTENCIA));
	}
}
