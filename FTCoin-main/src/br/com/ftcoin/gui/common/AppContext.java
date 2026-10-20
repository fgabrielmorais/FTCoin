package br.com.ftcoin.gui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import br.com.ftcoin.controllers.CarteiraController;
import br.com.ftcoin.controllers.MovimentacaoController;
import br.com.ftcoin.daos.CarteiraDAO;
import br.com.ftcoin.daos.DAOFactory;
import br.com.ftcoin.daos.DAOFactoryMariaDB;
import br.com.ftcoin.daos.DAOFactoryMemoria;
import br.com.ftcoin.daos.MovimentacaoDAO;
import br.com.ftcoin.daos.OraculoDAO;
import br.com.ftcoin.daos.TipoPersistencia;

/**
 * <p>
 * Contexto de execução e orquestrador de dependências da aplicação gráfica <b>FTCoin Studio</b>.
 * </p>
 *
 * <p>
 * Implementa o padrão <i>Singleton</i> para gerenciar o ciclo de vida dos controladores,
 * objetos de acesso a dados ({@code DAO}), serviços de internacionalização e a alternância
 * dinâmica de mecanismos de persistência (Memória versus MariaDB) em tempo de execução.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class AppContext {

    /**
     * Instância única do contexto mantida pelo padrão Singleton.
     */
    private static AppContext instanciaUnica;

    /**
     * Provedor reativo de mensagens internacionalizadas para a interface gráfica.
     */
    private final MensagensGui mensagens;

    /**
     * Mecanismo de persistência atualmente em operação na aplicação.
     */
    private TipoPersistencia tipoPersistenciaAtual;

    /**
     * Fábrica abstrata responsável por instanciar a família de DAOs ativa.
     */
    private DAOFactory daoFactoryAtual;

    /**
     * Objeto de acesso a dados para operações em carteiras.
     */
    private CarteiraDAO carteiraDAO;

    /**
     * Objeto de acesso a dados para operações em movimentações financeiras.
     */
    private MovimentacaoDAO movimentacaoDAO;

    /**
     * Objeto de acesso a dados para cotações fornecidas pelo oráculo.
     */
    private OraculoDAO oraculoDAO;

    /**
     * Controlador responsável pelos casos de uso do domínio de carteiras.
     */
    private CarteiraController carteiraController;

    /**
     * Controlador responsável pelos casos de uso de movimentações e cálculo de posição.
     */
    private MovimentacaoController movimentacaoController;

    /**
     * Lista de ouvintes notificados quando ocorre alternância na camada de persistência.
     */
    private final List<Runnable> ouvintesMudancaPersistencia = new ArrayList<>();

    /**
     * Construtor privado para inicialização dos subsistemas do contexto.
     */
    private AppContext() {
        this.mensagens = new MensagensGui(Locale.getDefault());
        this.tipoPersistenciaAtual = TipoPersistencia.MEMORIA;
        inicializarCamadaPersistencia(this.tipoPersistenciaAtual);
    }

    /**
     * Retorna a instância única do contexto da aplicação gráfica.
     *
     * @return A instância {@code AppContext} compartilhada
     */
    public static synchronized AppContext getInstancia() {
        if (instanciaUnica == null) {
            instanciaUnica = new AppContext();
        }
        return instanciaUnica;
    }

    /**
     * Inicializa a fábrica abstrata de persistência, os DAOs e os controladores correspondentes.
     *
     * @param tipo Tipo de persistência a ser inicializado
     */
    private void inicializarCamadaPersistencia(TipoPersistencia tipo) {
        this.daoFactoryAtual = switch (tipo) {
            case MEMORIA -> new DAOFactoryMemoria();
            case MARIADB -> new DAOFactoryMariaDB();
        };

        this.carteiraDAO = daoFactoryAtual.criarCarteiraDAO();
        this.movimentacaoDAO = daoFactoryAtual.criarMovimentacaoDAO();
        this.oraculoDAO = daoFactoryAtual.criarOraculoDAO();

        this.carteiraController = new CarteiraController(this.carteiraDAO, this.movimentacaoDAO);
        this.movimentacaoController = new MovimentacaoController(this.movimentacaoDAO, this.carteiraDAO, this.oraculoDAO);
        this.tipoPersistenciaAtual = tipo;
    }

    /**
     * Alterna o mecanismo de persistência dinamicamente entre os modos suportados.
     *
     * @param novoTipo Novo tipo de persistência desejado
     * @throws Exception Em caso de falha de conectividade ou indisponibilidade do banco
     */
    public synchronized void alternarPersistencia(TipoPersistencia novoTipo) throws Exception {
        if (novoTipo == null || novoTipo == this.tipoPersistenciaAtual) {
            return;
        }

        inicializarCamadaPersistencia(novoTipo);

        // Valida conectividade imediata com o banco relacional
        if (novoTipo == TipoPersistencia.MARIADB) {
            carteiraDAO.listarTodas();
        }

        notificarOuvintesPersistencia();
    }

    /**
     * Registra um ouvinte para receber notificações após a troca de persistência.
     *
     * @param ouvinte Ação executável a ser disparada
     */
    public synchronized void adicionarOuvintePersistencia(Runnable ouvinte) {
        if (ouvinte != null && !ouvintesMudancaPersistencia.contains(ouvinte)) {
            ouvintesMudancaPersistencia.add(ouvinte);
        }
    }

    /**
     * Dispara as notificações a todos os ouvintes de persistência registrados.
     */
    private void notificarOuvintesPersistencia() {
        for (Runnable ouvinte : new ArrayList<>(ouvintesMudancaPersistencia)) {
            try {
                ouvinte.run();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Retorna o provedor central de mensagens internacionalizadas da interface gráfica.
     *
     * @return O objeto {@code MensagensGui}
     */
    public MensagensGui getMensagens() {
        return mensagens;
    }

    /**
     * Retorna o tipo de persistência atualmente ativo no contexto.
     *
     * @return O {@code TipoPersistencia} ativo
     */
    public synchronized TipoPersistencia getTipoPersistenciaAtual() {
        return tipoPersistenciaAtual;
    }

    /**
     * Retorna o controlador de carteiras configurado no contexto.
     *
     * @return O controlador {@code CarteiraController}
     */
    public synchronized CarteiraController getCarteiraController() {
        return carteiraController;
    }

    /**
     * Retorna o controlador de movimentações configurado no contexto.
     *
     * @return O controlador {@code MovimentacaoController}
     */
    public synchronized MovimentacaoController getMovimentacaoController() {
        return movimentacaoController;
    }

    /**
     * Retorna o objeto de acesso a dados de carteiras.
     *
     * @return A instância ativa de {@code CarteiraDAO}
     */
    public synchronized CarteiraDAO getCarteiraDAO() {
        return carteiraDAO;
    }

    /**
     * Retorna o objeto de acesso a dados de movimentações.
     *
     * @return A instância ativa de {@code MovimentacaoDAO}
     */
    public synchronized MovimentacaoDAO getMovimentacaoDAO() {
        return movimentacaoDAO;
    }

    /**
     * Retorna o objeto de acesso a dados do oráculo de cotações.
     *
     * @return A instância ativa de {@code OraculoDAO}
     */
    public synchronized OraculoDAO getOraculoDAO() {
        return oraculoDAO;
    }
}
