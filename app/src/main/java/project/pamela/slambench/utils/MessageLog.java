package project.pamela.slambench.utils;

import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import project.pamela.slambench.SLAMBenchApplication;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class MessageLog {

    private static final Lock _mutex = new ReentrantLock(true);
    public static String _message = "";
    private static TextView _textView = null;

    private static void update() {
        if (_textView != null) {
            try {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    _textView.setText(Html.fromHtml(_message), TextView.BufferType.SPANNABLE);
                }
            } catch (Throwable e) {
                Log.e(SLAMBenchApplication.LOG_TAG,"error",e);
            }
        }
    }

    static private void addToMessage(String str) {
        try {
            _mutex.lock();
            try {
                if (SLAMBenchApplication.isDebug()) {
                    new SendDebugTask(str).execute();
                }
                _message += str;
            } finally {
                _mutex.unlock();
            }
        } catch (Throwable ie) {
            Log.d(SLAMBenchApplication.LOG_TAG, "############### LOCK ERROR ##########################");
        }
    }

    static public void addWarning(String msg_format, Object... args) {
        String msg = String.format(msg_format,args);
        String head = "Warning";
        for (String s : msg.split("\n")) {
            addToMessage("<font color='blue'> <b>[" + head + "]</b> " + s + "</font><br/>\n");
            Log.w(SLAMBenchApplication.LOG_TAG, s);
        }
        update();
    }

    static public void addError(String msg_format, Object... args) {
        String msg = String.format(msg_format,args);
        String head = "Error";
        for (String s : msg.split("\n")) {
            addToMessage("<font color='red'> <b>[" + head + "]</b> " + s + "</font><br/>\n");
            Log.e(SLAMBenchApplication.LOG_TAG, s);
        }
        update();
    }

    static public void addSuccess(String msg_format, Object... args) {
        String msg = String.format(msg_format,args);
        String head = "Success";
        for (String s : msg.split("\n")) {
            addToMessage("<font color='green'> <b>[" + head + "]</b> " + s + "</font><br/>\n");
            Log.i(SLAMBenchApplication.LOG_TAG, s);
        }
        update();
    }

    static public void addInfo(String msg_format, Object... args) {
        String msg = String.format(msg_format,args);
        String head = "Info";
        for (String s : msg.split("\n")) {
            addToMessage(" <b>[" + head + "]</b> " + s + "<br/>\n");
            Log.i(SLAMBenchApplication.LOG_TAG, s);
        }
        update();
    }

    static public void addDebug(String msg_format, Object... args) {
        String msg = String.format(msg_format,args);
        addToMessage("<!-- " + msg + " -->\n");
        Log.d(SLAMBenchApplication.LOG_TAG, msg);
    }

    static public void setTextView(TextView textView) {
        _textView = textView;
        update();
    }

    static public void unsetTextView() {
        _textView = null;
    }

    public static void clear() {
        try {
            _mutex.lock();
            try {
                _message = "";
            } finally {
                _mutex.unlock();
            }
        } catch (Throwable ie) {
            Log.d(SLAMBenchApplication.LOG_TAG, "############### LOCK ERROR ##########################");
        }
    }

}
