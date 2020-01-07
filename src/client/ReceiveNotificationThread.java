package client;

import java.io.*;
import java.net.*;

/**
 * Opens a {@link java.net.MulticastSocket} to wait for Event Notifications
 * and print them in a JAVA SWING window
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
                String[] params = received.split(","); 
                String description = params[0], name = params[1], location = params[2];
                long time = Long.parseLong(params[3]);
                int level = Integer.parseInt(params[4]);
                CreateWindow.showIncomingEvent(location, name, description, time, level);
            }

            socket.leaveGroup(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}