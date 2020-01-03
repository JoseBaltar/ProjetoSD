package middle_client;

import java.io.*;
import java.net.*;

import middle_client.utils.ClientLoginProtocol;
import middle_client.utils.EventNotificationProtocol;

/**
 * Registo e Login <br/>
 * Redireciona as notificações dos clientes para o servidor <br/>
 * Abre threads relativas a um evento decorrente.
 */
public class MiddleClientCommunicationThread extends Thread {
    private static String DISPLAY = "\nMiddle-Client: ";
    private static final String LOGIN_CANCEL = "Client canceled the login. Terminating connection ...";
    private static final String LOGIN = "Client Logged In!";
    private static final String LOGOUT = "Successfully Logged Out. Terminating connection ...";
    private static final String SENT_NOTIFICATION = "Notification sent Successfully!"; // don't write \n or \r into this string

    private Socket clientConnection;
    private Socket mainServerConnection;

    private String multicastIPAdress;
    private int multicastPort;
    
    private ClientLoginProtocol login_protocol;
    private EventNotificationProtocol notification_protocol;

    /**
     * @param locationName Location name relative to this Middle-Client
     * @param clientConnection Client/Middle-Client socket connection
     * @param mainServerConnection Middle-Client/Server socket connection, the same for every thread 
     * @param multicastIPAddress This Location, Middle-Client, respective multicastIP address
     * @param multicastPort This Location, Middle-Client, respective multicast address PORT
     */
    MiddleClientCommunicationThread(String locationName, Socket clientConnection, Socket mainServerConnection, 
                        String multicastIPAddress, int multicastPort) {
        super();
        this.clientConnection = clientConnection;
        this.mainServerConnection = mainServerConnection;
        this.DISPLAY = "\n" + clientConnection.getInetAddress() + " - Middle-Client: ";

        this.multicastIPAdress = multicastIPAddress;
        this.multicastPort = multicastPort;

        this.login_protocol = new ClientLoginProtocol(locationName);
        this.notification_protocol = new EventNotificationProtocol();
    }

    public void run() {
        try (
        // Input from Client
        BufferedReader in_cli = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
        // Output to Client
        PrintWriter out_cli = new PrintWriter(clientConnection.getOutputStream());
        // Output to Server
        PrintWriter out_srv = new PrintWriter(mainServerConnection.getOutputStream());
        // Input from Server shouldn't exist because mainServerConnection is shared by every End-Client Thread
        // Various synchronization problems would arise using this InputStream.
        ) {
            /** Register and Login End-Client */
            boolean quit = false;
            String clientInp, processed;
            String[] response;

            out_cli.println(login_protocol.processInput("")); // send first message to Client
            while (!quit && (clientInp = in_cli.readLine()) != null) {
                if (clientInp.equalsIgnoreCase("%quit")) {
                    /** Check if client exited during login */
                    System.out.println(DISPLAY + LOGIN_CANCEL);
                    quit = true;
                } else {
                    /** Process client input through the protocol */
                    processed = login_protocol.processInput(clientInp);

                    if (processed.equalsIgnoreCase("logged-in")) {
                        out_cli.println(processed);
                        System.out.println(DISPLAY + LOGIN);
                        // send extra information to client, about the multicast connection
                        out_cli.println(multicastIPAdress + ":" + multicastPort);
                        // notify server of client login on this location
                        out_srv.println("%login:" + login_protocol.getLoginUsername());
                        
                        /** Client Logged-In, start receiving event notifications */
                        out_cli.println(notification_protocol.processInput(""));
                        while (!quit && (clientInp = in_cli.readLine()) != null) {

                            if (clientInp.equalsIgnoreCase("%logout")) {
                                /** Check if client logged out. Stop communication with this server */
                                // notify server of client logout on this location
                                out_srv.println("%logout:" + login_protocol.getLoginUsername());
                                // notify client of successfull logout
                                out_cli.println(LOGOUT);
                                System.out.println(DISPLAY + LOGOUT);
                                quit = true;

                            } else {
                                processed = notification_protocol.processInput("");
                                
                                if (processed.equalsIgnoreCase("processed")) {
                                    // redirect event notification into main server
                                    out_srv.println(notification_protocol.processInput(""));
                                    // notify client about sent notification
                                    out_cli.println(SENT_NOTIFICATION);
                                } else {
                                    out_cli.println(processed);
                                }
                            }

                        } /** notification processing cicle */

                    } else {
                        out_cli.println(processed);
                    }
                    
                }

            } /** client login cicle */
            
            System.out.println("-------------\nService terminated on Client " + clientConnection.getInetAddress() + "!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // LEGENDA:
    // - End-Client, cliente consumidor final, que se liga a uma localização
    // - Middle-Client, a localização, que se liga ao servidor principal
    // - Main-Server, o servidor principal
    // - mainServerConnection, java.net.Socket da conexão entre o Middle-Client e o Main-Server
    // - out.srv, mainServerConnection.getOutputStream() enviar dados para o Main-Server
    // - in.srv, mainServerConnection.getInputStream() receber dados do Main-Server

    // OS DOIS PEDAÇOS DE CODIGO ABAIXO FORAM REMOVIDOS DEVIDO A NÃO SER POSSÍVEL RECEBER MENSAGENS DO SERVIDOR
    // QUE SEJAM ESPECIFICAS DE UM SÓ End-Client

    // ISTO É, O mainServerConnnection Socket É PARTILHADO ENTRE TODAS AS THREADS DE End-Clients CONECTADOS A UM Middle-Client
    // (esta partilha existe uma vez que só deve existir uma única conexão entre o Middle-Client e Main Server)
    // (isto é provado pelo desenho da árvore de conexões entre as entidades)

    // NO FUNDO, SÓ DEVEM SER ENVIADAS (através da OutpuStream) MENSAGENS PARA O Main-Server E NUNCA FICAR À ESPERA DE
    // RESPOSTA DO MESMO, UMA VEZ QUE NÃO SE SABE O QUE ESTÁ NO BUFFER DO InputStream LIGADO AO SERVIDOR.

    // SERIA POSSÍVEL CRIAR UMA ESPÉCIE DE GESTÃO PARA ESTAS MENSAGENS, NO ENTANTO O NÍVEL DE COMPLEXIDADE É ESTRONDOSAMENTE ALTO
    // para dar um exemplo, se 10 end-clients enviassem uma mensagem para o main-server ao mesmo tempo e ficassem à espera
    // de resposta (enviar através da OutpuStream do Socket, "out_srv.println(MSG);" e depois "in_srv.readLine();"), 
    // não só o servidor teria de colocar por ordem as mensagens recebidas como também todos os "readLine();" deveriam
    // estar sincronizados para que quem enviou primeiro a mensagem recebesse primeiro a mensagem do servidor.
    // Neste caso, o servidor também só poderia enviar as mensagens de volta para o Middle-Client consoante a ordem
    // de receção das mensagens
    // O número de sincronismos necessários é estupidamente alto, tal como a velocidade de processamento dos pedidos seria
    // extremamente precária.

    // EM CONCLUSÃO, A SOLUÇÃO PASSA POR NUNCA FICAR À ESPERA DE RESPOSTAS DO Main-Server NAS THREADS DE CONEXÃO DE CADA
    // End-Client AO Middle-Client

    /*
    /** Process server output through Protocol /
    processed = notification_protocol.processInput(in_srv.readLine());

    // ... check processed, or not
    out_cli.println(processed);

    // NOTA: esta parte poderia estar a ser feita pelo protocolo. Só não tenho a certeza disso ainda.
    // processed = notification_protocol.processInput(clientInp);
    // Mas parece pouca coisa para criar um protocolo só para isto. 
    // Pode-me estar a falhar alguma coisa no entanto.

    /** Process server output 
     * - a string representing an array of strings separated by ","
     * with all response details
     /
    response = in_srv.readLine().split(",");

    if (response[0].equals("invalid-data")) {
        /** Invalid data - [1] data details /
        // notify client about the data
        out_cli.println("Invalid Notification Data! Details: " + response[1]);

    } else if (response[0].equals("in-progress")) {
        /** Notification relates to an already in progress event
         * [1] socket port /

        // reenviar a notificação?
        // avisar o cliente que o evento ja foi notificado?
        // falta decidir ...
        out_cli.println("Request sent and processed successfully!");

    } else {
        /**
         * New Event, nothing else is needed because thread "WaitOccurrenceThread" handles that.
         * Though, something has to be returned to the client still
         /
        out_cli.println("Request sent and processed successfully!");

    }
    */
    /*
    if (processed.startsWith("findUserLogin")) {
        /** When username and password exist, check for other login from same user /

        // request server for login check
        out_srv.println("%" + processed); // returns the location name if true
        // send server response to protocol
        processed = login_protocol.processInput(in_srv.readLine()); 

        if (processed.equalsIgnoreCase("logged-in")) {
            out_cli.println(processed);
            // send extra information to client, about the multicast connection
            out_cli.println(multicastIPAdress + ":" + multicastPort);
            // notify server of client login on this location
            out_srv.println("%login:" + login_protocol.getLoginUsername());
            
            /** Client Logged-In, start receiving event notifications /
            out_cli.println(notification_protocol.processInput(""));
            while (!quit && (clientInp = in_cli.readLine()) != null) {

                if (clientInp.equalsIgnoreCase("%logout")) {
                    /** Check if client logged out. Stop communication with this server /
                    // notify server of client logout on this location
                    out_srv.println("%logout:" + login_protocol.getLoginUsername());
                    // notify client of successfull logout
                    out_cli.println("Successfully Logged Out ...");
                    quit = true;
                } else {

                    /** Redirect client input into main server /
                    out_srv.println(clientInp);

                    /** Process server output through Protocol /
                    processed = notification_protocol.processInput(in_srv.readLine());

                    // ... check processed, or not
                    out_cli.println(processed);

                    // NOTA: esta parte poderia estar a ser feita pelo protocolo. Só não tenho a certeza disso ainda.
                    // processed = notification_protocol.processInput(clientInp);
                    // Mas parece pouca coisa para criar um protocolo só para isto. 
                    // Pode-me estar a falhar alguma coisa no entanto.
                    // TODO resolver esta situação

                    /** Process server output 
                     * - a string representing an array of strings separated by ","
                     * with all response details
                     /
                    response = in_srv.readLine().split(",");

                    if (response[0].equals("invalid-data")) {
                        /** Invalid data - [1] data details /
                        // notify client about the data
                        out_cli.println("Invalid Notification Data! Details: " + response[1]);

                    } else if (response[0].equals("in-progress")) {
                        /** Notification relates to an already in progress event
                         * [1] socket port /

                        // reenviar a notificação?
                        // avisar o cliente que o evento ja foi notificado?
                        // falta decidir ...
                        out_cli.println("Request sent and processed successfully!");

                    } else {
                        /**
                         * New Event, nothing else is needed because thread "WaitOccurrenceThread" handles that.
                         * Though, something has to be returned to the client still
                         /
                        out_cli.println("Request sent and processed successfully!");
                
                    }
                }

            } /** notification processing cicle /
        
        } else {
            out_cli.println(processed);
        }
    }
*/
}