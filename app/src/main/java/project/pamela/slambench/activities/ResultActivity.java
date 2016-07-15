package project.pamela.slambench.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.TitlePageIndicator;

import java.util.List;
import java.util.Vector;

import project.pamela.slambench.R;
import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.fragments.RankPlot;
import project.pamela.slambench.fragments.ResultListFragment;
import project.pamela.slambench.utils.MyPagerAdapter;
import project.pamela.slambench.utils.UploadResultsTask;


/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */


public class ResultActivity extends CommonActivity implements View.OnClickListener, DialogInterface.OnClickListener, UploadResultsTask.OnUploadResultsTaskTerminateListener {




    public static final String SELECTED_TEST_TAG = "selected_test";
    public static final String SHARE_VIA = "Share via";

    private boolean want_to_share = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        // LAYOUT - LAYOUT - LAYOUT - LAYOUT - LAYOUT - LAYOUT

        super.onCreate(savedInstanceState);

        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_oncreate_happened, this.getLocalClassName()));

        setContentView(R.layout.activity_result);

        Button shareButton = (Button) findViewById(R.id.shareButton);
        shareButton.setOnClickListener(this);

        if (SLAMBenchApplication.getBenchmark() == null) {
            shareButton.setEnabled(false);
        }

        Button retryButton = (Button) findViewById(R.id.retryButton);
        retryButton.setOnClickListener(this);

        if (SLAMBenchApplication.getBenchmark() != null) {

            double best = SLAMBenchApplication.getBenchmark().getBestSpeed();

            TextView _score = (TextView) findViewById(R.id.scoreView);
            if (_score != null) {
                double res = (best == 0) ? 0 : 1.0 / best;
                _score.setText(String.format(getString(R.string.best_result), res ));
            }
        }


        //initialize the pager
        this.initialisePaging();

        Toast.makeText(getApplicationContext(), getString(R.string.msg_swipe_to_see_more), Toast.LENGTH_LONG).show();


    }

    /**
     * Initialise the fragments to be paged
     */
    private void initialisePaging() {

        List<Fragment> fragments = new Vector<>();
        fragments.add(Fragment.instantiate(this, ResultListFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, RankPlot.class.getName()));

        List<String> titles= new Vector<>();
        titles.add("Your results");
        titles.add("Devices ranking");
        /*
      maintains the pager adapter
     */
        MyPagerAdapter mPagerAdapter = new MyPagerAdapter(super.getSupportFragmentManager(), fragments , titles);
        //
        ViewPager pager = (ViewPager) super.findViewById(R.id.viewpager);
        pager.setAdapter(mPagerAdapter);

        //Bind the title indicator to the adapter

        TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.titles);
        titleIndicator.setViewPager(pager);
        titleIndicator.setTextColor(Color.BLACK);
        titleIndicator.setSelectedColor(Color.BLACK);

    }

    @Override
    protected void onResume() {

        super.onResume();

        // SEND RESULT - SEND RESULT - SEND RESULT - SEND RESULT
        if (SLAMBenchApplication.getBenchmark() != null && SLAMBenchApplication.getBenchmark().getIdentifier() == null) {
            send_result();
        }

    }



    private void send_result() {

        if (SLAMBenchApplication.getBenchmark() == null) {
            return;
        }

        new UploadResultsTask(this.getApplication()).execute();

    }


    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                //Yes button clicked
                send_result();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                //No button clicked
                break;
        }
    }
    private void share_result() {


        if (SLAMBenchApplication.getBenchmark().getIdentifier() == null) {

            want_to_share = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.msg_upload_failed_try_again)).setPositiveButton(getString(R.string.msg_yes), this)
                    .setNegativeButton(getString(R.string.msg_no), this).show();

        } else {

            want_to_share = false;
            try {
                String shareBody = getString(R.string.url_upload, SLAMBenchApplication.getBenchmark().getIdentifier() ) ;
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType(getString(R.string.sys_textplain));
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.txt_share_header));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, SHARE_VIA));

            } catch (NullPointerException e) {
                Log.e(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_share_failed_null_pointer), e);

            } catch (Throwable e) {
                Log.e(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_share_failed) , e );
            }

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_onstop_happened,this.getLocalClassName()));

    }

    @Override
    public void onBackPressed() {
        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_onbackpressed_happened,this.getLocalClassName()));
        SLAMBenchApplication.resetBenchmark();
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.shareButton:
                this.share_result();
                break;
            case R.id.retryButton:
                onBackPressed();
                break;

        }
    }

    @Override
    public void onUploadResultsTaskTerminate(String identifier) {


        if (SLAMBenchApplication.getBenchmark() != null) {

            SLAMBenchApplication.getBenchmark().setIdentifier(identifier);

            if (identifier == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.msg_upload_failed_try_again)).setPositiveButton(getString(R.string.msg_yes), this)
                        .setNegativeButton(getString(R.string.msg_no), this).show();
                return;
            }

        }


        if (want_to_share) {
            share_result();
        }

    }

}
