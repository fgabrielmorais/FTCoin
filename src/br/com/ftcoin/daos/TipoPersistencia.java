package br.com.ftcoin.daos;

public enum TipoPersistencia {
	MEMORIA("memory"),
	MARIADB("mariadb");

	private final String valorConfiguracao;

	TipoPersistencia(String valorConfiguracao) {
		this.valorConfiguracao = valorConfiguracao;
	}

	public String getValorConfiguracao() {
		return valorConfiguracao;
	}

	public static TipoPersistencia fromString(String entrada) {
		if (entrada == null || entrada.trim().isEmpty()) {
			throw new PersistenciaException("erro.configuracao.persistenciaAusente", valoresAceitos());
		}

		for (TipoPersistencia tipo : values()) {
			if (tipo.valorConfiguracao.equalsIgnoreCase(entrada.trim())) {
				return tipo;
			}
		}

		throw new PersistenciaException("erro.configuracao.persistenciaInvalida", entrada, valoresAceitos());
	}
	
	private static String valoresAceitos() {
		return MEMORIA.valorConfiguracao + ", " + MARIADB.valorConfiguracao;
	}
}
