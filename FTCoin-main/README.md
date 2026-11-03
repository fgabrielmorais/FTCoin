# FTCoin Studio — Interface gráfica

O FTCoin Studio é a interface gráfica Swing do sistema acadêmico de gerenciamento de carteiras da moeda virtual FTCoin, desenvolvido na UNICAMP (Campus Limeira).

## Funcionalidades

- Dashboard com resumo das carteiras e operações.
- Cadastro, edição e exclusão de carteiras. Carteiras com movimentações registradas não podem ser excluídas.
- Registro de compras e vendas, com validação de quantidade positiva e saldo disponível.
- Consulta de cotação por data e cache de consultas.
- Cinco relatórios: posição da carteira, histórico de operações, consolidado do portfólio, desempenho e histórico de cotações.
- Português, inglês e espanhol, com troca de idioma durante a execução.
- Manual de ajuda e créditos nos três idiomas.

A troca de idioma atualiza os textos e a apresentação de números e datas, preservando campos preenchidos, seleções e aba atual. Os valores monetários continuam em reais (BRL); mudar o idioma não converte a moeda.

## Projeto correto

Execute os comandos deste documento dentro da pasta `FTCoin-main/`, que contém o `pom.xml` da GUI.

A árvore `src/` da raiz do repositório pertence a outra cópia do projeto, voltada ao terminal. Compilar essa árvore não compila a GUI.

A classe principal da interface gráfica é:

```text
br.com.ftcoin.gui.main.FTCoinGuiApp
```

## Pré-requisitos

- JDK 21, com `java` e `javac` disponíveis.
- Maven disponível como `mvn`.
- MariaDB apenas para utilizar o modo de armazenamento correspondente.

O projeto Maven está configurado com `release` 21.

## Compilar e executar

Dentro de `FTCoin-main/`, compile e prepare as dependências:

```bash
mvn compile dependency:copy-dependencies -DincludeScope=runtime
```

Execute no Windows:

```powershell
java -cp "target\classes;target\dependency\*" br.com.ftcoin.gui.main.FTCoinGuiApp
```

Execute no Linux ou macOS:

```bash
java -cp "target/classes:target/dependency/*" br.com.ftcoin.gui.main.FTCoinGuiApp
```

A compilação exige acesso às dependências Maven quando elas ainda não estiverem disponíveis localmente.

### Inicializador do Windows

Na raiz do repositório, execute:

```powershell
.\iniciar_interface.bat
```

O script compila o projeto da pasta `FTCoin-main/`, prepara as dependências e inicia a GUI a partir de `target/classes`.

Ele utiliza JAVA_HOME e MAVEN_HOME quando essas variáveis estão definidas; caso contrário, procura Java e Maven no PATH.

Os binários antigos de `FTCoin-main/bin` não são utilizados por esse inicializador.

## Idiomas

O idioma inicial acompanha o idioma padrão da JVM:

- Português: português do Brasil.
- Inglês: inglês dos Estados Unidos.
- Espanhol: espanhol da Espanha.
- Outros idiomas: alternativa em português.

O seletor **Idioma**, na janela principal, permite mudar o idioma sem reiniciar.

Para iniciar em inglês no Windows:

```powershell
java "-Duser.language=en" "-Duser.country=US" -cp "target\classes;target\dependency\*" br.com.ftcoin.gui.main.FTCoinGuiApp
```

Para espanhol, utilize `-Duser.language=es -Duser.country=ES`.

As janelas de ajuda, créditos e formulários modais abrem no idioma ativo. Enquanto uma janela modal está aberta, ela bloqueia o acesso ao seletor da janela principal.

## Quantidades, datas e valores

- Quantidades aceitam ponto ou vírgula como separador decimal, sem separador de milhares. Por exemplo, `2.5` e `2,5` representam a mesma quantidade.
- Os campos de entrada de data usam `AAAA-MM-DD`, independentemente do idioma.
- Datas de apresentação e valores numéricos acompanham o idioma selecionado.
- Valores monetários são expressos em reais (BRL).

### Limitação atual das datas de movimentação

A GUI rejeita datas futuras. Entretanto, o controlador registra compras e vendas na data atual e utiliza a cotação do dia.

Informar uma data passada no formulário pode alterar a estimativa apresentada, mas não cria uma movimentação retroativa.

## Armazenamento e cotações

A GUI inicia no modo memória e pode ser utilizada sem MariaDB. A inicialização inclui carteiras e operações de demonstração.

No modo memória, os dados são perdidos ao encerrar o processo. A cotação é simulada e determinada pela data; não representa preços obtidos de uma API financeira real.

A consulta de cotação utiliza cache temporário. A latência apresentada pode incluir atraso simulado.

O seletor de armazenamento também oferece MariaDB, que exige servidor, estrutura de tabelas e configuração de conexão adequados. A troca de modo não migra os dados entre memória e banco.

Em caso de falha de conexão, confira a disponibilidade do servidor e a configuração de acesso com a equipe responsável pelo banco. Não versione credenciais.

## Resultados financeiros

A posição utiliza custo médio ponderado de aquisição:

- Compra aumenta o saldo e o custo de aquisição e recalcula o custo médio.
- Venda apura resultado realizado pela diferença entre a cotação de venda e o custo médio, multiplicada pela quantidade vendida.
- Resultado não realizado corresponde ao valor de mercado da posição restante menos seu valor a custo.
- Resultado total soma realizado e não realizado.

O manual disponível no botão **Ajuda** explica as telas e os indicadores.

## Encerramento

Feche a janela principal pelo X para encerrar a aplicação. Fechar apenas Ajuda ou Créditos encerra somente aquele diálogo.

## Versão de terminal

Este projeto também contém classes da interface de terminal, cujo ponto de entrada é `br.com.ftcoin.main.Main`.

O terminal possui mecanismo próprio de tradução. O suporte a três idiomas e à troca durante a execução descrito neste documento refere-se à GUI; não deve ser presumido para o terminal.

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
