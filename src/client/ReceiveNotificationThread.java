package client;

import java.io.*;
import java.net.*;

/**
 * 
 */
public class ReceiveNotificationThread extends Thread {

    protected String multicastSocketIP;
    protected int port;

    /**
     * 
     */
    ReceiveNotificationThread(String multicastIP, int port) {
        super();
        this.multicastSocketIP = multicastIP;
        this.port = port;
    }

    @Override
    public void run() {
        try (
            MulticastSocket socket = new MulticastSocket(port)
        ){
            InetAddress address = InetAddress.getByName(multicastSocketIP);
            socket.joinGroup(address);

            DatagramPacket packet;

            // get notification
            while (!this.isInterrupted()) {

                byte[] buf = new byte[256];
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet); // wait for packet from server

                String received = new String(packet.getData(), 0, packet.getLength());
                // String eventName = received.substring(0, received.indexOf(","));
                // int eventSeverity = Integer.parseInt(received.substring(received.indexOf(",")));
                System.out.println("Notification: " + received); // TODO Output notification (fazer uma janela no swing)
            }

            socket.leaveGroup(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}