package br.com.ftcoin.daos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.TipoOperacao;

public class MovimentacaoDAOMariaDB extends MovimentacaoDAO {

	private static final Logger LOG = Logger.getLogger(MovimentacaoDAOMariaDB.class.getName());

	@Override
    public void inserir(Movimentacao mov) {
        String sql = "INSERT INTO MOVIMENTACAO (IdCarteira, TipoOperacao, Quantidade, Data) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mov.getIdCarteira());
            stmt.setString(2, String.valueOf(mov.getTipoOperacao().getOperacao()));
            stmt.setBigDecimal(3, mov.getQuantidadeMovimentada());

            // Converte o LocalDate do Java para o Date do SQL
            stmt.setDate(4, Date.valueOf(mov.getDataOperacao()));

            stmt.executeUpdate();
            LOG.log(Level.FINE, "INSERT MOVIMENTACAO: {0} na carteira {1}", new Object[] { mov.getTipoOperacao(), mov.getIdCarteira() });
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Falha ao inserir movimentação da carteira " + mov.getIdCarteira() + " no MariaDB", e);
            throw new PersistenciaException("erro.persistencia.movimentacao.salvar", e);
        }
    }


    @Override
    public List<Movimentacao> buscarPorCarteira(int idCarteira) {
        List<Movimentacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM MOVIMENTACAO WHERE IdCarteira = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCarteira);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            LOG.log(Level.FINE, "SELECT MOVIMENTACAO: {0} registro(s) da carteira {1}", new Object[] { lista.size(), idCarteira });
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Falha ao buscar movimentações da carteira " + idCarteira + " no MariaDB", e);
            throw new PersistenciaException("erro.persistencia.movimentacao.buscar", e);
        }
        return lista;
    }


    @Override
    public List<Movimentacao> listarTodas() {
        List<Movimentacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM MOVIMENTACAO";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
            LOG.log(Level.FINE, "SELECT MOVIMENTACAO: {0} registro(s)", lista.size());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Falha ao listar movimentações no MariaDB", e);
            throw new PersistenciaException("erro.persistencia.movimentacao.listar", e);
        }
        return lista;
    }


    private Movimentacao mapear(ResultSet rs) throws SQLException {
        Movimentacao mov = new Movimentacao();
        mov.setIdMovimento(rs.getInt("IdMovimento"));
        mov.setIdCarteira(rs.getInt("IdCarteira"));
        mov.setTipoOperacao(TipoOperacao.fromCodigo(rs.getString("TipoOperacao")));
        mov.setQuantidadeMovimentada(rs.getBigDecimal("Quantidade"));
        mov.setDataOperacao(rs.getDate("Data").toLocalDate());
        LOG.finest(() -> "Linha lida: IdMovimento=" + mov.getIdMovimento() + ", IdCarteira=" + mov.getIdCarteira()
                + ", Tipo=" + mov.getTipoOperacao() + ", Quantidade=" + mov.getQuantidadeMovimentada()
                + ", Data=" + mov.getDataOperacao());
        return mov;
    }
}
