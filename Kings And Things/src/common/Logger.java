package common;

import java.util.Date;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;

/**
 * Class Logger is used to flush the information stored on the console to a file
 */
public class Logger {

    /**
     * output all information stored in log to a file.
     */
    public static synchronized void flush( String name, String data){
    	String date = new Date().toString().replaceAll( "[:]", " ");
    	File file = new File("Logs");
    	if( !file.exists()){
    		file.mkdir();
    	}
        try( BufferedWriter LogFile = new BufferedWriter( new FileWriter( "Logs\\" + name + " - " + date + ".txt"))){
            LogFile.write( data);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
