package br.com.ftcoin.gui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.com.ftcoin.daos.CarteiraDAO;
import br.com.ftcoin.daos.DAOFactory;
import br.com.ftcoin.daos.MovimentacaoDAO;
import br.com.ftcoin.daos.OraculoDAO;
import br.com.ftcoin.daos.PersistenciaException;
import br.com.ftcoin.daos.TipoPersistencia;
import br.com.ftcoin.services.CarteiraService;
import br.com.ftcoin.services.MovimentacaoService;

/**
 * Estado compartilhado da interface gráfica: mensagens no idioma ativo, mecanismo de persistência
 * em uso e os serviços montados sobre ele.
 * <p>
 * Não é Singleton: é criado uma vez pelo {@code FTCoinGuiApp} e entregue pelo construtor à janela,
 * aos painéis, aos presenters e aos serviços da interface. Quando o usuário troca de banco, o contexto
 * recria a fábrica de DAOs e os serviços, e avisa as telas para recarregarem os dados.
 * </p>
 *
 * @author Grupo B.05
 * @version 1.0.0
 */
public class ContextoAplicacao {

    private static final Logger LOG = Logger.getLogger(ContextoAplicacao.class.getName());

    private final MensagensGui mensagens;
    private final List<Runnable> ouvintesMudancaPersistencia = new ArrayList<>();

    private TipoPersistencia tipoPersistenciaAtual;
    private DAOFactory daoFactoryAtual;
    private CarteiraDAO carteiraDAO;
    private MovimentacaoDAO movimentacaoDAO;
    private OraculoDAO oraculoDAO;
    private CarteiraService carteiraService;
    private MovimentacaoService movimentacaoService;

    /**
     * Cria o contexto e monta a persistência inicial.
     *
     * @param mensagens textos da interface no idioma ativo
     * @param tipoInicial mecanismo de persistência usado ao abrir a aplicação
     * @throws PersistenciaException se a persistência inicial não puder ser criada
     */
    public ContextoAplicacao(MensagensGui mensagens, TipoPersistencia tipoInicial) {
        this.mensagens = mensagens;
        inicializarCamadaPersistencia(tipoInicial);
    }

    // Recria fábrica, DAOs e serviços; só substitui o estado atual se a nova fábrica for criada
    private void inicializarCamadaPersistencia(TipoPersistencia tipo) {
        DAOFactory fabricaAnterior = this.daoFactoryAtual;
        this.daoFactoryAtual = DAOFactory.criar(tipo);
        if (fabricaAnterior != null) {
            // Libera a conexão do mecanismo que deixou de ser usado
            fabricaAnterior.fechar();
        }

        this.carteiraDAO = daoFactoryAtual.criarCarteiraDAO();
        this.movimentacaoDAO = daoFactoryAtual.criarMovimentacaoDAO();
        this.oraculoDAO = daoFactoryAtual.criarOraculoDAO();

        this.carteiraService = new CarteiraService(this.carteiraDAO, this.movimentacaoDAO);
        this.movimentacaoService = new MovimentacaoService(this.movimentacaoDAO, this.carteiraDAO, this.oraculoDAO);
        this.tipoPersistenciaAtual = tipo;
        LOG.log(Level.INFO, "Persistência ativa: {0}", tipo.getValorConfiguracao());
    }

    /**
     * Troca o mecanismo de persistência e avisa as telas registradas.
     *
     * @param novoTipo mecanismo desejado; {@code null} ou o mecanismo atual não fazem nada
     * @throws PersistenciaException se o MariaDB não estiver configurado ou não responder
     */
    public synchronized void alternarPersistencia(TipoPersistencia novoTipo) {
        if (novoTipo == null || novoTipo == this.tipoPersistenciaAtual) {
            return;
        }

        LOG.log(Level.FINE, "Alternando persistência de {0} para {1}", new Object[] {
                this.tipoPersistenciaAtual.getValorConfiguracao(), novoTipo.getValorConfiguracao() });
        inicializarCamadaPersistencia(novoTipo);

        // Valida a conexão logo na troca, em vez de deixar o erro para a primeira tela
        if (novoTipo == TipoPersistencia.MARIADB) {
            try {
                carteiraDAO.listarTodas();
            } catch (RuntimeException e) {
                // O stack trace do banco é registrado pelo DAO; aqui fica só o efeito na aplicação
                LOG.log(Level.WARNING, "MariaDB indisponível ao trocar a persistência: {0}", e.getMessage());
                throw e;
            }
        }

        notificarOuvintesPersistencia();
    }

    /**
     * Libera os recursos da persistência ativa, como a conexão com o MariaDB. Chamado ao fechar a aplicação.
     */
    public synchronized void encerrar() {
        if (daoFactoryAtual != null) {
            daoFactoryAtual.fechar();
        }
    }

    /**
     * Registra uma ação a executar depois de cada troca de persistência.
     *
     * @param ouvinte ação a executar, normalmente o recarregamento de uma tela
     */
    public synchronized void adicionarOuvintePersistencia(Runnable ouvinte) {
        if (ouvinte != null && !ouvintesMudancaPersistencia.contains(ouvinte)) {
            ouvintesMudancaPersistencia.add(ouvinte);
        }
    }

    private void notificarOuvintesPersistencia() {
        for (Runnable ouvinte : new ArrayList<>(ouvintesMudancaPersistencia)) {
            try {
                ouvinte.run();
            } catch (Exception e) {
                // Uma tela que falha ao recarregar não impede as demais
                LOG.log(Level.SEVERE, "Falha ao avisar uma tela sobre a troca de persistência", e);
            }
        }
    }

    /**
     * Devolve os textos da interface no idioma ativo.
     *
     * @return o provedor de mensagens
     */
    public MensagensGui getMensagens() {
        return mensagens;
    }

    /**
     * Devolve o mecanismo de persistência em uso.
     *
     * @return o tipo de persistência ativo
     */
    public synchronized TipoPersistencia getTipoPersistenciaAtual() {
        return tipoPersistenciaAtual;
    }

    /**
     * Devolve o serviço de carteiras da persistência ativa.
     *
     * @return o serviço de carteiras
     */
    public synchronized CarteiraService getCarteiraService() {
        return carteiraService;
    }

    /**
     * Devolve o serviço de movimentações da persistência ativa.
     *
     * @return o serviço de movimentações
     */
    public synchronized MovimentacaoService getMovimentacaoService() {
        return movimentacaoService;
    }

    /**
     * Devolve o DAO de carteiras da persistência ativa.
     *
     * @return o DAO de carteiras
     */
    public synchronized CarteiraDAO getCarteiraDAO() {
        return carteiraDAO;
    }

    /**
     * Devolve o DAO de movimentações da persistência ativa.
     *
     * @return o DAO de movimentações
     */
    public synchronized MovimentacaoDAO getMovimentacaoDAO() {
        return movimentacaoDAO;
    }

    /**
     * Devolve o DAO de cotações da persistência ativa.
     *
     * @return o DAO de cotações
     */
    public synchronized OraculoDAO getOraculoDAO() {
        return oraculoDAO;
    }
}
