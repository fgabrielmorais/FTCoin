package br.com.ftcoin.controllers;

import br.com.ftcoin.daos.CarteiraDAO;
import br.com.ftcoin.daos.MovimentacaoDAO;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.RegraNegocioException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CarteiraController {

    private static final Logger LOG = Logger.getLogger(CarteiraController.class.getName());

    private CarteiraDAO carteiraDAO;
    private MovimentacaoDAO movimentacaoDAO;

    public CarteiraController(CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO) {
        this.carteiraDAO = carteiraDAO;
        this.movimentacaoDAO = movimentacaoDAO;
    }

    public Carteira criarCarteira(String nomeTitular, String corretora) {
        Carteira novaCarteira = new Carteira();
        novaCarteira.setNomeTitular(nomeTitular);
        novaCarteira.setCorretora(corretora);

        carteiraDAO.inserir(novaCarteira);
        // O nome do titular é dado pessoal: a carteira é identificada pelo ID
        LOG.log(Level.INFO, "Carteira {0} criada (corretora={1})", new Object[] { novaCarteira.getId(), corretora });
        return novaCarteira;
    }

    public List<Carteira> listarTodasCarteiras() {
        return carteiraDAO.listarTodas();
    }

    public Carteira buscarCarteira(int id) {
        return carteiraDAO.buscarExistente(id);
    }

    public Carteira editarCarteira(int id, String novoTitular, String novaCorretora) {
        carteiraDAO.buscarExistente(id);

        Carteira carteiraEditada = new Carteira();
        carteiraEditada.setId(id);
        carteiraEditada.setNomeTitular(novoTitular);
        carteiraEditada.setCorretora(novaCorretora);

        carteiraDAO.atualizar(carteiraEditada);
        LOG.log(Level.INFO, "Carteira {0} editada (corretora={1})", new Object[] { id, novaCorretora });
        return carteiraEditada;
    }

    public void excluirCarteira(int id) {
        carteiraDAO.buscarExistente(id);

        int movimentacoes = movimentacaoDAO.buscarPorCarteira(id).size();
        if (movimentacoes > 0) {
            // A recusa é registrada como WARNING por quem trata a exceção (presenter/menu)
            LOG.log(Level.FINE, "Exclusão da carteira {0} recusada: {1} movimentação(ões) vinculada(s)",
                    new Object[] { id, movimentacoes });
            throw new RegraNegocioException("erro.carteira.possuiMovimentacoes");
        }

        carteiraDAO.excluir(id);
        LOG.log(Level.INFO, "Carteira {0} excluída", id);
    }
}
