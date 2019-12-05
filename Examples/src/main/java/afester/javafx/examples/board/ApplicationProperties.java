package afester.javafx.examples.board;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class ApplicationProperties {

    // the actual storage
    private final Properties p = new Properties();

    private ApplicationProperties() {
        
    }

    public static ApplicationProperties load() {
        ApplicationProperties result = new ApplicationProperties();

        try {
            InputStream is = new FileInputStream("application.xml");
            result.p.loadFromXML(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void save() {
        try {
            OutputStream os = new FileOutputStream("application.xml");
            p.storeToXML(os, "BreadBoardEditor");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setString(String key, String value) {
        p.setProperty(key, value);
    }

    public String getString(String key) {
        return p.getProperty(key);
    }

//    public Number getNumber(String key) {
//        return null;
//    }
//
//    public List<?> getList(String key) {
//        return null;
//    }
}