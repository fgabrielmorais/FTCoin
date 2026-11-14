# FTCoin: roteiro de ensaio e gravação do vídeo

Segue a ordem das seções 3.1 a 3.15 das orientações do vídeo. Para cada passo: o comando ou o clique e o resultado
esperado. Os valores foram conferidos em um ensaio automático (os mesmos serviços usados pela tela, em memória) com o
oráculo simulado e com o servidor real.

Preparação, antes de gravar:

- JDK 21 e Maven no `PATH` (ou `JAVA_HOME` e `MAVEN_HOME`). Terminal na raiz do projeto.
- Para usar o servidor real, `src/oracle.properties` com `oracle.provider=api`, `oracle.group` e `oracle.password`. Sem
  esse arquivo, o programa usa o oráculo simulado. Tela cheia e fonte grande no terminal.
- Dependências preparadas: `mvn -B dependency:copy-dependencies -DincludeScope=runtime`.

---

## 3.1. Javadoc

1. Gerar o Javadoc (PowerShell, na raiz; o `javadoc` do JDK precisa estar no `PATH`):

   ```powershell
   (Get-ChildItem src -Recurse -Filter *.java).FullName | ForEach-Object { $_.Replace('\','/') } | Set-Content fontes.txt -Encoding ascii
   javadoc -Xdoclint:all -encoding UTF-8 -docencoding UTF-8 -charset UTF-8 --module-path target/dependency -d target/site/apidocs @fontes.txt
   ```

   Resultado esperado: sem nenhum aviso. Apague o `fontes.txt` depois (ele não deve ir para o ZIP).
2. Abrir `target/site/apidocs/index.html`. Mostrar a página inicial e a lista de todas as classes (72 arquivos de código:
   71 tipos mais o `module-info`), percorrendo os pacotes `models`, `services`, `daos`, `network`, `config` e `gui`.
3. Abrir os detalhes de duas ou três classes (sugestão: `Posicao`, `OraculoDAOCache`, `MovimentacaoService`) e de uma
   interface (`ICarteiraView`, em `gui.views.contracts`).

## 3.2. Código-fonte: Singleton e Factory

1. Abrir `src/br/com/ftcoin/daos/ConexaoMariaDB.java`: construtor privado, instância única e `getInstancia()`
   sincronizado. Explicar que os DAOs de MariaDB compartilham essa conexão.
2. Abrir `DAOFactory.java` (Abstract Factory) e as fábricas concretas `DAOFactoryMemoria` e `DAOFactoryMariaDB`.
   Explicar que o sistema recebe os DAOs sem saber qual mecanismo está em uso.

## 3.3. Testes (1): código e cobertura

1. Mostrar a pasta `test/` e os pacotes de teste.
2. Abrir alguns testes como exemplo: `PlanilhaApuracaoTest` (compara o custo médio com a planilha oficial),
   `CenarioRoteiroTest` (executa o roteiro 3.11 em código) e `OraculoDAOCacheTest` (descarte LRU).
3. Gerar e abrir a cobertura: `mvn -B clean test`, depois `target/site/jacoco/index.html`. Cobertura de linhas por
   pacote: `models` 92,8%, `services` 89,0%, `daos` 51,8%, `network` 100%. Explicar que `daos` fica abaixo porque as classes
   de MariaDB exigem um banco, e que `gui.views` (telas Swing) não tem teste automatizado.

## 3.4. Cache de cotações

1. Abrir `src/br/com/ftcoin/daos/OraculoDAOCache.java`.
2. Comentar: estrutura de dados (tabela hash encadeada, `LinkedHashMap` em ordem de acesso, sincronizada); limite de 512
   datas com descarte da menos recentemente usada (LRU); busca O(1) pela data. Falhas e "hoje sem cotação" nunca ficam
   no cache; datas passadas sem cotação ficam.
3. Descrever a consulta por API: `OraculoDAOApi` abre um socket, envia um `NetworkRequest` (grupo, senha, faixa de
   datas) e recebe um `NetworkResponse` com as cotações, ou a mensagem de erro do servidor.

## 3.5. Compilação

```powershell
mvn -B clean compile
```

Resultado esperado: `BUILD SUCCESS`, sem nenhuma linha `WARNING` do compilador (`-Xlint:all -Werror` está ligado). Destacar
essa ausência de avisos.

## 3.6. Testes (2): execução

```powershell
mvn -B clean test
```

Resultado esperado: `Tests run: 122, Failures: 0, Errors: 0` e `BUILD SUCCESS`. Mostrar o resumo por classe e a geração do
relatório JaCoCo. Os logs `INFO` e `WARNING` durante os testes (por exemplo, "Exclusão ignorada: carteira 99") são
esperados.

## 3.7. Internacionalização

Repetir o bloco abaixo para cada idioma, fechando o programa entre eles.

Iniciar (no PowerShell, depois de `mvn -B compile dependency:copy-dependencies -DincludeScope=runtime`):

```powershell
java -cp "target\classes;target\dependency\*" br.com.ftcoin.gui.main.FTCoinGuiApp --lang=pt
java -cp "target\classes;target\dependency\*" br.com.ftcoin.gui.main.FTCoinGuiApp --lang=en
java -cp "target\classes;target\dependency\*" br.com.ftcoin.gui.main.FTCoinGuiApp --lang=es
```

(Ou `.\iniciar_interface.bat --lang=es`.) Teste de argumento inválido: `--lang=xx` imprime a linha de uso e encerra com
código 2, sem abrir janela.

| Passo | pt | en | es |
|---|---|---|---|
| Menus (barra superior) | Visão geral, Carteiras, Movimentações, Cotação, Relatórios, Ajuda, Sobre, Sair | Overview, Wallets, Transactions, Price, Reports, Help, About, Exit | Resumen, Carteras, Movimientos, Cotización, Informes, Ayuda, Acerca de, Salir |
| Seletores | Idioma, Banco | Language, Database | Idioma, Almacenamiento |
| Abas de movimentação | Compra, Venda | Buy, Sell | Compra, Venta |
| Abas de relatório | Posição, Histórico, Consolidado, Desempenho, Oráculo, Por identificador, Por titular | Position, History, Portfolio, Performance, Oracle, By ID, By holder | Posición, Historial, Consolidado, Rendimiento, Oráculo, Por identificador, Por titular |

1. Percorrer todos os botões e abas da tabela acima, conferindo que não sobra nenhum texto em outro idioma.
2. Ajuda e créditos: botões **Ajuda** e **Sobre** (Help/About, Ayuda/Acerca de).
3. Operação inválida no cadastro de carteiras: na tela **Carteiras**, digitar no campo de ID um número que não existe
   (por exemplo, `99`) e clicar em **Excluir**. Mensagem esperada:
   - pt: "Nenhuma carteira encontrada com o ID 99."
   - en: "No wallet found with ID 99."
   - es: "No se ha encontrado ninguna cartera con el ID 99."

   Alternativa: **Nova carteira** com titular de 1 ou 2 letras ("O titular deve ter entre 3 e 100 caracteres." /
   "Holder name must have between 3 and 100 characters." / "El nombre del titular debe tener entre 3 y 100 caracteres.").
4. Data inválida: na tela **Cotação**, digitar `abc` no lugar da data e clicar em Consultar (Query / Consultar):
   - pt: "Data inválida. Use o formato DD/MM/AAAA."
   - en: "Invalid date. Please use DD/MM/YYYY format."
   - es: "Fecha no válida. Utilice el formato DD/MM/AAAA."
5. Fechar com **Sair** (Exit / Salir), confirmando no diálogo.

## 3.8. Seleção de banco de dados

1. Mostrar o seletor **Banco** (Database / Almacenamiento) na barra superior, com "Memória Volátil (Simulação)" e
   "MariaDB (Produção / SQL)". Mostrar também a opção de linha de comando `--db=memory|mariadb` e o trecho de
   `FTCoinGuiApp` que a usa.
2. Trocar para MariaDB: se o banco estiver configurado (`src/database.properties` e `docs/script.sql`), a barra de status
   mostra "MARIADB (Conectado)"; sem banco, aparece o diálogo "Falha na Conexão com MariaDB" e o programa continua em
   memória. Depois, voltar para memória (a troca não copia os dados) e seguir com a demonstração em memória.

## 3.9. Carteiras

Iniciar sem `--demo`: `java -cp "target\classes;target\dependency\*" br.com.ftcoin.gui.main.FTCoinGuiApp`.

1. Abrir **Relatórios**, aba **Por identificador**: nenhuma carteira cadastrada (tabela vazia). Na tela **Carteiras**, a
   lista também está vazia.
2. Cadastrar duas carteiras em **Nova carteira** (os IDs saem 1 e 2). Pausar o vídeo e cadastrar mais oito (IDs 3 a 10).
3. Retomar. Relatórios, **Por identificador**: IDs 1 a 10 em ordem. **Por titular**: ordem alfabética do titular.
4. Voltar a **Carteiras** e consultar duas carteiras: digitar o ID no campo e clicar em **Consultar** (abre uma janela com
   ID, titular, corretora e saldo). Editar duas (digitar o ID, por exemplo 2 e 7, e clicar em **Editar carteira**).
   Relatório **Por identificador**: os nomes novos aparecem.
5. Excluir as carteiras 5 e 10: digitar o ID e clicar em **Excluir**, confirmando. Mensagem: "Carteira 5 excluída com
   sucesso!".
6. Efeito das exclusões: digitar 5 (ou 10) no campo de ID e clicar em **Consultar** ou em **Editar carteira**. A tela mostra
   que a carteira não existe mais. Depois, os relatórios **Por identificador** e **Por titular** mostram só 8 carteiras
   (IDs 1, 2, 3, 4, 6, 7, 8, 9).

Mensagem de carteira não encontrada (conferida no ensaio automático e no teste do presenter):

- pt: "Nenhuma carteira encontrada com o ID 5."
- en: "No wallet found with ID 5."
- es: "No se ha encontrado ninguna cartera con el ID 5."

## 3.10. Cotações

Na tela **Cotação**, digitar a data e clicar em Consultar. Resultados esperados (as mensagens saem nos três idiomas):

| Data | Servidor real | Simulado | Mensagem |
|---|---|---|---|
| 31/12/2025 | 235,31 | 5,69 | (valor; ver divergência abaixo) |
| 01/01/2026 | 300,22 | 5,05 | (valor) |
| 31/10/2026 | 300,79 | 5,05 | (valor) |
| 01/06/2000 | sem cotação | sem cotação | pt: "Não há cotação cadastrada no Oráculo para a data 01/06/2000." en: "The Oracle has no price registered for 01/06/2000." es: "No hay una cotización registrada en el Oráculo para la fecha 01/06/2000." |
| 15/11/2050 | data futura | data futura | pt: "Não existe cotação para data futura: 15/11/2050." en: "There is no price for a future date: 15/11/2050." es: "No existe cotización para una fecha futura: 15/11/2050." |

Atenção ao ano nas duas últimas datas: as mensagens mostram 2000 e 2050 em formato `DD/MM/AAAA`. Uma segunda consulta da
mesma data passada mostra a origem "Cache".

## 3.11. Movimentação (1)

Usar a carteira de ID 1 em **Movimentações**. Digitar a data em `DD/MM/AAAA` e a quantidade segundo o idioma (em
português e espanhol `1.000` é mil e `2.600` é duas mil e seiscentas; em inglês, `1,000` e `2,600`). Os saldos esperados
são os mesmos com qualquer oráculo. O ganho realizado acumulado depende do oráculo e está em reais.

| # | Data | Operação | Qtd | Resultado | Saldo após | Ganho realizado (servidor) | Ganho realizado (simulado) |
|---|---|---|---|---|---|---|---|
| 1 | 01/01/2026 | Venda | 500 | **recusada**, carteira vazia | 0 | n/a | n/a |
| 2 | 01/02/2026 | Compra | 1.000 | aceita | 1.000 | 0,00 | 0,00 |
| 3 | 01/02/2026 | Venda | 500 | aceita (mesmo dia) | 500 | 0,00 | 0,00 |
| 4 | 02/02/2026 | Venda | 500 | aceita | 0 | 39.275,00 | 185,00 |
| 5 | 25/02/2026 | Compra | 1.000 | aceita | 1.000 | 39.275,00 | 185,00 |
| 6 | 02/03/2026 | Compra | 2.000 | aceita | 3.000 | 39.275,00 | 185,00 |
| 7 | 10/03/2026 | Venda | 400 | aceita | 2.600 | 73.133,67 | 538,33 |
| 8 | 01/04/2026 | Venda | 2.600 | aceita (saldo total) | 0 | 240.877,00 | 365,00 |

Mensagem da venda recusada (item 1):

- pt: "Operação recusada: em 01/01/2026 a carteira teria 0 FTC e a venda é de 500 FTC."
- en: "Transaction refused: on 01/01/2026 the wallet would hold 0 FTC and the sale is of 500 FTC."
- es: "Operación rechazada: el 01/01/2026 la cartera tendría 0 FTC y la venta es de 500 FTC."

Relatórios: depois do item 4, mostrar **Posição** (saldo 0), **Histórico** (3 linhas de operação, sem contar a recusada) e o
ganho/perda (cartões de ganho realizado, não realizado e total). Depois do item 7, mostrar de novo o ganho/perda (saldo
2.600). Cada compra mostra a cotação aplicada da própria data.

Pausar o vídeo e registrar as 18 operações de setembro de 2026. Saldo após cada uma (o mesmo em qualquer oráculo; o saldo
nunca fica negativo, o mínimo é 40 e o final é 540):

| Nº | Data | Compra | Venda | Saldo após | Ganho realizado (servidor) | Ganho realizado (simulado) |
|---|---|---|---|---|---|---|
| 1 | 01/09/2026 | 1.000 | | 1.000 | 240.877,00 | 365,00 |
| 2 | 04/09/2026 | 120 | | 1.120 | 240.877,00 | 365,00 |
| 3 | 05/09/2026 | | 500 | 620 | 244.409,32 | 594,64 |
| 4 | 06/09/2026 | 500 | | 1.120 | 244.409,32 | 594,64 |
| 5 | 08/09/2026 | | 80 | 1.040 | 245.527,20 | 609,77 |
| 6 | 09/09/2026 | | 500 | 540 | 261.168,93 | 384,30 |
| 7 | 10/09/2026 | | 500 | **40** | 262.115,66 | 343,84 |
| 8 | 11/09/2026 | 1.000 | | 1.040 | 262.115,66 | 343,84 |
| 9 | 13/09/2026 | | 500 | 540 | 291.515,92 | 214,40 |
| 10 | 15/09/2026 | | 500 | **40** | 300.961,18 | -50,04 |
| 11 | 16/09/2026 | 1.000 | | 1.040 | 300.961,18 | -50,04 |
| 12 | 18/09/2026 | | 500 | 540 | 285.645,42 | -188,10 |
| 13 | 20/09/2026 | | 500 | **40** | 277.339,66 | -461,16 |
| 14 | 21/09/2026 | 1.000 | | 1.040 | 277.339,66 | -461,16 |
| 15 | 23/09/2026 | | 500 | 540 | 266.620,40 | -599,54 |
| 16 | 25/09/2026 | | 500 | **40** | 275.536,14 | -367,93 |
| 17 | 26/09/2026 | 1.000 | | 1.040 | 275.536,14 | -367,93 |
| 18 | 28/09/2026 | | 500 | **540** | 300.963,67 | -1,33 |

Posição final: saldo 540. Com o servidor real, ganho realizado 300.963,67; não realizado 15.900,33 (cotação de hoje
274,59, em 13/11/2026) e total 316.864,00. Com o simulado, realizado -1,33; não realizado 314,93 e total 313,60. A cotação
de "hoje" muda a cada dia, então o não realizado e o total variam com a data da gravação; o realizado e o saldo não.

## 3.12. Movimentação (2)

1. Retomar o vídeo.
2. **Relatórios**, aba **Histórico**, carteira 1: 25 movimentações (7 de fevereiro a abril, 18 de setembro), em ordem
   cronológica. Esperar o relatório carregar por inteiro e rolar a tabela para mostrá-lo na íntegra.

## 3.13. Relatórios

Nada a fazer: já foram mostrados nas etapas anteriores. Se sobrar tempo, mostrar **Imprimir relatório**.

## 3.14. Ajuda e créditos

Abrir **Ajuda** e **Sobre**, deixando cada texto na tela o tempo de uma leitura rápida.

## 3.15. Saída

Clicar em **Sair** e confirmar. O programa fecha as conexões e encerra.

---

## Divergências achadas no ensaio

1. **31/12/2025 tem cotação.** O servidor real tem cotações desde 01/01/2009. A data 31/12/2025 devolve valor (235,31), e
   a tela não mostra mensagem de erro. Se o roteiro esperava "sem cotação" para 31/12/2025, essa expectativa não se
   cumpre com o servidor real. O oráculo simulado foi alinhado ao servidor (sem cotação antes de 01/01/2009).
2. **Carteira inexistente (corrigida).** A tela só trabalhava com a linha selecionada, então não dava para consultar, editar ou
   excluir uma carteira que não existe. Foi acrescentado o campo de ID na tela **Carteiras**, com os botões **Consultar**,
   **Editar carteira** e **Excluir**, e agora a mensagem de carteira não encontrada aparece pela tela.
3. **Cotação de hoje.** O não realizado e o total das tabelas acima dependem do dia da gravação.
