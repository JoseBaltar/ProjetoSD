package middle_client;

import java.io.IOException;
import java.net.*;

import middle_client.models.EventModel;
import middle_client.utils.CreateWindow;
import middle_client.utils.EventTracking;
import middle_client.utils.UserTracking;

/**
 * Thread que espera por sinalizações de ocorrencias, por parte do servidor.
 * 
 * Faz uso de um socket UDP e retorna um "check" para o servidor quando recebe a notificação.
 */
public class WaitOccurrenceThread extends Thread {

    private DatagramSocket listeningSocket = null;
    private DatagramSocket broadcastSocket = null;
    private EventTracking eventTracking;
    private UserTracking userTracking;
    private EventModel notifiedEvent;

    private String multicastIPAddress;
    private int multicastPort;

    WaitOccurrenceThread(EventTracking eventTracking, UserTracking userTracking) throws SocketException {
        super();
        this.eventTracking = eventTracking;
        this.userTracking = userTracking;
        this.listeningSocket = new DatagramSocket(); // socket for listening to server notifications
        this.broadcastSocket = new DatagramSocket(); // this socket port is obsolete since nothing is getting sent to it
    }

    WaitOccurrenceThread(String multicastIPAddress, int multicastPort, EventTracking eventTracking, UserTracking userTracking) throws SocketException {
        super();
        this.multicastIPAddress = multicastIPAddress;
        this.multicastPort = multicastPort;
        this.eventTracking = eventTracking;
        this.userTracking = userTracking;
        this.listeningSocket = new DatagramSocket(); // socket for listening to server notifications
        this.broadcastSocket = new DatagramSocket(); // this socket port is obsolete since nothing is getting sent to it
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                // DatagramPacket used to receive a datagram from the socket
                byte[] buf = new byte[512];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                listeningSocket.receive(packet); // wait for server notification

                // NOTA: verificar a autenticidade da mensagem 
                // (verificar uma palavra chave, por exemplo, enviado pelo servidor)

                // get server address and port and occurrence event information
                String response = new String(packet.getData(), 0, packet.getLength()),
                    eventDetails = response.substring(0, response.indexOf(":"));
                InetAddress address = packet.getAddress();
                int port = packet.getPort(), 
                    serverListeningPort = Integer.parseInt(response.substring(response.indexOf(":") + 1));

                // send ok message to server
                buf = "OK".getBytes();
                packet = new DatagramPacket(buf, buf.length, address, port);
                listeningSocket.send(packet);

                // instanciate the event model
                String[] params = eventDetails.split(","); 
                String description = params[1];
                int eventSeverity = Integer.parseInt(params[0]);
                notifiedEvent = new EventModel(eventSeverity, description);

                if (eventSeverity == 3) {
                    String location = params[2];
                    // broadcast the ocurrence to all clients
                    this.broadcastEventToClients(notifiedEvent.getDescription(), notifiedEvent.getEventName(), 
                                location, notifiedEvent.getInitime(), notifiedEvent.getSeverity(),
                                multicastIPAddress, multicastPort);
                } else {
                    // broadcast the ocurrence to all clients
                    this.broadcastEventToClients(notifiedEvent.getDescription(), notifiedEvent.getEventName(), 
                                "National", notifiedEvent.getInitime(), notifiedEvent.getSeverity(),
                                multicastIPAddress, multicastPort);
                }
                // start sending reports to the server
                Thread event = new SendReportsThread(address, serverListeningPort, notifiedEvent);
                event.start();

                // add event to shared object
                eventTracking.addActiveEvent(notifiedEvent);
                CreateWindow.addEventFinishWindow(notifiedEvent, eventTracking, event);

            } catch (IOException e) {
                e.printStackTrace();
                this.interrupt();
            }
        }
        if (broadcastSocket != null) listeningSocket.close();
        if (broadcastSocket != null) broadcastSocket.close();
    }

    /**
     * Broadcast the Occurring Event Details to all Clients linstening at the
     * multicast IP and PORT
     * 
     * @param description event description
     * @param name event name
     * @param location event location
     * @param multicastIP multicast address group to send the notification
     * @param multicastPort listening port for the sockets
     */
    public void broadcastEventToClients(String description, String name, String location, long time, int level,
                            String multicastIP, int multicastPort) {
        try {
            // construct packet
            byte[] buf = (name + "," + description + "," + location + "," + time + "," + level).getBytes(); // = "VAO TODOS MORRER".getBytes();

            // send it
            InetAddress group = InetAddress.getByName(multicastIP);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, multicastPort);
            broadcastSocket.send(packet);
            
            // all logged in clients should be notified ... 
            notifiedEvent.addNotifiedCount(userTracking.getNumberLoggedUsers());

        } catch (IOException e) {
            e.printStackTrace();
            this.interrupt();
        }
    }

    public void setMulticastIP(String address) {
        this.multicastIPAddress = address;
    }

    public void setMulticastPort(int port) {
        this.multicastPort = port;
    }

    /**
     * Get the generated DatagramSocket local port.
     * @return port
     */
    public int getSocketPort() {
        return listeningSocket.getLocalPort();
    }
}