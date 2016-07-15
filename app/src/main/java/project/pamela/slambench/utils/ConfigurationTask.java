package project.pamela.slambench.utils;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.opengl.GLES10;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Stack;

import javax.microedition.khronos.opengles.GL10;

import project.pamela.slambench.BuildConfig;
import project.pamela.slambench.R;
import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.jni.Commands;
import project.pamela.slambench.jni.KFusion;
import project.pamela.slambench.models.SLAMBench;
import project.pamela.slambench.models.SLAMConfiguration;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */



public class ConfigurationTask extends AsyncTask<Void, String, Boolean> {

    private static final String START_URL = "http://homepages.inf.ed.ac.uk/bbodin/start.php";
    private static final int    MAX_TRY_COUNT = 5;
    private SLAMBenchApplication _application = null;
    private final ConfigurationResult _result = new ConfigurationResult();

    public ConfigurationTask(Application application) {
        super();
        if (application instanceof SLAMBenchApplication) {
            this._application = (SLAMBenchApplication) application;
        } else {
            this._application = null;
        }
    }

    protected void onPreExecute()
    {

        _application.getCurrentActivity().updateDialog(0,-1,"Configuration in progress", "Data collection...");

        // JNI Load
        _result.libraryLoaded = Commands.get_loaded_library();
        _result.callWorked = Commands.get_true();
        _result.rootAvailable = Commands.try_root();
        _result.ocl_driver = Commands.get_opencl_info();


        // Architecture test
        _result.architectureName = Commands.get_architecture();


        for (KFusion.ImplementationId v : KFusion.getImplementations()) {
            _result.available_implementations.add(v);
        }

        _result.capacity = MobileStats.getInstance().getCapacity();
        _result.full_capacity = MobileStats.getInstance().getFullCapacity();

        _result.charge = MobileStats.getInstance().getCharge();
        _result.temp = MobileStats.getInstance().getTemp();
        _result.current = MobileStats.getInstance().getCurrent();
        _result.voltage = MobileStats.getInstance().getVoltage();
        _result.power = MobileStats.getInstance().getPower();

        MessageLog.addDebug("Battery Information are : ");
        MessageLog.addDebug("capacity =  " + Double.toString(_result.capacity));
        MessageLog.addDebug("full_capacity =  " + Double.toString(_result.full_capacity));
        MessageLog.addDebug("Charge =  " + Double.toString(_result.charge));
        MessageLog.addDebug("current =  " + Double.toString(_result.current));
        MessageLog.addDebug("temp =  " + Double.toString(_result.temp));
        MessageLog.addDebug("voltage =  " + Double.toString(_result.voltage));
        MessageLog.addDebug("power =  " + Double.toString(_result.power));

        MessageLog.addInfo(_application.getResources().getString(R.string.current_os_version) + System.getProperty("os.version"));
        MessageLog.addInfo(_application.getResources().getString(R.string.current_os_name) + System.getProperty("os.name"));
        MessageLog.addInfo(_application.getResources().getString(R.string.current_os_arch) + System.getProperty("os.arch"));
        MessageLog.addInfo(_application.getResources().getString(R.string.current_sdk_version) + android.os.Build.VERSION.SDK_INT);
        MessageLog.addInfo(_application.getResources().getString(R.string.current_device_name) + android.os.Build.DEVICE);
        MessageLog.addInfo(_application.getResources().getString(R.string.current_model_name) + android.os.Build.MODEL);
        MessageLog.addInfo(_application.getResources().getString(R.string.current_product_name) + android.os.Build.PRODUCT);
        MessageLog.addInfo(_application.getResources().getString(R.string.current_manufacturer_name) + android.os.Build.MANUFACTURER);
        MessageLog.addInfo(_application.getResources().getString(R.string.current_brand_name) + android.os.Build.BRAND);
        MessageLog.addInfo(_application.getResources().getString(R.string.current_board_name) + android.os.Build.BOARD);
        MessageLog.addInfo(_application.getResources().getString(R.string.current_bootload_name) + android.os.Build.BOOTLOADER);
        MessageLog.addInfo(_application.getResources().getString(R.string.current_hardware_name) + Build.HARDWARE);
        MessageLog.addInfo(_application.getResources().getString(R.string.current_cpu_governor) + MobileStats.getCPUGovernor());


        MessageLog.addInfo(MobileStats.getCPUInfo());
        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();
        MessageLog.addInfo("maxMemorySize:" + Long.toString(maxMemory / (1024 * 1024)) + "Mo");

        ActivityManager am = (ActivityManager) _application.getSystemService(Context.ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass();
        MessageLog.addInfo("memoryClass:" + Integer.toString(memoryClass) + "Mo");


        if (!_result.libraryLoaded) {
            MessageLog.addError(_application.getResources().getString(R.string.current_library_failed));
        }


        // JNI test
        if (!_result.callWorked) {
            MessageLog.addError(_application.getResources().getString(R.string.call_test_failed));
        }

        MessageLog.addInfo(_application.getResources().getString(R.string.current_architecture) + _result.architectureName);

        // OpenCL test
        if ((_result.ocl_driver == null) || (_result.ocl_driver.contains("not found"))) {
            MessageLog.addWarning(_application.getResources().getString(R.string.current_opencl_device) + Commands.get_opencl_info());
        } else {
            MessageLog.addInfo(_application.getResources().getString(R.string.current_opencl_device) + Commands.get_opencl_info());
        }

        // test CPP lib
        if (_result.available_implementations.contains(KFusion.ImplementationId.KFUSION_CPP)) {
            MessageLog.addSuccess(_application.getResources().getString(R.string.cpp_worked));
        } else {
            MessageLog.addError(_application.getResources().getString(R.string.cpp_failed));
        }

        // test NEON lib
        if (_result.available_implementations.contains(KFusion.ImplementationId.KFUSION_NEON)) {
            MessageLog.addSuccess(_application.getResources().getString(R.string.neon_worked));
        } else {
            MessageLog.addError(_application.getResources().getString(R.string.neon_failed));
        }


        // test OMP lib
        if (_result.available_implementations.contains(KFusion.ImplementationId.KFUSION_OMP)) {
            MessageLog.addSuccess(_application.getResources().getString(R.string.omp_worked));
        } else {
            MessageLog.addError(_application.getResources().getString(R.string.omp_failed));
        }


        /**
         * SHOULD BE MOVED
         */

        // test OCL lib
        if (_result.available_implementations.contains(KFusion.ImplementationId.KFUSION_OCL)) {

            MessageLog.addSuccess(_application.getResources().getString(R.string.ocl_worked));
            try {
                InputStream src = _application.getAssets().open(_application.getString(project.pamela.slambench.R.string.kernel_filename));
                OutputStream dst = new FileOutputStream(_application.getCacheDir() + "/" + _application.getString(project.pamela.slambench.R.string.kernel_filename));
                FilesDownloadTask.copyStream(src, dst);
                src.close();
                dst.flush();
                dst.close();
                MessageLog.addSuccess(_application.getResources().getString(R.string.ocl_kernel_found));
            } catch (Throwable e) {
                e.printStackTrace();
                MessageLog.addWarning(_application.getResources().getString(R.string.ocl_kernel_not_found));
            }

        } else {
            MessageLog.addWarning(_application.getResources().getText(R.string.ocl_failed).toString());
        }

        /**
         * End of configuration
         * Create the benchmark
         */


        _application.getCurrentActivity().updateDialog(0,-1,"Configuration in progress", "Configuration download...");

    }

    protected void onProgressUpdate(String... progress) {
        _application.getCurrentActivity().updateDialog(0, -1, "Configuration in progress", progress[0]);
    }

    protected Boolean doInBackground(Void... urls) {


        /* Download UID and dataset list in exchange of first log information */

        String url = START_URL;
        String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
        String query;


        try {
            query = String.format("version=%d&q=%s",  BuildConfig.VERSION_CODE,  URLEncoder.encode(MessageLog._message, charset));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return true;
        }

        SLAMConfiguration entries;

        int max_try = MAX_TRY_COUNT;

        while (max_try > 0) {
            this.publishProgress(_application.getResources().getString(R.string.start_configuration_download));
            try {
                URLConnection urlConnection;
                urlConnection = new URL(url).openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
                urlConnection.setRequestProperty("Accept-Charset", charset);
                urlConnection.setRequestProperty("Accept-Encoding", "identity");
                //urlConnection.connect();
                final OutputStream outputStream;

                outputStream = urlConnection.getOutputStream();
                outputStream.write(query.getBytes(charset));
                outputStream.flush();
                outputStream.close();
                final InputStream inputStream = urlConnection.getInputStream();
                int contentLength = urlConnection.getContentLength();
                if (contentLength == -1) {
                    Log.e(SLAMBenchApplication.LOG_TAG, "unknown configuration content length");
                } else {
                    Log.i(SLAMBenchApplication.LOG_TAG, "configuration content length: " + contentLength + " bytes");
                }

                SLAMBenchXmlParser parser = new SLAMBenchXmlParser();
                entries = parser.parse(inputStream);


                inputStream.close();
                this.publishProgress(_application.getResources().getString(R.string.finish_configuration_download));
                SLAMBenchApplication.setBenchmark(new SLAMBench(entries));
                if (entries == null ) {
                    Log.e(SLAMBenchApplication.LOG_TAG,  _application.getString(R.string.log_configuration_error));
                    return false;
                }
                return true;


            } catch (IOException e) {
                Log.e(SLAMBenchApplication.LOG_TAG, _application.getString(R.string.log_configuration_error), e);
                this.publishProgress(_application.getResources().getString(R.string.try_configuration_download_again, max_try));
                max_try -= 1;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    Log.e(SLAMBenchApplication.LOG_TAG, _application.getString(R.string.log_configuration_error), e1);
                }
            } catch (XmlPullParserException e1) {
                Log.e(SLAMBenchApplication.LOG_TAG,  _application.getString(R.string.log_configuration_error), e1);
                return false;
            }
        }



        try {
            InputStream inputStream = this._application.getAssets().open("demo.xml");
            SLAMBenchXmlParser parser = new SLAMBenchXmlParser();
            entries = parser.parse(inputStream);
                inputStream.close();
                this.publishProgress(_application.getResources().getString(R.string.finish_configuration_download));
                SLAMBenchApplication.setBenchmark(new SLAMBench(entries));
                if (entries == null ) {
                   Log.e(SLAMBenchApplication.LOG_TAG,  _application.getString(R.string.log_configuration_error));
                    return false;
                }
        } catch (IOException e) {
            Log.e(SLAMBenchApplication.LOG_TAG, _application.getString(R.string.log_configuration_error), e);
            return false;
        } catch (XmlPullParserException e) {
            Log.e(SLAMBenchApplication.LOG_TAG, _application.getString(R.string.log_configuration_error), e);
            return false;
        }

        Log.e(SLAMBenchApplication.LOG_TAG,  _application.getString(R.string.debug_local_configuration_file_used));
        return true;

    }

    protected void onPostExecute(Boolean res)
    {

        MessageLog.addDebug(_application.getString(R.string.debug_end_of_configuration));
        if (_application.getCurrentActivity() == null) {
            MessageLog.addDebug(_application.getString(R.string.debug_flaw_in_the_system));
        } else {
            _application.getCurrentActivity().closeDialog();
            if (_application.getCurrentActivity() instanceof OnConfigurationTaskTerminateListener) {
                ((OnConfigurationTaskTerminateListener) _application.getCurrentActivity()).onConfigurationTaskTerminate();
            }
        }
    }

    public interface OnConfigurationTaskTerminateListener {
        void onConfigurationTaskTerminate();
    }

    public class ConfigurationResult {
        public final Stack<KFusion.ImplementationId> available_implementations = new Stack<>();
        public String architectureName;
        public boolean callWorked;
        public boolean libraryLoaded;
        public boolean rootAvailable;
        public String ocl_driver;
        public double power;
        public double capacity;
        public double full_capacity;
        public double charge;
        public double current;
        public double temp;
        public double voltage;

    }
}
