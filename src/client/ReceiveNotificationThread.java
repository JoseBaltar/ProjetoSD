package client;

import java.io.*;
import java.net.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextArea;

/**
 * Opens a {@link java.net.MulticastSocket} to wait for Event Notifications
 * and print them in a JAVA SWING window
 */
public class ReceiveNotificationThread extends Thread {

    private JFrame guiFrame;
    private JTextArea displayIncoming;
    private JTextArea displayLast;
    private JTextArea displayList; 

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
                showIncomingEvent(location, name, description);
            }

            socket.leaveGroup(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showIncomingEvent(String location, String eventName, String description) {

    }

    /**
     * TODO
     * @param event
     */
    private void createEventNotificationWindow() {
        // create the window to display incoming notifications
        guiFrame = new JFrame();
    }

}