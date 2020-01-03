package middle_client;

import jdk.jfr.Event;
import middle_client.utils.EventModel;

import java.io.IOException;
import java.net.*;
import java.sql.Timestamp;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Thread que representa o decorrer de um evento. São enviadas por aqui
 * (cliente) relatórios relativos ao evento decorrente, para o Servidor
 * 
 * TODO fazer um "event tracker" e uma forma de "acabar o evento" (pode ser através de um botão numa janela)
 */
public class SendReportsThread extends Thread {

    private DatagramSocket socket = null;
    private InetAddress serverAddress;
    private int serverPort;
    private EventModel eventModel;

    SendReportsThread(InetAddress serverAddress, int serverPort, EventModel eventModel) throws SocketException {
        super();
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.eventModel = eventModel;
        this.socket = new DatagramSocket();
    }

    private EventModel getEventModel(){
        return eventModel;
    }

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
    }


    @Override
    public void run() {
        /*
         * São enviados periodicamente relatorios para o servidor com o estado do evento,
         * numero de notificados e tempo decorrido. Socket UDP.
         * 
         * TODO
         */
    }
}