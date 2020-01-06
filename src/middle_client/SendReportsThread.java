package middle_client;

import middle_client.utils.EventModel;

import java.io.IOException;
import java.net.*;

/**
 * Thread que representa o decorrer de um evento. São enviadas por aqui
 * (cliente) relatórios relativos ao evento decorrente, para o Servidor
 * 
 * São enviados periodicamente relatorios para o servidor com o estado do evento,
 * numero de notificados e tempo decorrido. Socket UDP.
 * 
 * UDP DatagramSocket que envia relatorios para o Servidor, até que este dê como terminado
 */
public class SendReportsThread extends Thread {

    private DatagramSocket socket = null;
    private final int REPORT_SEND_TIMER = 30000; // 30 seconds

    private InetAddress serverAddress;
    private int serverListeningPort;
    private EventModel eventModel;

    SendReportsThread(InetAddress serverAddress, int serverListeningPort, EventModel eventModel) throws SocketException {
        super();
        this.serverAddress = serverAddress;
        this.serverListeningPort = serverListeningPort;
        this.eventModel = eventModel;
        this.socket = new DatagramSocket();
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            try {
                Thread.sleep(REPORT_SEND_TIMER);
                byte[] buf = logInformation().getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, serverListeningPort);
                socket.send(packet);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

        // when Location ends the Event, send the last report to server sinalizing the end of the event
        byte[] buf = finalLogInfo().getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, serverListeningPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public EventModel getEvent() {
        return this.eventModel;
    }

    private String logInformation(){
        float currentelapsedtime = (System.currentTimeMillis() - eventModel.getInitime()) / 100;
        return "Location: " + eventModel.getLocationName() + "EventName: " + eventModel.getEventName() 
                        + ", ID:" + eventModel.getId() + ", Description: " + eventModel.getDescription()
                        + ", NumberOfIdentifiedPersonel: " + eventModel.getNotifiedCount() + " CurrentTimeSeconds: " + currentelapsedtime;
    }
  
    private String finalLogInfo(){
        return logInformation()+", Status: Concluded, !";
    }

    /*
    public synchronized void LogReport(){
        Logger logger = Logger.getLogger("EventLog"+ eventModel.getId());
        FileHandler fh;

        float currentelapsedtime = System.nanoTime() - eventModel.getInitime();

        String information = "Name: "+eventModel.getName() + ", ID:" + eventModel.getId() + ", Identified personel: "+eventModel.getNotifiedcount() + " Current Time: "+ currentelapsedtime;

        try {
            fh = new FileHandler("EventLog"+ eventModel.getId()+".log", true);
            logger.addHandler(fh);
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fh.setFormatter(simpleFormatter);
            logger.log(Level.INFO, information);
            fh.close();
        } catch (IOException | SecurityException ex) {

        }
    } */
}