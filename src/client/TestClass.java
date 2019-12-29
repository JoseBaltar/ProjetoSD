package client;

import java.io.*;
import java.net.*;

/**
 * ESTA CLASSE É PARA APAGAR, SERVIU SO PARA TESTES
 * 
 * O QUE SERIA FEITO AQUI É FEITO DIRETAMENTE NA CLASSE Citizen NO METODO MAIN
 */
public class TestClass extends Thread {
    private static final String EXIT_INFO = "-------------\nTo close the connection write 'Logout'.\n-------------";
    private static final String EXIT_WARNING = "-------------\nService terminated!";
    private static final String CLIENT_MESSAGE = "\n<message> ";

    private Socket middleClientConnection;

    public TestClass(Socket middleClientConnection) {
        super();
        this.middleClientConnection = middleClientConnection;
    }

    public void run() {
        try (
        // Input
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        // Output to MiddleClient
        PrintWriter out = new PrintWriter(middleClientConnection.getOutputStream());
        ) {
            String userInput;
            System.out.print(EXIT_INFO + CLIENT_MESSAGE);
            while ((userInput = stdIn.readLine()) != null) {
                if (userInput.equals("Logout")) {
                    System.out.println(EXIT_WARNING);
                    break;
                } else {
                    // Enviar o input do Cliente para o Servidor
                    out.println(userInput);
                    System.out.print(CLIENT_MESSAGE);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading input from Client!");
            e.printStackTrace();
        }
    }
}

/*
import java.io.*;
import java.net.*;

public class TestClass extends Thread {
    private static final String EXIT_INFO = "-------------\nTo close the connection write 'Logout'.\n-------------";
    private static final String EXIT_WARNING = "-------------\nService terminated!";
    private static final String CLIENT_MESSAGE = "\n<message> ";

    private BufferedReader input_reader;
    private PrintWriter server_out;
    private String multicast_address;

    public TestClass(BufferedReader input_reader, PrintWriter server_out, String address) {
        super();
        this.input_reader = input_reader;
        this.server_out = server_out;
        this.multicast_address = address;
    }

    @Override
    public void run() {
        try {
            String userInput;
            System.out.print(EXIT_INFO + CLIENT_MESSAGE);
            while ((userInput = input_reader.readLine()) != null) {
                /*
                if (!broadcast.isAlive()) {
                    break;
                } else if (userInput.equals("Bye")) {
                    System.out.println(EXIT_WARNING);
                    break;
                } else {
                    /*
                     * System.out.print(CLIENT_MESSAGE); userInput = userInput.replace("\n", "") +
                     * ":" + input_reader.readLine();
                     *
                    // Enviar o input do Cliente para o Servidor
                    server_out.println(userInput);
                    System.out.print(CLIENT_MESSAGE);
                }
                *
            }
        } catch (IOException exc) {
            System.out.println("Error reading input from Client!");
        }
    }
}
*/