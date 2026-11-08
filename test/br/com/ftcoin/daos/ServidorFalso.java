package br.com.ftcoin.daos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import network.NetworkRequest;

/**
 * Servidor de cotações de teste, em uma porta local livre, que fala o mesmo protocolo do servidor do professor:
 * lê um {@link NetworkRequest} e responde com um objeto Java. Guarda as requisições recebidas.
 * <p>
 * Se a resposta for {@code null}, o servidor aceita a conexão e não responde (usado para testar o timeout).
 * </p>
 */
class ServidorFalso {

    private final ServerSocket servidor;
    private final List<NetworkRequest> recebidas = new ArrayList<>();
    private Object resposta;

    ServidorFalso() throws IOException {
        // Porta 0: o sistema escolhe uma porta livre
        this.servidor = new ServerSocket(0);
        Thread atendimento = new Thread(this::atender);
        atendimento.setDaemon(true);
        atendimento.start();
    }

    int porta() {
        return servidor.getLocalPort();
    }

    synchronized void responder(Object resposta) {
        this.resposta = resposta;
    }

    synchronized List<NetworkRequest> recebidas() {
        return recebidas;
    }

    private synchronized Object respostaAtual() {
        return resposta;
    }

    private synchronized void guardar(NetworkRequest requisicao) {
        recebidas.add(requisicao);
    }

    private void atender() {
        while (!servidor.isClosed()) {
            try {
                Socket socket = servidor.accept();
                ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
                NetworkRequest requisicao = (NetworkRequest) entrada.readObject();
                guardar(requisicao);

                Object objeto = respostaAtual();
                if (objeto != null) {
                    ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
                    saida.writeObject(objeto);
                    saida.flush();
                    socket.close();
                }
            } catch (IOException | ClassNotFoundException e) {
                // Cliente desconectou (ex.: timeout) ou o servidor foi fechado: nada a fazer
            }
        }
    }

    void fechar() throws IOException {
        servidor.close();
    }
}
