package middle_client;

import java.io.IOException;
import java.net.*;

import middle_client.utils.EventModel;
import middle_client.utils.EventTracking;

/**
 * Thread que espera por sinalizações de ocorrencias, por parte do servidor.
 * 
 * Faz uso de um socket UDP e retorna um "check" para o servidor quando recebe a notificação.
 */
public class WaitOccurrenceThread extends Thread {

    private DatagramSocket listeningSocket = null;
    private DatagramSocket broadcastSocket = null;
    private EventTracking eventTracking;

    private String multicastIPAddress;
    private int multicastPort;

    WaitOccurrenceThread(EventTracking eventTracking) throws SocketException {
        super();
        this.eventTracking = eventTracking;
        this.listeningSocket = new DatagramSocket(); // socket for listening to server notifications
        this.broadcastSocket = new DatagramSocket(65535); // this socket port is obsolete since nothing is getting sent to it
    }

    WaitOccurrenceThread(String multicastIPAddress, int multicastPort, EventTracking eventTracking) throws SocketException {
        super();
        this.multicastIPAddress = multicastIPAddress;
        this.multicastPort = multicastPort;
        this.eventTracking = eventTracking;
        this.listeningSocket = new DatagramSocket(); // socket for listening to server notifications
        this.broadcastSocket = new DatagramSocket(65535); // this socket port is obsolete since nothing is getting sent to it
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                // DatagramPacket used to receive a datagram from the socket
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                listeningSocket.receive(packet); // wait for server notification

                // NOTA: verificar a autenticidade da mensagem 
                // (verificar uma palavra chave, por exemplo, enviado pelo servidor)

                // get server address and port and occurrence event information
                String response = new String(packet.getData(), 0, packet.getLength()),
                    eventDetails = response.substring(0, response.indexOf(":"));
                InetAddress address = packet.getAddress();
                int port = packet.getPort(), 
                    serverListeningPort = Integer.parseInt(response.substring(response.indexOf(":")));

                // send ok message to server
                buf = "OK".getBytes();
                packet = new DatagramPacket(buf, buf.length, address, port);
                listeningSocket.send(packet);

                // broadcast the ocurrence to all clients
                this.broadcastEventToClients(eventDetails, multicastIPAddress, multicastPort);
                // instanciate the event model
                String eventName = eventDetails.substring(0, eventDetails.indexOf(","));
                int eventSeverity = Integer.parseInt(eventDetails.substring(eventDetails.indexOf(",")));
                EventModel eventModel = new EventModel(eventName, eventSeverity);
                // start sending reports to the server
                Thread event = new SendReportsThread(address, serverListeningPort, eventModel);
                event.start();

                // add event to shared object
                eventTracking.addActiveEvent(eventModel);
                this.createEventFinishWindow(event);

            } catch (IOException e) {
                e.printStackTrace();
                this.interrupt();
            }
        }
        if (broadcastSocket != null) listeningSocket.close();
        if (broadcastSocket != null) broadcastSocket.close();
    }

    
    /**
     * TODO
     * @param event
     */
    private void createEventFinishWindow(Thread event) {
        // interrupt thread and remove event from shared object
    }

    /**
     * TODO
     * @param eventDetails
     * @param multicastIP
     * @param multicastPort
     */
    public void broadcastEventToClients(String eventDetails, String multicastIP, int multicastPort) {
        try {
            byte[] buf = new byte[256];

            // construct packet
            buf = eventDetails.getBytes();// = "VAO TODOS MORRER".getBytes();

            // send it
            InetAddress group = InetAddress.getByName(multicastIP);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, multicastPort);
            broadcastSocket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
            this.interrupt();
        }
    }

    /**
     * Get the generated DatagramSocket local port.
     * @return {@link #socket} port
     */
    public int getSocketPort() {
        return listeningSocket.getLocalPort();
    }
}