-- ====================================================================
-- SCRIPT DE DEFINIÇÃO DE DADOS (DDL) - FTCOIN
-- Banco de dados relacional (MariaDB), com as duas tabelas do enunciado:
-- CARTEIRA e MOVIMENTACAO.
--
-- O banco é escolhido na URL de conexão (db.url, em database.properties).
-- Crie o banco, selecione-o e só então execute este script, por exemplo:
--   USE NOME_DO_BANCO;
-- O oráculo de cotações NÃO é uma tabela: vem do servidor (API) ou do
-- oráculo simulado, ambos com cache.
-- ====================================================================

-- --------------------------------------------------------
-- Tabela: CARTEIRA
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS CARTEIRA (
  IdCarteira INT NOT NULL AUTO_INCREMENT,
  Titular VARCHAR(50) NOT NULL,
  Corretora VARCHAR(50) NOT NULL,
  PRIMARY KEY (IdCarteira)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------
-- Tabela: MOVIMENTACAO
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS MOVIMENTACAO (
  IdCarteira INT NOT NULL,
  IdMovimento INT NOT NULL AUTO_INCREMENT,
  Data DATE NOT NULL,
  TipoOperacao CHAR(1) NOT NULL,
  Quantidade DECIMAL(10,3) NOT NULL,
  PRIMARY KEY (IdMovimento),
  KEY idx_carteira_movimentacao (IdCarteira),
  CONSTRAINT fk_mov_carteira FOREIGN KEY (IdCarteira) REFERENCES CARTEIRA (IdCarteira) ON DELETE RESTRICT,
  CONSTRAINT ck_mov_tipo CHECK (TipoOperacao IN ('C', 'V')),
  CONSTRAINT ck_mov_quantidade CHECK (Quantidade >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
