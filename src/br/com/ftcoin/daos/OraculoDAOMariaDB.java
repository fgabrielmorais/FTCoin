package br.com.ftcoin.daos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OraculoDAOMariaDB extends OraculoDAO {

	private static final Logger LOG = Logger.getLogger(OraculoDAOMariaDB.class.getName());
	
	@Override
	    public BigDecimal buscarCotacaoPorData(LocalDate data) {
	        String sql = "SELECT Cotacao FROM Oraculo WHERE Data = ?";
	        
	        try (Connection conn = ConnectionFactory.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql)) {
	            
	            stmt.setDate(1, Date.valueOf(data));
	            
	            try (ResultSet rs = stmt.executeQuery()) {
	                if (rs.next()) {
	                    BigDecimal cotacao = rs.getBigDecimal("Cotacao");
	                    LOG.log(Level.FINE, "SELECT Oraculo: cotação de {0} = {1}", new Object[] { data, cotacao });
	                    return cotacao;
	                }
	            }
	            LOG.log(Level.FINE, "SELECT Oraculo: sem cotação cadastrada para {0}", data);
	        } catch (SQLException e) {
	            LOG.log(Level.SEVERE, "Falha ao buscar a cotação de " + data + " no MariaDB", e);
	            throw new PersistenciaException("erro.persistencia.oraculo.buscar", e);
	        }
	        
	        return null; 
	    }
	}

