package br.com.ftcoin.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.ftcoin.models.Movimentacao;

/**
 * Movimentações guardadas em uma lista na memória.
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class MovimentacaoDAOMemoria extends MovimentacaoDAO {

	private static final Logger LOG = Logger.getLogger(MovimentacaoDAOMemoria.class.getName());

	private final List<Movimentacao> tabelaMovimentacoes = new ArrayList<>();
	private int contadorId = 1;

	/**
	 * Cria a lista vazia de movimentações.
	 */
	public MovimentacaoDAOMemoria() {
		super();
	}

	@Override
	public void inserir(Movimentacao mov) {
		mov.setIdMovimento(contadorId++);
		tabelaMovimentacoes.add(mov);
		LOG.log(Level.FINE, "Movimentação {0} ({1}) inserida em memória na carteira {2}",
				new Object[] { mov.getIdMovimento(), mov.getTipoOperacao(), mov.getIdCarteira() });
	}

	@Override
	public List<Movimentacao> buscarPorCarteira(int idCarteira) {
		List<Movimentacao> filtrado = new ArrayList<>();
		for (Movimentacao m : tabelaMovimentacoes) {
			if (m.getIdCarteira() == idCarteira) {
				filtrado.add(m);
			}
		}
		LOG.log(Level.FINEST, "{0} movimentação(ões) da carteira {1} em memória", new Object[] { filtrado.size(), idCarteira });
		return filtrado;
	}

	@Override
	public List<Movimentacao> listarTodas() {
		LOG.log(Level.FINEST, "Listando {0} movimentação(ões) em memória", tabelaMovimentacoes.size());
		return tabelaMovimentacoes;
	}
}
