import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class manage_server {
    int replication_factor = 3;
    // this is a facade 
    public void startServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(getip(), 8080), 0);
        server.createContext("/writefile", new WrtieHandler());
        server.createContext("/readfile", new ReadHandler());
        server.createContext("/deletefile", new DeleteHandler());
        server.start();
        System.out.println("Server running on port 8080");
    }
     private String getip() {
        try {
            // Get the local host
            InetAddress inetAddress = InetAddress.getLocalHost();

            // Get the IP address
            String ipAddress = inetAddress.getHostAddress();

            return ipAddress;
        } catch (Exception e) {
            System.out.println("Unable to get the IP address: " + e.getMessage());
        }
        return "";
    }
    
}