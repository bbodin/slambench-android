package project.pamela.slambench.activities;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import project.pamela.slambench.R;
import project.pamela.slambench.fragments.RankPlot;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */


public class MyDialogFragment extends DialogFragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RankPlot dialog = new RankPlot();
        View v = inflater.inflate(R.layout.dialog_ranking, container, false);
        getChildFragmentManager().beginTransaction().add(R.id.fragment_container, dialog).commit();
        return v;
    }

}
