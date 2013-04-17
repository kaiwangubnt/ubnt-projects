/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mongoshardingmigration;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author kaiwangubiquiti
 */
public class MongoShardingMigration {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // data source
            MongoClient mongoSource = new MongoClient("ec2-107-21-70-147.compute-1.amazonaws.com", 37017);
            DB dbSource = mongoSource.getDB("ace");
            DBCollection collSource = dbSource.getCollection("hour");
            DBCursor cursor = collSource.find();
            
            // data target
            MongoClient mongoTarget = new MongoClient("localhost",27017);
            DB dbTarget = mongoTarget.getDB("ace");
            DBCollection collTarget = dbTarget.getCollection("hour");
            
            try {
                while (cursor.hasNext()) {
                    BasicDBObject sourceDoc = (BasicDBObject) cursor.next();
                    BasicDBObject targetDoc = (BasicDBObject)sourceDoc.clone();
                    
                    long timestamp = sourceDoc.getLong("time");
                    System.out.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(timestamp)));

                    targetDoc.put("time", new Date(timestamp));
                    collTarget.insert(targetDoc);
                }
            } finally {
                cursor.close();
            }
        } catch (UnknownHostException ex) {
        }
    }
}
