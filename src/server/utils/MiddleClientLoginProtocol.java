package server.utils;

import com.google.gson.*;

import java.io.*;

/**
 * TODO fazer o registo e login para ficheiros, utilizar o shared object "UserTracking"
 */
public class MiddleClientLoginProtocol {
    private static enum MainStates {
        DUMMY
    };

    private static enum SecStates {
        NOT_DEFINED
    };

    private MainStates main_state = MainStates.DUMMY;
    private SecStates sec_state = SecStates.NOT_DEFINED;
    private String middleclientip, multicastip, password, locationName;
    private int serverport, multicastport, waitingport;
    private UserTracking tracking;

    public MiddleClientLoginProtocol(UserTracking tracking){
        this.tracking = tracking;
    }

    private boolean dummy = false;

    public String processInput(String theInput) {
        String theOutput = null;

        // Check special inputs
        if (theInput.equals("%dummy")) {
            main_state = MainStates.DUMMY;
            sec_state = SecStates.NOT_DEFINED;
        }

        /**
         * TODO no final da validação para o login, enviar uma resposta com "getport"
         * , para adicionar a porta do socket em "waitOccurrenceThread" 
         * (ver o codigo do login em "MiddleClientCommunicationThread")
         * 
         */

        if (dummy) {

        } else if (main_state == MainStates.DUMMY) {

        } else {
            // more ...
        }
        return theOutput;
    }

    public boolean registerMiddleClient() {

        Gson gson = new Gson(); // Instância gson para escrever o ficheiro Json
        File pathf = new File("middleclientlist.json"); // Ficheiro de destino
        JsonElement file = this.loadMCFromJSONFile();
        JsonArray clientes
                = (file != null && file.isJsonArray()
                ? file.getAsJsonArray() : new JsonArray());

        JsonObject client = new JsonObject();
        client.addProperty("locationName", locationName);
        client.addProperty("middleclientip",middleclientip);
        client.addProperty("multicastip", multicastip);
        client.addProperty("password", password);
        client.addProperty("serverport", serverport);
        client.addProperty("multicastport", multicastport);
        client.addProperty("waitingport", waitingport);

        tracking.addRegisteredMC(client);
        clientes.add(client);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathf))) {
            writer.write(gson.toJson(clientes));
            writer.flush();
        } catch (IOException ex) {
            System.err.println("[" + ex.getClass().getName() + "] "
                    + "Erro na escrita do ficheiro" );
            return false;
        }

        return true;
    }
    public JsonElement loadMCFromJSONFile() {
        JsonElement json; // JsonElement correspondente ao ficheiro
        try
        { // Leitura do ficheiro e parse para uma instância de JsonElement
            FileReader inputFile = new FileReader("middleclientlist.json");

            JsonParser parser = new JsonParser();
            json = parser.parse(inputFile);

        } catch (FileNotFoundException ex)
        { // Retorna null se o ficheiro não existir
            return null;
        }

        if (json.isJsonArray() && json.getAsJsonArray().size() == 0)
        {
            return null;
        }

        return json;
    }

}