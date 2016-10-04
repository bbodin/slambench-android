package project.pamela.slambench.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import project.pamela.slambench.SLAMBenchApplication;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */
class SendDebugTask extends AsyncTask<Void, Void, Boolean> {

    private static boolean used;
    private static String skiplines = "";
    private String _line;

    public SendDebugTask(String line) {
        super();
        this._line = line;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (used) {
            skiplines += _line;
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        if (used) {
            return false;
        }
        used = true;

        if (!skiplines.equals("")) {
            _line += skiplines;
            skiplines = "";
        }

        String url = "http://homepages.inf.ed.ac.uk/bbodin/debug.php";
        String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
        String query;

        try {
            query = String.format("q=%s", URLEncoder.encode(_line, charset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return true;
        }

        try {
            URLConnection urlConnection;
            urlConnection = new URL(url).openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
            urlConnection.setRequestProperty("Accept-Charset", charset);
            //urlConnection.connect();
            final OutputStream outputStream;

            outputStream = urlConnection.getOutputStream();
            outputStream.write(query.getBytes(charset));
            outputStream.flush();
            outputStream.close();
            final InputStream inputStream = urlConnection.getInputStream();
            int contentLength = urlConnection.getContentLength();
            inputStream.close();
            if (contentLength == -1) {
                return true;
            } else {
                Log.i(SLAMBenchApplication.LOG_TAG, "DEBUG content length: " + contentLength + " bytes");
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }

    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPreExecute();
        if (result) used = false;
    }

}
