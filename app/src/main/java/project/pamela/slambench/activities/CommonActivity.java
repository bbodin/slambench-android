package project.pamela.slambench.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import project.pamela.slambench.R;
import project.pamela.slambench.SLAMBenchApplication;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

@SuppressLint("Registered")
public class CommonActivity extends ActionBarActivity {

    static  volatile private boolean      _dialog_stop_required = false;
    static  private ProgressDialog        _dialog = null;



    @Override
    protected void onResume() {
        super.onResume();
        ((SLAMBenchApplication)getApplication()).setCurrentActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (((SLAMBenchApplication)getApplication()).getCurrentActivity() == this) {
            if (_dialog != null) {
                _dialog.dismiss();
            }
            ((SLAMBenchApplication)getApplication()).setCurrentActivity(null);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_oncreate_happened, this.getLocalClassName()));
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_about:
                openAbout();
                return true;
            case R.id.action_releasenotes:
                openReleaseNotes();
                return true;
            //case R.id.action_ranking:
            //    openRanking();
            //    return true;
            case R.id.action_debug:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                setDebug(item.isChecked());
                return true;
            case R.id.run_live:
                Intent live_intent = new Intent(this, LiveActivity.class);
                startActivity(live_intent);
                return true;
            case R.id.run_record:
                Intent record_intent = new Intent(this, RecordActivity.class);
                startActivity(record_intent);
                return true;
            case R.id.home:
                this.onBackPressed();
                return true;
            default:
                Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_unknown_option, String.valueOf(item.getItemId())));
                this.onBackPressed();
                return true;
        }
    }


    private void setDebug(boolean value) {

        SLAMBenchApplication.setDebug(value);

        if (value) {
            Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_open_debug_menu));
            final AlertDialog d = new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.debug_title))
                    .setPositiveButton(getResources().getString(android.R.string.ok), null) //Set to null. We override the onclick
                    .setMessage(getResources().getString(R.string.debug_message))
                    .create();
            d.show();
        }

    }

    private void openAbout() {

        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_open_about_us));

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.PAMELA_SLAMBENCH_URL)));
        startActivity(browserIntent);

    }

    private void openReleaseNotes() {

        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_open_release_notes));

        final AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.releasenotes_title))
                .setPositiveButton(getResources().getString(android.R.string.ok), null) //Set to null. We override the onclick
                .setMessage((getResources().getString(R.string.releasenotes_message)))
                .create();
        d.show();

    }

    void openError(String msg) {

        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_open_error_dialog,msg));

        final AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.error_title))
                .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(0);
                    }
                })
                .setMessage(getString(R.string.msg_error_info, msg))
                .create();
        d.setCancelable(false);
        d.show();

    }


    void __openRanking() {
        MyDialogFragment dialog = new MyDialogFragment();
        dialog.show(getSupportFragmentManager(), "asdf");
    }




    public void closeDialog() {
        _dialog_stop_required = false;
        if (_dialog != null) {
            Log.d(SLAMBenchApplication.LOG_TAG,"Close dialog ");
            _dialog.dismiss();
            _dialog = null;
        }
    }

    public boolean updateDialog(Integer value, Integer maximum, String title,String message) {

        if (_dialog == null) {

            Log.d(SLAMBenchApplication.LOG_TAG,"Create dialog " + title + " " + message);
            _dialog = new ProgressDialog(this);
            _dialog.setCancelable(false);

            _dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.button_text_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    _dialog_stop_required = true;
                }
            });

            _dialog.setProgressNumberFormat(getString(R.string.progressbar_download_file_format));
            _dialog.setTitle(title);
            _dialog.setMessage(message);
        } else {
            try {
                _dialog.setTitle(title);
                _dialog.setMessage(message);
            } catch (NullPointerException e) {
                Log.d(SLAMBenchApplication.LOG_TAG,"[FIXME] Well known exception to fix.",e);
            }
        }

        if (!_dialog_stop_required) {
            if (maximum == -1) {
                _dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                _dialog.setIndeterminate(true);
            } else {
                _dialog.setIndeterminate(false);
                _dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                _dialog.setProgress(value);
                _dialog.setMax(maximum);
            }

            if (!_dialog.isShowing()) {
                Log.d(SLAMBenchApplication.LOG_TAG, "Show dialog ");
                _dialog.show();
            }
        }



        return _dialog_stop_required;

    }

}
