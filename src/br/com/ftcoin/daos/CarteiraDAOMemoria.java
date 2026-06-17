package br.com.ftcoin.daos;

import br.com.ftcoin.models.Carteira;
import java.util.ArrayList;
import java.util.List;

public class CarteiraDAOMemoria implements ICarteiraDAO {
    
    // A nossa "tabela" do banco de dados na memória RAM
    private List<Carteira> tabelaCarteiras = new ArrayList<>();
    private int contadorId = 1;

    @Override
    public void inserir(Carteira carteira) {
        carteira.setId(contadorId++);
        tabelaCarteiras.add(carteira);
    }

    @Override
    public List<Carteira> listarTodas() {
        return tabelaCarteiras;
    }

    @Override
    public Carteira buscarPorId(int id) {
        for (Carteira c : tabelaCarteiras) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    @Override
    public void atualizar(Carteira carteira) {
        // No modo memória, se a gente atualiza o objeto, ele já atualiza na lista
    }

    @Override
    public void excluir(int id) {
        tabelaCarteiras.removeIf(c -> c.getId() == id);
    }
}