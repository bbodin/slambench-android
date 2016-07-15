package project.pamela.slambench.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.appcompat.BuildConfig;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import project.pamela.slambench.R;
import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.models.SLAMMode;
import project.pamela.slambench.utils.ConfigurationTask;
import project.pamela.slambench.utils.MessageLog;
import project.pamela.slambench.utils.MobileStats;
import project.pamela.slambench.utils.RootUtil;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */


public class MainActivity extends CommonActivity implements View.OnClickListener  , ConfigurationTask.OnConfigurationTaskTerminateListener{


    private static final int MIN_CACHE_SIZE = 200;
    private static boolean   configuration_launched = false;
    private static boolean   configuration_finished = false;

    Button _runButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_oncreate_happened, this.getLocalClassName()));
        setContentView(R.layout.activity_main);

        _runButton = (Button) findViewById(R.id.runButton);

        TextView message = (TextView) findViewById(R.id.descriptionView);
        message.setMovementMethod(new ScrollingMovementMethod());

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String text_msg = getResources().getString(R.string.app_description) + " ";

        text_msg += "This is the version " + (pInfo != null ? pInfo.versionName : "unknown") + " (" + BuildConfig.BUILD_TYPE +  ") of the benchmark." + "<br/>";

        text_msg += "<b>" + getString(R.string.app_warning) + "</b>" + "<br/>";

        if (RootUtil.isDeviceRooted()) {
            text_msg +=  "<font color='red'> <b>" + getResources().getText(R.string.live_mode_available) + "</b></font>" + "<br/>";
        }

        long need_size    = MIN_CACHE_SIZE;
        long current_size =  (MobileStats.getAvailableInternalMemorySize() + MobileStats.getUsedInternalMemorySize(this)) / (1024 * 1024);

        if (current_size < need_size) {
            text_msg += "<font color='red'> <b>" + getResources().getString(R.string.insufficient_memory, need_size, current_size) + "</b></font>" + "<br/>";
        }

        message.setText(Html.fromHtml(text_msg + ""), TextView.BufferType.SPANNABLE);

        _runButton.setOnClickListener(this);
        Log.d(SLAMBenchApplication.LOG_TAG, "Main activity content constructed.");


    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.runButton:
                configuration_finished = false;
                configuration_launched  = true;
                MessageLog.clear();
                Log.d(SLAMBenchApplication.LOG_TAG, " Button push in the Main activity");
                new ConfigurationTask(this.getApplication()).execute();
                v.setEnabled(false);
        }
    }


    void selectMode () {

        if (SLAMBenchApplication.getBenchmark() == null) {
            openError(getString(R.string.error_fail_configuration_download));
            return;
        }
        if (SLAMBenchApplication.getBenchmark().getAvailableModes().size() == 0) {
            openError(getString(R.string.error_fail_configuration_download));
            return;
        }
        ArrayList names = new ArrayList(SLAMBenchApplication.getBenchmark().getAvailableModes().size());

        for (SLAMMode m : SLAMBenchApplication.getBenchmark().getAvailableModes()) {
            names.add(m.get_name() + " (" + m.get_description() + ")");

        }

        AlertDialog.Builder  alertDialog = new AlertDialog.Builder(MainActivity.this);
        LinearLayout llayout = new LinearLayout(this);
        ListView lv = new ListView(this);



        llayout.addView(lv);
        alertDialog.setView(llayout);
        alertDialog.setTitle(getString(R.string.title_select_mode));
        alertDialog.setCancelable(false);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,names);
        lv.setAdapter(adapter);
        final AlertDialog d = alertDialog.show();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                MessageLog.addInfo(getString(R.string.msg_select_mode,SLAMBenchApplication.getBenchmark().getAvailableModes().get(position).get_name()));
                runBenchmarkMode(position);
                d.dismiss();




            }
        });
    }


    void runBenchmarkMode (final int position) {

        if (SLAMBenchApplication.getBenchmark() == null) {
            openError(getResources().getString(R.string.error_benchmark_not_found));
            return;
        }

        if (SLAMBenchApplication.getCurrentTest() != null) {
            openError(getResources().getString(R.string.error_benchmark_in_progress));
            return;
        }

        if (SLAMBenchApplication.getBenchmark().getMessage() != null) {


        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(getString(R.string.title_information));
        alertDialog.setMessage(SLAMBenchApplication.getBenchmark().getMessage());
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.button_start),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        SLAMBenchApplication.resetBenchmark();
                        Intent intent = new Intent(MainActivity.this, BenchmarkActivity.class);
                        intent.putExtra(SLAMBenchApplication.SELECTED_MODE_TAG, position);
                        startActivity(intent);
                    }
                });
        alertDialog.show();

        } else {
            SLAMBenchApplication.resetBenchmark();
            Intent intent = new Intent(this, BenchmarkActivity.class);
            intent.putExtra(SLAMBenchApplication.SELECTED_MODE_TAG, position);
            startActivity(intent);
        }


    }

    @Override
    public void onConfigurationTaskTerminate() {
        configuration_finished = true;
        _runButton.setEnabled(true);
        selectMode();
    }
}
