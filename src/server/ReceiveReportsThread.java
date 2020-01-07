package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import server.utils.EventTracking;

/**
 * Thread que representa o decorrer de um evento. São recebidas aqui (servidor) relatórios relativos ao evento
 * decorrente, por parte do Cliente Intermédio
 */
public class ReceiveReportsThread extends Thread {

    private DatagramSocket socket;
    private EventTracking eventTracking;
    private int TIMEOUT = 60 * 60 * 24; // 1 day

    ReceiveReportsThread(EventTracking eventTracking) throws SocketException {
        this.socket = new DatagramSocket();
        this.eventTracking = eventTracking;
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
        boolean stop = false;
        while (!stop) {
            try {
                byte[] buf = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.setSoTimeout(TIMEOUT);
                socket.receive(packet);

                String reportinfo = new String(buf, 0, packet.getLength());
                int start = reportinfo.indexOf("LocationName: ") + 14, 
                    end = reportinfo.indexOf(",", start);
                String location = reportinfo.substring(start, end);
                start = reportinfo.indexOf("DangerLevel: ") + 13;
                end = reportinfo.indexOf(",", start);
                int degree = Integer.parseInt(reportinfo.substring(start, end));
                if(!reportinfo.contains("!")){
                    LogReport(reportinfo, "EventReport"+timegen);
                }else{
                    LogReport(reportinfo, "FinalEventReport"+timegen);
                    eventTracking.removeActiveEvent(location, degree);
                    stop = true;
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
            fh = new FileHandler("../files/logs/"+name+".log", true);
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