package Main_Process;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import Rating_PK.Rating;
public class CDR_Rating_Process {

    public static void main(String[] args) {

        try 
        {
            Socket socket = new Socket("localhost", 5000);
            System.out.println("══════════════════════════════════════════════════════════════════════════════");
            System.out.println("💰 Rating Engine is Running ⚙️");
            System.out.println("══════════════════════════════════════════════════════════════════════════════");
            System.out.println("[⚙️💰 RATING ] :  Connected To Parsing Process Server Successfully ✅");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            while (true) {

                String message = in.readLine();

                if (message == null) {
                    System.out.println("[⚙️💰 RATING ] : Connection closed by Parsing Process ❌🔌");
                    break;
                }
                String [] Msg = message.split(":");
                if (Msg[0].equals("START_RATING")) 
                {
                    int FileID = Integer.parseInt(Msg[1]);
                    System.out.println("══════════════════════════════════════════════════════════════════════════════");
                    System.out.println("[⚙️💰 RATING ] : Starting Rating Money ... 💰📞");  
                    System.out.println("══════════════════════════════════════════════════════════════════════════════");
                    Rating RatingProcess = new Rating(FileID);
                    System.out.println("[⚙️💰 RATING ] : Rating Process In Prograss ⚙️ 💰⏳");
                    RatingProcess.StartRating(RatingProcess.Rating_Get_File_ID());
                    System.out.println("[⚙️💰 RATING ] : Rating Process Has Been Done For File ID  : (" + RatingProcess.Rating_Get_File_ID()+ ") 💰✅");
                    out.println("RATING_DONE");
                    System.out.println("[⚙️💰 RATING ] : Sent Done Flag To Parsing ✔️📤");
                }
            }

        } catch (Exception e) {
            System.out.println("Cannot connect to server. Make sure Parsing is running.");
        }
    }
}