package br.com.ftcoin.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.ftcoin.models.Carteira;

public class CarteiraDAOMariaDB extends CarteiraDAO {

	private static final Logger LOG = Logger.getLogger(CarteiraDAOMariaDB.class.getName());

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
            LOG.log(Level.FINE, "INSERT CARTEIRA: carteira {0} criada", carteira.getId());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Falha ao inserir carteira no MariaDB", e);
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
            LOG.log(Level.FINE, "SELECT CARTEIRA: {0} registro(s)", lista.size());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Falha ao listar carteiras no MariaDB", e);
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
            LOG.log(Level.FINE, "SELECT CARTEIRA: carteira {0} não encontrada", id);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Falha ao buscar a carteira " + id + " no MariaDB", e);
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
            int linhas = stmt.executeUpdate();
            registrarAlteracao("UPDATE", carteira.getId(), linhas);

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Falha ao atualizar a carteira " + carteira.getId() + " no MariaDB", e);
            throw new PersistenciaException("erro.persistencia.carteira.atualizar", e);
        }
    }


    @Override
    public void excluir(int id) {
        String sql = "DELETE FROM CARTEIRA WHERE IdCarteira = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int linhas = stmt.executeUpdate();
            registrarAlteracao("DELETE", id, linhas);

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Falha ao excluir a carteira " + id + " no MariaDB", e);
            throw new PersistenciaException("erro.persistencia.carteira.excluir", e);
        }
    }


    // Nenhuma linha alterada indica que a carteira sumiu entre a busca e a gravação
    private void registrarAlteracao(String comando, int id, int linhas) {
        if (linhas == 0) {
            LOG.log(Level.WARNING, "{0} CARTEIRA: nenhuma linha alterada para a carteira {1}", new Object[] { comando, id });
        } else {
            LOG.log(Level.FINE, "{0} CARTEIRA: carteira {1} ({2} linha(s))", new Object[] { comando, id, linhas });
        }
    }


    private Carteira mapear(ResultSet rs) throws SQLException {
        Carteira carteira = new Carteira(
            rs.getInt("IdCarteira"),
            rs.getString("Titular"),
            rs.getString("Corretora")
        );
        // O nome do titular é dado pessoal e não vai para o log
        LOG.finest(() -> "Linha lida: IdCarteira=" + carteira.getId() + ", Corretora=" + carteira.getCorretora());
        return carteira;
    }
}
