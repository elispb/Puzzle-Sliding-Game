package mobile.labs.acw;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class DownloadManager {
    private String StreamReader(InputStream stream){
        String unParsedJSON = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
            String line = "";
            while (line != null) {
                unParsedJSON += line;
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return unParsedJSON;
    }

    public String LoadJSON(Context context, String fileName, String url){
        String unParsedJSON = "";
        try { //Read from file
            FileInputStream stream = context.openFileInput(fileName);
            unParsedJSON = StreamReader(stream);
        } catch (FileNotFoundException fileNotFound) {
            try { //Download from internet
                InputStream stream = (InputStream) new URL(url).getContent();
                FileOutputStream writer = null;
                unParsedJSON = StreamReader(stream);
                try{ //Save downloaded file
                    writer = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                    writer.write(unParsedJSON.getBytes());
                } catch (Exception fileWriteException){
                    Log.i("Error",fileWriteException.getMessage());
                } finally {
                    writer.close();
                }
            } catch (IOException e) {
                Log.i("Error",e.getMessage());
            }
        }
        return unParsedJSON;
    }

    public String LoadJSON(Context context, String fileName){
        String unParsedJSON = "";
        try { //Read from file
            FileInputStream stream = context.openFileInput(fileName);
            unParsedJSON = StreamReader(stream);
        }  catch (IOException e) {
            Log.i("Error",e.getMessage());
        }
        return unParsedJSON;
    }

    public boolean FileExists(String filename){
        File file = new File(filename);
        boolean result = file.exists();
        return result;
    }
}
