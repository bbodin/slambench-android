package project.pamela.slambench.utils;

import android.app.Application;
import android.os.AsyncTask;

import project.pamela.slambench.R;
import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.jni.KFusion;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */


/**
 * Async Proxy to the JNI process function of KFusion
 */
public class ComputeFrameTask extends AsyncTask<Void, Void, Boolean> {


    private final SLAMBenchApplication _application;
    private final ComputeFrameResult _result;

    public ComputeFrameTask(Application application) {
        super();

        if (application instanceof SLAMBenchApplication) {
            this._application = (SLAMBenchApplication) application;
        } else {
            this._application = null;
        }

        this._result = new ComputeFrameResult();
    }

    protected Boolean doInBackground(Void... urls) {

        String process_line = KFusion.kfusion_process();

        _result.success = (process_line != null);

        if (!_result.success) {
            _result.message = "Computation Error.";
            _result.end_of_file = true;
            _result.reply = Scenario.ScenarioReply.FAILURE;
            return false;
        }


        if (process_line.equals("")) {
            _result.message = "No more frame to compute.";
            _result.end_of_file = true;
            _result.reply = Scenario.ScenarioReply.END_OF_FILE;
            return true;
        }


        /**
         * here we have a valid frame processed, we need to parse the result
         */

        String[] parts = process_line.split("\t");

        if (parts.length != KFusion.FieldIndex.values().length) {

            _result.message = "A frame has been computed but returned message is wrong. Found " + parts.length + " fields instead of " + KFusion.FieldIndex.values().length;
            _result.end_of_file = false;
            _result.reply = Scenario.ScenarioReply.FAILURE;
            return false;
        }

        _result.message = "A frame has been computed.";
        _result.end_of_file = false;
        _result.reply = Scenario.ScenarioReply.SUCCESS;
        _result.process_line = process_line;

        _result.frame = Integer.valueOf(parts[KFusion.FieldIndex.FRAME_NUMBER.getValue()]);

        _result.temperature = MobileStats.getInstance().getTemp();
        _result.current = MobileStats.getInstance().getCurrent();
        _result.voltage = MobileStats.getInstance().getVoltage();
        _result.power = MobileStats.getInstance().getPower();
        _result.freqs = MobileStats.getInstance().getCPUFrequencies();

        return true;
    }

    protected void onPostExecute(Boolean result) {
        if (_application.getCurrentActivity() == null) {
            MessageLog.addDebug(_application.getString(R.string.debug_flaw_in_the_system));
        } else if (_application.getCurrentActivity() instanceof OnComputeFrameTaskTerminateListener) {
            ((OnComputeFrameTaskTerminateListener)_application.getCurrentActivity()).onComputeFrameTaskTerminate(_result);
        }
    }

    public interface OnComputeFrameTaskTerminateListener {
        void onComputeFrameTaskTerminate(ComputeFrameResult output);
    }


}
