package middle_client;

import java.io.IOException;
import java.net.*;

import middle_client.utils.EventModel;

/**
 * Thread que representa o decorrer de um evento. São enviadas por aqui
 * (cliente) relatórios relativos ao evento decorrente, para o Servidor
 */
public class SendReportsThread extends Thread {

    private DatagramSocket socket = null;
    private InetAddress serverAddress;
    private int serverListeningPort;
    private EventModel event;

    SendReportsThread(InetAddress serverAddress, int serverListeningPort, EventModel event) throws SocketException {
        super();
        this.serverAddress = serverAddress;
        this.serverListeningPort = serverListeningPort;
        this.event = event;
    }

    @Override
    public void run() {
        /*
         * São enviados periodicamente relatorios para o servidor com o estado do evento,
         * numero de notificados e tempo decorrido. Socket UDP.
         * 
         * UDP DatagramSocket que envia relatorios para o Servidor, até que este dê como terminado
         */
    }

    public EventModel getEvent() {
        return this.event;
    }
}