# FTCoin: listagem e descrição das classes

Projeto FTCoin, Grupo B.05. O sistema é uma interface gráfica Swing no padrão MVP, com persistência em memória ou
em MariaDB. Esta listagem descreve a versão final do código e substitui a versão antiga (com Controllers e
`ICarteiraDAO`). Cada item diz a responsabilidade da classe, sem listar atributos nem métodos.

## Módulo

- **`module-info`**: descritor do módulo `FTCoin`. Declara que o sistema usa `java.sql` (MariaDB), `java.desktop` (Swing) e
  `java.logging`.

## Pacote `br.com.ftcoin.models`

Entidades do domínio, a apuração da posição e as exceções do sistema.

- **`Carteira`**: carteira de FTCoins de um titular em uma corretora. Valida o nome do titular e o da corretora.
- **`Cotacao`**: cotação da FTCoin em um dia, em reais, como informada pelo Oráculo. Calcula a variação em relação a
  uma cotação anterior.
- **`Movimentacao`**: compra ou venda de FTCoins registrada em uma carteira, com data, tipo e quantidade. Calcula o valor
  da operação a partir de uma cotação.
- **`Posicao`**: posição de uma carteira apurada pelo custo médio ponderado de aquisição. Aplica compras e vendas e
  calcula saldo, custo médio, ganho realizado, ganho não realizado, ganho total e ROI.
- **`TipoOperacao`** (enum): compra ou venda. Cada tipo sabe aplicar o próprio efeito sobre uma `Posicao` e tem o código
  gravado no banco (`C` ou `V`).
- **`FTCoinException`**: base das exceções do sistema. Carrega a chave de uma mensagem e seus argumentos, para que a
  tela a exiba no idioma do usuário.
- **`RegraNegocioException`**: violação de uma regra do domínio, como venda sem saldo, data futura ou carteira
  inexistente.

## Pacote `br.com.ftcoin.services`

Casos de uso. Concentram as regras de negócio e não conhecem a interface gráfica.

- **`CarteiraService`**: cadastro, consulta, edição, exclusão e listagem das carteiras (por identificador e por titular).
  Recusa a exclusão de carteira com movimentações.
- **`MovimentacaoService`**: registra compras e vendas na data informada, validando a cotação e o saldo ao longo da
  linha do tempo. Também entrega o histórico, a posição apurada de cada carteira e as cotações das operações.

## Pacote `br.com.ftcoin.daos`

Persistência, oráculos de cotação e cache.

- **`CarteiraDAO`**: contrato abstrato de persistência das carteiras, com uma implementação em memória e outra em MariaDB.
- **`CarteiraDAOMemoria`**: guarda as carteiras em uma lista na memória, com identificadores sequenciais.
- **`CarteiraDAOMariaDB`**: guarda as carteiras na tabela `CARTEIRA` do MariaDB.
- **`MovimentacaoDAO`**: contrato abstrato de persistência das movimentações das carteiras.
- **`MovimentacaoDAOMemoria`**: guarda as movimentações em uma lista na memória.
- **`MovimentacaoDAOMariaDB`**: guarda as movimentações na tabela `MOVIMENTACAO` do MariaDB.
- **`DAOFactory`**: fábrica abstrata (padrão Abstract Factory) das famílias de DAOs. O resto do sistema recebe os DAOs sem
  saber se os dados estão em memória ou no banco. Também cria o oráculo de cotações, já envolvido pelo cache.
- **`DAOFactoryMemoria`**: fábrica concreta que cria os DAOs em memória.
- **`DAOFactoryMariaDB`**: fábrica concreta que cria os DAOs de MariaDB, todos sobre a mesma conexão.
- **`ConexaoMariaDB`**: conexão única com o MariaDB (padrão Singleton), com construtor privado e acesso pelo método
  sincronizado `getInstancia()`. Abre a conexão na primeira consulta e a fecha ao encerrar.
- **`TipoPersistencia`** (enum): os mecanismos de persistência disponíveis (memória e MariaDB) e o nome curto de cada um.
- **`OraculoDAO`**: contrato abstrato de consulta à cotação diária da FTCoin.
- **`OraculoDAOMemoria`**: oráculo simulado. Calcula uma cotação estável por data e repete o contrato do servidor: sem
  cotação antes de 01/01/2009 e erro para data futura.
- **`OraculoDAOApi`**: oráculo que consulta o servidor de cotações do professor por socket, usando as classes do pacote
  `network`.
- **`OraculoDAOCache`**: cache em volta de qualquer oráculo. É uma tabela hash encadeada indexada pela data, com busca O(1)
  e limite de 512 datas, e descarta a entrada menos recentemente usada (LRU).
- **`FabricaOraculo`**: escolhe o oráculo (simulado ou servidor) pela chave `oracle.provider` do arquivo
  `oracle.properties`.
- **`OraculoException`**: falha na consulta ao Oráculo (rede, resposta inválida ou consulta recusada).
- **`PersistenciaException`**: falha de configuração ou de acesso ao mecanismo de persistência.

## Pacote `network`

Protocolo de rede do servidor de cotações, fornecido pelo professor.

- **`NetworkRequest`**: pedido enviado ao servidor, com grupo, senha e faixa de datas.
- **`NetworkResponse`**: resposta do servidor, com a lista de cotações ou uma mensagem de erro.
- **`ExchangeRate`**: cotação de uma data, como trafega no protocolo.

## Pacote `br.com.ftcoin.config`

Configuração dos logs.

- **`ConfiguracaoLog`**: ativa os logs da aplicação a partir do arquivo `logging.properties` e cria a pasta `logs/`.
- **`FormatadorLog`**: define o layout de cada linha de log (data e hora, nível, thread, logger e mensagem).
- **`RegistroDeFalhas`**: regra única para registrar falhas na camada de apresentação: erros previstos viram aviso com a
  chave da mensagem, e os inesperados são registrados com o rastreamento completo.

## Pacote `br.com.ftcoin.gui.main`

Partida da aplicação.

- **`FTCoinGuiApp`**: ponto de entrada. Lê os argumentos, monta o contexto, carrega os dados de demonstração se pedidos
  e abre a janela principal.
- **`ArgumentosAplicacao`** (record): lê e valida as opções da linha de comando (`--lang`, `--db` e `--demo`), sem
  depender de Swing.

## Pacote `br.com.ftcoin.gui.common`

Estado e utilitários compartilhados pela interface.

- **`ContextoAplicacao`**: estado compartilhado da interface: mensagens no idioma ativo, persistência em uso e os
  serviços montados sobre ela. Troca entre memória e MariaDB. Não é Singleton.
- **`MensagensGui`**: fornece os textos traduzidos (português, inglês e espanhol), troca o idioma em tempo de execução e
  avisa os componentes interessados. Formata as datas das mensagens como `DD/MM/AAAA`.
- **`GuiFormatador`**: formata moedas, quantidades, datas e percentuais e lê as quantidades e datas digitadas, segundo o
  idioma ativo.
- **`TextosTraduzidos`**: liga componentes de tela às chaves de mensagem e reaplica o texto quando o idioma muda.
- **`TarefaEmSegundoPlano`**: executa um trabalho demorado em uma virtual thread e devolve o resultado na thread do Swing,
  para a tela não travar.
- **`TemaCores`**: paleta de cores, fontes e padrões visuais da interface.
- **`Info`**: nome, versão e plataforma do sistema.
- **`DatabaseSelector`** (enum): opções do seletor de banco da barra superior.
- **`IdiomaSelector`** (enum): opções do seletor de idioma da barra superior.
- **`ModoNavegacao`** (enum): as telas principais e a chave do texto do botão de cada uma.
- **`OrigemCotacao`** (enum): procedência de uma cotação exibida, Oráculo ou cache.

## Pacote `br.com.ftcoin.gui.presenters`

Presenters do padrão MVP. Tratam os eventos da tela, validam a entrada e decidem o que a view mostra.

- **`CarteiraPresenter`**: valida e executa a consulta, a inclusão, a edição e a exclusão de carteiras, por ID ou pela linha selecionada, recusando IDs que não existem.
- **`MovimentacaoPresenter`**: valida a data e a quantidade digitadas, calcula a estimativa de valor e registra compras e
  vendas.
- **`CotacaoPresenter`**: dispara a consulta de cotação por data e mostra o resultado, a origem e os erros.
- **`DashboardPresenter`**: reúne os indicadores do painel inicial e as últimas operações.
- **`RelatoriosPresenter`**: monta os relatórios e entrega os dados para a tela.

## Pacote `br.com.ftcoin.gui.services`

- **`CotacaoCacheService`**: consulta cotações em segundo plano, pelo oráculo com cache, e entrega a resposta na thread
  do Swing.
- **`ImpressaoRelatorio`**: imprime os relatórios escolhidos em um único trabalho de impressão, que também pode ser
  salvo em PDF por uma impressora virtual.

## Pacote `br.com.ftcoin.gui.views`

Telas Swing (as views do MVP). Só exibem dados e repassam eventos ao presenter.

- **`MainFrame`**: janela principal, com a navegação, os seletores de idioma e de banco, a barra de status e as telas.
- **`DashboardPanel`**: painel inicial, com indicadores, atalhos e as últimas operações.
- **`CarteirasPanel`**: lista de carteiras, campo de ID para consultar, editar ou excluir, e os diálogos de criação, edição, consulta e confirmação de exclusão.
- **`MovimentacoesPanel`**: formulários de compra e venda, com estimativa de valor, e o histórico geral.
- **`CotacaoPanel`**: consulta de cotação por data, com a origem da resposta e o histórico recente.
- **`RelatoriosPanel`**: relatórios em tabelas, com destaque de cor para lucro e prejuízo, e a impressão.
- **`AjudaDialog`**: manual do usuário, no idioma ativo.
- **`CreditosDialog`**: créditos e informações institucionais, no idioma ativo.

### Pacote `br.com.ftcoin.gui.views.contracts`

Contratos das views, que os presenters usam sem conhecer o Swing.

- **`ICarteiraView`**: contrato da tela de carteiras.
- **`IMovimentacaoView`**: contrato da tela de movimentações.
- **`ICotacaoView`**: contrato da tela de cotação.
- **`IDashboardView`**: contrato do painel inicial.
- **`IRelatoriosView`**: contrato da tela de relatórios.

### Pacote `br.com.ftcoin.gui.views.components`

Componentes visuais reutilizáveis.

- **`ModernButton`**: botão de visual minimalista, com a cor indicando o tipo da ação.
- **`ModernCard`**: cartão com cantos arredondados para os indicadores.
- **`BadgeLabel`**: etiqueta arredondada que sinaliza estados e origens, como Oráculo ou cache.
- **`CurrencyTableCellRenderer`**: renderizador de células de tabela para valores monetários, percentuais e quantidades.
- **`EstiloTabela`**: aplica o visual minimalista às tabelas.
