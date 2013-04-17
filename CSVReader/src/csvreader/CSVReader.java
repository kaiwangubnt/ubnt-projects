/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csvreader;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class CSVReader {
    final static String GEOLOCATION_FIELDS[] = {"id", "country", "region", "city", "zip", 
                                                "latitude", "longitude", "metro", "area"};
    final static String GEOBLOCKS_FIELDS[] = {"start", "end", "id"};
    /*
     * Remove double quote
     * "abc" -> abc
     */
    public static String parseString(String value) {
        if (!value.equals("") && value.charAt(0) == '"') {
            value = value.substring(1);
            if (value.length() > 0 && !value.equals("") && value.charAt(value.length() - 1) == '"') {
                value = value.substring(0, value.length() - 1);
            }
        }
        return value;
    }

    /*
     * If the string starts with left double quote
     * "ab, return true
     * ab, return false
     */
    public static boolean hasOnlyLeftDoubleQuote(String value, boolean hasLeft) {
        if (!hasLeft) {
            if (value.length() == 1 && value.charAt(0) == '"') {
                return true;
            } else if (value.length() > 1 && value.charAt(0) == '"') {
                return true;
            }
        }
        return false;
    }

    /*
     *  If the string ends with right double quote 
     *  ab", return true
     */
    public static boolean hasOnlyRightDoubleQuote(String value, boolean hasLeft) {
        if (hasLeft) {
            if (value.length() == 1 && value.charAt(0) == '"') {
                return true;
            } else if (value.length() > 1 && value.charAt(value.length() - 1) == '"') {
                //HACK!!! special case
                if (value.length() >= 4 && value.charAt(value.length() - 2) == '\\' && value.charAt(value.length() - 3) == '8' && value.charAt(value.length() - 4) == 's') {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * "ab", return true
     * ab, return true
     * "ab, or ab", return false
     */
    public static boolean correntValue(String value) {
        if (value.equals("")) {
            return true;
        } else if (value.length() == 1) {
            if (value.charAt(0) == '"') {
                return false;
            } else {
                return true;
            }
        } else {
            if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
                if (value.charAt(value.length() - 2) == '\\') {
                    if (value.charAt(value.length() - 3) == '\\') { // HACK!!! Special Case
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            } else if (value.charAt(0) != '"' && value.charAt(value.length() - 1) != '"') {
                return true;
            }
        }
        return false;
    }

    /*
     * a,"b,c,d","e" will be splitted to [a, "b,c,d", "e"]
     * "b,c,d" will not be splitted
     */
    public static String[] safeSplit(String row, String delimiter) {
        ArrayList<String> result = new ArrayList<String>();
        String[] arr = row.split(delimiter);
        int startPos = -1;
        boolean wrongSplit = false;

        for (int i = 0; i < arr.length; i++) {
            if (!wrongSplit && correntValue(arr[i])) {
                result.add(arr[i]);
            } else {
                if (hasOnlyLeftDoubleQuote(arr[i], wrongSplit)) {
                    startPos = i;
                    wrongSplit = true;
                } else if (hasOnlyRightDoubleQuote(arr[i], wrongSplit)) {
                    if (wrongSplit) {
                        // merge, insert to result
                        String str = "";
                        for (int j = startPos; j <= i; j++) {
                            if (j == startPos) {
                                str += arr[j];
                            } else {
                                str += "," + arr[j];
                            }
                        }
                        result.add(str);
                        wrongSplit = false;
                    }
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /*
     * Length of user name cannot purpass 15,
     * Character of username can only be letter(any language),
     * Digit, "_", or "-"
     */
    public static boolean isValidUsername(String username) {
        boolean goodUsername = true;
        if (username.length() > 15) {
            System.out.println("Bad Username Length: " + username.length());
            goodUsername = false;
        } else {
            for (int i = 0; i < username.length(); i++) {
                char c = username.charAt(i);
                if (!Character.isLetterOrDigit(c) && c != '_' && c != '-') {
                    goodUsername = false;
                    System.out.println("Bad Character: " + c);
                    break;
                }
            }
        }
        if (!goodUsername) {
            System.out.println("Bad Username: " + username);
        }
        return goodUsername;
    }
    
    // import GeoLocation to mongoDb
//    public static void importGeoLocation(String fileName) throws Exception {
//        if (fileName == null) {
//            fileName = "/Users/ying/GeoLiteCity_20121106/GeoLiteCity-Location.csv";
//        }
//        BufferedReader CSVFile = new BufferedReader(new FileReader(fileName));
//
//        // Open mongodb connection
//        Mongo m = new Mongo("localhost", 37017);
//        DB db = m.getDB("ace");
//
//        DBCollection coll = db.getCollection("geo_location");
//        coll.remove(new BasicDBObject());
//
//        CSVFile.readLine(); // Read the first line of data.
//        CSVFile.readLine(); // Read the second line of data.
//        String currRow = null;
//        while ((currRow = CSVFile.readLine()) != null) {
//            String fields[] = currRow.split(",");
//            for (int i = 0; i < fields.length; i++) {
//                fields[i] = parseString(fields[i]);
//            }
//            BasicDBObject doc = new BasicDBObject();
//            for (int i = 0; i < fields.length; i++) {
//                if (GEOLOCATION_FIELDS[i].equals("city")) {
//                    String city = new String(fields[i].getBytes(), "ISO-8859-1");
//                    doc.put(GEOLOCATION_FIELDS[i], city);
//                } else if (GEOLOCATION_FIELDS[i].equals("id")) {
//                    doc.put(GEOLOCATION_FIELDS[i], Long.parseLong(fields[i]));
//                } else {
//                    doc.put(GEOLOCATION_FIELDS[i], fields[i]);
//                }
//            }
//            coll.insert(doc);
//        }
//        
//        // create index
//        coll.createIndex(new BasicDBObject("id", 1));
//        coll.createIndex(new BasicDBObject("country", 1));
//        coll.createIndex(new BasicDBObject("city", 1));
//
//        // Close the file once all data has been read.
//        CSVFile.close();
//    }
    
    // import GeoBlocks to mongoDb
//    public static void importGeoBlocks(String fileName) throws Exception {
//        if (fileName == null) {
//            fileName = "/Users/ying/GeoLiteCity_20121106/GeoLiteCity-Blocks.csv";
//        }
//        BufferedReader CSVFile = new BufferedReader(new FileReader(fileName));
//
//        // Open mongodb connection
//        Mongo m = new Mongo("localhost", 37017);
//        DB db = m.getDB("ace");
//
//        DBCollection coll = db.getCollection("geo_blocks");
//        coll.remove(new BasicDBObject());
//
//        CSVFile.readLine(); // Read the first line of data.
//        CSVFile.readLine(); // Read the second line of data.
//        String currRow = null;
//        while ((currRow = CSVFile.readLine()) != null) {
//            String fields[] = currRow.split(",");
//            for (int i = 0; i < fields.length; i++) {
//                fields[i] = parseString(fields[i]);
//            }
//            BasicDBObject doc = new BasicDBObject();
//            for (int i = 0; i < fields.length; i++) {
//                doc.put(GEOBLOCKS_FIELDS[i], Long.parseLong(fields[i]));
//            }
//            coll.insert(doc);
//        }
//        
//        // create index
//        coll.createIndex(new BasicDBObject("id", 1));
//        coll.createIndex(new BasicDBObject("start", 1));
//        coll.createIndex(new BasicDBObject("end", 1));
//        BasicDBObject dbObj = new BasicDBObject();
//        dbObj.put("start", 1);
//        dbObj.put("end", 1);
//        coll.createIndex(dbObj);
//
//        // Close the file once all data has been read.
//        CSVFile.close();        
//    }
    
//    public static void importForumDb(String fileName) throws Exception {
//        if (fileName == null) {
//            fileName = "/Users/kaiwangubiquiti/vbb_1user.csv";
//        }
//        BufferedReader CSVFile = new BufferedReader(new FileReader(fileName));
//        // hashset used to check duplicated username or email
//        HashSet<String> usernames = new HashSet<String>();
//        HashSet<String> emails = new HashSet<String>();
//
//        // Open mongodb connection
//        Mongo m = new Mongo("localhost", 27017);
//        DB db = m.getDB("ace");
//
//        // User Collection
//        DBCollection coll_user = db.getCollection("valid_forum_user");
//        coll_user.remove(new BasicDBObject());
//
//        // "duplicated_username" collection
//        DBCollection coll_duplicated_username = db.getCollection("duplicated_username");
//        coll_duplicated_username.remove(new BasicDBObject());
//
//        // "duplicated_email" collection
//        DBCollection coll_duplicated_email = db.getCollection("duplicated_email");
//        coll_duplicated_email.remove(new BasicDBObject());
//
//        int line = 1;
//        int validUser = 0;
//        int duplicateName = 0;
//        int duplicateEmail = 0;
//        int badUsername = 0;
//
//        String firstRow = CSVFile.readLine(); // Read the first line of data.
//        if (firstRow != null) {
//            /* attributes of document in mongodb collection */
//            String[] attributes = firstRow.split(",");
//            for (int i = 0; i < attributes.length; i++) {
//                attributes[i] = parseString(attributes[i]);
//            }
//            System.out.println("Attributes number: " + attributes.length);
//
//            String valueRow = CSVFile.readLine(); // Read next line of data.
//            line++;
//            while (valueRow != null) {
//                String[] values = safeSplit(valueRow, ",");
//
//                if (values.length != attributes.length) {
//                    System.out.println("NO!!!!!!!!!!!!!!!!!!!!!!!!!!!!, length: " + values.length + " line: " + line);
//                } else {
//                    BasicDBObject doc = new BasicDBObject();
//                    for (int i = 0; i < attributes.length; i++) {
//                        doc.put(attributes[i], parseString(values[i]));
//                    }
//
//                    String username = doc.getString("username");
//                    String email = doc.getString("email");
//                    // Check if username is valid
//                    doc.put("validUsername", isValidUsername(username));
//                    if (!isValidUsername(username)) {
//                        badUsername++;
//                    }
//
//                    if (!usernames.contains(username)) {
//                        usernames.add(username);
//                        // Check Email
//                        if (!emails.contains(email)) {
//                            emails.add(email);
//                            coll_user.insert(doc);
//                            validUser++;
//                        } else {
//                            duplicateEmail++;
//                            // insert to collection
//                            coll_duplicated_email.insert(doc);
//
//                            System.out.println("line: " + line);
//                            System.out.println(email);
//                            System.out.println("Duplicate email: " + valueRow);
//                        }
//                    } else {
//                        duplicateName++;
//                        // insert into collection
//                        coll_duplicated_username.insert(doc);
//
//                        System.out.println("line: " + line);
//                        System.out.println(username);
//                        System.out.println("Duplicate username row: " + valueRow);
//                    }
//                }
//                valueRow = CSVFile.readLine();
//                line++;
//            }
//        }
//
//        // Close the file once all data has been read.
//        CSVFile.close();
//
//        // End the printout with a blank line.
//        System.out.println("=====================================");
//        System.out.println("Valid user: " + validUser);
//        System.out.println("Duplicate username: " + duplicateName);
//        System.out.println("Duplicate email: " + duplicateEmail);
//        System.out.println("Total record: " + (line - 2));
//        System.out.println("Bad Username: " + badUsername);
//    }
    
    public static void generateSSOForumDb(String fileName) throws Exception {
        if (fileName == null) {
            fileName = "/Users/kaiwangubiquiti/vbb_1user.csv";
        }
        BufferedReader CSVFile = new BufferedReader(new FileReader(fileName));
        // hashset used to check duplicated username or email
        HashSet<String> usernames = new HashSet<String>();
        HashSet<String> emails = new HashSet<String>();

        // Open mongodb connection
        Mongo m = new Mongo("localhost", 27017);
        DB db = m.getDB("ace");
        
        // SSO Collection
        DBCollection coll_sso = db.getCollection("valid_forum_user");
        coll_sso.remove(new BasicDBObject());

        int line = 1;
        int validUser = 0;

        String firstRow = CSVFile.readLine(); // Read the first line of data.
        if (firstRow != null) {
            /* attributes of document in mongodb collection */
            String[] attributes = firstRow.split(",");
            for (int i = 0; i < attributes.length; i++) {
                attributes[i] = parseString(attributes[i]);
            }
            System.out.println("Attributes number: " + attributes.length);

            String valueRow = CSVFile.readLine(); // Read next line of data.
            line++;
            while (valueRow != null) {
                String[] values = safeSplit(valueRow, ",");

                if (values.length != attributes.length) {
                    System.out.println("NO!!!!!!!!!!!!!!!!!!!!!!!!!!!!, length: " + values.length + " line: " + line + ", content: " + valueRow);
                } else {
                    BasicDBObject doc = new BasicDBObject();
                    BasicDBObject ssoDoc = new BasicDBObject();
                    for (int i = 0; i < attributes.length; i++) {
                        doc.put(attributes[i], parseString(values[i]));
                    }

                    String username = doc.getString("username");
                    String email = doc.getString("email");
                    ssoDoc.put("username", username);
                    ssoDoc.put("email", email);
                    UUID ssoId = UUID.randomUUID();
                    ssoDoc.put("ssoid", ssoId.toString());
                    ssoDoc.put("normalized_username", username.toLowerCase());
                    ssoDoc.put("validUsername", isValidUsername(username));
                    coll_sso.insert(ssoDoc);
                    validUser++;
                }
                valueRow = CSVFile.readLine();
                line++;
            }
        }

        // Close the file once all data has been read.
        CSVFile.close();

        // End the printout with a blank line.
        System.out.println("=====================================");
        System.out.println("Valid user: " + validUser);
    }
    
    public static void addNormalizedUserName() throws Exception {
     // Open mongodb connection
        Mongo m = new Mongo("localhost", 27017);
        DB db = m.getDB("ace");
        
        // forum user Collection
        DBCollection collForumUser = db.getCollection("valid_forum_user");
        DBCursor cursor = collForumUser.find();
        try {
            while(cursor.hasNext()) {
                BasicDBObject doc = (BasicDBObject) cursor.next();
                String userName = doc.getString("username");
                BasicDBObject updatedDoc = new BasicDBObject(doc);
                updatedDoc.put("normalized_username", userName.toLowerCase());
                collForumUser.update(doc, updatedDoc);
            }
        } finally {
            cursor.close();
        }
        System.out.println("Done adding normalized username field");
    }
    
    public static void addNormalizedEmail() throws Exception{
         // Open mongodb connection
        Mongo m = new Mongo("localhost", 37017);
        DB db = m.getDB("ace");
        
        // forum user Collection
        DBCollection collForumUser = db.getCollection("valid_forum_user");
        DBCursor cursor = collForumUser.find();
        try {
            while(cursor.hasNext()) {
                BasicDBObject doc = (BasicDBObject) cursor.next();
                String email = doc.getString("email");
                BasicDBObject updatedDoc = new BasicDBObject(doc);
                updatedDoc.put("normalized_email", email.toLowerCase());
                collForumUser.update(doc, updatedDoc);
            }
        } finally {
            cursor.close();
        }
        System.out.println("Done adding normalized email field");
    }
    
    
    public static void addBundeToDb() throws UnknownHostException{
         // Open mongodb connection
        Mongo m = new Mongo("localhost", 27017);
        DB db = m.getDB("ace");
        
        // forum user Collection
        DBCollection collForumUser = db.getCollection("valid_forum_user");
        BasicDBObject ssoDoc = new BasicDBObject();
        
        ssoDoc.put("username", "kunle75");
        ssoDoc.put("email", "kunle_oshin@yahoo.com");
        UUID ssoId = UUID.randomUUID();
        ssoDoc.put("ssoid", ssoId.toString());
        ssoDoc.put("normalized_username", "kunle75");
        ssoDoc.put("validUsername", isValidUsername("kunle75"));
        
        collForumUser.insert(ssoDoc);
    }

    public static void checkSSOForumDb() throws Exception {
        // Open mongodb connection
        Mongo m = new Mongo("localhost", 27017);
        DB db = m.getDB("ace");
        
        // SSO Collection
        DBCollection coll_sso = db.getCollection("valid_forum_user");
        
        DBCursor cursor = coll_sso.find();
        try {
            while(cursor.hasNext()) {
                BasicDBObject oldDoc = (BasicDBObject)cursor.next();
                String username = oldDoc.getString("username");
                BasicDBObject newDoc = new BasicDBObject(oldDoc);
                newDoc.put("validUsername", isValidUsername(username));
                coll_sso.update(oldDoc, newDoc);                
            }
        } finally {
            cursor.close();
        }
        System.out.println("Done checking sso forum user validity");
    }

    public static void main(String[] arg) throws Exception {
//        addBundeToDb();
//        generateSSOForumDb(null);
//        addNormalizedUserName();
//        checkSSOForumDb();
        
        addNormalizedEmail();
        
    }
}
