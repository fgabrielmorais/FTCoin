-- ====================================================================
-- SCRIPT DE DEFINITION DATA (DDL) - FTCOIN 
-- Ambiente: Universidade de Campinas (UNICAMP) - WindServer
-- Base de Dados: PooI_1s26_B02
-- ====================================================================

CREATE DATABASE IF NOT EXISTS PooI_1s26_B02;
USE PooI_1s26_B02;

-- --------------------------------------------------------
-- Tabela: CARTEIRA
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS CARTEIRA (
  IdCarteira INT(11) NOT NULL AUTO_INCREMENT,
  Titular VARCHAR(50) NOT NULL,
  Corretora VARCHAR(50) NOT NULL,
  PRIMARY KEY (IdCarteira)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------
-- Tabela: MOVIMENTACAO
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS MOVIMENTACAO (
  IdCarteira INT(11) NOT NULL,
  IdMovimento INT(11) NOT NULL AUTO_INCREMENT,
  Data DATE DEFAULT NULL,
  TipoOperacao CHAR(1) DEFAULT NULL,
  Quantidade DECIMAL(10,3) DEFAULT NULL,
  PRIMARY KEY (IdMovimento),
  KEY idx_carteira_movimentacao (IdCarteira),
  CONSTRAINT fk_mov_carteira FOREIGN KEY (IdCarteira) REFERENCES CARTEIRA (IdCarteira)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------
-- Tabela: ORACULO
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS ORACULO (
  Data DATE NOT NULL,
  Cotacao DECIMAL(6,2) NOT NULL,
  PRIMARY KEY (Data)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;