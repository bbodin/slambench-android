package project.pamela.slambench.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.DynamicTableModel;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import java.text.DecimalFormat;
import java.util.Arrays;

import project.pamela.slambench.R;
import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.activities.ResultActivity;
import project.pamela.slambench.models.SLAMResult;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class AccuracyPlot extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        LinearLayout llLayout = (LinearLayout) inflater.inflate(R.layout.fragment_plot, container, false);


        // Retrieve bench number
        Intent mIntent = super.getActivity().getIntent();
        final int intValue = mIntent.getIntExtra(ResultActivity.SELECTED_TEST_TAG, 0);
        SLAMResult value = SLAMBenchApplication.getResults().get(intValue);

        if (!value.isFinished()) {
            Log.e(SLAMBenchApplication.LOG_TAG, this.getResources().getString(R.string.debug_proposed_value_not_valid));
            return llLayout;
        }


        // initialize our XYPlot reference:
        XYPlot plot = (XYPlot) llLayout.findViewById(R.id.mySimpleXYPlot);

        plot.setTitle(value.test.name);
        plot.setDomainLabel(this.getResources().getString(R.string.legend_frame_number));
        plot.setRangeLabel(this.getResources().getString(R.string.legend_ate));
        plot.setBackgroundColor(Color.WHITE);
        plot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);

        plot.getGraphWidget().getDomainLabelPaint().setColor(Color.BLACK);
        plot.getGraphWidget().getRangeLabelPaint().setColor(Color.BLACK);

        plot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.BLACK);
        plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);


        plot.getGraphWidget().setRangeValueFormat(
                new DecimalFormat("#####.##"));

        int border = 60;
        int legendsize = 110;

        plot.getGraphWidget().position(
                border, XLayoutStyle.ABSOLUTE_FROM_LEFT,
                border, YLayoutStyle.ABSOLUTE_FROM_BOTTOM,
                AnchorPosition.LEFT_BOTTOM);
        plot.getGraphWidget().setSize(new SizeMetrics(
                border + legendsize, SizeLayoutType.FILL,
                border, SizeLayoutType.FILL));

        // reduce the number of range labels
        plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 10);
        plot.setDomainValueFormat(new DecimalFormat("#"));
        plot.setRangeStep(XYStepMode.SUBDIVIDE, 10);

        plot.getLayoutManager().remove(plot.getTitleWidget());


        // Set legend
        plot.getLegendWidget().setTableModel(new DynamicTableModel(4, 2));

        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.BLACK);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setAlpha(140);

        plot.getLegendWidget().setBackgroundPaint(bgPaint);


        plot.getLegendWidget().setSize(new SizeMetrics(
                legendsize, SizeLayoutType.ABSOLUTE,
                0, SizeLayoutType.FILL));

        plot.getLegendWidget().position(
                0, XLayoutStyle.ABSOLUTE_FROM_RIGHT,
                0, YLayoutStyle.ABSOLUTE_FROM_TOP,
                AnchorPosition.RIGHT_TOP);

        plot.getDomainLabelWidget().position(0, XLayoutStyle.ABSOLUTE_FROM_CENTER, 0, YLayoutStyle.RELATIVE_TO_BOTTOM, AnchorPosition.BOTTOM_MIDDLE);


        int fillcolor = Color.rgb(255, 213, 226);
        int strokecolor = Color.rgb(225, 99, 70);

        Double accuracyNumbers[] = value.getATEList();
        XYSeries series = new SimpleXYSeries(Arrays.asList(accuracyNumbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, this.getResources().getString(R.string.legend_ate));
        LineAndPointFormatter seriesFormat = new LineAndPointFormatter(strokecolor, strokecolor, null, null);


        Paint lineFill = new Paint();
        lineFill.setAlpha(200);
        DisplayMetrics metrics = new DisplayMetrics();
        super.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        lineFill.setShader(new LinearGradient(0, 0, 0, metrics.heightPixels, Color.WHITE, fillcolor, Shader.TileMode.MIRROR));
        seriesFormat.setPointLabeler(new PointLabeler() {
            @Override
            public String getLabel(XYSeries series, int index) {
                return "o";
            }
        });
        seriesFormat.setFillPaint(lineFill);
        plot.addSeries(series, seriesFormat);


        // same as above
        LineAndPointFormatter lineFormat = new LineAndPointFormatter(Color.BLACK, null, null, null);
        //change the line width
        Paint paint = lineFormat.getLinePaint();
        paint.setStrokeWidth(5);
        lineFormat.setLinePaint(paint);

        if (SLAMBenchApplication.getBenchmark() != null) {
            Number[] line = {0, SLAMBenchApplication.getBenchmark().getMaxAccuracyError(), value.test.dataset.getFrameCount() - 1, SLAMBenchApplication.getBenchmark().getMaxAccuracyError()};
            XYSeries expected = new SimpleXYSeries(Arrays.asList(line), SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, getResources().getString(R.string.legend_error_limit));
            plot.addSeries(expected, lineFormat);
        }

        plot.redraw();

        plot.calculateMinMaxVals();
        PointF maxXY = new PointF(plot.getCalculatedMaxX().floatValue(), plot.getCalculatedMaxY().floatValue());

        plot.setDomainBoundaries(0, value.test.dataset.getFrameCount() - 1, BoundaryMode.FIXED);
        plot.setRangeBoundaries(0, maxXY.y * 1.2f, BoundaryMode.FIXED);


        plot.redraw();

        return llLayout;
    }
}

