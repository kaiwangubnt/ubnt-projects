/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mfiusergrowth;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kaiwangubiquiti
 */
public class MfiUserGrowth {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
            // Open mongodb connection
            Mongo m = new Mongo("localhost", 27017);
            DB db = m.getDB("ace");

            DBCollection coll = db.getCollection("admin");
            System.out.println("admin count: " + coll.getCount());

            Calendar cal = Calendar.getInstance();
            int startYear = 2012;
            int startMonth = 9;

            Date now = new Date();
            cal.setTime(now);
            int endYear = cal.get(Calendar.YEAR);
            int endMonth = cal.get(Calendar.MONTH);

            int monthes = 12 * (endYear - startYear) + (endMonth - startMonth);
            int[] counter = new int[monthes + 1];

            
            
            DBCursor cursor = coll.find();
            try {
                while (cursor.hasNext()) {
                    DBObject myDoc = cursor.next();
                    if (myDoc.containsField("activate_ts")) {
                        Date date = (Date) myDoc.get("activate_ts");
                        cal.setTime(date);
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int diff = 12 * (year - startYear) + (month - startMonth);
                        counter[diff]++;
                        if(diff==3){
                            System.out.println(date);
//                            System.out.println("year: "+year+" month: "+month);
                        }
                        

                    } else {
                        System.out.println("no timestamp");
                        System.out.println("id: "+(String)myDoc.get("name"));
                    }
                }

                for (int i : counter) {
                    System.out.println("count:" + i);
                }
                
                List l = Arrays.asList(counter);

            } finally {
                cursor.close();
            }

        } catch (UnknownHostException ex) {
            Logger.getLogger(MfiUserGrowth.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
