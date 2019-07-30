package com.dynatrace.easytravel.android.util;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebRequestHelper extends AsyncTask<String, String, String> {

    private String url;

    public static void loadURL(String url) {
        new WebRequestHelper(url).execute();
    }

    public WebRequestHelper(String url) {
        this.url = url;
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        InputStreamReader isReader = null;
        BufferedReader bufReader = null;
        StringBuffer readTextBuf = new StringBuffer();

        try {
            URL url = new URL(this.url);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            InputStream inputStream = urlConnection.getInputStream();
            isReader = new InputStreamReader(inputStream);
            bufReader = new BufferedReader(isReader);
            String line = bufReader.readLine();
            while(line != null) {
                readTextBuf.append(line);
                line = bufReader.readLine();
            }
            return readTextBuf.toString();
        }
        catch(Exception e) {
            // ignore
        }
        finally {
            try {
                if (bufReader != null) {
                    bufReader.close();
                    bufReader = null;
                }

                if (isReader != null) {
                    isReader.close();
                    isReader = null;
                }

                if (urlConnection != null) {
                    urlConnection.disconnect();
                    urlConnection = null;
                }
            }
            catch (IOException ex) {
                // ignore
            }
        }
        return null;
    }
}