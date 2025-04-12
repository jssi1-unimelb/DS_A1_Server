import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Worker extends Thread {
    private final WordDictionary dict;
    private final ConnectionPool pool;

    public Worker(WordDictionary dict, ConnectionPool pool) {
        this.dict = dict;
        this.pool = pool;
    }

    public void run() {
        while(true) {
            Socket client = pool.getConnectionRequest();

            try {
                // Open up read and write streams
                DataOutputStream output = new DataOutputStream(client.getOutputStream());
                DataInputStream input = new DataInputStream(client.getInputStream());

                // Keep the connection open until given the close command
                boolean end = false;

                while(!end) {
                    String clientRequest = input.readUTF().toLowerCase(); // Listen for client requests
                    if(clientRequest.equals("exit")) {
                        end = true;
                    } else {
                        String response = dict.handleRequest(clientRequest);
                        output.writeUTF(response);
                    }
                }

                // Sever client connection
                input.close();
                output.close();
                client.close();
                pool.killConnection();

            } catch(IOException ioe) {
                System.out.println("IOException: " + ioe.getMessage());
            }

            // Need to add exception to send the client side a message when the connection times out.
        }
    }
}
