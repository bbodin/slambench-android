package project.pamela.slambench.activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.opengl.GLES10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Debug;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import project.pamela.slambench.R;
import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.jni.KFusion;
import project.pamela.slambench.models.SLAMTest;
import project.pamela.slambench.utils.ComputeFrameResult;
import project.pamela.slambench.utils.ComputeFrameTask;
import project.pamela.slambench.utils.FilesDownloadTask;
import project.pamela.slambench.utils.InitializeKFusionTask;
import project.pamela.slambench.utils.KFusionRenderer;
import project.pamela.slambench.utils.MessageLog;
import project.pamela.slambench.utils.ReleaseKFusionTask;
import project.pamela.slambench.utils.Scenario;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */



public class BenchmarkActivity extends CommonActivity implements FilesDownloadTask.FilesDownloadTaskListener, ComputeFrameTask.OnComputeFrameTaskTerminateListener, InitializeKFusionTask.OnInitializeKFusionTaskTerminateListener, ReleaseKFusionTask.OnReleaseKFusionTaskTerminateListener {

    private static final String TRAJ_EXTENSION = ".traj";
    // In case benchmark is killed, these value stay on !
    private static Scenario scenario               = null;
    private static boolean  download_launched      = false;
    private static boolean  download_done          = false;
    private static boolean  end_of_benchmark       = false;
    private static boolean  wait_for_resume        = false;
    private static boolean  visible                = false;

    private GLSurfaceView _glview      = null;
    private ProgressBar   _progressBar = null;
    private TextView      _currentJob  = null;

    private int     _selected_mode_index = -1;

    private final int MEMORY_LIMIT = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_oncreate_happened, this.getLocalClassName()));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_benchmark);

        _progressBar = (ProgressBar) findViewById(R.id.progressBar);
        _currentJob = (TextView) findViewById(R.id.currentJob);
        _glview = (GLSurfaceView) findViewById(R.id.surfaceView);

        TextView _messageView = (TextView) findViewById(R.id.message);
        _messageView.setMovementMethod(new ScrollingMovementMethod());
        MessageLog.setTextView(_messageView);

        _glview.setEGLContextClientVersion(2);
        _glview.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        _glview.setRenderer(new KFusionRenderer());
        _glview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // Only draw when required


        updateTest();

        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_activity_constructed,this.getLocalClassName()));

        // Retrieve bench number
        Intent mIntent = this.getIntent();
        _selected_mode_index = mIntent.getIntExtra(SLAMBenchApplication.SELECTED_MODE_TAG, 0);


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_onstop_happened,this.getLocalClassName()));

    }

    @Override
    protected void onPause() {
        super.onPause();
        _glview.onPause();
        visible = false;
        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_onpause_happened, this.getLocalClassName()));
    }

    @Override
    public void onBackPressed() {
        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_onbackpressed_happened, this.getLocalClassName()));
        emergencyStop(getString(R.string.back_button_pressed));
    }

    @Override
    protected void onResume() {

        super.onResume();
        _glview.onResume();
        visible = true;
        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_onresume_happened, this.getLocalClassName()));

        /**
         * Create benchmark if not (at start only)
         */

        if (end_of_benchmark) {

            Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_reset_benchmark));

            end_of_benchmark   = false;
            download_launched = false;
            download_done     = false;

        }

        if (download_launched) {
            if (download_done) {
               if (wait_for_resume) {
                 wait_for_resume = false;
                 runState();
              }
            }
        } else {
            download_launched = true;
            prepareBenchmark(_selected_mode_index);
        }


    }


    /**
     * Update the GUI depending of the scenario
     */
    private void updateTest() {
        if (scenario == null || SLAMBenchApplication.getBenchmark() == null) return;
        if (Looper.myLooper() != Looper.getMainLooper()) return;

        String text = String.format(getString(R.string.msg_current_job), scenario.getTest().name, SLAMBenchApplication.remainTestCount());
        _currentJob.setText(text);
        _progressBar.setProgress(scenario.getCurrentFrame());
        _progressBar.setMax(scenario.getTotalFrame());

    }


    /**
     * This function affect scenario or run the result activity
     * Call runState !!
     */
    private void selectNewTest() {

        if (SLAMBenchApplication.getBenchmark() == null) {
            emergencyStop(getString(R.string.benchmark_error));
            return;
        }

        if ((scenario != null) && (!scenario.isFinsh())) {
            emergencyStop(getString(R.string.benchmark_error));
            return;
        }

        if (SLAMBenchApplication.selectNextTest()) {
            scenario = new Scenario(SLAMBenchApplication.getCurrentTest());
            runState();
        } else {
            // benchmark exists but no more test to run
            scenario = null;
            MessageLog.addInfo(getString(R.string.info_end_of_benchmark));
            MessageLog.addDebug(getString(R.string.info_total_cpu_overhead,Float.valueOf(Debug.threadCpuTimeNanos()) / 1e+9));

            goResultActivity();

        }

    }

    private void goResultActivity() {
        end_of_benchmark = true;
        Intent intent = new Intent(this, ResultActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // call this to finish the current activity
    }

    /**
     * Should only be execute by runState
     */
    private void emergencyStop(String reason) {

        scenario = null;
        MessageLog.addWarning(String.format(getString(R.string.benchmark_stopped), reason));
        // in any case ...
        new ReleaseKFusionTask(this.getApplication()).execute();
        goResultActivity();

    }

    /**
     * Set the next step of the scenario
     * and load activity depending on ,
     * if START_OF_PROGRAM => do nothing (may finish with no clues)
     * if END_OF_PROGRAM => save result , load next scenario , and run State again. (may loop forever ... )
     */
    private void runState() {

        if (!visible) {
            wait_for_resume = true;
            return;
        }

        if ((scenario == null)) {
            return;
        }

        Scenario.ScenarioState next;

        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_go_next_step));

        try {
            next = scenario.goNextState();
        } catch (Exception e) {
            Log.e(SLAMBenchApplication.LOG_TAG, getString(R.string.error_unknown_state),e);
            emergencyStop(getString(R.string.benchmark_error));
            return;
        }

        MessageLog.addDebug(getString(R.string.debug_state_timing) , next.toString() , Float.valueOf(Debug.threadCpuTimeNanos()) / 1e+9 ,  System.nanoTime() / 1e+9);


        switch (next) {

            case START_OF_PROGRAM:
                // nothing to do at start
                try {
                    scenario.finishState(Scenario.ScenarioReply.SUCCESS);
                } catch (Exception e) {
                    Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.error_start_of_program) , e);
                }
                break;

            case KFUSION_FRAME_EXECUTION:
                new ComputeFrameTask(this.getApplication()).execute();
                break;

            case KFUSION_INITILISATION:

                if (!this.memoryOK()) { // TODO : place it somewhere else
                    emergencyStop(getString(R.string.error_no_memory_left));
                    return;
                }
                new InitializeKFusionTask(this.getApplication(), scenario.getTest()).execute();
                break;

            case FREE_MEMORY:
                new ReleaseKFusionTask(this.getApplication()).execute();
                break;
            case END_OF_PROGRAM:
                try {
                    scenario.finishState(Scenario.ScenarioReply.SUCCESS);
                    String gt_file = new File(getCacheDir() + File.separator + scenario.getTest().dataset.getFilename() + TRAJ_EXTENSION).getAbsolutePath();
                    SLAMBenchApplication.queueResult(scenario.getResult(gt_file));
                    selectNewTest();

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(SLAMBenchApplication.LOG_TAG, getString(R.string.error_end_of_program) , e);
                }
                break;

            default:
                Log.e(SLAMBenchApplication.LOG_TAG, getString(R.string.error_unknown_state));
                emergencyStop(getString(R.string.benchmark_error));
                break;
        }
        updateTest();
    }



    public void prepareBenchmark(int mode_index) {

        /**
         *  Configuration is done, ready to use the benchmark object
         */


        printAvailableMemory();

        if (SLAMBenchApplication.getBenchmark() == null) {
            MessageLog.addError(getString(R.string.error_network));
            emergencyStop(getString(R.string.error_network));
            return;
        }

        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_populate_announcement));

        String skipped = "";
        ArrayList<String> dataset_filename_to_download = new ArrayList<>();
        ArrayList<Integer> dataset_filesize_to_download = new ArrayList<>();
        ArrayList<String> dataset_md5_to_download = new ArrayList<>();

        SLAMBenchApplication.resetTestQueue();

        for (SLAMTest t : SLAMBenchApplication.getBenchmark().getModeTests(mode_index)) {
            boolean valid = false;
            for (KFusion.ImplementationId n : KFusion.getImplementations()) {
                if (n.equals(t.implementation)) {

                    valid = true;
                    Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_configuration_queue , t.name));
                    SLAMBenchApplication.queueTest(t);

                    if (!dataset_filename_to_download.contains(t.dataset.getFilename())) {
                        dataset_filename_to_download.add(t.dataset.getFilename());
                        dataset_md5_to_download.add(t.dataset.getRawMD5());
                        dataset_filesize_to_download.add(t.dataset.getFileSize());

                        dataset_filename_to_download.add(t.dataset.getFilename() + TRAJ_EXTENSION);
                        dataset_md5_to_download.add(t.dataset.getTrajMD5());
                        dataset_filesize_to_download.add(t.dataset.getTrajSize());

                    }

                    break;
                }
            }
            if (!valid) {
                skipped += ", " + t.name;
            }
        }

        if (skipped.length() > 0) {
            MessageLog.addWarning(getResources().getText(R.string.these_tasks_are_ignored) + skipped + ".");
        }

        /**
         * Now the benchmark object is created, we can select a first scenario.
         */



        String[] array1 = dataset_filename_to_download.toArray(new String[dataset_filename_to_download.size()]);
        String[] array2 = dataset_md5_to_download.toArray(new String[dataset_md5_to_download.size()]);
        Integer[] array3 = dataset_filesize_to_download.toArray(new Integer[dataset_filesize_to_download.size()]);

        new FilesDownloadTask(this.getApplication(),array1,array2, array3).execute();

    }


    @Override
    public void onComputeFrameTaskTerminate(ComputeFrameResult output) {

        if ((scenario == null)) {
            MessageLog.addDebug(getString(R.string.debug_scenario_null));
            return;
        }
        if (!output.success) {
            MessageLog.addError(output.message);
            emergencyStop("benchmark error");
            return;
        }

        try {
            Log.d(SLAMBenchApplication.LOG_TAG, "onTaskTerminate, Task result = " + output);
            scenario.process_computation_result(output);


        } catch (Exception e) {
            openError("onTaskTerminate finishState Exception = " + e.getMessage());
            emergencyStop("benchmark error");
            return;
        }

        _glview.requestRender();
        this.runState();
    }



    @Override
    public void OnFilesDownloadTaskTerminate(FilesDownloadTask.FileLoadResult output) {

        download_done = true;

        if (output.success) {
            MessageLog.addSuccess("Download succeeded");
            selectNewTest();
        } else {
            MessageLog.addError(getString(R.string.msg_dataset_download_failed));
            openError(getString(R.string.msg_dataset_download_failed));
        }

    }

    @Override
    public void OnInitializeKFusionTaskTerminate(InitializeKFusionTask.InitializeKFusionResult output) {
        if ((scenario == null)) {
            MessageLog.addDebug(getString(R.string.debug_scenario_null));
            return;
        }

        try {
            Log.d(SLAMBenchApplication.LOG_TAG, "onTaskTerminate, Task result = " + output.toString());
            scenario.finishState(output.reply);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(SLAMBenchApplication.LOG_TAG, "onTaskTerminate finishState Exception = " + e.getMessage());
        }
        if (output.success) {
            MessageLog.addSuccess(output.message);
            MessageLog.addInfo(KFusion.kfusion_info());
        } else MessageLog.addError(output.message);

        this.runState();
    }

    @Override
    public void OnReleaseKFusionTaskTerminate(ReleaseKFusionTask.ReleaseKFusionResult output) {

        if ((scenario == null)) {
            return;
        }
        try {
            scenario.finishState(output.reply);
        } catch (Exception e) {
            Log.e(SLAMBenchApplication.LOG_TAG, "onTaskTerminate finishState Exception = " + e.getMessage(), e);
        }
        if (output.success) {
            MessageLog.addSuccess(output.message);
        } else {
            MessageLog.addError(output.message);
        }


        printAvailableMemory();

        this.runState();
    }

    private boolean memoryOK() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);

        Debug.MemoryInfo md = new Debug.MemoryInfo();
        Debug.getMemoryInfo(md);
        Runtime rt = Runtime.getRuntime();
        long used_memory = md.getTotalPss() / 1024;
        long max_memory = rt.maxMemory() / (1024 * 1024);
        return true;
        //return (max_memory - used_memory) > MEMORY_LIMIT;
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
