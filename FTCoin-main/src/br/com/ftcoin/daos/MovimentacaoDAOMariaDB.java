package br.com.ftcoin.daos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.TipoOperacao;

public class MovimentacaoDAOMariaDB extends MovimentacaoDAO {

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
        } catch (SQLException e) {
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
        } catch (SQLException e) {
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
        } catch (SQLException e) {
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
        return mov;
    }
}
