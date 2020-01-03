package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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

    public synchronized void LogReport(String information, String name){
        Logger logger = Logger.getLogger(name);
        FileHandler fh;

        try {
            fh = new FileHandler(name+".log", true);
            logger.addHandler(fh);
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fh.setFormatter(simpleFormatter);
            logger.log(Level.INFO, information);
            fh.close();
        } catch (IOException | SecurityException ex) {

        }
    }

    @Override
    public void run() {
        /*
         * Thread aberta quando um novo evento ocorre. Recebe relatórios do
         * middle-client através de um socket UDP
         * 
         * TODO
         */

        float timegen = System.currentTimeMillis();

        while (!Thread.interrupted())
        try {
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            String reportinfo = new String(buf, 0, packet.getLength());
            if(reportinfo.contains("!")){
                LogReport(reportinfo, "EventReport"+timegen);
            }else{
                LogReport(reportinfo, "FinalEventReport"+timegen);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int getLocalPort() {
        return socket.getLocalPort();
    }
}