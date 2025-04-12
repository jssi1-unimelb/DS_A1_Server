import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DictionaryServer {
    private final static int MAX_THREADS = 10;
    private final static int TIMEOUT = 1;
//    private final static int TIMEOUT = 60 * 1000;

    // Store workers in an array to prevent garbage collection from destroying the thread
    private final static Worker[] workers = new Worker[MAX_THREADS];

    public static void main(String args[]) throws IOException {
        WordDictionary dict = new WordDictionary(args[4]);
        ConnectionPool pool = new ConnectionPool(MAX_THREADS);
        for(int i = 0; i < MAX_THREADS; i++) {
            Worker newWorker = new Worker(dict, pool);
            newWorker.start();
            workers[i] = newWorker;
        }
        int port = Integer.parseInt(args[3]);

        try (ServerSocket server = new ServerSocket(port))
        {
            System.out.println("Server is online\n");

            while(true) {
                try (Socket socket = server.accept()) {
                    // Listen for connections
                    socket.setSoTimeout(TIMEOUT);

                    // Add connection to connection pool to be served
                    boolean connected = pool.connect(socket);

                    if(!connected) { // Server is busy
                        try {
                            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                            dos.writeUTF("Server is busy, please try again later");
                            dos.close();
                            socket.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
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