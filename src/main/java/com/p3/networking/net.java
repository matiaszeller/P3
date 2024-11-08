package com.p3.networking;


import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.InputStreamReader;




public class net {


    private static final String SERVER_ADDRESS = "localhost"; // local $
    private static final int SERVER_PORT = 10000;

    public String sendRequestToServer(String request) {

            StringBuilder response = new StringBuilder();
            try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                 OutputStream output = socket.getOutputStream();
                 PrintWriter writer = new PrintWriter(output, true);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Send req til server
                writer.println(request);

                // Resp
              response.append(reader.readLine());

            } catch (Exception e) {
                e.printStackTrace();
                response.append("Error: ").append(e.getMessage());
            }
            return response.toString();



    }
}
