import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DictionaryServer {
    public static void main(String args[]) throws IOException {
        WordDictionary dict = new WordDictionary();

        try {
            ServerSocket server = new ServerSocket(1234);
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
            System.out.println("IOException: " + ioe);
            ioe.printStackTrace();
        } catch(SecurityException se) {
            System.out.println("Security Exceptiion: " + se);
            se.printStackTrace();
        }
    }
}