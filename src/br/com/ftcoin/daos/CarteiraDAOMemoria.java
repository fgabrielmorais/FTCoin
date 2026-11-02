package br.com.ftcoin.daos;

import br.com.ftcoin.models.Carteira;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CarteiraDAOMemoria extends CarteiraDAO {

    private static final Logger LOG = Logger.getLogger(CarteiraDAOMemoria.class.getName());

    private List<Carteira> tabelaCarteiras = new ArrayList<>();
    private int contadorId = 1;

    @Override
    public void inserir(Carteira carteira) {
        carteira.setId(contadorId++);
        tabelaCarteiras.add(carteira);
        LOG.log(Level.FINE, "Carteira {0} inserida em memória", carteira.getId());
    }

    @Override
    public List<Carteira> listarTodas() {
        LOG.log(Level.FINEST, "Listando {0} carteira(s) em memória", tabelaCarteiras.size());
        return tabelaCarteiras;
    }

    @Override
    public Carteira buscarPorId(int id) {
        for (Carteira c : tabelaCarteiras) {
            if (c.getId() == id) {
                return c;
            }
        }
        LOG.log(Level.FINE, "Carteira {0} não encontrada em memória", id);
        return null;
    }

    @Override
    public void atualizar(Carteira carteira) {
        if (buscarPorId(carteira.getId()) == null) {
            LOG.log(Level.WARNING, "Atualização ignorada: carteira {0} não existe em memória", carteira.getId());
            return;
        }
        tabelaCarteiras.replaceAll(c -> c.getId() == carteira.getId() ? carteira : c);
        LOG.log(Level.FINE, "Carteira {0} atualizada em memória", carteira.getId());
    }

    @Override
    public void excluir(int id) {
        if (tabelaCarteiras.removeIf(c -> c.getId() == id)) {
            LOG.log(Level.FINE, "Carteira {0} excluída da memória", id);
        } else {
            LOG.log(Level.WARNING, "Exclusão ignorada: carteira {0} não existe em memória", id);
        }
    }
}