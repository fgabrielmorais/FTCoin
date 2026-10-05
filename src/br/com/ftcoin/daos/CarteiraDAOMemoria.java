package br.com.ftcoin.daos;

import br.com.ftcoin.models.Carteira;
import java.util.ArrayList;
import java.util.List;

public class CarteiraDAOMemoria extends CarteiraDAO {
    
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
        tabelaCarteiras.replaceAll(c -> c.getId() == carteira.getId() ? carteira : c);
    }

    @Override
    public void excluir(int id) {
        tabelaCarteiras.removeIf(c -> c.getId() == id);
    }
}