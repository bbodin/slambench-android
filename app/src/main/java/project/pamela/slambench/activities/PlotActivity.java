package project.pamela.slambench.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.viewpagerindicator.TitlePageIndicator;

import java.util.List;
import java.util.Vector;

import project.pamela.slambench.R;
import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.fragments.AccuracyPlot;
import project.pamela.slambench.fragments.DurationPlot;
import project.pamela.slambench.fragments.FrequencyPlot;
import project.pamela.slambench.fragments.PowerPlot;
import project.pamela.slambench.fragments.TemperaturePlot;
import project.pamela.slambench.utils.MyPagerAdapter;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class PlotActivity extends FragmentActivity {

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        Log.d(SLAMBenchApplication.LOG_TAG, getString(R.string.debug_oncreate_happened, this.getLocalClassName()));
        super.setContentView(R.layout.activity_plot);

        //initialize the pager
        this.initialisePaging();



        Toast.makeText(getApplicationContext(), getString(R.string.msg_swipe_to_see_more),  Toast.LENGTH_LONG).show();

    }

    /**
     * Initialise the fragments to be paged
     */
    private void initialisePaging() {

        List<Fragment> fragments = new Vector<>();
        fragments.add(Fragment.instantiate(this, DurationPlot.class.getName()));
        fragments.add(Fragment.instantiate(this, AccuracyPlot.class.getName()));
        fragments.add(Fragment.instantiate(this, TemperaturePlot.class.getName()));
        fragments.add(Fragment.instantiate(this, PowerPlot.class.getName()));
        fragments.add(Fragment.instantiate(this, FrequencyPlot.class.getName()));

        List<String> titles= new Vector<>();
        titles.add("Time");
        titles.add("Accuracy");
        titles.add("Temperature");
        titles.add("Power");
        titles.add("Frequency");
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
}
