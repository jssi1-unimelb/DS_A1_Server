import java.net.Socket;
import java.util.LinkedList;

public class ConnectionPool {
    private final LinkedList<Socket> queuedConnections;
    private volatile int concurrentConnections;
    private final int maxConnections;

    public ConnectionPool(int maxConnections) {
        this.queuedConnections = new LinkedList<>();
        this.concurrentConnections = 0;
        this.maxConnections = maxConnections;
    }

    // Queues up connection request
    public synchronized boolean connect(Socket socket) {
        if(concurrentConnections == maxConnections) {
            return false; // Server is busy
        }
        queuedConnections.add(socket); // Add to queue
        notifyAll(); // Notify sleeping threads that a new connection is available
        return true;
    }

    public synchronized Socket getConnectionRequest() {
        while(queuedConnections.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Connection picked up by thread");
        concurrentConnections++;
        return queuedConnections.removeFirst();
    }

    public synchronized void killConnection() {
        concurrentConnections--;
    }
}
