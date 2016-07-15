package project.pamela.slambench.activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Debug;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import project.pamela.slambench.R;
import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.jni.Commands;
import project.pamela.slambench.jni.KFusion;
import project.pamela.slambench.models.SLAMBench;
import project.pamela.slambench.models.SLAMResult;
import project.pamela.slambench.models.SLAMTest;
import project.pamela.slambench.utils.ComputeFrameResult;
import project.pamela.slambench.utils.ComputeFrameTask;
import project.pamela.slambench.utils.InitializeKFusionTask;
import project.pamela.slambench.utils.KFusionRenderer;
import project.pamela.slambench.utils.MessageLog;
import project.pamela.slambench.utils.ReleaseKFusionTask;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class LiveActivity extends CommonActivity implements View.OnClickListener, InitializeKFusionTask.OnInitializeKFusionTaskTerminateListener, ReleaseKFusionTask.OnReleaseKFusionTaskTerminateListener, ComputeFrameTask.OnComputeFrameTaskTerminateListener {


    private static final String MODULES_XML_PATH = "/data/ni/modules.xml";
    private static final String OPEN_NI_INI_PATH = "/data/ni/OpenNI.ini";
    private static final String GLOBAL_DEFAULTS_KINECT_INI_PATH = "/data/ni/GlobalDefaultsKinect.ini";

    private static boolean _initialization_launched = false;
    private static boolean _visible                 = false;
    private static boolean _end_of_stream           = false;
    private static boolean _wait_for_resume         = false;
    private static SLAMResult _result               = null;

    private GLSurfaceView _glview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_oncreate_happened, this.getLocalClassName()));

        setContentView(R.layout.activity_benchmark);

        LinearLayout l = (LinearLayout) findViewById(R.id.benchmark_top_layout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        Button stop = new Button(this);
        stop.setText(getResources().getText(R.string.stopButton));
        stop.setId(R.id.stopButton);
        stop.setOnClickListener(this);
        l.addView(stop, 1, params);

        TextView _currentJob = (TextView) findViewById(R.id.currentJob);
        _glview = (GLSurfaceView) findViewById(R.id.surfaceView);

        _currentJob.setText(getString(R.string.txt_live_jobname));
        TextView _messageView = (TextView) findViewById(R.id.message);
        _messageView.setMovementMethod(new ScrollingMovementMethod());
        MessageLog.setTextView(_messageView);

        LinearLayout layout = (LinearLayout) findViewById(R.id.benchmark_activity_layout);
        layout.setOnClickListener(this);

        _glview.setEGLContextClientVersion(2);
        _glview.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        _glview.setRenderer(new KFusionRenderer());
        _glview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // Only draw when required


    }

    private boolean checkCamera() {

        // FIRST CHECK USB DEVICES
        boolean kinect_found = false;
        boolean xtion_found = false;
        boolean kinect_readable = false;
        boolean openni_modules_config_exists = false;
        boolean openni_kinect_config_exists = false;
        UsbDevice _camera = null;

        UsbManager manager = (UsbManager) getSystemService(USB_SERVICE);

        Iterator<Map.Entry<String, UsbDevice>> it = manager.getDeviceList().entrySet().iterator();
        System.out.println(String.format("checkCamera."));
        while (it.hasNext()) {
            System.out.println(String.format("Test USB ... ."));
            Map.Entry<String, UsbDevice> pair = it.next();
            UsbDevice device = pair.getValue();
            System.out.println(pair.getKey() + " = " + pair.getValue() + " HasPermission=" + manager.hasPermission(device));
            if ((device.getProductId() == 685) && (device.getVendorId() == 1118)) {
                MessageLog.addInfo("Audio Kinect device found");
            } else if ((device.getProductId() == 686) && (device.getVendorId() == 1118)) {
                MessageLog.addSuccess("Video Kinect device found");
                _camera = device;
                kinect_found = true;
            } else if ((device.getProductId() == 688) && (device.getVendorId() == 1118)) {
                MessageLog.addInfo("Motor Kinect device found");
            } else if ((device.getProductId() == 1536) && (device.getVendorId() == 7463)) {
                MessageLog.addSuccess("Video Asus Xtion Pro Live (old version) device found");
                _camera = device;
                xtion_found = true;
            } else if ((device.getProductId() == 1537) && (device.getVendorId() == 7463)) {
                MessageLog.addSuccess("Video Asus Xtion Pro Live (new version) device found");
                _camera = device;
                xtion_found = true;
            } else {
                MessageLog.addInfo(getString(R.string.msg_unknow_device));
            }

            it.remove(); // avoids a ConcurrentModificationException
        }

        if (_camera == null) {
            openError(getString(R.string.msg_no_sensor_found));
            return false;
        }


        String configurationFile = GLOBAL_DEFAULTS_KINECT_INI_PATH;

        if (xtion_found) {
            configurationFile = OPEN_NI_INI_PATH;
        }

        // try to fix the kinect access

        Process p;
        try {

            // Preform su to get root privileges
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());

            // Set the driver path name
            String module_path = "/system/lib";
            if (new File(this.getFilesDir().getParentFile().getPath() + "/lib/libXnDeviceSensorV2.so").canRead()) {
                module_path = this.getFilesDir().getParentFile().getPath() + "/lib/";
            }
            if (new File(this.getApplicationInfo().nativeLibraryDir + "/libXnDeviceSensorV2.so").canRead()) {
                module_path = this.getApplicationInfo().nativeLibraryDir;
            }

            // write a new  module file.
            os.writeBytes("rm -rf /data/ni\n");
            os.writeBytes("mkdir -p /data/ni\n");
            os.writeBytes("echo \"<Modules>\" > /data/ni/modules.xml\n");
            os.writeBytes("echo \"    <Module path='" + module_path + "/libnimMockNodes.so' />\" >> /data/ni/modules.xml\n");
            os.writeBytes("echo \"    <Module path='" + module_path + "/libnimCodecs.so' />\" >> /data/ni/modules.xml\n");
            os.writeBytes("echo \"    <Module path='" + module_path + "/libnimRecorder.so' />\" >> /data/ni/modules.xml\n");
            os.writeBytes("echo \"    <Module path='" + module_path + "/libXnDeviceSensorV2.so' configDir='/data/ni/' />\" >> /data/ni/modules.xml\n");
            os.writeBytes("echo \"    <Module path='" + module_path + "/libXnDeviceFile.so' configDir='/data/ni/' />\" >> /data/ni/modules.xml\n");
            os.writeBytes("echo \"</Modules>\"  >> /data/ni/modules.xml\n");
            os.writeBytes("chmod 755 /data/ni/modules.xml\n");
            os.writeBytes("chmod 755 /data/ni/\n");


            // Write the  configuration files
            os.writeBytes("mkdir -p /data/ni\n");
            if (xtion_found) {
                os.writeBytes("echo \"[Log]\"               >  " + configurationFile + "\n");
                os.writeBytes("echo \"Verbosity=0\"         >> " + configurationFile + "\n");
                os.writeBytes("echo \"LogToConsole=1\"         >> " + configurationFile + "\n");
                os.writeBytes("echo \"LogToFile=0\"         >> " + configurationFile + "\n");
            } else {
                os.writeBytes("echo \"\"               >  " + configurationFile + "\n");
            }
            os.writeBytes("echo \"[Device]\"               >>  " + configurationFile + "\n");
            if (kinect_found) {
                os.writeBytes("echo \"UsbInterface=1\"         >> " + configurationFile + "\n");
            }
            if (xtion_found) {
                os.writeBytes("echo \"UsbInterface=2\"         >> " + configurationFile + "\n");
                os.writeBytes("echo \"[Drivers]\"               >>  " + configurationFile + "\n");
                os.writeBytes("echo \"Repository=" + module_path + "\" >> " + configurationFile + "\n");
            }
            os.writeBytes("chmod 755 " + configurationFile + "\n");


            // Get full access to USB (FIXME: need to only use the kinect camera but not for the moment)
            // will be replace by new File(_camera.getDeviceName()).absolutepath
            os.writeBytes("chmod 777 /dev/bus/usb/*/*\n");


            // Close the terminal
            os.writeBytes("exit\n");
            os.flush();


            try {
                p.waitFor();
                if (p.exitValue() != 255) {
                    Log.i(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_root_command_worked));
                } else {
                    Log.e(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_root_command_failed));
                }
            } catch (InterruptedException e) {
                Log.e(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_root_command_failed) , e);
            }
        } catch (IOException e) {
            Log.e(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_root_command_failed), e);
        }


        // Now check anyway


        if (new File(_camera.getDeviceName()).canRead()) {
            MessageLog.addInfo(getString(R.string.msg_right_device));
            kinect_readable = true;
        } else {
            MessageLog.addError(getString(R.string.msg_no_right_device));
        }


        if (new File(MODULES_XML_PATH).canRead()) {
            MessageLog.addInfo(getString(R.string.msg_right_module_file));
            openni_modules_config_exists = true;
        } else  {
            MessageLog.addError(getString(R.string.msg_no_right_module_file));
        }

        if (new File(configurationFile).canRead()) {
            MessageLog.addInfo(getString(R.string.msg_right_sensor_file));
            openni_kinect_config_exists = true;
        } else {
            MessageLog.addError(getString(R.string.msg_no_right_sensor_file));
        }


        if (kinect_readable && openni_kinect_config_exists && openni_modules_config_exists) {
            MessageLog.addInfo(getString(R.string.msg_live_can_be_done));
            return true;
        } else {
            MessageLog.addError(getString(R.string.msg_live_can_not_be_done));
            return false;
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.stopButton:
                _end_of_stream = true;
                break;
            default:
                break;
        }
    }


    @Override
    protected void onResume() {

        super.onResume();
        _glview.onResume();

        _visible = true;

        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_onresume_happened, this.getLocalClassName()));

        if (_initialization_launched) {
                if (_wait_for_resume) {
                    _wait_for_resume = false;
                    new ComputeFrameTask(this.getApplication()).execute();
                }
        } else {
            _initialization_launched = true;

            if (!checkCamera()) {
                return;
            }

            SLAMTest live_mode;
            boolean openclReady = Commands.is_opencl_available();

            if (openclReady) {
                live_mode = new SLAMTest(getString(R.string.opencl_live_test_name), KFusion.ImplementationId.KFUSION_OCL, SLAMBench.arg_live, new SLAMBench.Dataset("","",0,0,"","",0));
            } else {
                live_mode = new SLAMTest(getString(R.string.openmp_live_test_name), KFusion.ImplementationId.KFUSION_OMP, SLAMBench.arg_live, new SLAMBench.Dataset("","",0,0,"","",0));
            }
            _result = new SLAMResult(live_mode);

            new InitializeKFusionTask(this.getApplication(), live_mode).execute();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        _glview.onPause();
        _visible = false;
        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_onpause_happened, this.getLocalClassName()));
    }


    @Override
    protected void onStop() {
        super.onStop();
        _visible = false;
        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_onstop_happened, this.getLocalClassName()));
    }

    @Override
    public void onBackPressed() {
        _end_of_stream = true;
        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_onbackpressed_happened, this.getLocalClassName()));
    }
    @Override
    public void onComputeFrameTaskTerminate(ComputeFrameResult output) {

        if (!output.success) {
            MessageLog.addError(output.message);
            openError(getString(R.string.debug_ontaskterminate_error));
            _end_of_stream = true;
        }


        if (output.end_of_file || _end_of_stream) {
            _end_of_stream = true;
            _result.finishIt(null);
            new ReleaseKFusionTask(this.getApplication()).execute();


        } else {

            if (!_visible) {
                _result.addFrameResult(output);
                _wait_for_resume = true;
                return;
            } else {
                new ComputeFrameTask(this.getApplication()).execute();
                _result.addFrameResult(output);
            }
            _glview.requestRender();
        }
    }


    @Override
    public void OnInitializeKFusionTaskTerminate(InitializeKFusionTask.InitializeKFusionResult output) {

        if (output.success) {
            MessageLog.addSuccess(output.message);
            if (!_end_of_stream) {
                new ComputeFrameTask(this.getApplication()).execute();
            }
        } else {
            MessageLog.addError(output.message);
            openError(getString(R.string.msg_live_init_error));
            return;
        }

        _glview.requestRender();

        printAvailableMemory();
    }

    @Override
    public void OnReleaseKFusionTaskTerminate(ReleaseKFusionTask.ReleaseKFusionResult output) {

        if (output.success) {
            MessageLog.addSuccess(output.message);
        } else {
            MessageLog.addError(output.message);
        }

        _glview.requestRender();

        SLAMBenchApplication.queueResult(_result);

        _initialization_launched = false;
        _visible                 = false;
        _end_of_stream           = false;
        _wait_for_resume         = false;
        _result               = null;

        printAvailableMemory();

        Intent intent = new Intent(this, ResultActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // call this to finish the current activity
    }
    private void printAvailableMemory() {


        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);

        Debug.MemoryInfo md = new Debug.MemoryInfo();
        Debug.getMemoryInfo(md);

        Runtime rt = Runtime.getRuntime();

        long availableMegs = mi.availMem / 1048576L;

        long used_memory = md.getTotalPss() / 1024;
        long max_memory = rt.maxMemory() / (1024 * 1024);

        MessageLog.addInfo("Used memory:" + Long.toString(used_memory) + "Mo" + " max app memory:" + Long.toString(max_memory) + "Mo" + " available memory:" + Long.toString(availableMegs) + "Mo");

        MessageLog.addDebug(
                " dalvikPss:" + Long.toString(md.dalvikPss) + "Ko" +
                        " nativePss:" + Long.toString(md.nativePss) + "Ko" +
                        " otherPss:" + Long.toString(md.otherPss) + "Ko"
        );

        MessageLog.addDebug(
                " NativeHeapAllocatedSize:" + Debug.getNativeHeapAllocatedSize() / 1048576L + "Mo" +
                        " NativeHeapSize:" + Debug.getNativeHeapSize() / 1048576L + "Mo");

    }

}
