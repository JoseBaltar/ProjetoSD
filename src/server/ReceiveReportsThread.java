package server;

import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Thread que representa o decorrer de um evento. São recebidas aqui (servidor) relatórios relativos ao evento
 * decorrente, por parte do Cliente Intermédio
 * 
 * TODO acabar o metodo Run da Thread
 */
public class ReceiveReportsThread extends Thread {

    private DatagramSocket socket;

    ReceiveReportsThread() throws SocketException {
        this.socket = new DatagramSocket();
    }

    @Override
    public void run() {
        /*
         * Thread aberta quando um novo evento ocorre. Recebe relatórios do
         * middle-client através de um socket UDP
         * 
         * UDP DatagramSocket que espera por relatorios do Middle-Client
         * (espera até que o relatorio ative a flag de finalização do evento)
         * 
         */
    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }
}