package server;

import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Thread que representa o decorrer de um evento. São recebidas aqui (servidor) relatórios relativos ao evento
 * decorrente, por parte do Cliente Intermédio
 */
public class ReceiveReportsThread extends Thread {

    private DatagramSocket socket;

    private NetworkPorts shared;

    ReceiveReportsThread(NetworkPorts shared) throws SocketException {
        this.shared = shared;
        DatagramSocket temp;
        while (shared.addEventPeriodPort((temp = new DatagramSocket()).getLocalPort()) == -1) {
            // check if port is already in use
            temp.close();
        }
        this.socket = temp;
    }

    @Override
    public void run() {
        /*
         * Thread aberta quando um novo evento ocorre. Recebe relatórios do
         * middle-client através de um socket UDP
         * 
         * TODO
         */
    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }
}