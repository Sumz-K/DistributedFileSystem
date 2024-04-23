import java.io.*;
import java.util.*;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class DeleteHandler implements HttpHandler {
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
        
        JSONArray jarray = new JSONArray(body);
        JSONObject file_and_filename = jarray.getJSONObject(0);
        
        
        JSONArray new_array = new JSONArray();
        for(int i = 1 ; i <jarray.length() ;i++){
            new_array.put(jarray.getJSONObject(i));
        }
        
        String full_filename = file_and_filename.get("Name").toString();
        System.out.println("Full file name "+full_filename);
        

        System.out.println(new_array.toString());
        
        ArrayList<JSONObject> arrayList = convertJsonStringToArrayList(new_array.toString());
        String response = "";

        for (JSONObject obj : arrayList) {
            try {
                ArrayList<String> endpoints = getEndPoints(obj);
                deleteBlock(endpoints, obj.get("Hash").toString(),full_filename);
            } catch (Exception e) {
                System.out.println("Error in deleting");
            }
        }

        response = "Deleted ";
        sendResponse(exchange, response, 200);
    }

    private ArrayList<JSONObject> convertJsonStringToArrayList(String jsonString) {
        ArrayList<JSONObject> arrayList = new ArrayList<>();
        try {
            // Parse JSON string to JSONArray
            JSONArray jsonArray = new JSONArray(jsonString);

            // Convert JSONArray to ArrayList<JSONObject>
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                arrayList.add(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    private ArrayList<String> getEndPoints(JSONObject jsonObject) {
        // Create an empty ArrayList to store node values
        ArrayList<String> nodeValues = new ArrayList<>();

        Iterator<String> keys = jsonObject.keys();

        // Iterate over the keys
        while (keys.hasNext()) {
            String key = keys.next();
            // Check if the key contains the substring "Node"
            if (key.contains("Node")) {
                // If it does, add the corresponding value to the ArrayList

                try {
                    nodeValues.add(jsonObject.getString(key));
                } catch (Exception e) {
                    System.out.println("Error: while fetching url" + e);
                }
            }
        }
        return nodeValues;
    }

    private void deleteBlock(ArrayList<String> endpoints, String Filename,String full_filename) {
        Request request = new Request();
        for (String endpoint : endpoints) {
            System.out.println("Status code = " + request.statusCode);
            
            request.get(endpoint + "/delete/?filename=" + Filename);
            Mongo_db mongo_db = new Mongo_db();
            mongo_db.update_occupied("Storage", "Datanode info", "endpoint", 70);
            Mongo_db newone = new Mongo_db();
            System.out.println(Filename);
            newone.remove_node_mapping("Storage", "Blocks to node mapping", full_filename);
        }
    }

    private void sendResponse(HttpExchange exchange, String response, int StatusCode) throws IOException {
        exchange.sendResponseHeaders(StatusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
