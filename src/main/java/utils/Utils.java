package utils;

import org.openqa.selenium.Platform;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class Utils {

    private static Properties properties;

    public static String getProperty(String propertyName) throws IOException {
        if (properties == null)
            properties = new Properties();
        InputStream inputStream = new FileInputStream("config.properties");
        properties.load(inputStream);
        return properties.getProperty(propertyName);
    }

    public static String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    public static String generateRandomString(int length) {
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String str = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            char c = alphabet.charAt(random.nextInt(alphabet.length() - 1));
            str += c;
        }

        return str;
    }

    public static Platform getCurrentPlatform() {
        String operationSystem = System.getProperty("os.name").toLowerCase();
        if (operationSystem.contains("win"))
            return Platform.WINDOWS;
        else if (operationSystem.contains("nix") || operationSystem.contains("nux") || operationSystem.contains("aix"))
            return Platform.LINUX;
        else if (operationSystem.contains("mac"))
            return Platform.MAC;
        else
            return null;
    }

    public static String readValueFromClipboard() throws IOException, UnsupportedFlavorException {
        return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
    }

    public static String getCurrentRootLocation(){
        return System.getProperty("user.dir");
    }

    public static String getScreenshotPath(){
        String location="";
        switch (OsCheck.getOperatingSystemType()) {
            case Windows:
                location=".\\screenshots\\";
                break;

            case Mac:
                location="./screenshots/";
                break;

            case Linux:
                location="./screenshots/";
                break;
        }

        return  location;
    }

    public static String getScreenshotDirName(){
        String location="";
        switch (OsCheck.getOperatingSystemType()) {
            case Windows:
                location="\\screenshots\\";
                break;

            case Mac:
                location="/screenshots/";
                break;

            case Linux:
                location="/screenshots/";
                break;
        }

        return  location;
    }

    public static void cmdKill(){
        try {
            Runtime.getRuntime().exec("taskkill /f /im cmd.exe") ;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}