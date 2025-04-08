import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DictionaryServer {
    public static void main(String args[]) throws IOException {
        WordDictionary dict = new WordDictionary();

        try {
            ServerSocket server = new ServerSocket(1234);
            while(true) {
                System.out.println("Server open for business");
                Socket socket = server.accept(); // Open server to connections, block
                System.out.println("Server has accepted a connection");

                // Get the output stream associated with the socket
                OutputStream socketOut = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(socketOut);

                // Send a msg
                dos.writeUTF("You motherfucker");

                // Get the input stream
                InputStream socketIn = socket.getInputStream();
                DataInputStream dis = new DataInputStream(socketIn);

                String request = dis.readUTF();
                System.out.println(request);

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