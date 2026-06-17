package br.com.ftcoin.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import br.com.ftcoin.models.Carteira;

public class CarteiraDAOdb implements ICarteiraDAO {
	
	
	
	//INSERIR UMA CARTEIRA
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
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar carteira: " + e.getMessage(), e);
        }
    }

	
	//LISTAR TODAS AS CARTEIRAS
	@Override
    public List<Carteira> listarTodas() {
        List<Carteira> lista = new ArrayList<>();
        String sql = "SELECT * FROM CARTEIRA";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Carteira c = new Carteira();
                c.setId(rs.getInt("id"));
                c.setNomeTitular(rs.getString("Titular"));
                c.setCorretora(rs.getString("Corretora"));
                lista.add(c);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar carteiras: " + e.getMessage(), e);
        }
        return lista;
    }

	
	//BUSCAR UMA CARTEIRA POR UM ID
    @Override
    public Carteira buscarPorId(int id) {
        String sql = "SELECT * FROM CARTEIRA WHERE idCarteira = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Carteira c = new Carteira();
                    c.setId(rs.getInt("id"));
                    c.setNomeTitular(rs.getString("nome_titular"));
                    c.setCorretora(rs.getString("corretora"));
                    return c;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar carteira: " + e.getMessage(), e);
        }
        return null;
    }
    
    
    
    //ATUALIZAR AS INFORMAÇÕES DA CARTEIRA
    @Override
    public void atualizar(Carteira carteira) {
        String sql = "UPDATE CARTEIRA SET Titular = ?, Corretora = ? WHERE idCarteira = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Setando os novos valores
            stmt.setString(1, carteira.getNomeTitular());
            stmt.setString(2, carteira.getCorretora());
            // Dizendo ao banco QUAL carteira vai ser alterada (pelo ID)
            stmt.setInt(3, carteira.getId());
            
            int linhasAfetadas = stmt.executeUpdate();
            
            if (linhasAfetadas == 0) {
                System.out.println("Nenhuma carteira foi atualizada. Verifique se o ID existe.");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar a carteira: " + e.getMessage(), e);
        }
    }

    
    //EXCLUIR AS INFORMAÇÕES DA CARTEIRA
    @Override
    public void excluir(int id) {
        String sql = "DELETE FROM CARTEIRA WHERE idCarteira = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int linhasAfetadas = stmt.executeUpdate();
            
            if (linhasAfetadas == 0) {
                System.out.println("Nenhuma carteira foi excluída. Verifique se o ID existe.");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao excluir a carteira: " + e.getMessage(), e);
        }
    }
}