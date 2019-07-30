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
        try {
            // Creating & connection Connection with url and required Header.
            URL url = new URL(this.url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            int statusCode = urlConnection.getResponseCode();

            // Connection success. Proceed to fetch the response.
            if (statusCode == 200) {
                InputStream it = new BufferedInputStream(urlConnection.getInputStream());
                InputStreamReader read = new InputStreamReader(it);
                BufferedReader buff = new BufferedReader(read);
                StringBuilder data = new StringBuilder();
                String chunks;
                while ((chunks = buff.readLine()) != null) {
                    if (data.length() != 0)
                        data.append("\r\n");
                    data.append(chunks);
                }
                String content = data.toString();
                return content;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}