import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer implements Runnable{
    protected int serverPort;
    protected ServerSocket serverSocket;
    // protected boolean isStop;

    public WebServer(int port){
        this.serverPort = port;
    }

    @Override
    public void run() {
        openServerSocket();
        System.out.println("Start listening on port " + this.serverPort);
        Socket clientSocket;
        while(true){
            try {
                clientSocket = serverSocket.accept();
                // System.out.println("Connecting with " + clientSocket);
            }catch (IOException e){
                throw new RuntimeException("Error while connecting to client");
            }

            new Thread(
                    new WorkerRunable(clientSocket, "MultiThreadServer")
            ).start();
        }

    }
/*
    private  void stop(){
        this.isStop = true;
        try {
            this.serverSocket.close();
        }catch (IOException e){
            throw new RuntimeException("Error while closing server", e);
        }
    }
    */

    private void openServerSocket(){
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        }catch (IOException e){
            throw new RuntimeException("Cannot open port " + this.serverPort , e);
        }
    }
}
