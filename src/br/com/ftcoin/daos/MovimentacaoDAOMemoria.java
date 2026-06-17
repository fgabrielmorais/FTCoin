package br.com.ftcoin.daos;

import br.com.ftcoin.models.Movimentacao;
import java.util.ArrayList;
import java.util.List;

public class MovimentacaoDAOMemoria implements IMovimentacaoDAO {

    private List<Movimentacao> tabelaMovimentacoes = new ArrayList<>();
    private int contadorId = 1;

    @Override
    public void inserir(Movimentacao mov) {
        mov.setIdMovimento(contadorId++);
        tabelaMovimentacoes.add(mov);
    }

    @Override
    public List<Movimentacao> buscarPorCarteira(int idCarteira) {
        List<Movimentacao> filtrado = new ArrayList<>();
        for (Movimentacao m : tabelaMovimentacoes) {
            if (m.getIdCarteira() == idCarteira) {
                filtrado.add(m);
            }
        }
        return filtrado;
    }

    @Override
    public List<Movimentacao> listarTodas() {
        return tabelaMovimentacoes;
    }
}