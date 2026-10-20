package br.com.ftcoin.daos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class OraculoDAOMariaDB extends OraculoDAO {
	
	@Override
	    public BigDecimal buscarCotacaoPorData(LocalDate data) {
	        String sql = "SELECT Cotacao FROM Oraculo WHERE Data = ?";
	        
	        try (Connection conn = ConnectionFactory.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql)) {
	            
	            stmt.setDate(1, Date.valueOf(data));
	            
	            try (ResultSet rs = stmt.executeQuery()) {
	                if (rs.next()) {
	                    return rs.getBigDecimal("Cotacao"); 
	                }
	            }
	        } catch (SQLException e) {
	            throw new PersistenciaException("erro.persistencia.oraculo.buscar", e);
	        }
	        
	        return null; 
	    }
	}

