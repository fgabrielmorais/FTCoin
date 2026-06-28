package br.com.ftcoin.services;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.ftcoin.daos.IOraculoDAO;
import br.com.ftcoin.daos.OraculoDAOMemoria;
import br.com.ftcoin.daos.OraculoDAOdb;
import br.com.ftcoin.models.Oraculo;

public class OraculoService {
	
	private static OraculoService instancia;
	// private IOraculoDAO oraculoDAO = new OraculoDAOdb();
	private IOraculoDAO oraculoDAO = new OraculoDAOMemoria();
	
	private Oraculo cacheAtual;
	
	private OraculoService() {}
	
	public static OraculoService getInstancia() {
		if(instancia == null) {
			instancia = new OraculoService();
		}
		return instancia;
	}
	
	
	public BigDecimal obterCotacaoDoDia() {
		LocalDate hoje = LocalDate.now();
		
		
		// Verificação a respeito da existência do cache e se a data é a atual
		if(cacheAtual != null && cacheAtual.getData().equals(hoje)) {
			System.out.println("Usando cotação em cache (R$ " + cacheAtual.getCotacao() + ")");
			return cacheAtual.getCotacao();
		}
		
		// Se não tiver cache, busca a cotação real
		System.out.println("Cache vazio ou desatualizado. Buscando nova cotação...");
		BigDecimal novaCotacao = buscarCotacaoNoBanco(hoje);
		
		
		// Atualiza o cache com o novo valor e a data de hoje
		cacheAtual = new Oraculo(hoje, novaCotacao);
		return cacheAtual.getCotacao();
	}
	
	

	private BigDecimal buscarCotacaoNoBanco(LocalDate data) {
        BigDecimal cotacaoBanco = oraculoDAO.buscarCotacaoPorData(data);
        
        if (cotacaoBanco != null) {
            return cotacaoBanco;
        }
        
        // Se o banco de dados da faculdade estiver vazio hoje e retornar null,
        System.out.println("\n [OraculoService]: Cotação para o dia " + data + " não encontrada no banco. Usando cotação de contingência (R$ 5,50).\n");
        return new BigDecimal("5.50");
    }
}
