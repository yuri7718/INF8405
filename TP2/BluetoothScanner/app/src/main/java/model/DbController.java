package model;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DbController {

    private final Context context;
    private JSONObject data;

    public DbController(Context context) {
        this.context = context;
    }

    public void setDevicesLocations(JSONObject arr) {
        try {
            this.data.put("devices_locations", arr);

            // Convert JsonObject to String Format
            String userString = this.data.toString();
            // Define the File Path and its Name
            File file = new File(context.getFilesDir(),"DB.json");
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            //save file
            bufferedWriter.write(userString);
            bufferedWriter.close();

        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
            return;
        }
    }

    public void setFavourites(JSONObject arr) {
        try {
            this.data.put("favourites", arr);

            // Convert JsonObject to String Format
            String userString = this.data.toString();
            // Define the File Path and its Name
            File file = new File(context.getFilesDir(),"DB.json");
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            //save file
            bufferedWriter.write(userString);
            bufferedWriter.close();

        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
            return;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void readDataFromDB(){
        //getting the initial positions of blocks from a json file
        this.data = new JSONObject();
        try {
            InputStream is = null;
            //verify if its in cache
            //Log.i("test-test", String.valueOf(Paths.get(context.getFilesDir() + "/DB.json")));
            File file = new File(context.getFilesDir() + "/DB.json");
            Log.i("test-test", String.valueOf(context.getFilesDir()));
            if (file.exists()) {
                is = new FileInputStream(context.getFilesDir() + "/DB.json");
                //reading the file
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                this.data = new JSONObject(new String(buffer, StandardCharsets.UTF_8));
            } else{
                this.data.put("devices_locations", new JSONObject());
                this.data.put("favourites", new JSONObject());

            }
        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public JSONObject getDevicesLocations(){
        readDataFromDB();
        try {
            return (JSONObject) (this.data.get("devices_locations"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public JSONObject getFavourites(){
        readDataFromDB();
        try {
            return (JSONObject) (this.data.get("favourites"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

}
