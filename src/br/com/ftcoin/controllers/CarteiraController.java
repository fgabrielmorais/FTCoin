package br.com.ftcoin.controllers;

import br.com.ftcoin.daos.CarteiraDAO;
import br.com.ftcoin.daos.MovimentacaoDAO;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.RegraNegocioException;
import java.util.List;

public class CarteiraController {

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
        return carteiraEditada;
    }

    public void excluirCarteira(int id) {
        carteiraDAO.buscarExistente(id);

        if (!movimentacaoDAO.buscarPorCarteira(id).isEmpty()) {
            throw new RegraNegocioException("erro.carteira.possuiMovimentacoes");
        }

        carteiraDAO.excluir(id);
    }
}
