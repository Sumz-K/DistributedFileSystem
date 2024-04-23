import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.crypto.Data;

import org.bson.Document;
import org.json.JSONException;

public class Mongo_db {
    String connectionString;
    ServerApi serverApi;
    MongoClientSettings settings;

    Mongo_db() {
        this.connectionString = "mongodb+srv://Datanode_Manager:Datanode_Manager_Passcode@storage.rt8sdgl.mongodb.net/?retryWrites=true&w=majority&appName=Storage";
        this.serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        this.settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();
    }

    String insertIntoMongo(String Database, String dbCollection, Map<String, ArrayList<String>> dict, String filename,
            String file_hash) {
        String ret;
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase(Database);
                MongoCollection<Document> collection = database.getCollection(dbCollection);
                Document doc = new Document("Filename", filename);
                doc.append("Hash", file_hash);
                Document data = new Document();
                try {
                    fillin_data(data, dict);
                } catch (Exception e) {
                    System.out.println("Error in fillin_data");
                }
                doc.append("data", data);
                collection.insertOne(doc);
                ret = "Success";
                return ret;
            } catch (MongoException e) {
                //System.out.println("error in connecting to mongo database");
                ret = "Error: " + e.getMessage();
            }
        } catch (MongoException e) {
            ret = "Error: " + e.getMessage();
        }
        return ret;

    }
    String remove_node_mapping(String Database, String dbCollection, String filename) {
        String ret;
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase(Database);
                MongoCollection<Document> collection = database.getCollection(dbCollection);
               collection.deleteOne(Filters.eq("Filename", filename));

                ret = "Success";
                return ret;
            } catch (MongoException e) {
                //System.out.println("error in connecting to mongo database");
                ret = "Error: " + e.getMessage();
            }
        } catch (MongoException e) {
            ret = "Error: " + e.getMessage();
        }
        return ret;

    }

    
    public void update_occupied(String Database,String dbcollection, String node, int size ){
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase(Database);
                MongoCollection<Document> collection = database.getCollection(dbcollection);
                

                collection.updateOne(
                        Filters.eq("Endpoint", node.trim()), // Query condition
                        Updates.inc("AvailableStorage", size) // Decrement age field by decrementBy value
                );
                
                
            } catch (MongoException e) {
                // System.out.println("error in connecting to mongo database");
                System.out.println("Error: " + e.getMessage());
            }
        } catch (MongoException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    private void fillin_data(Document obj, Map<String, ArrayList<String>> dict)
            throws IOException, JSONException {
        System.out.println("populating");
        for (String key : dict.keySet()) {
            String[] keys = key.split("-", 2);
            Document doc1 = new Document(keys[1], dict.get(key));
            obj.append(keys[0], doc1);
        }

    }
}
