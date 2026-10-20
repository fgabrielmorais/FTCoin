# FTCoin - Sistema de Gerenciamento de Carteira Digital

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)](https://mariadb.org/)

O **FTCoin** é um sistema de linha de comando para acompanhar carteiras de uma moeda virtual. O projeto foi desenvolvido na disciplina de Programação Orientada a Objetos do curso de **Sistemas de Informação da UNICAMP (Campus Limeira)**.

O sistema cadastra carteiras, registra compras e vendas de FTCoins e apura o resultado de cada carteira com base nas cotações fornecidas pelo Oráculo.

---

## Funcionalidades

- **Carteiras:** incluir, consultar, listar, editar e excluir. Uma carteira com movimentações não pode ser excluída.
- **Movimentações:** compra e venda de FTCoins, com recusa de venda acima do saldo.
- **Relatório da carteira:** saldo, custo médio, valor a custo, valor de mercado, ganho/perda realizado, não realizado e total, além do histórico de operações.
- **Oráculo:** cotação da moeda por data.
- **Idiomas:** português e inglês, escolhidos pelo idioma do sistema operacional.

### Como o ganho/perda é calculado

A posição é apurada pelo **custo médio ponderado de aquisição**. Cada movimentação é valorizada pela cotação do Oráculo na data em que ocorreu.

- **Compra:** aumenta o saldo e o custo total, o que recalcula o custo médio.
- **Venda:** mantém o custo médio e realiza o resultado `quantidade × (cotação da venda − custo médio)`.
- **Ganho/perda não realizado:** `saldo × cotação atual − saldo × custo médio`.
- **Ganho/perda total:** soma dos resultados realizados com o não realizado.

Todos os cálculos usam `BigDecimal` com arredondamento `HALF_EVEN`.

---

## Arquitetura

- **Linguagem:** Java 21.
- **Padrão:** MVC. A view só exibe e lê dados, os controllers orquestram os casos de uso e o domínio concentra as regras e os cálculos.
- **Persistência:** padrão DAO com classes abstratas (`CarteiraDAO`, `MovimentacaoDAO`, `OraculoDAO`) e duas implementações de cada: em memória e em MariaDB. A `DAOFactory` é o único ponto que escolhe entre elas.
- **Interface:** a classe `Menu` é genérica e recebe o título e as opções (texto e ação). Nenhum texto de interface está no código: todos vêm dos arquivos `Messages_<idioma>.properties`.
- **Erros:** as exceções do sistema carregam a chave da mensagem, e a view as exibe no idioma do usuário.

### Estrutura de pacotes

```text
src/
├── application.properties         # mecanismo de persistência
├── database.properties.example    # modelo das credenciais do banco
├── Messages_pt.properties         # textos em português
├── Messages_en.properties         # textos em inglês
└── br/com/ftcoin/
    ├── main/          # ponto de entrada
    ├── views/         # menu genérico, telas, console e mensagens
    ├── controllers/   # casos de uso
    ├── models/        # entidades, apuração da posição e exceções do domínio
    └── daos/          # persistência em memória e em MariaDB
```

---

## Como executar

### Pré-requisitos

- JDK 21.
- Maven (ou uma IDE com suporte a projetos Maven).
- MariaDB, apenas para o modo banco de dados.

### Passos

1. Clone o repositório:

   ```bash
   git clone https://github.com/fgabrielmorais/FTCoin.git
   ```

2. Importe o projeto na IDE (Eclipse, IntelliJ ou VS Code) como **projeto Maven**.
3. Execute a classe `br.com.ftcoin.main.Main`.

O projeto vem configurado para o modo memória e roda sem banco de dados. Nesse modo, os dados são perdidos ao encerrar e as cotações são simuladas: variam conforme a data, e a mesma data devolve sempre o mesmo valor.

### Usando o MariaDB

1. Execute o script `docs/script.sql` no banco.
2. Copie `src/database.properties.example` para `src/database.properties` e preencha a URL, o usuário e a senha. Esse arquivo está no `.gitignore` e não deve ser versionado.
3. Em `src/application.properties`, troque para:

   ```properties
   storage.provider=mariadb
   ```

### Idioma

O idioma segue o do sistema operacional; idiomas sem tradução caem em português. Para forçar o inglês, execute com as opções de JVM `-Duser.language=en -Duser.country=US`.

---

## Equipe de desenvolvimento

* **Ana**
* **Arthur**
* **Erik**
* **Gabriel Morais Felix**
* **Hyago**
* **Isabela**
* **Pedro**
* **Vitor**

---

## Licença

Este projeto foi desenvolvido para fins estritamente acadêmicos.
