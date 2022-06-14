package org.example;

import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.NoSuchElementException;

public class Database extends ListenerAdapter {
    //simple database connection
    public static MongoCollection collection;
    @Override
    public void onReady(ReadyEvent e){
        String uri = System.getenv("uri");
        MongoClientURI clientURI = new MongoClientURI(uri);
        MongoClient client = new MongoClient(clientURI);
        MongoDatabase database = client.getDatabase("hunt");
        collection = database.getCollection("hunt");

    }



    public static void set(String Id, String Key, String value){
        try{
            updateDB(Id, Key, value);
        }catch (Exception exception){
            createDB(Id);
            updateDB(Id,Key, value);
        }
    }

    public static Document get(String Id) throws InterruptedException {
        try{
            return (Document) collection.find(new Document("serverId", Id)).cursor().next();
        } catch (Exception exception){
            createDB(Id);
            Thread.sleep(500);
            return (Document) collection.find(new Document("serverId", Id)).cursor().next();
        }
    }

    public static void addUpdate(String serverId, String key, String value){

        Document document = null;
        try{
            document = (Document) collection.find(new Document("serverId", serverId)).cursor().next();
        }catch (NoSuchElementException exception){
            createDB(serverId);
        }
        Document Updatedocument;
        try{
             Updatedocument = new Document(key, value + document.get(key));
        }catch (Exception exception){
             Updatedocument = new Document(key, value);
        }
        Bson updateKey = new Document("$set", Updatedocument);
        collection.updateOne(document, updateKey);

    }



    public static void createDB(String Id){

        Document document = new Document("serverId", Id);
        document.append("triggers", "");
        //database template will go here
        collection.insertOne(document);

    }


    public static void updateDB(String Id, String key, String value){
        Document document = null;
        try{
            document = (Document) collection.find(new Document("serverId", Id)).cursor().next();
        }catch (NoSuchElementException exception){
            createDB(Id);
        }
        Document Updatedocument = new Document(key, value);
        Bson updateKey = new Document("$set", Updatedocument);
        collection.updateOne(document, updateKey);
    }
}
