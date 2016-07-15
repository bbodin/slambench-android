package project.pamela.slambench;

import android.app.Application;
import android.util.Log;

import java.util.List;
import java.util.Stack;

import project.pamela.slambench.activities.CommonActivity;
import project.pamela.slambench.models.SLAMBench;
import project.pamela.slambench.models.SLAMResult;
import project.pamela.slambench.models.SLAMTest;
import project.pamela.slambench.utils.MessageLog;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */


public class SLAMBenchApplication extends Application {

    public final static String LOG_TAG = "SLAMBench";
    private static SLAMBench benchmark = null;
    private static boolean _debug = false;
    public static String SELECTED_MODE_TAG = "SELECTED_MODE_TAG";
    private CommonActivity _current_activity;
    private static Stack<SLAMResult> _result_queue = null;
    private static  final Stack<SLAMTest> _test_queue = new Stack<>();
    private static  SLAMTest _current_test = null;
    public static boolean isDebug() {
        return _debug;
    }


    public static void setDebug(boolean debug) {
        _debug = debug;
    }

    public static SLAMBench getBenchmark() {
        return benchmark;
    }

    public static void resetBenchmark() {
        resetResults();
        resetTestQueue();
    }

    public static void setBenchmark(SLAMBench benchmark) {
        resetResults();
        SLAMBenchApplication.benchmark = benchmark;
    }


    public static void resetResults() {
        _result_queue = new  Stack<SLAMResult>();
    }
    public static void queueResult(SLAMResult t) {
        if (getResults() == null) {
            resetResults();
        }
        _result_queue.push(t);
    }

    public static List<SLAMResult> getResults() {
        return _result_queue;
    }


    public static int remainTestCount() {
        return _test_queue.size();
    }
    public static void resetTestQueue() {
        _test_queue.clear();
        _current_test = null;
    }

    public static  boolean selectNextTest() {
        if (_test_queue.isEmpty()) {
            _current_test = null;
            return false;
        } else {
            _current_test = _test_queue.pop();
            return true;
        }
    }

    public  static  SLAMTest getCurrentTest() {
        return _current_test;
    }

    public  static  void queueTest(SLAMTest t) {
        _test_queue.push(t);
    }



    @Override
    public void onLowMemory() {
        MessageLog.addDebug("Low memory warning !");

    }

    @Override
    public void onTrimMemory(int level) {
        MessageLog.addDebug("Trim memory = " + level + " !");
    }


    public CommonActivity getCurrentActivity() {
        return this._current_activity;
    }

    public void setCurrentActivity(CommonActivity currentActivity) {
        if (currentActivity != null) {
            Log.d(LOG_TAG,"New valid activity is " + currentActivity.getLocalClassName());
        } else {
            if (_current_activity != null) {
                Log.d(LOG_TAG,"Unset previous activity " + _current_activity.getLocalClassName());
            } else {
                Log.d(LOG_TAG,"Unset previous null activity ");
            }
        }
        this._current_activity = currentActivity;
    }


}
