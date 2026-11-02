package br.com.ftcoin.gui.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.ftcoin.controllers.CarteiraController;
import br.com.ftcoin.controllers.MovimentacaoController;
import br.com.ftcoin.gui.common.AppContext;
import br.com.ftcoin.gui.dto.CarteiraDTO;
import br.com.ftcoin.gui.dto.MovimentacaoDTO;
import br.com.ftcoin.gui.dto.PosicaoRelatorioDTO;
import br.com.ftcoin.gui.dto.RelatorioConsolidadoItemDTO;
import br.com.ftcoin.gui.dto.RelatorioDesempenhoDTO;
import br.com.ftcoin.gui.dto.RelatorioHistoricoCotacaoDTO;
import br.com.ftcoin.gui.dto.ResumoDashboardDTO;
import br.com.ftcoin.models.Carteira;
import br.com.ftcoin.models.Movimentacao;
import br.com.ftcoin.models.Posicao;

/**
 * <p>
 * Serviço responsável por compilar, apurar e estruturar os dados analíticos da interface gráfica.
 * </p>
 *
 * <p>
 * Atua como ponte entre os controladores de domínio e os cinco relatórios especializados da {@code GUI},
 * bem como fornece os dados agregados para os cartões de KPI do Dashboard principal.
 * </p>
 *
 * @author Equipe FTCoin
 * @version 1.0.0
 * @since 2026-10-20
 */
public class RelatoriosService {

    /**
     * Serviço de cotações com cache utilizado para valorização a mercado.
     */
    private final CotacaoCacheService cotacaoService;

    /**
     * Constrói o serviço de relatórios injetando a dependência de cotações.
     *
     * @param cotacaoService Instância de {@link CotacaoCacheService}; se {@code null}, instancia padrão
     */
    public RelatoriosService(CotacaoCacheService cotacaoService) {
        this.cotacaoService = (cotacaoService != null) ? cotacaoService : new CotacaoCacheService();
    }

    /**
     * Retorna a apuração consolidada de posição da carteira pelo Custo Médio Ponderado (Relatório 1).
     *
     * @param idCarteira Identificador numérico da carteira
     * @return O objeto {@link PosicaoRelatorioDTO} com as métricas financeiras
     */
    public PosicaoRelatorioDTO obterPosicaoCarteira(int idCarteira) {
        CarteiraController carteiraController = AppContext.getInstancia().getCarteiraController();
        MovimentacaoController movController = AppContext.getInstancia().getMovimentacaoController();

        Carteira carteira = carteiraController.buscarCarteira(idCarteira);
        Posicao posicao = movController.apurarPosicao(idCarteira);
        BigDecimal cotacaoAtual = movController.buscarCotacaoAtual();

        BigDecimal saldo = posicao.getSaldo();
        BigDecimal custoMedio = posicao.getCustoMedio();
        BigDecimal valorACusto = posicao.getValorACusto();
        BigDecimal valorMercado = posicao.calcularValorDeMercado(cotacaoAtual);
        BigDecimal ganhoRealizado = posicao.getGanhoRealizado();
        BigDecimal ganhoNaoRealizado = posicao.calcularGanhoNaoRealizado(cotacaoAtual);
        BigDecimal ganhoTotal = posicao.calcularGanhoTotal(cotacaoAtual);

        BigDecimal roi = BigDecimal.ZERO;
        if (valorACusto.signum() > 0) {
            roi = ganhoTotal.multiply(BigDecimal.valueOf(100)).divide(valorACusto, 2, RoundingMode.HALF_EVEN);
        }

        return new PosicaoRelatorioDTO(
                carteira.getId(),
                carteira.getNomeTitular(),
                carteira.getCorretora(),
                cotacaoAtual,
                saldo,
                custoMedio,
                valorACusto,
                valorMercado,
                ganhoRealizado,
                ganhoNaoRealizado,
                ganhoTotal,
                roi
        );
    }

    /**
     * Retorna o extrato detalhado de operações da carteira selecionada (Relatório 2).
     *
     * @param idCarteira Identificador numérico da carteira
     * @return A lista cronológica decrescente de movimentações em {@link MovimentacaoDTO}
     */
    public List<MovimentacaoDTO> obterHistoricoOperacoes(int idCarteira) {
        CarteiraController carteiraController = AppContext.getInstancia().getCarteiraController();
        MovimentacaoController movController = AppContext.getInstancia().getMovimentacaoController();

        Carteira carteira = carteiraController.buscarCarteira(idCarteira);
        List<Movimentacao> movs = movController.buscarHistorico(idCarteira);

        List<MovimentacaoDTO> resultado = new ArrayList<>();
        for (Movimentacao m : movs) {
            BigDecimal cotacao = movController.buscarCotacao(m.getDataOperacao());
            BigDecimal total = movController.calcularValorDaOperacao(m);
            resultado.add(new MovimentacaoDTO(
                    m.getIdMovimento(),
                    m.getIdCarteira(),
                    carteira.getNomeTitular(),
                    carteira.getCorretora(),
                    m.getDataOperacao(),
                    m.getTipoOperacao(),
                    m.getQuantidadeMovimentada(),
                    cotacao,
                    total
            ));
        }

        return resultado.reversed();
    }

    /**
     * Retorna a visão analítica de todas as carteiras e suas participações patrimoniais (Relatório 3).
     *
     * @return A lista consolidada de itens em {@link RelatorioConsolidadoItemDTO}
     */
    public List<RelatorioConsolidadoItemDTO> obterConsolidadoCarteiras() {
        CarteiraController carteiraController = AppContext.getInstancia().getCarteiraController();
        MovimentacaoController movController = AppContext.getInstancia().getMovimentacaoController();
        BigDecimal cotacaoAtual = movController.buscarCotacaoAtual();

        List<Carteira> carteiras = carteiraController.listarTodasCarteiras();
        if (carteiras.isEmpty()) {
            return Collections.emptyList();
        }

        BigDecimal patrimonioTotal = BigDecimal.ZERO;
        List<Posicao> posicoes = new ArrayList<>();

        for (Carteira c : carteiras) {
            Posicao p = movController.apurarPosicao(c.getId());
            posicoes.add(p);
            patrimonioTotal = patrimonioTotal.add(p.calcularValorDeMercado(cotacaoAtual));
        }

        List<RelatorioConsolidadoItemDTO> itens = new ArrayList<>();
        for (int i = 0; i < carteiras.size(); i++) {
            Carteira c = carteiras.get(i);
            Posicao p = posicoes.get(i);

            BigDecimal valorMercado = p.calcularValorDeMercado(cotacaoAtual);
            BigDecimal participacao = BigDecimal.ZERO;
            if (patrimonioTotal.signum() > 0) {
                participacao = valorMercado.multiply(BigDecimal.valueOf(100))
                        .divide(patrimonioTotal, 2, RoundingMode.HALF_EVEN);
            }

            BigDecimal ganhoTotal = p.calcularGanhoTotal(cotacaoAtual);

            itens.add(new RelatorioConsolidadoItemDTO(
                    c.getId(),
                    c.getNomeTitular(),
                    c.getCorretora(),
                    p.getSaldo(),
                    valorMercado,
                    participacao,
                    ganhoTotal
            ));
        }

        return itens;
    }

    /**
     * Retorna o ranking de rentabilidade e taxa de retorno sobre investimento por carteira (Relatório 4).
     *
     * @return A lista de registros em formato {@link RelatorioDesempenhoDTO}
     */
    public List<RelatorioDesempenhoDTO> obterDesempenhoLucratividade() {
        CarteiraController carteiraController = AppContext.getInstancia().getCarteiraController();
        MovimentacaoController movController = AppContext.getInstancia().getMovimentacaoController();
        BigDecimal cotacaoAtual = movController.buscarCotacaoAtual();

        List<Carteira> carteiras = carteiraController.listarTodasCarteiras();
        List<RelatorioDesempenhoDTO> resultado = new ArrayList<>();

        for (Carteira c : carteiras) {
            Posicao p = movController.apurarPosicao(c.getId());
            BigDecimal investido = p.getValorACusto();
            BigDecimal valorAtual = p.calcularValorDeMercado(cotacaoAtual);
            BigDecimal lucroPrejuizo = p.calcularGanhoTotal(cotacaoAtual);

            BigDecimal roi = BigDecimal.ZERO;
            if (investido.signum() > 0) {
                roi = lucroPrejuizo.multiply(BigDecimal.valueOf(100)).divide(investido, 2, RoundingMode.HALF_EVEN);
            }

            resultado.add(new RelatorioDesempenhoDTO(
                    c.getId(),
                    c.getNomeTitular(),
                    c.getCorretora(),
                    investido,
                    valorAtual,
                    lucroPrejuizo,
                    roi
            ));
        }

        return resultado;
    }

    /**
     * Retorna a evolução histórica de cotações do oráculo com as oscilações diárias (Relatório 5).
     *
     * @param dias Quantidade de dias a serem apurados
     * @return A série temporal em formato {@link RelatorioHistoricoCotacaoDTO}
     */
    public List<RelatorioHistoricoCotacaoDTO> obterHistoricoCotacoes(int dias) {
        return cotacaoService.obterHistoricoRecente(dias);
    }

    /**
     * Retorna o resumo consolidado de indicadores-chave e feed recente para o Dashboard.
     *
     * @return O objeto {@link ResumoDashboardDTO} consolidado
     */
    public ResumoDashboardDTO obterResumoDashboard() {
        CarteiraController carteiraController = AppContext.getInstancia().getCarteiraController();
        MovimentacaoController movController = AppContext.getInstancia().getMovimentacaoController();
        BigDecimal cotacaoAtual = movController.buscarCotacaoAtual();

        List<Carteira> carteiras = carteiraController.listarTodasCarteiras();
        BigDecimal patrimonioTotal = BigDecimal.ZERO;
        BigDecimal saldoTotalFTC = BigDecimal.ZERO;
        BigDecimal ganhoTotalConsolidado = BigDecimal.ZERO;
        List<MovimentacaoDTO> ultimasMovs = new ArrayList<>();

        for (Carteira c : carteiras) {
            Posicao p = movController.apurarPosicao(c.getId());
            patrimonioTotal = patrimonioTotal.add(p.calcularValorDeMercado(cotacaoAtual));
            saldoTotalFTC = saldoTotalFTC.add(p.getSaldo());
            ganhoTotalConsolidado = ganhoTotalConsolidado.add(p.calcularGanhoTotal(cotacaoAtual));

            List<Movimentacao> historico = movController.buscarHistorico(c.getId());
            for (Movimentacao m : historico) {
                BigDecimal cotacao = movController.buscarCotacao(m.getDataOperacao());
                BigDecimal total = movController.calcularValorDaOperacao(m);
                ultimasMovs.add(new MovimentacaoDTO(
                        m.getIdMovimento(),
                        c.getId(),
                        c.getNomeTitular(),
                        c.getCorretora(),
                        m.getDataOperacao(),
                        m.getTipoOperacao(),
                        m.getQuantidadeMovimentada(),
                        cotacao,
                        total
                ));
            }
        }

        ultimasMovs.sort((a, b) -> b.dataOperacao().compareTo(a.dataOperacao()));
        if (ultimasMovs.size() > 8) {
            ultimasMovs = ultimasMovs.subList(0, 8);
        }

        return new ResumoDashboardDTO(
                patrimonioTotal,
                saldoTotalFTC,
                cotacaoAtual,
                carteiras.size(),
                ganhoTotalConsolidado,
                ultimasMovs
        );
    }

    /**
     * Retorna todas as carteiras cadastradas encapsuladas em formato {@link CarteiraDTO}.
     *
     * @return A lista de carteiras cadastradas com saldo e valor de mercado atualizados
     */
    public List<CarteiraDTO> listarCarteirasDTO() {
        CarteiraController carteiraController = AppContext.getInstancia().getCarteiraController();
        MovimentacaoController movController = AppContext.getInstancia().getMovimentacaoController();
        BigDecimal cotacaoAtual = movController.buscarCotacaoAtual();

        List<Carteira> lista = carteiraController.listarTodasCarteiras();
        List<CarteiraDTO> dtos = new ArrayList<>();

        for (Carteira c : lista) {
            Posicao p = movController.apurarPosicao(c.getId());
            BigDecimal valorMercado = p.calcularValorDeMercado(cotacaoAtual);
            dtos.add(new CarteiraDTO(c.getId(), c.getNomeTitular(), c.getCorretora(), p.getSaldo(), valorMercado));
        }

        return dtos;
    }
}
