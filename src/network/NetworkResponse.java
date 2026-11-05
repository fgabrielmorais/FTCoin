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

public final class NetworkResponse implements Serializable
   {
   private static final long serialVersionUID = 1L;

   public List<ExchangeRate> exchangeRates;
   public String             errorMessage;

   public NetworkResponse(List<ExchangeRate> exchangeRates, String errorMessage)
      {
      super();
      this.exchangeRates = exchangeRates;
      this.errorMessage  = errorMessage;
      }

   public final String getErrorMessage()
      {
      return errorMessage;
      }

   public final List<ExchangeRate> getExchangeRates()
      {
      return exchangeRates;
      }

   }
