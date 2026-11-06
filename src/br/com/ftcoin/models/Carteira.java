package br.com.ftcoin.models;

/**
 * Carteira de FTCoins de um titular em uma corretora.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class Carteira {

	private int id;
	private String nomeTitular;
	private String corretora;

	/**
	 * Cria uma carteira vazia, a ser preenchida pelos setters.
	 */
	public Carteira() {
		// Campos preenchidos depois, com validação nos setters
	}

	/**
	 * Cria uma carteira já gravada, sem validar os campos (usado ao ler da persistência).
	 *
	 * @param id identificador da carteira
	 * @param nomeTitular nome do titular
	 * @param corretora nome da corretora
	 */
	public Carteira(int id, String nomeTitular, String corretora) {
		this.id = id;
		this.nomeTitular = nomeTitular;
		this.corretora = corretora;
	}

	/**
	 * Devolve o identificador.
	 *
	 * @return o identificador da carteira
	 */
	public int getId() {
		return id;
	}

	/**
	 * Define o identificador, atribuído pela persistência.
	 *
	 * @param id identificador da carteira
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Devolve o nome do titular.
	 *
	 * @return o nome do titular
	 */
	public String getNomeTitular() {
		return nomeTitular;
	}

	/**
	 * Define o nome do titular, sem espaços nas pontas.
	 *
	 * @param nomeTitular nome do titular
	 * @throws RegraNegocioException se o nome estiver vazio
	 */
	public void setNomeTitular(String nomeTitular) {
		this.nomeTitular = exigirPreenchido(nomeTitular, "erro.carteira.titularObrigatorio");
	}

	/**
	 * Devolve o nome da corretora.
	 *
	 * @return o nome da corretora
	 */
	public String getCorretora() {
		return corretora;
	}

	/**
	 * Define o nome da corretora, sem espaços nas pontas.
	 *
	 * @param corretora nome da corretora
	 * @throws RegraNegocioException se o nome estiver vazio
	 */
	public void setCorretora(String corretora) {
		this.corretora = exigirPreenchido(corretora, "erro.carteira.corretoraObrigatoria");
	}

	private static String exigirPreenchido(String valor, String chaveDoErro) {
		if (valor == null || valor.trim().isEmpty()) {
			throw new RegraNegocioException(chaveDoErro);
		}
		return valor.trim();
	}
}
