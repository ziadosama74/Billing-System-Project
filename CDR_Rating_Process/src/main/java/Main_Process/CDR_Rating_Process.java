package Main_Process;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CDR_Rating_Process {

    public static void main(String[] args) {

        try {
            Socket socket = new Socket("localhost", 5000);

            System.out.println("Connected to Parsing Server");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            while (true) {

                String message = in.readLine();

                if (message == null) {
                    break;
                }

                if (message.startsWith("START_RATING")) {

                    System.out.println("Received: " + message);

                    String[] parts = message.split("\\|");

                    if (parts.length > 1) {
                        System.out.println("Files Count: " + parts[1]);
                    }

                    startRating();
                }
            }

        } catch (Exception e) {
            System.out.println("Cannot connect to server. Make sure Parsing is running.");
        }
    }

    public static void startRating() {
        System.out.println("Rating Process Started 🔥");
        // put your rating logic here
    }
}