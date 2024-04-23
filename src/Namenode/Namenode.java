import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import com.mongodb.client.FindIterable;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Exchanger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.SortOrder;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

interface DatabaseConnectionFactory{
    DatabaseConnection createConnection(String connectionString);
}


interface DatabaseConnection {
    HashMap<String,ArrayList<String>> getNodedetails();
    MongoClientSettings connect_todb();
    HashMap<String,List<String>> fetch_blockdetails(MongoClientSettings settings,String filename);
}



class MongoDBConnection implements DatabaseConnection {
    private String connectionString;

    public MongoDBConnection(String connectionString) {
        this.connectionString = connectionString;
    }                                                                                         


    public MongoClientSettings connect_todb(){
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        return settings;
    }

    public HashMap<String,List<String>> fetch_blockdetails(MongoClientSettings settings,String filename){
        HashMap<String,List<String>> details=new HashMap<>();
        try(MongoClient mongoclient=MongoClients.create(settings)){
            MongoDatabase database=mongoclient.getDatabase("Storage");
            MongoCollection<Document> collection=database.getCollection("Blocks to node mapping");


            //System.out.print("count "+collection.countDocuments());

            FindIterable<Document> iterDoc = collection.find();

            
            System.out.print(filename+"\n");
            for(Document doc:iterDoc){
                String f=doc.get("Filename").toString();
                //System.out.print(f+" ");

                if(f.trim().equals(filename.trim())){
                    ArrayList<String> props=new ArrayList<>();
                    //System.out.print(doc.get("data"));
                    Object dataObject = doc.get("data");
                    Document data_doc=new Document();
                    if (dataObject instanceof Document) {
                        data_doc = (Document) dataObject;
                    }

                    for (Map.Entry<String, Object> entry : data_doc.entrySet()) {
                        String blockId = entry.getKey();
                        Document blockDocument = (Document) entry.getValue();
        
                        List<String> urlList = new ArrayList<>();
                        for (Map.Entry<String, Object> urlEntry : blockDocument.entrySet()) {
                            String hash=urlEntry.getKey();
                            List<String> urls = (List<String>) urlEntry.getValue();
                            urlList.add(hash);
                            urlList.addAll(urls);
                        }
            


                        details.put(blockId,urlList);
                        
                    }
            
                }

            }
            


        }
        catch(Exception e){
            System.out.print(e.getStackTrace());
            
        }
        return details;

        
    }


    @Override
    public HashMap<String,ArrayList<String>> getNodedetails() {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings=connect_todb();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("Storage");
            MongoCollection<Document> collection = database.getCollection("Datanode info");
            
            FindIterable<Document> iterDoc = collection.find();
            HashMap<String,ArrayList<String>> details=new HashMap<>();

            for (Document doc : iterDoc) {

                String name=doc.get("Name").toString();
                String endpoint=doc.get("Endpoint").toString();
                String available_storage=doc.get("AvailableStorage").toString();

                ArrayList<String>props=new ArrayList<>();
                props.add(endpoint);
                props.add(available_storage);

                details.put(name, props);

            }
            

            
            return details;

        } catch (MongoException e) {
            System.out.println("Error in connecting to MongoDB database");
            throw e;
        }
        
    }
}


class MongoDBConnectionFactory implements DatabaseConnectionFactory {
    private static MongoDBConnectionFactory instance;

    private MongoDBConnectionFactory() {
        
    }

    public static synchronized MongoDBConnectionFactory getInstance() {
        if (instance == null) {
            instance = new MongoDBConnectionFactory();
        }
        return instance;
    }

    @Override
    public DatabaseConnection createConnection(String connectionString) {
        return new MongoDBConnection(connectionString);
    }
}




abstract class RequestHandler{
    protected RequestHandler nextHandler;
    public void setNextHandler(RequestHandler nextHandler){
        this.nextHandler=nextHandler;
    }

    public abstract void handleRequest(HttpExchange exchange,DatabaseConnection databaseConnection);

    protected void sendResponse(HttpExchange exchange,int StatusCode,String response) {
        try{
        exchange.sendResponseHeaders(StatusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
        }catch(IOException e){
            System.out.print(e.getStackTrace());
        }
    }
}


class GetRequestHandler extends RequestHandler{
    @Override
    public void handleRequest(HttpExchange exchange,DatabaseConnection databaseConnection){
        if("GET".equals(exchange.getRequestMethod())){
            handleGetRequest(exchange,databaseConnection);
        }
        else if(nextHandler!=null){
            nextHandler.handleRequest(exchange,databaseConnection);
        }
        else{
            System.out.print("this method isnt supported\n");
        }
    }

    private void handleGetRequest(HttpExchange exchange,DatabaseConnection databaseConnection){
        String url=exchange.getRequestURI().toString();
        //System.out.print(url);
        String filename="";
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("filename")) {
                        filename = keyValue[1];
                        break; 
                    }
                }
            } else {
                System.out.println("No query parameters found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.print(filename);

        MongoClientSettings settings=databaseConnection.connect_todb();


        HashMap<String,List<String>> details=databaseConnection.fetch_blockdetails(settings,filename);

        JSONArray arr=new JSONArray();

        for (Map.Entry<String, List<String>> entry : details.entrySet()){
            String key=entry.getKey();
            List<String> value=entry.getValue();

            //System.out.print(value.get(0)+"\n");

            JSONObject jo=new JSONObject();
            
            try{
                jo.put("BlockID", key);
                jo.put("Hash", value.get(0));
                for(int i=1;i<value.size();i++){
                    String datanode=value.get(i);

                    //System.out.print(datanode+"\n");
                    
                    String nodenum="Node"+i;
                    jo.put(nodenum, datanode);

                }
                arr.put(jo);

            }catch(JSONException j){
                System.out.print(j.getStackTrace());
            }

            
            

        }



        for (int i = 0; i < arr.length(); i++) {
            try {
                
                JSONObject obj = arr.getJSONObject(i);
                

                System.out.println(obj.toString()+"\n");
            } catch (JSONException e) {
                
                e.printStackTrace();
            }
        }
        
        
        // System.out.println("TOstring "+arr.toString());
        System.out.println("Posting");
        Request req = new Request();
        req.post("https://datanode-manager-ckusr.run-ap-south1.goorm.site/readfile",arr);
        System.out.println("get request response"+req.reply_in_text());
        





        sendResponse(exchange, 200,req.reply_in_text());
    }
    
}



class PostRequestHandler extends RequestHandler{
    @Override
    public void handleRequest(HttpExchange exchange,DatabaseConnection databaseConnection){
        if("POST".equals(exchange.getRequestMethod())){
            handlePostRequest(exchange, databaseConnection);
        }
        else if(nextHandler!=null){
            nextHandler.handleRequest(exchange,databaseConnection);
        }
        else{
            System.out.print("This method isnt supported\n");
        }
    }
    private void handlePostRequest(HttpExchange exchange,DatabaseConnection databaseConnection) {
        
        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        StringBuilder requestBody = new StringBuilder();
        String line;
        try{
        while ((line = br.readLine()) != null) {
            requestBody.append(line).append("\n");
        }
        br.close();
    }catch(IOException e){
        System.out.print(e.getStackTrace());
    }
        String body = requestBody.toString();
        System.out.println("Received Post Body");
        System.out.println(body);

        HashMap<String,ArrayList<String>> details=databaseConnection.getNodedetails();
        ArrayList<String> useable_datanodes=new ArrayList<>();
        
        for (Map.Entry<String, ArrayList<String>> entry : details.entrySet()) {
            String key = entry.getKey();
            ArrayList<String> values = entry.getValue();

            double space=Double.parseDouble(values.get(1));
            if(space>=128 && useable_datanodes.size() < 3 ){
                useable_datanodes.add(values.get(0));
            }
            
        }
        for(String s:useable_datanodes){
            System.out.println(s);
        }

        sendDatanodeList(useable_datanodes,body);
        sendResponse(exchange, 200,"Successfully uploaded ");

    }
    private void sendDatanodeList(ArrayList<String> list,String body){
        String endpoint="https://datanode-manager-ckusr.run-ap-south1.goorm.site/writefile";
        Request req=new Request();
        JSONObject json=new JSONObject();
        String[] lines = body.split(":", 2);
        String file_name=lines[0];
        String file_content=lines[1];
        System.out.println(file_name);
        try{
        json.put("filename", file_name);
        json.put("contents", file_content);
        json.put("Endpoints", list.toString());
        System.out.println(list.toString());
        req.post(endpoint, json);
        System.out.println(req.reply_in_text());
        
        }
        catch(JSONException e){
            System.out.println(e.getStackTrace());
        }
    }
}



class DeleteRequestHandler extends RequestHandler{
    @Override
    public void handleRequest(HttpExchange exchange,DatabaseConnection databaseConnection){
        if("DELETE".equals(exchange.getRequestMethod())){
            handleDeleteRequest(exchange, databaseConnection);
        }
        else if(nextHandler!=null){
            nextHandler.handleRequest(exchange,databaseConnection);
        }
        else{
            System.out.print("This method isnt supported\n");
        }
    }
    private void handleDeleteRequest(HttpExchange exchange,DatabaseConnection databaseConnection){
        String url=exchange.getRequestURI().toString();
        //System.out.print(url);
        String filename="";
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("filename")) {
                        filename = keyValue[1];
                        break; 
                    }
                }
            } else {
                System.out.println("No query parameters found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.print(filename);

        MongoClientSettings settings=databaseConnection.connect_todb();


        HashMap<String,List<String>> details=databaseConnection.fetch_blockdetails(settings,filename);

        JSONArray arr=new JSONArray();
        JSONObject jod=new JSONObject();
        try{
            jod.put("Name", filename);
            arr.put(jod);
        }catch(JSONException j){
            System.out.print(j.getLocalizedMessage());
        }
        
        for (Map.Entry<String, List<String>> entry : details.entrySet()){
            String key=entry.getKey();
            List<String> value=entry.getValue();

            //System.out.print(value.get(0)+"\n");

            JSONObject jo=new JSONObject();
            
            try{
                jo.put("BlockID", key);
                jo.put("Hash", value.get(0));
                for(int i=1;i<value.size();i++){
                    String datanode=value.get(i);

                    //System.out.print(datanode+"\n");
                    
                    String nodenum="Node"+i;
                    jo.put(nodenum, datanode);

                }
                arr.put(jo);

            }catch(JSONException j){
                System.out.print(j.getStackTrace());
            }

            
            

        }
       
        System.out.println(arr.toString());
        Request req = new Request();
        req.post("https://datanode-manager-ckusr.run-ap-south1.goorm.site/deletefile",arr);
        
        




        sendResponse(exchange, req.statusCode,"Successfully deleted ");
    }
}
class NamenodeServer{
    
    
    private RequestHandler requestHandler;
    private DatabaseConnection databaseConnection;
    
    
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
    
    
    public NamenodeServer(RequestHandler requestHandler,DatabaseConnection databaseConnection){
        this.requestHandler=requestHandler;
        this.databaseConnection=databaseConnection;
    }
    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(getip(), 6000), 0);
        server.createContext("/receive", new ConnectionHandler());
        server.start();
        System.out.println("Server running on port 6000");
    }

    class ConnectionHandler implements HttpHandler{

        public void handle(HttpExchange exchange) throws IOException {
            requestHandler.handleRequest(exchange,databaseConnection);
        }
    }
}

public class Namenode {
    public static void main(String[] args) throws IOException{
        String mongoConnectionString = "mongodb+srv://Datanode_Manager:Datanode_Manager_Passcode@storage.rt8sdgl.mongodb.net/?retryWrites=true&w=majority&appName=Storage";

        DatabaseConnectionFactory factory=MongoDBConnectionFactory.getInstance();
        DatabaseConnection databaseConnection=factory.createConnection(mongoConnectionString);

        RequestHandler getHandler=new GetRequestHandler();
        RequestHandler postHandler=new PostRequestHandler();
        RequestHandler deleteHandler=new DeleteRequestHandler();

        getHandler.setNextHandler(postHandler);
        postHandler.setNextHandler(deleteHandler);

        NamenodeServer server=new NamenodeServer(getHandler,databaseConnection);
        server.startServer();
    }
}
