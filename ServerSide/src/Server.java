import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private final int serverPort;
    private ArrayList<ServerWork> workerList = new ArrayList<>();

    public Server(int serverPort) {
        this.serverPort = serverPort;
    }

    public List<ServerWork> getWorkerList() {
        return workerList;
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);

            while(true) {
                System.out.println("Accepting connection");
                Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket);
                ServerWork worker = new ServerWork( this, clientSocket );
                workerList.add(worker);
                worker.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeWorker(ServerWork serverWork) {
        workerList.remove(serverWork);
    }
}
