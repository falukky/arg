package utils;

import com.aventstack.extentreports.Status;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class MongoDB {

    private static MongoClient mongo;

    public static void Init() throws Exception {
        ExtentReport.extentTest = ExtentReport.extentReports.createTest("Setup", "Initiate Mongo DB");
        createMongoDbDocker();
        Thread.sleep(10000);
        connect();
    }

    public static void connect() throws IOException {
        ExtentReport.extentTest.log(Status.INFO, "Try to connect to Mongo database...");
        mongo = new MongoClient("localhost", 27017);
        MongoCredential credential = MongoCredential.createCredential(Utils.getProperty("mongouser"), Utils.getProperty("databasename"), Utils.getProperty("mongopassword").toCharArray());
        ExtentReport.extentTest.log(Status.INFO, "Successfully connected to Mongo database");
    }

    public static void addRecord(String title, String description, String language, String stars, String tags, String time, String pageLoadElapsedTime) {

        // Accessing the database
        MongoDatabase database = mongo.getDatabase("myDb");

        //database.createCollection("customers", null);
        MongoCollection<Document> collection = database.getCollection("searchresults");
        Document document = new Document();
        document.put("Title", title);
        document.put("Description", description);
        document.put("Language", language);
        document.put("Stars", stars);
        document.put("Tags", tags);
        document.put("Time", time);
        document.put("PageLoadElapsedTime", pageLoadElapsedTime);

        ExtentReport.extentTest.log(Status.INFO, "Add record to Mongo database");
        collection.insertOne(document);
    }

    public static void addDbRecordsToReport() {
        // Accessing the database
        MongoDatabase database = mongo.getDatabase("myDb");
        MongoCollection<Document> collection = database.getCollection("searchresults");
        FindIterable<Document> iterDoc = collection.find();

        String title = "";
        String description = "";
        String language = "";
        String stars = "";
        String tags = "";
        String time = "";
        String pageLoadElapsedTime = "";

        for (Document doc : iterDoc) {
            for (Map.Entry<String, Object> entry : doc.entrySet()) {
                //System.out.println(entry.getKey() + ": " + entry.getValue());
                if (entry.getKey().equals("Title"))
                    title = entry.getValue().toString();
                if (entry.getKey().equals("Description"))
                    description = entry.getValue().toString();
                if (entry.getKey().equals("Language"))
                    language = entry.getValue().toString();
                if (entry.getKey().equals("Stars"))
                    stars = entry.getValue().toString();
                if (entry.getKey().equals("Tags"))
                    tags = entry.getValue().toString();
                if (entry.getKey().equals("Time"))
                    time = entry.getValue().toString();
                if (entry.getKey().equals("PageLoadElapsedTime"))
                    pageLoadElapsedTime = entry.getValue().toString();
            }

            writeToReport(title, description, language, stars, tags, time, pageLoadElapsedTime);
        }
    }

    private static void writeToReport(String title, String description, String language, String stars, String tags, String time, String pageLoadElapsedTime) {
        ExtentReport.extentTest.log(
                Status.INFO,
                "<table>\n" +
                        "  <tr>\n" +
                        "    <th>Title</th>\n" +
                        "    <th>Description</th>\n" +
                        "    <th>Language</th>\n" +
                        "    <th>Stars</th>\n" +
                        "    <th>Tags</th>\n" +
                        "    <th>Time</th>\n" +
                        "    <th>Load time</th>\n" +
                        "  </tr>\n" +
                        "  <tr>\n" +
                        "    <td>title</td>\n".replace("title", title) +
                        "    <td>description</td>\n".replace("description", description) +
                        "    <td>language</td>\n".replace("language", language) +
                        "    <td>stars</td>\n".replace("stars", stars) +
                        "    <td>tags</td>\n".replace("tags", tags) +
                        "    <td>time</td>\n".replace("time", time) +
                        "    <td>output</td>\n".replace("output", pageLoadElapsedTime) +
                        "  </tr>\n" +
                        "</table>");
    }

    public static void clearDB() {
        ExtentReport.extentTest.log(Status.INFO, "Clear Mongo database");
        mongo.dropDatabase("myDb");
    }

    public static void createMongoDbDocker() throws Exception {
        ExtentReport.extentTest.log(Status.INFO, "Create and start Mongo DB from docker");
        String[] command = {"cmd.exe", "/C", "Start", Utils.getCurrentRootLocation() + "\\configMongo.bat"};
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        Thread.sleep(5000);
    }

    public static String readMongoDockerId() throws InterruptedException, IOException {
        ExtentReport.extentTest.log(Status.INFO, "Read Mongo id from command prompt...");
        String command = String.format("cmd /c start cmd.exe /K \"docker ps > %s\"", Utils.getCurrentRootLocation() + "\\dockers.txt");
        Runtime.getRuntime().exec(command);
        Thread.sleep(3000);

        String currentLocation = Utils.getCurrentRootLocation() + "\\dockers.txt";
        File file = new File(currentLocation);

        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();

        String str = new String(data, "UTF-8");
        String[] lines = str.split("\n");
        if (lines.length > 1) {
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                String[] arr = line.split("\\s+");
                if (arr.length > 1 && arr[1].equals("mongo"))
                    return arr[0];
            }
        }

        return "";
    }

    public static void stopMongo() throws Exception {
        String id = readMongoDockerId();
        ExtentReport.extentTest.log(Status.INFO, String.format("Mongo docker id is '%s'", id));
        if (id == ""){
            ExtentReport.extentTest.log(Status.WARNING, "Failed to read Mongo docker id");
            throw new Exception("Cannot read Mongo docker id");
        }

        // Write into .bat file the command to stop docker.
        ExtentReport.extentTest.log(Status.INFO, "Stop Mongo...");
        String batFile=Utils.getCurrentRootLocation() + "\\stopMongo.bat";
        String[] command = {"cmd.exe", "/C", "Start", batFile};
        FileWriter writer = new FileWriter(batFile);
        writer.write(String.format("docker stop %s", id));
        writer.close();
        Thread.sleep(2000);

        // Execute command.
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
    }
}
