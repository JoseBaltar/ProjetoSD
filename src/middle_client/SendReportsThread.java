package middle_client;

import java.io.IOException;
import java.net.*;

/**
 * Thread que representa o decorrer de um evento. São enviadas por aqui
 * (cliente) relatórios relativos ao evento decorrente, para o Servidor
 * 
 * TODO fazer um "event tracker" e uma forma de "acabar o evento" (pode ser através de um botão numa janela) (isto deveria estar no SharedObject "ClientCommunicationTracking")
 * TODO acabar metodo Run
 */
public class SendReportsThread extends Thread {

    private DatagramSocket socket = null;
    private InetAddress serverAddress;
    private int serverListeningPort;

    SendReportsThread(InetAddress serverAddress, int serverListeningPort) throws SocketException {
        super();
        this.serverAddress = serverAddress;
        this.serverListeningPort = serverListeningPort;
        this.socket = new DatagramSocket();
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
}