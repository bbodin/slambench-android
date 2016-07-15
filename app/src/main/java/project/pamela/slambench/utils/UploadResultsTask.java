package project.pamela.slambench.utils;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPOutputStream;

import project.pamela.slambench.R;
import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.activities.CommonActivity;
import project.pamela.slambench.models.SLAMResult;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class UploadResultsTask extends AsyncTask<Void, Integer, Boolean> {

    private String               _identifier;
    private final SLAMBenchApplication _application;


    public UploadResultsTask(Application application) {
        super();
        if (application instanceof SLAMBenchApplication) {
            this._application = (SLAMBenchApplication) application;
        } else {
            this._application = null;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        CommonActivity activity = _application.getCurrentActivity();
        if (activity != null) {
            _application.getCurrentActivity().updateDialog(0, -1, _application.getString(R.string.msg_uploading_results_title), _application.getString(R.string.msg_uploading_results));
        }
    }

    private String compress(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes());
            gzip.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "Compression Error";
        }
        try {
            return out.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "Encoding Error";
        }

    }

    private boolean copyToInternet() {

        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<?xml-stylesheet type=\"text/xsl\" href=\"http://homepages.inf.ed.ac.uk/bbodin/slambench.xsl\"?>\n"
                + "<SLAMBench>\n";
        data += "<MessageLog>" + MessageLog._message + "</MessageLog>\n";
        if (SLAMBenchApplication.getBenchmark() != null) {
            data += "<uid>" + SLAMBenchApplication.getBenchmark().getUid() + "</uid>\n";
            for (SLAMResult sm : SLAMBenchApplication.getResults()) {
                data += sm.toXML() + "\n";
            }
        }
        data += "</SLAMBench>\n";

        Log.d(SLAMBenchApplication.LOG_TAG, "Result size is " + data.length());
        String compressed_data = compress(data);
        Log.d(SLAMBenchApplication.LOG_TAG, "Compress size is " + compressed_data.length());

        try {

            String url = "http://homepages.inf.ed.ac.uk/bbodin/storegz.php";

            final URLConnection urlConnection = new URL(url).openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-encoding", "gzip");
            urlConnection.setRequestProperty("Content-type", "application/octet-stream");

            GZIPOutputStream dos1 = new GZIPOutputStream(urlConnection.getOutputStream());
            dos1.write(data.getBytes());
            dos1.flush();
            dos1.close();

            final InputStream inputStream = urlConnection.getInputStream();

            int contentLength = urlConnection.getContentLength();
            if (contentLength == -1) {
                Log.e(SLAMBenchApplication.LOG_TAG, "unknown download content length after upload");
            } else {
                Log.i(SLAMBenchApplication.LOG_TAG, "download content length after upload: " + contentLength + " bytes");
            }
            byte[] buffer = new byte[1024];
            int read = inputStream.read(buffer);
            if (read >= 0) {
                this._identifier = new String(buffer);
                inputStream.close();
            }

            return true;
        } catch (Throwable e) {
            Log.w(SLAMBenchApplication.LOG_TAG, "ERROR ... " + e.toString() + " " + e.getMessage(), e );
        }
        return false;

    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return copyToInternet();
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (_application.getCurrentActivity() == null) {
            MessageLog.addDebug(_application.getString(R.string.debug_flaw_in_the_system));
        } else {
            _application.getCurrentActivity().closeDialog();
            if (_application.getCurrentActivity() instanceof OnUploadResultsTaskTerminateListener) {
                ((OnUploadResultsTaskTerminateListener) _application.getCurrentActivity()).onUploadResultsTaskTerminate(this._identifier);
            }
        }

    }


    public interface OnUploadResultsTaskTerminateListener {
        void onUploadResultsTaskTerminate(String identifier);
    }
}
