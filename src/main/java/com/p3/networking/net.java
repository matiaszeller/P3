package main.java.com.p3.networking;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.InputStreamReader;
import javafx.scene.control.TextArea;





public class net {

    private static final String SERVER_ADDRESS = "192.168.0.50"; // Brug local $
    private static final int SERVER_PORT = 35000;

    private TextArea output;

    private void fetchEmployeeUsername(int personId) {
        String request = "getEmployeeUsername:" + personId;
        String response = sendRequestToServer(request);
        output.setText("Employee Username:\n" + response);
    }


    private void addClockInTime(int personId) {
        String request = "addClockInTime:" + personId;
        String response = sendRequestToServer(request);
        output.setText("Clock In Response:\n" + response);
    }

    private String sendRequestToServer(String request) {
        StringBuilder response = new StringBuilder();
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             OutputStream output = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(output, true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
        
            // Send req til server
            writer.println(request);
        
            // Resp
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.append("Error: ").append(e.getMessage());
        }
        return response.toString();
    }   
}
