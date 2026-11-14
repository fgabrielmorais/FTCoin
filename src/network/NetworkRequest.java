/*
 * ************************************************************************************************************************
 * Prof. Dr. Andrï¿½ F. de Angelis
 * Unicamp - Universidade Estadual de Campinas
 * FT - Faculdade de Tecnologia
 * ************************************************************************************************************************
 * Escrito em Java 20, usando IDE Eclipse 2023-06, em ambiente Windows 11
 *
 * Versï¿½o 1.0
 * 07.Outubro.2026
 *
 * ************************************************************************************************************************
 *
 * Esta classe implementa o protocolo de requisiï¿½ï¿½o de rede para o Projeto FT Coin ver. 2026.
 *
 * ************************************************************************************************************************
 */

package network;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Pedido de cotações enviado ao servidor: grupo, senha e faixa de datas.
 */
public final class NetworkRequest implements Serializable
   {
   private static final long serialVersionUID = 1L;

   /** Nome do grupo. */
   private final String      groupName;
   /** Senha do grupo. */
   private final String      groupPassword;
   /** Primeira data da faixa pedida. */
   private final LocalDate   firstDate;
   /** Última data da faixa pedida. */
   private final LocalDate   lastDate;

   /**
    * Cria o pedido.
    *
    * @param groupName nome do grupo
    * @param groupPassword senha do grupo
    * @param firstDate primeira data da faixa
    * @param lastDate última data da faixa
    */
   public NetworkRequest(String groupName, String groupPassword, LocalDate firstDate, LocalDate lastDate)
      {
      super();
      this.groupName     = groupName;
      this.groupPassword = groupPassword;
      this.firstDate     = firstDate;
      this.lastDate      = lastDate;
      }

   /**
    * Devolve a primeira data da faixa pedida.
    *
    * @return a primeira data
    */
   public final LocalDate getFirstDate()
      {
      return firstDate;
      }

   /**
    * Devolve o nome do grupo.
    *
    * @return o nome do grupo
    */
   public final String getGroupName()
      {
      return groupName;
      }

   /**
    * Devolve a senha do grupo.
    *
    * @return a senha do grupo
    */
   public final String getGroupPassword()
      {
      return groupPassword;
      }

   /**
    * Devolve a última data da faixa pedida.
    *
    * @return a última data
    */
   public final LocalDate getLastDate()
      {
      return lastDate;
      }

   }
