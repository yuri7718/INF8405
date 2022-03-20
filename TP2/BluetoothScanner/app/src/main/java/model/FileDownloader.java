package model;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

class FileDownloader extends AsyncTask<String, String, String> {

    private final Context context;
    protected ProgressDialog mProgress;


    /**
     * Before starting background thread Show Progress Bar Dialog
     * */
    FileDownloader(Context context){
        this.context = context;
        mProgress = new ProgressDialog(context);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgress.setMessage("Getting Directions :) ");
        mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgress.setIndeterminate(true);
        mProgress.setProgress(0);
        mProgress.setCancelable(false);
        mProgress.show();
    }

    /**
     * Downloading file in background thread
     * */
    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {
            URL url = new URL(f_url[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);


            Scanner s = new Scanner(input).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";

            // Define the File Path and its Name
            File file = new File(context.getFilesDir(),"directions.json");
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            //save file
            bufferedWriter.write(result);

            bufferedWriter.close();

            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return null;
    }

    /**
     * Updating progress bar
     * */
    protected void onProgressUpdate(String... progress) {

    }

    /**
     * After completing background task Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(String file_url) {

    }


}

