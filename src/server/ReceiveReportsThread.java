package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
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

    ReceiveReportsThread() throws SocketException {
        this.socket = new DatagramSocket();
    }

    @Override
    public void run() {
        /*
         * Thread aberta quando um novo evento ocorre. Recebe relatórios do
         * middle-client através de um socket UDP
         * 
         * UDP DatagramSocket que espera por relatorios do Middle-Client
         * (espera até que o relatorio ative a flag de finalização do evento)
         * 
         */

        float timegen = System.currentTimeMillis();

        while (!Thread.interrupted()) {
            try {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String reportinfo = new String(buf, 0, packet.getLength());
                if(!reportinfo.contains("!")){
                    LogReport(reportinfo, "EventReport"+timegen);
                }else{
                    LogReport(reportinfo, "FinalEventReport"+timegen);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public int getLocalPort() {
        return socket.getLocalPort();
    }
}