package project.pamela.slambench.utils;

import android.app.Application;
import android.os.AsyncTask;

import project.pamela.slambench.R;
import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.jni.KFusion;
import project.pamela.slambench.models.SLAMTest;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class InitializeKFusionTask extends AsyncTask<Void, Void, String> {

    private final SLAMBenchApplication _application;
    private SLAMTest _test = null;
    private boolean _success = false;
    public InitializeKFusionTask(Application application, SLAMTest t) {
        super();
        if (application instanceof SLAMBenchApplication) {
            this._application = (SLAMBenchApplication) application;
        } else {
            this._application = null;
        }
        this._test = t;
    }

    protected String doInBackground(Void... urls) {
        if (_test != null) {

            String arguments = "kfusion " + _test.arguments.value;


            if ((_test.dataset != null)) {
                if (!(_test.dataset.getFilename().equals(""))) {
                    arguments += " " + ("-i");
                    arguments += " " + (_application.getCacheDir().getAbsolutePath() + "/" + _test.dataset.getFilename());
                }
            }
            _success = KFusion.kfusion_init(_test.implementation, arguments.split(" "));
        } else {
            _success = false;
            return "Test has been initialized because test variable was not !";
        }
        if (_success)
            return "KFusion context is ready.";
        else
            return "Fail to initialize KFusion context.";
    }

    protected void onPostExecute(String result) {

        MessageLog.addDebug(_application.getString(R.string.debug_end_of_initialization));
        if (_application.getCurrentActivity() == null) {
            MessageLog.addDebug(_application.getString(R.string.debug_flaw_in_the_system));
        } else if (_application.getCurrentActivity() instanceof OnInitializeKFusionTaskTerminateListener) {
            ((OnInitializeKFusionTaskTerminateListener)_application.getCurrentActivity()).OnInitializeKFusionTaskTerminate(new InitializeKFusionResult(_success, result));
        }
    }

    public interface OnInitializeKFusionTaskTerminateListener {
        void OnInitializeKFusionTaskTerminate(InitializeKFusionResult output);
    }

    public class InitializeKFusionResult {
        public final boolean success;
        public final String message;
        public final Scenario.ScenarioReply reply;

        public InitializeKFusionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.reply = success ? Scenario.ScenarioReply.SUCCESS : Scenario.ScenarioReply.FAILURE;
        }
    }
}
