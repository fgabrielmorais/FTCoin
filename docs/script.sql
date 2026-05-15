CREATE DATABASE IF NOT EXISTS ftcoin;
USE ftcoin;


---Carteira
---Armazenamento de dados básicos como identificação do titular e da corretora
CREATE TABLE Carteira (
	id INT PRIMARY KEY,
	nome_titular VARCHAR(255) NOT NULL,
	corretora VARCHAR(255) NOT NULL
)

---Oraculo
---Armazenamento da cotação diária. Data como PK para garantir unicidade por dia
CREATE TABLE Oraculo(
	data_cotacao DATE PRIMARY KEY,
	cotacao DECIMAL(18,8) NOT NULL CHECK (cotacao >= 0)
)



---Movimentacao
--- Registro de operações de compra (C) ou venda(V)
---Chave estrangeira vinculada a tabela Carteira
CREATE TABLE Movimentacao(
	id_movimento INT PRIMARY KEY AUTO_INCREMENT,
	id_carteira INT NOT NULL,
	data_operacao DATE NOT NULL,
	tipo_operacao CHAR(1) NOT NULL,
	quantidade_movimentada DECIMAL(18, 8) NOT NULL,
	
	CONSTRAINT fk_carteira_movimentacao
		FOREIGN KEY (id_carteira)
		REFERENCES Carteira(id)
		ObeN DELETE CASCADE,
		
	CONSTRAINT chk_tipo_operacao
		CHECK (tipo_operacao IN ('C', 'V')),

	CONSTRAINT chk_quantidade_positiva
		CHECK (quantidade_movimentada >= 0)

)