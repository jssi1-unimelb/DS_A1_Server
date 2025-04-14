// Jiachen Si 1085839
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

            try (
                // Open up read and write streams
                DataInputStream input = new DataInputStream(client.getInputStream());
                DataOutputStream output = new DataOutputStream(client.getOutputStream());
                ) {
                output.writeUTF(GsonUtil.gson.toJson(new Response("connected to server")));
                // Keep the connection open until given the close command
                boolean end = false;
                while(!end) {
                    // Listen for client requests
                    String requestRaw = input.readUTF();
                    Request request = GsonUtil.gson.fromJson(requestRaw, Request.class);

                    Response response = null;
                    if(request.command.equals("exit")) {
                        end = true;
                        response = new Response("connection closed");
                        response.setUnavailable();
                        pool.killConnection();
                    } else {
                        response = dict.handleRequest(request);
                    }
                    String responseJson = GsonUtil.gson.toJson(response);
                    output.writeUTF(responseJson);
                }
            } catch(IOException ioe) {
                System.out.println("IOException: " + ioe.getMessage());
                pool.killConnection();
            }
        }
    }
}
