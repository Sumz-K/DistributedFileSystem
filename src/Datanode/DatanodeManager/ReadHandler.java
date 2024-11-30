import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;   

public class ReadHandler implements HttpHandler {
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
        ArrayList<JSONObject> arrayList = convertJsonStringToArrayList(body);
        
        String response = "";
        String[] b64_stringArray = new String[arrayList.size()];
        for (JSONObject obj : arrayList) {
            try {
                ArrayList<String> endpoints = getEndPoints(obj);
                
                String base64encoded_string = retriveBlock(endpoints, obj.get("Hash").toString());
                int index = getNum(obj.get("BlockID").toString());

                System.out.println(
                        "index = " + index + " endpoints = " + endpoints + " encoded string= " + base64encoded_string);
                if (base64encoded_string == null) {
                    response = "Error";
                    sendResponse(exchange, response, 404);
                    return;
                }
                b64_stringArray[index] = base64encoded_string;
            } catch (Exception e) {
                System.out.println("Error in reading");
            }
        }
        int len = 0;
        for (String encString : b64_stringArray) {
            byte[] bytes1 = Base64.getDecoder().decode(encString);
            len += bytes1.length;
            
        }
        byte[] combinedBytes = new byte[len];
        int mark = 0;
        for (String encString : b64_stringArray) {
            byte[] bytes1 = Base64.getDecoder().decode(encString);
            System.arraycopy(bytes1, 0, combinedBytes, mark, bytes1.length);
            mark += bytes1.length;
        }
        
        response = Base64.getEncoder().encodeToString(combinedBytes);
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

    private int getNum(String input) {
        // Regular expression to match numeric part
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);

        // Find and return the numeric part as an integer
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        // Return 0 if no numeric part found
        return 0;
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
                    System.out.println("Error: while fetching url"+e);
                }
            }
        }
        return nodeValues;
    }

    private String retriveBlock(ArrayList<String> endpoints, String Filename) {
        Request request = new Request();
        for (String endpoint : endpoints) {
            System.out.println("Status code = " + request.statusCode);
            request.get(endpoint + "?filename=" + Filename);
            if (request.statusCode == 200) {
                
                return request.reply_in_text();
            }
        }
        return null;
    }

    private void sendResponse(HttpExchange exchange, String response, int StatusCode) throws IOException {
        exchange.sendResponseHeaders(StatusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
