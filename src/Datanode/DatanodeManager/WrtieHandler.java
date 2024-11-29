import java.io.*;
import java.util.*;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

public class WrtieHandler implements HttpHandler {
    int replication_factor = 3;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            handlePostRequest(exchange);
        }   
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {

        // filename:data   
        BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            requestBody.append(line).append("\n");
        }
        br.close();

        // Print the request body
        System.out.println("Received POST request body:");
        String body = requestBody.toString();
        System.out.println(body);
        // filename : data :[node1,node2,node3] 
        //parsing the request body
        // filename:base64data:array
        String[] lines = body.split(":",3);
        byte[] array = Base64.getDecoder().decode(lines[1].trim());
        ArrayList<String> datanodes = stringToArraylist(lines[2].trim());
        String filename = lines[0];

        
        hash_and_file obj = new hash_and_file(array);
        Map<String, byte[]> mapping = new HashMap<>();
        mapping = obj.split_and_give_me();
        FileSplitting filehashing = new FileSplitting();
        String file_hash = filehashing.getFilehash(array);

        
        Map<String, ArrayList<String>> file_block_locations = delivery(mapping, datanodes);

        
        // System.out.println(filename + ":" + file_hash);
        //hash->128kb block
        
        System.out.println("Successfully written now writing into mongo database");
        Mongo_db mongo_db = new Mongo_db();
        String response = mongo_db.insertIntoMongo("Storage", "Blocks to node mapping", file_block_locations, filename,
                file_hash);
        
        // Send a response

        
        sendResponse(exchange, response, 200);

    }

    private ArrayList<String> stringToArraylist(String input) {
        // Remove square brackets and whitespace
        ArrayList<String> arrayList = new ArrayList<>();

        String stringWithoutBrackets = input.substring(1, input.length() - 1).trim();
        String[] array = stringWithoutBrackets.split("\\s*,\\s*");
        for (String element : array) {
            arrayList.add(element);
        }
        return arrayList;
    }
    
    private Map<String, ArrayList<String>> delivery(Map<String, byte[]> map, ArrayList<String> datanodes) {
        
        Random random = new Random();
        Mongo_db mongo_db = new Mongo_db();
        // mongo_db.update_occupied("Storage", "Datanode info", node,size);

        System.out.println("in delivery");
        Map<String, ArrayList<String>> block_to_location = new HashMap<>();
        int count = 0;
        // replication_factor = datanodes.size();
        for (Map.Entry<String, byte[]> entry : map.entrySet()) {

            ArrayList<String> copy = DeepCopy(datanodes);
            ArrayList<String> visited = new ArrayList<String>();

            for (int i = 0; i < replication_factor; i++) {
                Request obj = new Request();
                int randomIndex = random.nextInt(copy.size());
                String random_url;
                JSONObject post_json = new JSONObject();
                String filename = entry.getKey();
                byte[] value = entry.getValue();
                if (visited.size() == datanodes.size()) {
                    randomIndex = random.nextInt(visited.size());
                    random_url = visited.get(randomIndex);
                }
                else {
                    random_url = copy.get(randomIndex);
                    mongo_db.update_occupied("Storage", "Datanode info", random_url, -value.length);
                }
                
                try {
                    post_json.put("contents", Base64.getEncoder().encodeToString(value));
                    post_json.put("filename", filename);
                    System.out.println("random url "+random_url);
                    System.out.println("filename"+filename);
                    
                    obj.post(random_url, post_json);
                    // System.out.println(object.reply_in_text());
                } catch (Exception e) {
                    System.out.println("Error in delivery");
                }
                visited.add(copy.get(randomIndex));
                copy.remove(randomIndex);
            }
            block_to_location.put("Block " + count++ + "-" + entry.getKey(), visited);
        }
        return block_to_location;
    }

    private ArrayList<String> DeepCopy(ArrayList<String> list) {
        ArrayList<String> copiedList = new ArrayList<>();
        for (String item : list) {
            copiedList.add(new String(item));
        }
        return copiedList;
    }

    private void sendResponse(HttpExchange exchange, String response, int StatusCode) throws IOException {
        exchange.sendResponseHeaders(StatusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
