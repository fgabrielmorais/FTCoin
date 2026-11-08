package br.com.ftcoin.daos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Oráculo de teste: devolve as cotações cadastradas, conta as consultas e pode falhar sob demanda.
 */
class OraculoFalso extends OraculoDAO {

    private final Map<LocalDate, BigDecimal> cotacoes = new HashMap<>();
    private int consultas = 0;
    private boolean falhar = false;

    OraculoFalso com(LocalDate data, String valor) {
        cotacoes.put(data, new BigDecimal(valor));
        return this;
    }

    void falhar(boolean falhar) {
        this.falhar = falhar;
    }

    int consultas() {
        return consultas;
    }

    @Override
    public BigDecimal buscarCotacaoPorData(LocalDate data) {
        consultas++;
        if (falhar) {
            throw new OraculoException("erro.oraculo.rede", "falha simulada");
        }
        return cotacoes.get(data);
    }
}
