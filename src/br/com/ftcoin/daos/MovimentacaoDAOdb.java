package br.com.ftcoin.daos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.TipoOperacao;

public class MovimentacaoDAOdb implements IMovimentacaoDAO{
	
	//INSERIR UM TIPO DE MOVIMENTACAO PARA A CARTEIRA
	@Override
    public void inserir(Movimentacao mov) {
        String sql = "INSERT INTO MOVIMENTACAO (IdCarteira, TipoOperacao, Quantidade, Data) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, mov.getIdCarteira());
            stmt.setString(2, mov.getTipoOperacao().name().substring(0, 1));
            stmt.setBigDecimal(3, mov.getQuantidadeMovimentada());
        
            // Converte o LocalDate do Java para o Date do SQL
            stmt.setDate(4, Date.valueOf(mov.getDataOperacao())); 
            
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao registrar movimentação: " + e.getMessage(), e);
        }
    }

	
	
	//BUSCAR DETERMINADA CARTEIRA PARA MOVIMENTACAO
    @Override
    public List<Movimentacao> buscarPorCarteira(int idCarteira) {
        List<Movimentacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM MOVIMENTACAO WHERE IdCarteira = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idCarteira);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Movimentacao mov = new Movimentacao();
                    mov.setIdMovimento(rs.getInt("IdMovimento"));
                    mov.setIdCarteira(rs.getInt("IdCarteira"));
                    String letraBanco = rs.getString("TipoOperacao"); // ou "TipoOperacao", dependendo do nome da sua coluna
                    mov.setTipoOperacao(letraBanco.equals("C") ? TipoOperacao.COMPRA : TipoOperacao.VENDA);
                    mov.setQuantidadeMovimentada(rs.getBigDecimal("Quantidade"));
                    mov.setDataOperacao(rs.getDate("Data").toLocalDate());
                    lista.add(mov);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar movimentações: " + e.getMessage(), e);
        }
        return lista;
    }
    
    
    //LISTAR TODAS AS MOVIMENTACOES FEITAS
    @Override
    public List<Movimentacao> listarTodas() {
        List<Movimentacao> lista = new ArrayList<>();
        String sql = "SELECT * FROM movimentacoes";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Movimentacao mov = new Movimentacao();
                // Lendo as colunas com os nomes exatos apontados pelo erro
                mov.setIdMovimento(rs.getInt("IdMovimento"));
                mov.setIdCarteira(rs.getInt("IdCarteira"));
                mov.setTipoOperacao(TipoOperacao.valueOf(rs.getString("TipoOperacao")));
                mov.setQuantidadeMovimentada(rs.getBigDecimal("Quantidade"));
                mov.setDataOperacao(rs.getDate("Data").toLocalDate());
                lista.add(mov);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar todas as movimentações: " + e.getMessage(), e);
        }
        return lista; 
        }

}
