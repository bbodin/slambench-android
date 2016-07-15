package project.pamela.slambench.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import project.pamela.slambench.R;
import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.activities.PlotActivity;
import project.pamela.slambench.jni.KFusion;
import project.pamela.slambench.models.SLAMResult;
import project.pamela.slambench.utils.MessageLog;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

class SLAMResultAdapter extends ArrayAdapter<SLAMResult> {

    private final Context context;
    private final int layoutResourceId;
    private final List<SLAMResult> data;

    public SLAMResultAdapter(Context context, int layoutResourceId, List<SLAMResult> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
        SLAMResult result = data.get(position);
        return result.isFinished();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SLAMResultHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new SLAMResultHolder();
            holder.txtTitle = (TextView) row.findViewById(R.id.txtTitle);
            holder.txtDetail = (TextView) row.findViewById(R.id.txtDetail);
            holder.imgIcon = (ImageView) row.findViewById(R.id.icTest);
            row.setTag(holder);
        } else {
            holder = (SLAMResultHolder) row.getTag();
        }

        SLAMResult result = data.get(position);

        holder.txtTitle.setText(result.test.name);
        holder.txtDetail.setText(result.toString());
        if (!result.isFinished()) {
            holder.txtTitle.setTextColor(Color.GRAY);
            holder.txtDetail.setTextColor(Color.GRAY);
        } else {
            holder.txtTitle.setTextColor(Color.BLACK);
            holder.txtDetail.setTextColor(Color.BLACK);
        }

        Resources resources = this.context.getResources();
        holder.imgIcon.setImageDrawable(resources.getDrawable(getImplementationIcon(result.test.implementation)));

        return row;
    }

    private int getImplementationIcon(KFusion.ImplementationId implementation) {


        switch (implementation) {

            case NONE:
                return R.drawable.logo;
            case KFUSION_CPP:
                return R.drawable.ic_cpp;
            case KFUSION_OMP:
                return R.drawable.ic_openmp;
            case KFUSION_OCL:
                return R.drawable.ic_opencl;
            case KFUSION_MARE:
                return R.drawable.ic_mare;
            case KFUSION_NEON:
                return R.drawable.ic_neon;

        }

        return R.drawable.logo;

    }

    static class SLAMResultHolder {
        TextView txtTitle;
        TextView txtDetail;
        ImageView imgIcon;
    }
}


public class ResultListFragment extends Fragment {

    public static final String SELECTED_TEST_TAG = "selected_test";
    private LinearLayout llLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        // LAYOUT - LAYOUT - LAYOUT - LAYOUT - LAYOUT - LAYOUT

        super.onCreate(savedInstanceState);

        llLayout = (LinearLayout) inflater.inflate(R.layout.fragment_result, container, false);

        TextView _messageView = (TextView) llLayout.findViewById(R.id.message);
        _messageView.setMovementMethod(new ScrollingMovementMethod());
        MessageLog.setTextView(_messageView);


        if (SLAMBenchApplication.getResults() != null) {

            SLAMResultAdapter adapter = new SLAMResultAdapter(llLayout.getContext(), R.layout.list_row, SLAMBenchApplication.getResults());
            ListView lv = (ListView) llLayout.findViewById(R.id.listView);
            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position,
                                        long arg3) {

                    Intent myIntent = new Intent(v.getContext(), PlotActivity.class);
                    myIntent.putExtra(SELECTED_TEST_TAG, position);
                    startActivity(myIntent);


                }
            });

        }

        return llLayout;
    }





}
