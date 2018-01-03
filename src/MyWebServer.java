public class MyWebServer {
    public static void main(String[] args) throws InterruptedException{
        WebServer server = new WebServer(80);
        server.run();
    }
}
