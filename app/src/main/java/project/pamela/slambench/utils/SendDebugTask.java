package project.pamela.slambench.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

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

        return true;

    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPreExecute();
        if (result) used = false;
    }

}
