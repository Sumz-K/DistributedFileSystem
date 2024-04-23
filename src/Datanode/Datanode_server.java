import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.net.URLDecoder;

class Datanode {
    private String name;
    private String ip;
    private double availableStorage;
    private boolean isAlive;
    private DatanodeStore store;

    public Datanode(String name, String ip, double availableStorage, DatanodeStore store) {
        this.name = name;
        this.ip = ip;
        this.availableStorage = availableStorage;
        this.isAlive = true;
        this.store = store;
    }
    
    public void delete(String filename){
        store.delete(filename);
    }
    
    public byte[] retrieveFile(String filename) {
        // System.out.print("here now\n");
        return store.retrieve(filename);
    }

    public String writeFile(byte[] contents, String location) {
        return store.writeFile(contents, location);
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public double getAvailableStorage() {
        return availableStorage;
    }

    public boolean isAlive() {
        return isAlive;
    }
}

class DatanodeStore {
    private String path;
    private String filename;

    public DatanodeStore(String basePath) {
        this.path = basePath;
    }

    public void delete(String filename){
         File file = new File(path+filename);
        file.delete();
    } 
    
    public byte[] retrieve(String file) {

        try {

            filename = path + file;
            System.out.print(filename);
            ProcessBuilder processBuilder = new ProcessBuilder("cat", filename);

            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            byte[] fileContents = outputStream.toByteArray();

            inputStream.close();
            outputStream.close();

            return fileContents;
        } catch (IOException e) {
            return null;
        }
    }

    public String writeFile(byte[] file, String location) {
        // location is where i write the file to
        // File outPutFile = File.createTempFile("temp-", "-unsplit", new
        // File(location));
        

            writehelper(file, location);

            return "Writing......";
        
    }

    private void writehelper(byte[] contents, String location) {
        try (FileOutputStream fos = new FileOutputStream(location)) {
            fos.write(contents);
            System.out.println("Data written to file successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class DatanodeServer {
    Datanode datanode;

    public DatanodeServer(Datanode datanode) {
        this.datanode = datanode;
    }

    public void startServer() throws Exception {
        // System.out.print(datanode.getIp());
        HttpServer server = HttpServer.create(new InetSocketAddress(datanode.getIp(), 8080), 0);
        server.createContext("/fetchfile", new ConnectionHandler());
        server.createContext("/fetchfile/delete", new DeleteHandler());
        server.start();
        System.out.println("Server running on port 8080");
    }

    class DeleteHandler implements HttpHandler{
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                handleDeleteRequest(exchange);
            } 
        }
        private void handleDeleteRequest(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().toString();
            // System.out.print(uri);

            // http://192.168.1.2:6000/fetchfile?filename=eight_kb.jpeg
            String file = "";
            String[] parts = uri.split("\\?", 2);
            if (parts.length == 2) {
                String params = parts[1];
                String[] key_val = params.split("=", 2);
                if (key_val.length == 2 && key_val[0].equals("filename")) {
                    try {
                        file = URLDecoder.decode(key_val[1], "UTF-8");
                        // System.out.print(file);
                    } catch (Exception e) {
                        System.out.print(e);
                    }
                }
            }
            System.out.println(file);
            datanode.delete(file);
            byte data[] = "Deleted".getBytes();
            sendResponse(exchange, data);
        
        }
        private void sendResponse(HttpExchange exchange, byte[] response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }
    
    class ConnectionHandler implements HttpHandler {
        // private Datanode datanode;
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                handlePostRequest(exchange);
            } else if ("GET".equals(exchange.getRequestMethod())) {
                handleGetRequest(exchange);
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().toString();
            // System.out.print(uri);

            // http://192.168.1.2:6000/fetchfile?filename=eight_kb.jpeg
            String file = "";
            String[] parts = uri.split("\\?", 2);
            if (parts.length == 2) {
                String params = parts[1];
                String[] key_val = params.split("=", 2);
                if (key_val.length == 2 && key_val[0].equals("filename")) {
                    try {
                        file = URLDecoder.decode(key_val[1], "UTF-8");
                        // System.out.print(file);
                    } catch (Exception e) {
                        System.out.print(e);
                    }
                }
            }
            System.out.println(file);
            byte[] data = datanode.retrieveFile(file);
            sendResponse(exchange, data);

        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {

            // filename:data
            System.out.print("Request received\n");
            InputStream requestBody = exchange.getRequestBody();
            BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
            StringBuilder requestBodyBuilder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                requestBodyBuilder.append(line);
            }
            String requestBodyString = requestBodyBuilder.toString();
            // System.out.print(requestBodyString);
            // System.out.print(requestBodyString);

            String writepath = "";
            String[] parts = requestBodyString.split(":", 2);

            System.out.println("parts array " + parts[0] + " " + parts[1]);

            String prefix_path = "/workspace/Datanode5/storage/";
            writepath = prefix_path+ parts[0];

            // byte[] contents = Base64.getDecoder().decode(parts[1]);
            byte[] contents = parts[1].getBytes();
            System.out.println(writepath);
            // Access the Datanode instance from the enclosing class DatanodeServer
            datanode.writeFile(contents, writepath);
            byte[] dummy_resp = {};
            sendResponse(exchange, dummy_resp);
        }

        private void sendResponse(HttpExchange exchange, byte[] response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }

    }
}

public class Datanode_server {

    public static String getip() {
        try {
            // Get the local host
            InetAddress inetAddress = InetAddress.getLocalHost();

            // Get the IP address
            String ipAddress = inetAddress.getHostAddress();

            return ipAddress;
        } catch (UnknownHostException e) {
            System.out.println("Unable to get the IP address: " + e.getMessage());
        }
        return "";
    }
    public static void main(String[] args) throws Exception {
        DatanodeStore store = new DatanodeStore("/workspace/Datanode5/storage/");
        System.out.println(getip());
        Datanode datanode = new Datanode("Node6", getip(), 100.0, store);
        DatanodeServer server = new DatanodeServer(datanode);
        server.startServer();
    }
}
