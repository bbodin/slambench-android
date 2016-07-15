package project.pamela.slambench.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class RootUtil {

    private static final String LOG_ROOT_UTIL = "ROOT_UTIL";

    private static String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while (i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    public static String whoIsRoot() {

        String res = "";

        try {
            res += "You are : ";
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("id");
            pb.redirectErrorStream(true);
            Process process;

            Log.d(LOG_ROOT_UTIL, "Run process...");
            process = pb.start();
            Log.d(LOG_ROOT_UTIL, "process run...");
            InputStream in = process.getInputStream();
            Log.d(LOG_ROOT_UTIL, "Stream taken...");
            res += readStream(in);
            process.destroy();


        } catch (Exception e) {
            Log.d(LOG_ROOT_UTIL, "Error = " + e.getMessage(),e);
            res += "ERROR";
        }


        try {
            res += "Root is :";
            ProcessBuilder pb2 = new ProcessBuilder();
            pb2.command("/system/bin/su", "-c", "id");
            pb2.redirectErrorStream(true);

            Log.d(LOG_ROOT_UTIL, "Run process...");
            Process process = pb2.start();
            Log.d(LOG_ROOT_UTIL, "process run...");
            InputStream in = process.getInputStream();
            Log.d(LOG_ROOT_UTIL, "Stream taken...");
            res += readStream(in);
            process.destroy();

        } catch (Exception e) {
            Log.d(LOG_ROOT_UTIL, "Error = " + e.getMessage());
            e.printStackTrace();
            res += "ERROR";
        }

        return res;
    }

    public static Boolean isRootWorks() {
        try {

            // Preform su to get root privileges
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());

            os.writeBytes("id\n");

            // Close the terminal
            os.writeBytes("exit\n");
            os.flush();


            try {
                p.waitFor();
                return p.exitValue() != 255;
            } catch (InterruptedException e) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

    }

    public static boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3() || checkRootMethod4();
    }

    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkRootMethod2() {
        return new File("/system/app/Superuser.apk").exists();
    }

    private static boolean checkRootMethod3() {
        String[] paths = {"/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean checkRootMethod4() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }
}


