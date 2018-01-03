import java.io.*;
import java.net.Socket;

public class WorkerRunable implements Runnable{
    protected String serverResponse = null;
    protected Socket clientSocket = null;


    public WorkerRunable(Socket client, String Content){
        this.serverResponse = Content;
        this.clientSocket = client;
    }

    @Override
    public void run() {
        try {
            HttpRequest request = new HttpRequest(this.clientSocket.getInputStream());
            HttpResponse response = new HttpResponse(request);
            response.response(this.clientSocket.getOutputStream());
        }catch (IOException e){
            throw new RuntimeException("Runtime Error");
        }catch (NullPointerException e){
            e.printStackTrace();
            System.out.println("Null Exception " + e.getMessage());
        }finally {
            try {
                this.clientSocket.close();
                // System.out.println("Socket closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
