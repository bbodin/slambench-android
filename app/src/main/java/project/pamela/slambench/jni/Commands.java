package project.pamela.slambench.jni;

import android.util.Log;

import project.pamela.slambench.SLAMBenchApplication;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */


public class Commands {

    private static Boolean _valid;

    static {
        Log.d(SLAMBenchApplication.LOG_TAG, "Load JNI Start.");
        try {
            System.loadLibrary("jnicommands");
            _valid = true;
        } catch (Throwable e) {
            Log.d(SLAMBenchApplication.LOG_TAG, "Load JNI failed : " + e.getMessage() + '.');
            _valid = false;
        } finally {
            Log.d(SLAMBenchApplication.LOG_TAG, "Load JNI : Result = " + _valid);
        }
    }

    private static native boolean gettrue();

    private static native boolean tryroot();

    private static native String getarchitecture();

    private static native boolean isopenclavailable();
    private static native String getopenclinfo();

    public static boolean get_true() {

        Log.d(SLAMBenchApplication.LOG_TAG, "jni_get_true Start.");
        boolean res;
        try {
            res = gettrue();
        } catch (Throwable e) {
            Log.d(SLAMBenchApplication.LOG_TAG, "jni_get_true : Error = " + e.getMessage());
            return false;
        }
        Log.d(SLAMBenchApplication.LOG_TAG, "jni_get_true : Result = " + res);
        return res;

    }

    public static boolean try_root() {

        Log.d(SLAMBenchApplication.LOG_TAG, "jni_tryroot Start.");
        boolean res;
        try {
            res = tryroot();
        } catch (Throwable e) {
            Log.d(SLAMBenchApplication.LOG_TAG, "jni_tryroot : Error = " + e.getMessage());
            return false;
        }
        Log.d(SLAMBenchApplication.LOG_TAG, "jni_tryroot : Result = " + res);
        return res;
    }

    public static String get_architecture() {

        Log.d(SLAMBenchApplication.LOG_TAG, "get_architecture Start.");
        String res;
        try {
            res = getarchitecture();
        } catch (Throwable e) {
            Log.d(SLAMBenchApplication.LOG_TAG, "get_architecture : Error = " + e.getMessage());
            res = "Error";
        }
        Log.d(SLAMBenchApplication.LOG_TAG, "get_architecture : Result = " + res);
        return res;

    }
    public static boolean is_opencl_available() {

        Log.d(SLAMBenchApplication.LOG_TAG, "is_opencl_available Start.");
        boolean res;
        try {
            res = isopenclavailable();
        } catch (Throwable e) {
            Log.d(SLAMBenchApplication.LOG_TAG, "is_opencl_available : Error = " + e.getMessage());
            res = false;
        }
        Log.d(SLAMBenchApplication.LOG_TAG, "is_opencl_available : Result = " + res);
        return res;
    }
    public static String get_opencl_info() {

        Log.d(SLAMBenchApplication.LOG_TAG, "is_opencl_available Start.");
        String res;
        try {
            res = getopenclinfo();
        } catch (Throwable e) {
            Log.d(SLAMBenchApplication.LOG_TAG, "is_opencl_available : Error = " + e.getMessage());
            res = "Error";
        }
        Log.d(SLAMBenchApplication.LOG_TAG, "is_opencl_available : Result = " + res);
        return res;

    }

    public static boolean get_loaded_library() {
        return Commands._valid;
    }


}
