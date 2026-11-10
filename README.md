# FTCoin - Sistema de Gerenciamento de Carteira Digital

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)](https://mariadb.org/)

O **FTCoin** é um sistema para acompanhar carteiras de uma moeda virtual. O projeto foi desenvolvido na disciplina de Programação Orientada a Objetos do curso de **Sistemas de Informação da UNICAMP (Campus Limeira)**.

O sistema cadastra carteiras, registra compras e vendas de FTCoins e apura o resultado de cada carteira com base nas cotações fornecidas pelo Oráculo. A interface é o **FTCoin Studio**, uma aplicação gráfica em Swing.

---

## Funcionalidades

- **Dashboard:** resumo do patrimônio, saldo, cotação do dia e últimas operações.
- **Carteiras:** incluir, consultar, listar, editar e excluir. Uma carteira com movimentações não pode ser excluída.
- **Movimentações:** compra e venda de FTCoins, com estimativa do valor antes de confirmar e recusa de venda acima do saldo.
- **Cotação:** consulta por data no Oráculo (simulado ou servidor de cotações do professor), com cache e indicação da origem (Oráculo ou cache).
- **Relatórios:** posição da carteira, histórico de operações, consolidado do portfólio, desempenho, histórico de cotações e listagens de carteiras ordenadas por identificador e por titular.
- **Idiomas:** português, inglês e espanhol, com troca durante a execução.
- **Ajuda, créditos e saída:** manual do usuário e tela de créditos nos três idiomas, e o botão **Sair**, que fecha as conexões e encerra o programa.

### Como o ganho/perda é calculado

A posição é apurada pelo **custo médio ponderado de aquisição**. Cada movimentação é valorizada pela cotação do Oráculo na data em que ocorreu.

- **Compra:** aumenta o saldo e o custo total, o que recalcula o custo médio.
- **Venda:** mantém o custo médio e realiza o resultado `quantidade × (cotação da venda − custo médio)`.
- **Ganho/perda não realizado:** `saldo × cotação atual − saldo × custo médio`.
- **Ganho/perda total:** soma dos resultados realizados com o não realizado.

Todos os cálculos usam `BigDecimal` com arredondamento `HALF_EVEN`.

---

## Arquitetura

- **Linguagem:** Java 21, com `module-info`.
- **Interface gráfica (MVP):** cada tela implementa um contrato `I*View` e só exibe e lê dados. O presenter da tela recebe a view e os serviços pelo construtor, trata os eventos e decide o que mostrar.
- **Modelo:** as entidades (`Carteira`, `Movimentacao`, `Cotacao`), a apuração da posição (`Posicao`) e os serviços `CarteiraService` e `MovimentacaoService` concentram as regras e os cálculos, inclusive os dos relatórios (ROI, participação no portfólio, variação da cotação). As telas recebem as próprias entidades, sem objetos intermediários.
- **Contexto da aplicação:** a GUI cria um único `ContextoAplicacao` na inicialização e o injeta nas telas. Ele guarda os DAOs, os serviços e as mensagens, e é o único ponto que troca entre memória e MariaDB.
- **Persistência:** padrão DAO com classes abstratas (`CarteiraDAO`, `MovimentacaoDAO`) e duas implementações de cada, uma por tabela: em memória e em MariaDB. A `DAOFactory` (Abstract Factory) escolhe entre elas. Os DAOs não são Singleton; o Singleton fica só na conexão com o banco (`ConexaoMariaDB`), fechada ao sair.
- **Oráculo:** a chave `oracle.provider` em `oracle.properties` escolhe a origem das cotações: `simulado` (padrão, sem rede e sem banco: a cotação é calculada localmente nos dois modos de persistência) ou `api` (servidor de cotações, cliente `OraculoDAOApi` sobre as classes de protocolo do pacote `network`). Em qualquer caso o oráculo é envolvido pelo `OraculoDAOCache`, que busca cada data uma vez por execução, guarda também as datas passadas sem cotação e nunca guarda falhas. As consultas que podem ir à rede rodam em virtual threads, sem travar a tela.
- **Textos:** nenhum texto de interface fica no código. Os textos curtos vêm dos arquivos `Messages_<idioma>.properties`, e o manual e os créditos, de arquivos HTML por idioma em `gui/textos`.
- **Erros:** as exceções do sistema carregam a chave da mensagem, e a interface as exibe no idioma ativo.
- **Logs:** `java.util.logging`, configurado em `logging.properties`, grava em `logs/`.

### Estrutura de pacotes

```text
src/
├── database.properties.example    # modelo das credenciais do banco
├── oracle.properties.example      # modelo da configuração do oráculo (simulado ou API)
├── logging.properties             # configuração dos logs
├── Messages_pt.properties         # textos em português
├── Messages_en.properties         # textos em inglês
├── Messages_es.properties         # textos em espanhol
├── network/                       # protocolo de rede do servidor de cotações (fornecido pelo professor)
└── br/com/ftcoin/
    ├── models/        # entidades, apuração da posição e exceções do domínio
    ├── services/      # casos de uso (carteiras e movimentações)
    ├── daos/          # persistência em memória e em MariaDB, oráculos e cache de cotações
    ├── config/        # configuração dos logs
    └── gui/           # interface gráfica (Swing, padrão MVP)
        ├── main/          # ponto de entrada da GUI
        ├── common/        # contexto, mensagens, formatação e enums da interface
        ├── presenters/    # um presenter por tela
        ├── views/         # telas, contratos (contracts/) e componentes visuais
        ├── services/      # consultas de cotação da interface (em segundo plano)
        └── textos/        # manual e créditos em HTML, um arquivo por idioma
```

---

## Como executar

### Pré-requisitos

- JDK 21.
- Maven, ou uma IDE com suporte a projetos Maven.
- MariaDB, apenas para o modo banco de dados.

### Interface gráfica

No Windows, execute na raiz do repositório:

```powershell
.\iniciar_interface.bat
```

O script compila o projeto com Maven, prepara as dependências e abre a interface. Ele usa `JAVA_HOME` e `MAVEN_HOME` quando definidas; caso contrário, procura Java e Maven no `PATH`.

Pelo terminal, em qualquer sistema:

```bash
mvn compile dependency:copy-dependencies -DincludeScope=runtime
```

```powershell
java -cp "target\classes;target\dependency\*" br.com.ftcoin.gui.main.FTCoinGuiApp
```

No Linux ou macOS, troque o separador do classpath por `:` (`target/classes:target/dependency/*`).

Também é possível importar o projeto na IDE como **projeto Maven** e executar a classe `br.com.ftcoin.gui.main.FTCoinGuiApp`.

A interface inicia no modo memória, com carteiras e operações de demonstração, e roda sem banco de dados. O seletor **Banco** da barra superior troca para MariaDB durante a execução; a troca não copia os dados de um modo para o outro.

### Modo memória

Os dados são perdidos ao encerrar. Com o oráculo simulado (padrão), as cotações variam conforme a data, e a mesma data devolve sempre o mesmo valor. Não representam preços reais de mercado.

### Usando o servidor de cotações (API)

1. Copie `src/oracle.properties.example` para `src/oracle.properties`.
2. Mantenha `oracle.provider=api` e preencha `oracle.group` e `oracle.password` com os dados do grupo. Host, porta e tempo limite já vêm com os valores do servidor do professor. Esse arquivo está no `.gitignore` e não deve ser versionado.
3. Recompile e abra a interface. As cotações passam a vir do servidor, tanto no modo memória quanto no MariaDB.

Sem o `oracle.properties`, ou com `oracle.provider=simulado`, a aplicação usa o oráculo simulado. Datas sem cotação no servidor aparecem como "Sem cotação", e falhas de rede são exibidas com a mensagem traduzida, sem travar a tela.

### Usando o MariaDB

1. Execute o script `docs/script.sql` no banco.
2. Copie `src/database.properties.example` para `src/database.properties` e preencha a URL, o usuário e a senha. Esse arquivo está no `.gitignore` e não deve ser versionado.
3. Na interface, escolha MariaDB no seletor **Banco**.

### Testes

Os testes unitários e de integração (JUnit 5) ficam em `test/`, fora de `src/`. Para executá-los:

```bash
mvn test
```

Cobrem a apuração por custo médio, as entidades, os serviços com DAOs em memória, o cache do oráculo, o cliente da API contra um servidor local falso (sucesso, sem cotação, credenciais recusadas, resposta inválida, rede e timeout) e a paridade das chaves de mensagens nos três idiomas. Nenhum teste precisa de rede ou de banco de dados.

### Idioma

O idioma inicial segue o da JVM: português do Brasil, inglês dos Estados Unidos ou espanhol da Espanha. Outros idiomas caem em português. Na interface gráfica, o seletor **Idioma** troca o idioma sem reiniciar e preserva os campos preenchidos e a aba aberta.

Para iniciar em outro idioma, use as opções de JVM `-Duser.language=en -Duser.country=US` (inglês) ou `-Duser.language=es -Duser.country=ES` (espanhol).

### Quantidades, datas e valores

- Quantidades aceitam ponto ou vírgula como separador decimal, sem separador de milhares: `2.5` e `2,5` são a mesma quantidade.
- As datas usam o formato `DD/MM/AAAA` em qualquer idioma, na tela e nos campos (a tela Cotação também aceita `AAAA-MM-DD`). Datas futuras são recusadas.
- Os números seguem o idioma escolhido, mas as datas têm sempre o mesmo formato. Os valores monetários são sempre em reais (BRL): trocar o idioma não converte a moeda.
- **Limitação atual:** a compra ou venda é registrada com a data do dia e a cotação atual. O campo de data do formulário de movimentações só mostra a data de hoje e não pode ser editado; não é possível registrar uma operação retroativa.

---

## Equipe e papéis

O desenvolvimento seguiu a metodologia ágil **Kanban**, com a seguinte distribuição de responsabilidades:

* **Gabriel Morais Felix**: Líder de Arquitetura, Versionamento e Integração e Desenvolvimento de Lógica de Negócio.
* **Erik & Vitor**: Desenvolvimento de Dados (Models e DAOs).
* **Hyago & Arthur**: Desenvolvimento de Lógica de Negócio (Controllers).
* **Isabela & Ana**: Desenvolvimento de Experiência do Usuário (Views CLI).
* **Pedro**: Interface de Usuário e Garantia de Qualidade (QA/Testes).

---

## Licença

Este projeto foi desenvolvido para fins estritamente acadêmicos.
