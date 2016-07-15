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


public class ReleaseKFusionTask extends AsyncTask<Void, Void, String> {

    private final SLAMBenchApplication _application;
    private boolean _success = false;

    public ReleaseKFusionTask(Application application) {
        super();
        if (application instanceof SLAMBenchApplication) {
            this._application = (SLAMBenchApplication) application;
        } else {
            this._application = null;
        }
    }

    protected String doInBackground(Void... urls) {
        _success = KFusion.kfusion_release();
        if (_success)
            return "KFusion context is released.";
        else
            return "Fail to release KFusion context.";
    }

    protected void onPostExecute(String result) {
        MessageLog.addDebug(_application.getString(R.string.debug_end_of_release));
        if (_application.getCurrentActivity() == null) {
            MessageLog.addDebug(_application.getString(R.string.debug_flaw_in_the_system));
        } else if (_application.getCurrentActivity() instanceof OnReleaseKFusionTaskTerminateListener) {
            ((OnReleaseKFusionTaskTerminateListener)_application.getCurrentActivity()).OnReleaseKFusionTaskTerminate(new ReleaseKFusionResult(_success, result));
        }
    }

    public interface OnReleaseKFusionTaskTerminateListener {
        void OnReleaseKFusionTaskTerminate(ReleaseKFusionResult output);


    }

    public class ReleaseKFusionResult {
        public final boolean success;
        public final String message;
        public final Scenario.ScenarioReply reply;

        public ReleaseKFusionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.reply = success ? Scenario.ScenarioReply.SUCCESS : Scenario.ScenarioReply.FAILURE;
        }
    }
}
