package br.com.ftcoin.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.ftcoin.models.Carteira;

public class CarteiraDAOMariaDB extends CarteiraDAO {

	@Override
	public void inserir(Carteira carteira) {
		String sql = "INSERT INTO CARTEIRA (Titular, Corretora) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, carteira.getNomeTitular());
            stmt.setString(2, carteira.getCorretora());
            stmt.executeUpdate();

            // Pega o ID gerado automaticamente pelo banco e coloca no objeto
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    carteira.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("erro.persistencia.carteira.salvar", e);
        }
    }


	@Override
    public List<Carteira> listarTodas() {
        List<Carteira> lista = new ArrayList<>();
        String sql = "SELECT * FROM CARTEIRA";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("erro.persistencia.carteira.listar", e);
        }
        return lista;
    }


    @Override
    public Carteira buscarPorId(int id) {
        String sql = "SELECT * FROM CARTEIRA WHERE IdCarteira = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("erro.persistencia.carteira.buscar", e);
        }
        return null;
    }


    @Override
    public void atualizar(Carteira carteira) {
        String sql = "UPDATE CARTEIRA SET Titular = ?, Corretora = ? WHERE IdCarteira = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, carteira.getNomeTitular());
            stmt.setString(2, carteira.getCorretora());
            stmt.setInt(3, carteira.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenciaException("erro.persistencia.carteira.atualizar", e);
        }
    }


    @Override
    public void excluir(int id) {
        String sql = "DELETE FROM CARTEIRA WHERE IdCarteira = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenciaException("erro.persistencia.carteira.excluir", e);
        }
    }


    private Carteira mapear(ResultSet rs) throws SQLException {
        return new Carteira(
            rs.getInt("IdCarteira"),
            rs.getString("Titular"),
            rs.getString("Corretora")
        );
    }
}
