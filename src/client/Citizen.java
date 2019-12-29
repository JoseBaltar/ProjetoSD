package client;

import java.io.*;
import java.net.*;

/**
 * Abrir duas threads. <br/>
 * - TCP, que comunica com o Middle-Client através de um protocolo<br/>
 * - UDP, fica à espera de receber notificações (aberto depois do registo e
 * login no Middle-Client)<br/>
 */
public class Citizen {
    private static final String EXIT_INFO = "-------------\nTo close the connection write '%exit'.\n-------------\n";
    private static final String EXIT_WARNING = "\n-----\nConnection terminated!\n-----\n";
    private static final String CLIENT_MESSAGE = "\nClient: ";
    private static final String SERVER_RESPONSE = "Server: ";

    public static void main(String[] args) throws IOException {
        /** Check arguments */
        if (args.length != 2) {
            System.out.println("Wrong Arguments!\n\nUsage:\n\t"
                    + "java client.Citizen <middle_client_ip> <middle_client_port>");
        } else if (!args[1].matches("\\d+")) {
            System.out.println("Wrong Arguments! Make sure <port> is a valid integer."
                    + "\n\nUsage:\n\tjava client.Citizen <middle_client_ip> <middle_client_port>");
        } else {

            // Tendo em conta que há a possibilidade de fazer "logout", receber o ip e porta do servidor
            // por argumento deixa de fazer sentido.
            // Solução passa por criar um ciclo while aqui, antes do try-catch with resources abaixo, que
            // pede o input do ip e porta do servidor de cada vez de faz logout e, obviamente, quando começa.
            // O ciclo é quebrado pela introdução do "%exit".
            // 
            // (Ainda estou a pensar nisto, por isso é que tenho este comentário.)

            /** Connect to middle_client.CivilProtection server */
            String serverIP = args[0];
            int serverPort = Integer.parseInt(args[1]);
            Thread notificationThread = null;
            try (
                Socket serverConnection = new Socket(serverIP, serverPort); /** middle_client server connection */
                PrintWriter to_server = 
                            new PrintWriter(serverConnection.getOutputStream(), true); /** output stream to server */
                BufferedReader from_server = new BufferedReader(
                            new InputStreamReader(serverConnection.getInputStream())); /** input stream from server */
                BufferedReader stdIn = 
                            new BufferedReader(new InputStreamReader(System.in)); /** system.in input stream */
            ) {

                /** Register and Login into Middle-Client Server before anything else */
                boolean exit = false;
                String userInput;
                String serverOutput;
                System.out.print(
                    Citizen.EXIT_INFO + Citizen.SERVER_RESPONSE + from_server.readLine() + Citizen.CLIENT_MESSAGE);
                while (!exit && (userInput = stdIn.readLine()) != null) {
                    // terminate communication
                    if (userInput.equals("%exit")) {
                        to_server.println(userInput);
                        System.out.println(Citizen.EXIT_WARNING);
                        System.exit(0);
                    } else {
                        // send input to server
                        to_server.println(userInput);
                        if ((serverOutput = from_server.readLine()).equalsIgnoreCase("logged-in")) {
                            // log-in successfull
                            System.out.print("\nSuccessfully Logged In. Starting Notification Services ...\n");
                            exit = true;
                        } else {
                            // print server response
                            System.out.print(Citizen.SERVER_RESPONSE + serverOutput + Citizen.CLIENT_MESSAGE);
                        }
                    }
                }

                /** Logged In, start waiting for notification from the server */
                serverOutput = from_server.readLine();
                int separator = serverOutput.indexOf(":", 0);
                String multicastIP = serverOutput.substring(0, separator);
                int multicastPort = Integer.parseInt(serverOutput.substring(separator, serverOutput.length()));
                notificationThread = new ReceiveNotificationThread(multicastIP, multicastPort);
                notificationThread.start();

                /** Continue communication with the server. Send notifications. */
                System.out.print(Citizen.CLIENT_MESSAGE);
                while ((userInput = stdIn.readLine()) != null) {
                    // terminate communication
                    if (userInput.equals("%exit")) {
                        to_server.println(userInput);
                        System.out.println(Citizen.EXIT_WARNING);
                        System.exit(0);
                    } else if (userInput.equals("%logout")) {
                        to_server.println(userInput);
                        
                        // ver nota na linha 28

                    } else {
                        // send input to server
                        to_server.println(userInput);
                        // print server response
                        System.out.print(Citizen.SERVER_RESPONSE + from_server.readLine() + Citizen.CLIENT_MESSAGE);
                    }
                }

            } catch (UnknownHostException e) {
                System.err.println("Don't know about host: " + serverIP);
                System.exit(-1);
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection.");
                System.exit(-1);
            } finally {
                if (notificationThread != null) notificationThread.interrupt();
            }
        }
    }
}