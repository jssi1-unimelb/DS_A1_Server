import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DictionaryServer {
    public static void main(String args[]) throws IOException {
        WordDictionary dict = new WordDictionary(args[4]);

        try {
            int port = Integer.parseInt(args[3]);
            ServerSocket server = new ServerSocket(port);
            System.out.println("Server is online\n");
            while(true) {
                // Open server to connections, block
                Socket socket = server.accept();

                // Open the input and output stream
                OutputStream socketOut = socket.getOutputStream(); // Send to the client
                DataOutputStream dos = new DataOutputStream(socketOut);
                InputStream socketIn = socket.getInputStream();    // Receive from the Client
                DataInputStream dis = new DataInputStream(socketIn);

                // Keep the connection open until given the close command
                boolean end = false;
                while(!end) {
                    String clientRequest = dis.readUTF().toLowerCase();
                    if(clientRequest.equals("exit")) {
                        end = true;
                    } else {
                        String response = dict.handleRequest(clientRequest);
                        dos.writeUTF(response);
                    }
                }
                System.out.println("Connection Closed");

                // Close the connection
                dos.close();
                socketOut.close();
                socket.close();
            }
        } catch(IOException ioe) {
            System.out.println("Invalid initial file provided");
        } catch(SecurityException se) {
            System.out.println("Security Exception: " + se);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Port Provided");
        }
    }
}