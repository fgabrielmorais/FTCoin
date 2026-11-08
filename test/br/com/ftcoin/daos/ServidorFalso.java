package br.com.ftcoin.daos;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import network.NetworkRequest;

/**
 * Servidor de cotações de teste, em uma porta local livre, no mesmo protocolo do servidor do professor:
 * lê um {@link NetworkRequest} e responde um objeto Java. Guarda as requisições recebidas.
 */
final class ServidorFalso implements AutoCloseable {

    private final ServerSocket servidor;
    private final Function<NetworkRequest, Object> resposta;
    private final List<NetworkRequest> recebidas = Collections.synchronizedList(new ArrayList<>());
    private final List<Socket> abertos = Collections.synchronizedList(new ArrayList<>());

    /**
     * @param resposta calcula o objeto a enviar; devolver {@code null} faz o servidor aceitar e ficar mudo
     */
    ServidorFalso(Function<NetworkRequest, Object> resposta) throws IOException {
        this.servidor = new ServerSocket(0);
        this.resposta = resposta;
        Thread atendimento = new Thread(this::atender, "servidor-falso");
        atendimento.setDaemon(true);
        atendimento.start();
    }

    int porta() {
        return servidor.getLocalPort();
    }

    List<NetworkRequest> recebidas() {
        return recebidas;
    }

    private void atender() {
        while (!servidor.isClosed()) {
            try {
                Socket socket = servidor.accept();
                abertos.add(socket);
                ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
                NetworkRequest requisicao = (NetworkRequest) entrada.readObject();
                recebidas.add(requisicao);
                Object objeto = resposta.apply(requisicao);
                if (objeto != null) {
                    ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
                    saida.writeObject(objeto);
                    saida.flush();
                    socket.close();
                }
            } catch (IOException | ClassNotFoundException e) {
                // Conexão encerrada pelo cliente (ex.: timeout) ou servidor fechado: nada a fazer
            }
        }
    }

    @Override
    public void close() throws IOException {
        servidor.close();
        synchronized (abertos) {
            for (Socket socket : abertos) {
                socket.close();
            }
        }
    }
}
