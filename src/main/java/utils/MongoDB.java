package utils;

import com.aventstack.extentreports.Status;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.io.IOUtils;
import org.bson.Document;

import java.io.*;
import java.util.List;
import java.util.Map;

public class MongoDB {

    private static MongoClient mongo;
    private static String id;

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

        ExtentReport.extentTest.log(Status.INFO, String.format("Add new record to Mongo database (%s)", title));
        collection.insertOne(document);
    }

    public static void addDbRecordsToReport() throws IOException {
        // Accessing the database
        MongoDatabase database = mongo.getDatabase(Utils.getProperty("databasename"));
        MongoCollection<Document> collection = database.getCollection("searchresults");
        FindIterable<Document> iterDoc = collection.find();

        String title = "";
        String description = "";
        String language = "";
        String stars = "";
        String tags = "";
        String time = "";
        String pageLoadElapsedTime = "";

        ExtentReport.extentTest.log(Status.INFO, "Read all DB record and add to HTML report");
        for (Document doc : iterDoc) {
            for (Map.Entry<String, Object> entry : doc.entrySet()) {
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

            createHtmlTable(title, description, language, stars, tags, time, pageLoadElapsedTime);
        }
    }

    private static void createHtmlTable(String title, String description, String language, String stars, String tags, String time, String pageLoadElapsedTime) {
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
        Process process;

        switch (OsCheck.getOperatingSystemType()) {
            case Windows:
                String[] command = {"cmd.exe", "/C", "Start", Utils.getCurrentRootLocation() + "\\configMongo.bat"};
                process = Runtime.getRuntime().exec(command);
                process.waitFor();
                parseId(null);
                break;

            case Mac:
            case Linux:
                String[] createDB = {"bash", "-c", "docker volume create --name=mydb"};
                process = Runtime.getRuntime().exec(createDB);

                String[] runDB = {"bash", "-c", "docker run -d -p 27017:27017 -v mydb:/data/db mongo"};
                process = Runtime.getRuntime().exec(runDB);
                Thread.sleep(3000);

                String[] ids = {"bash", "-c", "docker ps"};
                process = Runtime.getRuntime().exec(ids);
                StringBuffer stringBuffer = new StringBuffer();

                List<String> result = IOUtils.readLines(process.getInputStream());
                //result = IOUtils.readLines(process.getInputStream());
                for (String line : result)
                    stringBuffer.append(line);
                Thread.sleep(3000);
                parseId(stringBuffer.toString());
                System.out.println("Docker id is: " + id);
                break;
        }

        Thread.sleep(5000);
    }

    private static void parseId(String output) throws InterruptedException, IOException {
        ExtentReport.extentTest.log(Status.INFO, "Read Mongo id from command prompt...");

        switch (OsCheck.getOperatingSystemType()) {
            case Windows:
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
                            id = arr[0];
                    }
                }
                break;

            case Mac:
            case Linux:
                for (String str2 : output.split("\\s+")) {
                    if (str2.startsWith("NAMES"))
                        id = str2.replace("NAMES", "");
                }
                break;
        }

        ExtentReport.extentTest.log(Status.INFO, String.format("Mongo docker id is '%s'", id));
    }

    public static void stopMongo() throws Exception {

        if (id != "" && id != null) {
            ExtentReport.extentTest.log(Status.INFO, "Stop Mongo docker");

            switch (OsCheck.getOperatingSystemType()) {
                case Windows:
                    // Write into .bat file the stop docker command.
                    ExtentReport.extentTest.log(Status.INFO, "Stop Mongo...");
                    String batFile = Utils.getCurrentRootLocation() + "\\stopMongo.bat";
                    String[] command = {"cmd.exe", "/C", "Start", batFile};
                    FileWriter writer = new FileWriter(batFile);
                    writer.write(String.format("docker stop %s", id));
                    writer.close();
                    Thread.sleep(2000);

                    // Execute command.
                    Process process = Runtime.getRuntime().exec(command);
                    process.waitFor();
                    break;

                case Mac:
                case Linux:
                    String[] stopDB = {"bash", "-c", String.format("docker stop %s", id)};
                    Runtime.getRuntime().exec(stopDB);
                    break;
            }
        } else
            ExtentReport.extentTest.log(Status.WARNING, "Failed to read Mongo docker id, Mongo still running");
    }
}
