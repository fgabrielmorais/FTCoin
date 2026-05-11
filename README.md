
# 🪙 FTCoin - Sistema de Gerenciamento de Carteira Digital

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)](https://mariadb.org/)

O **FTCoin** é um sistema robusto de acompanhamento de carteiras de moedas virtuais desenvolvido para a interface de linha de comando (CLI). O projeto foi concebido como parte da disciplina de Programação Orientada a Objetos no curso de **Sistemas de Informação da UNICAMP (Campus Limeira)**.

O sistema permite que investidores gerenciem seus ativos, realizem compras e vendas com validação de saldo e acompanhem a rentabilidade de suas carteiras através de relatórios detalhados baseados em cotações reais (via "Oráculo").

---

## 🚀 Funcionalidades Principal

- **Gestão de Carteiras:** CRUD completo (Incluir, Consultar, Editar e Excluir) de carteiras de investimento.
- **Operações Financeiras:** Sistema de compra e venda de ativos com atualização dinâmica de saldo.
- **Relatórios Consolidados:** - Listagem de carteiras por identificador ou nome do titular.
    - Exibição de saldo atualizado e histórico de movimentações.
    - Cálculo de ganhos ou perdas totais baseado na cotação do dia.
- **Oráculo de Cotação:** Integração para busca de valores reais da moeda virtual.

### ✨ Diferenciais (Recursos Extras)
- **Cache de Cotações:** Implementação de um sistema de cache em memória para evitar consultas redundantes ao oráculo, otimizando o desempenho da aplicação.
- **CLI Customizada:** Interface de terminal estilizada com cores (ANSI Escape Codes), facilitando a leitura de dados financeiros e a navegação nos menus.

---

## 🛠️ Tecnologias e Arquitetura

O projeto foi estruturado utilizando padrões de engenharia de software para garantir escalabilidade e manutenção simplificada:

- **Linguagem:** Java 8 ou superior.
- **Arquitetura:** MVC (Model-View-Controller) para separação de responsabilidades.
- **Persistência:** Padrão **DAO (Data Access Object)** com suporte híbrido:
    - Simulação em memória (para desenvolvimento ágil).
    - Banco de dados relacional remoto (**MariaDB**).
- **Transporte de Dados:** Uso de **DTOs (Data Transfer Objects)** para comunicação entre camadas.
- **Gerenciamento de Dependências:** **Maven** para automação do driver JDBC e bibliotecas.

---

## 🏗️ Estrutura de Pacotes

```text
src/br/com/ftcoin/
├── controllers/   # Regras de negócio e orquestração
├── daos/          # Abstração de persistência (Memória e MariaDB)
├── models/        # Entidades do domínio e DTOs
├── services/      # Integração com Oráculo e Cache
└── views/         # Interface de usuário (CLI) e formatação visual

```

---

## 🔧 Como Executar

### Pré-requisitos

* Java JDK 8+ instalado.
* Maven instalado.
* Instância do MariaDB (opcional para modo banco de dados).

### Instalação

1. Clone este repositório:
```bash
git clone [https://github.com/fgabrielmorais/FTCoin.git](https://github.com/fgabrielmorais/FTCoin.git)

```


2. Importe o projeto na sua IDE (Eclipse/VS Code/IntelliJ) como um **Projeto Maven**.
3. Caso utilize o banco de dados, execute o script SQL contido em `/docs/script.sql`.
4. Execute a classe `Main.java` localizada no pacote raiz.

---

## 👥 Equipe e Papéis

O desenvolvimento seguiu a metodologia ágil **Kanban**, com a seguinte distribuição de responsabilidades:

* **Gabriel Morais Felix**: Líder de Arquitetura, Versionamento e Integração (Serviços e Cache).
* **Erik & Vitor**: Desenvolvimento de Dados (Models e DAOs).
* **Hyago & Arthur**: Desenvolvimento de Lógica de Negócio (Controllers).
* **Isabela & Ana**: Desenvolvimento de Experiência do Usuário (Views CLI).
* **Pedro**: Interface de Usuário e Garantia de Qualidade (QA/Testes).

---

## 📝 Licença

Este projeto foi desenvolvido para fins estritamente acadêmicos.

```

Para usar, basta criar um arquivo chamado `README.md` na raiz do seu projeto no Eclipse, colar esse conteúdo e salvar. Quando você subir para o GitHub, ele ficará com essa aparência profissional automaticamente!

```