// Jiachen Si 1085839
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DictionaryServer {
    private final static int MAX_THREADS = 10;
    private final static int TIMEOUT = 5 * 60 * 1000; // 5 minute timeout

    // Store workers in an array to prevent garbage collection from destroying the thread
    private final static Worker[] workers = new Worker[MAX_THREADS];

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);

        try (ServerSocket server = new ServerSocket(port))
        {
            WordDictionary dict = new WordDictionary(args[1]);
            ConnectionPool pool = new ConnectionPool(MAX_THREADS);
            for(int i = 0; i < MAX_THREADS; i++) {
                Worker newWorker = new Worker(dict, pool);
                newWorker.start();
                workers[i] = newWorker;
            }

            System.out.println("Server is online\n");

            while(true) {
                // Listen for connections
                Socket socket = server.accept();
                socket.setSoTimeout(TIMEOUT);

                // Add connection to connection pool to be served
                boolean connected = pool.connect(socket);

                if(!connected) { // Server is busy
                    socket.close();
                }
            }
        } catch(IOException ioe) {
            System.out.println("IOException: " + ioe.getMessage());
        } catch(SecurityException se) {
            System.out.println("Security exception: " + se.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Runtime Exception: " + e.getMessage());
        }
    }
}