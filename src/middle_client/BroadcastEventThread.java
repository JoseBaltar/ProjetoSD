package middle_client;

import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;

/**
 * Abre uma janela que imprime notificações de catástrofes relativas ao Cliente.
 * 
 * TODO fazer um "event tracker" e uma forma de "acabar o evento" (pode ser através de um botão numa janela) (isto deveria estar no SharedObject "ClientCommunicationTracking")
 */
public class BroadcastEventThread extends Thread{

    private DatagramSocket socket = null;
    private String multicastIP, event;
    private int multicastPort;

    private Semaphore hasNotification = null; // provavelmente nao se vai usar isto, ver o to-do
    // private long WAIT = 5000; // substituido pelo semaphore

    BroadcastEventThread(String event, String multicastIP, int multicastPort) throws SocketException {
        super();
        this.event = event;
        this.multicastIP = multicastIP;
        this.multicastPort = multicastPort;

        socket = new DatagramSocket(65535); // this socket port is obsolete since nothing is getting sent to it
    }

    public void run() {
        while (!this.isInterrupted()) {
            try {
                // wait for notification from server
                hasNotification.acquire(); // check the defined to-do

                byte[] buf = new byte[256];

                // construct packet
                buf = event.getBytes();// = "VAO TODOS MORRER".getBytes();

                // send it
                InetAddress group = InetAddress.getByName(multicastIP);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, group, multicastPort);
                socket.send(packet);

            } catch (IOException e) {
                e.printStackTrace();
                this.interrupt();
            } catch (InterruptedException e) {
                e.printStackTrace();
                this.interrupt();
            }
        }
        socket.close();
    }
}