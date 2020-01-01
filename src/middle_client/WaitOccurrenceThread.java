package middle_client;

import java.io.IOException;
import java.net.*;

/**
 * Thread que espera por sinalizações de ocorrencias, por parte do servidor.
 * 
 * Faz uso de um socket UDP e retorna um "check" para o servidor quando recebe a notificação.
 */
public class WaitOccurrenceThread extends Thread {

    private DatagramSocket socket = null;

    private String multicastIPAdress;
    private int multicastPort;

    WaitOccurrenceThread() throws SocketException {
        super();
        this.socket = new DatagramSocket(); // socket for listening to server notifications
    }

    WaitOccurrenceThread(String multicastIPAddress, int multicastPort) throws SocketException {
        super();
        this.multicastIPAdress = multicastIPAddress;
        this.multicastPort = multicastPort;
        this.socket = new DatagramSocket(); // socket for listening to server notifications
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                // DatagramPacket used to receive a datagram from the socket
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet); // wait for server notification

                // NOTA: verificar a autenticidade da mensagem 
                // (verificar uma palavra chave, por exemplo, enviado pelo servidor)

                // get server address and port and occurrence event information
                String response = new String(packet.getData(), 0, packet.getLength()),
                    event = response.substring(0, response.indexOf(":"));
                InetAddress address = packet.getAddress();
                int port = packet.getPort(), 
                    serverListeningPort = Integer.parseInt(response.substring(response.indexOf(":")));

                // send ok message to server
                buf = "OK".getBytes();
                packet = new DatagramPacket(buf, buf.length, address, port);
                socket.send(packet);

                // broadcast the ocurrence to all clients
                new BroadcastEventThread(event, multicastIPAdress, multicastPort).start();
                // start sending reports to the server
                new SendReportsThread(address, serverListeningPort).start();

            } catch (IOException e) {
                e.printStackTrace();
                this.interrupt();
            }
        }
        socket.close();
    }

    /**
     * Get the generated DatagramSocket local port.
     * @return {@link #socket} port
     */
    public int getSocketPort() {
        return socket.getLocalPort();
    }
}