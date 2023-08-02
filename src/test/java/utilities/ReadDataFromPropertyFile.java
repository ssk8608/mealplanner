package utilities;

import java.io.FileReader;
import java.util.Properties;

public class ReadDataFromPropertyFile {

        public static String getProperty(String propName) {
            Properties properties=new Properties();
            FileReader reader = null;
            //    System.out.println("Environment is :" + System.getProperty("env") );
            try {
                if(System.getProperty("env")=="uat"){
                    reader = new FileReader("src/test/resources/datafiles/qa.properties");
                }
                else if(System.getProperty("env")=="qa"){
                    reader = new FileReader("src/test/resources/datafiles/qa.properties");
                }  // src/test/resources/datafiles/qa.properties
                else{
                    reader=new FileReader("src/test/resources/datafiles/qa.properties");
                }

                System.out.println("reader is :" + reader);
                properties.load(reader);
            }
            catch(Exception ex){
                System.out.println("File not found");
            }
            return properties.getProperty(propName);
        }
}

