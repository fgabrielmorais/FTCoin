/*
 * ************************************************************************************************************************
 * Prof. Dr. André F. de Angelis
 * Unicamp - Universidade Estadual de Campinas
 * FT - Faculdade de Tecnologia
 * ************************************************************************************************************************
 * Escrito em Java 20, usando IDE Eclipse 2023-06, em ambiente Windows 11
 *
 * Versão 1.0
 * 07.Outubro.2026
 *
 * ************************************************************************************************************************
 *
 * Esta classe implementa o protocolo de resposta do servidor de rede para o Projeto FT Coin ver. 2026.
 *
 * ************************************************************************************************************************
 */
package network;

import java.io.Serializable;
import java.util.List;

/**
 * Resposta do servidor: a lista de cotações ou uma mensagem de erro.
 */
public final class NetworkResponse implements Serializable
   {
   private static final long serialVersionUID = 1L;

   /**
    * Cotações devolvidas, uma por dia da faixa pedida.
    */
   @SuppressWarnings("serial") // List do protocolo do professor; as implementações usadas são serializáveis
   public List<ExchangeRate> exchangeRates;
   /**
    * Mensagem de erro do servidor; {@code null} quando a consulta deu certo.
    */
   public String             errorMessage;

   /**
    * Cria a resposta.
    *
    * @param exchangeRates cotações devolvidas
    * @param errorMessage mensagem de erro, ou {@code null}
    */
   public NetworkResponse(List<ExchangeRate> exchangeRates, String errorMessage)
      {
      super();
      this.exchangeRates = exchangeRates;
      this.errorMessage  = errorMessage;
      }

   /**
    * Devolve a mensagem de erro do servidor.
    *
    * @return a mensagem, ou {@code null} se não houve erro
    */
   public final String getErrorMessage()
      {
      return errorMessage;
      }

   /**
    * Devolve as cotações da resposta.
    *
    * @return a lista de cotações
    */
   public final List<ExchangeRate> getExchangeRates()
      {
      return exchangeRates;
      }

   }
