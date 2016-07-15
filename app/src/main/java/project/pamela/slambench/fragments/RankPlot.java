package project.pamela.slambench.fragments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidplot.exception.PlotRenderException;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.SeriesRenderer;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.util.PixelUtils;
import com.androidplot.util.ValPixConverter;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.List;

import project.pamela.slambench.R;
import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.models.SLAMRank;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class RankPlot extends Fragment {

    private XYPlot plot;
    private XYSeries series1;
    private MyBarFormatter formatter1;
    private MyBarFormatter currentFormatter;
    private MyBarFormatter openclFormatter;
    private MyBarFormatter cppFormatter;

    private Integer selection;
    private Double values[] ;
    private String devices[] ;
    private String versions[] ;
    private final String YOUR_DEVICE = "Your device";
    private final int RANK_SIZE = 30;
    private final float BAR_GAP  = 5f;

    class MyBarFormatter extends BarFormatter {
        public MyBarFormatter(int fillColor, int borderColor) {
            super(fillColor, borderColor);
        }

        @Override
        public Class<? extends SeriesRenderer> getRendererClass() {
            return MyBarRenderer.class;
        }

        @Override
        public SeriesRenderer getRendererInstance(XYPlot plot) {
            return new MyBarRenderer(plot);
        }
    }

    class MyBarRenderer extends BarRenderer<MyBarFormatter> {

        public MyBarRenderer(XYPlot plot) {
            super(plot);
        }

        @Override
        public MyBarFormatter getFormatter(int index, XYSeries series) {
            if (devices[index].equals(YOUR_DEVICE)) {
                return currentFormatter;
            } else {
                if (versions[index].contains("ocl")) {
                    return openclFormatter;
                } if (versions[index].contains("cpp") || versions[index].contains("omp")) {
                    return cppFormatter;
                } else {
                    return getFormatter(series);
                }
            }
        }

        @Override
        public void onRender(Canvas canvas, RectF plotArea) throws PlotRenderException {

            super.onRender(canvas, plotArea);
            List<XYSeries> seriesList = getPlot().getSeriesListForRenderer(this.getClass());

            Paint paint = new Paint();
            Rect bounds = new Rect();

            paint.setColor(Color.BLUE);
            paint.setTextSize(PixelUtils.spToPix(12));
            paint.setTextAlign(Paint.Align.LEFT);


            // Determine roughly how wide (rough_width) this bar should be. This is then used as a default width
            // when there are gaps in the data or for the first/last bars.
            float f_rough_width = ((plotArea.width() - ((seriesList.size() - 1) * BAR_GAP)) / (seriesList.size() - 1));
            int rough_width = (int) f_rough_width;
            if (rough_width < 0) rough_width = 0;


            float desired_width  = plotArea.height() - 5;
            float desired_height = rough_width;

            for (String n : devices) {
                paint.getTextBounds(n, 0, n.length(), bounds);
                float textSize = paint.getTextSize();
                while (bounds.width() > desired_width || bounds.height() > desired_height) {
                    textSize--;
                    paint.setTextSize(textSize);
                    paint.getTextBounds(n, 0, n.length(), bounds);
                }
            }


            for (XYSeries series : seriesList) {

                for (int i = 0; i < series.size(); i++) {

                    Double xVal = series.getX(i).doubleValue() + 0.5;
                    float pixX = ValPixConverter.valToPix(xVal, getPlot().getCalculatedMinX().doubleValue(), getPlot().getCalculatedMaxX().doubleValue(), plotArea.width(), false) + (plotArea.left);
                    int intX = (int) pixX - 5;

                    Double yVal = 0.0;
                    float pixY = ValPixConverter.valToPix(yVal, getPlot().getCalculatedMinY().doubleValue(), getPlot().getCalculatedMaxY().doubleValue(), plotArea.height(), true) + plotArea.top;
                    int   intY = (int) pixY - 5;

                    //Log.d(SLAMBenchApplication.LOG_TAG, "  Draw item " + Integer.toString(i) + " at position " + Double.toString(xVal) + "," + Double.toString(yVal));
                    //Log.d(SLAMBenchApplication.LOG_TAG,"  Draw item " + Integer.toString(i) + " at pixel " + Double.toString(pixX) + "," + Double.toString(pixY));

                    // needed some paint to draw with so I'll just create it here for now:
                    paint.setTextSize(PixelUtils.spToPix(12));
                    String n = devices[i];
                    paint.getTextBounds(n, 0, n.length(), bounds);
                    float textSize = paint.getTextSize();
                    while (bounds.width() > desired_width || bounds.height() > desired_height) {
                        textSize--;
                        paint.setTextSize(textSize);
                        paint.getTextBounds(n, 0, n.length(), bounds);
                    }


                    drawVerticalText(canvas,devices[i],paint, intX,intY);

                }
            }
        }

        /**
         * @param canvas
         * @param text   the text to be drawn
         * @param x      x-coord of where the text should be drawn
         * @param y      y-coord of where the text should be drawn
         */
        protected void drawVerticalText(Canvas canvas, String text, Paint paint, float x, float y) {

            // record the state of the canvas before the draw:
            canvas.save(Canvas.ALL_SAVE_FLAG);

            // center the canvas on our drawing coords:
            canvas.translate(x, y);

            // rotate into the desired "vertical" orientation:
            canvas.rotate(-90);

            // draw the text; note that we are drawing at 0, 0 and *not* x, y.
            canvas.drawText(text, 0, 0, paint);

            // restore the canvas state:
            canvas.restore();
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {





        // Replace LinearLayout by the type of the root element of the layout you're trying to load
        LinearLayout llLayout = (LinearLayout) inflater.inflate(R.layout.rank_plot, container, false);


        TextView oclLegend = (TextView) llLayout.findViewById(R.id.oclLegend);
        TextView cppLegend = (TextView) llLayout.findViewById(R.id.cppLegend);
        TextView otherLegend = (TextView) llLayout.findViewById(R.id.otherLegend);
        TextView yoursLegend = (TextView) llLayout.findViewById(R.id.yoursLegend);

        oclLegend.setBackgroundColor(getResources().getColor(R.color.oclLegendColor));
        cppLegend.setBackgroundColor(getResources().getColor(R.color.cppLegendColor));
        otherLegend.setBackgroundColor(getResources().getColor(R.color.othersLegendColor));
        yoursLegend.setBackgroundColor(getResources().getColor(R.color.yoursLegendColor));

        /*
        if (SLAMBenchApplication.getBenchmark() == null) {
            // Need to run the configuration download ....
            try {
                InputStream inputStream = this.getActivity().getAssets().open("demo.xml");
                SLAMBenchXmlParser parser = new SLAMBenchXmlParser();
                SLAMConfiguration entries = parser.parse(inputStream);
                inputStream.close();
                SLAMBenchApplication.setBenchmark(new SLAMBench(entries));
                if (entries == null ) {
                    Log.e(SLAMBenchApplication.LOG_TAG, getString(R.string.log_configuration_error));
                }
            } catch (IOException e) {
                Log.e(SLAMBenchApplication.LOG_TAG, getString(R.string.log_configuration_error), e);
            } catch (XmlPullParserException e) {
                Log.e(SLAMBenchApplication.LOG_TAG, getString(R.string.log_configuration_error), e);
            }

        }
        */

        if (SLAMBenchApplication.getBenchmark() == null) {
            return llLayout;
        }

        List<SLAMRank> ranks = SLAMBenchApplication.getBenchmark().getDevicesRank();


        values = new Double[ranks.size() + 1];
        devices = new String[ranks.size() + 1];
        versions = new String[ranks.size() + 1];

        for (int i = 0 ; i < ranks.size() ; i++) {
            SLAMRank r = ranks.get(i);
            values[i] = r.get_result();
            devices[i] = r.get_device();

            versions[i] = (r.get_test() == null) ? "" : r.get_test().name;
        }

        double best = SLAMBenchApplication.getBenchmark().getBestSpeed();

        values[values.length - 1] = (best <= 0) ? 0 : 1.0 / best;
        devices[values.length - 1] = YOUR_DEVICE;
        if (SLAMBenchApplication.getBenchmark() != null && SLAMBenchApplication.getBenchmark().getBestResult() != null && SLAMBenchApplication.getBenchmark().getBestResult().test != null) {
            versions[values.length - 1] = SLAMBenchApplication.getBenchmark().getBestResult().test.name;
        } else {
            versions[values.length - 1] = "";
        }
        for (int n = 0; n < values.length; n++) {
            for (int m = 0; m < values.length - 1 - n; m++) {
                if ((values[m].compareTo(values[m + 1])) <= 0) {
                    String swapString = devices[m];
                    devices[m] = devices[m + 1];
                    devices[m + 1] = swapString;
                    swapString = versions[m];
                    versions[m] = versions[m + 1];
                    versions[m + 1] = swapString;
                    Double swapInt = values[m];
                    values[m] = values[m + 1];
                    values[m + 1] = swapInt;
                }
            }
        }

        int start = Math.max(0, values.length - RANK_SIZE - 1);
        int end   =             values.length - 1;

        Log.d(SLAMBenchApplication.LOG_TAG,"Default ArrayOfRange(" + Integer.valueOf(start) + "," + Integer.valueOf(end) +")");

        for (int i = 0 ; i < values.length ; i++) {
            if (devices[i].equals(YOUR_DEVICE)) {
                start = Math.max(0, i - (RANK_SIZE / 2));
                end = Math.min(values.length - 1,start + RANK_SIZE);
                if (end - start < RANK_SIZE) {
                    start = Math.max(0,end - RANK_SIZE);
                }
                break;
            }
        }

        Log.d(SLAMBenchApplication.LOG_TAG,"ArrayOfRange(" + Integer.valueOf(start) + "," + Integer.valueOf(end) +")");

        if (best > 0) {
            values  = Arrays.copyOfRange(values, start, end);
            devices = Arrays.copyOfRange(devices, start, end);
            versions= Arrays.copyOfRange(versions, start, end);
        }  else {
            values  = Arrays.copyOf(values, values.length - 1);
            devices = Arrays.copyOf(devices, devices.length - 1);
            versions= Arrays.copyOf(versions,  versions.length - 1);
        }
        // initialize our XYPlot reference:
        plot = (XYPlot) llLayout.findViewById(R.id.mySimpleXYPlot);

        // colors
        plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
        plot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
        plot.getBorderPaint().setColor(Color.WHITE);
        plot.getBackgroundPaint().setColor(Color.WHITE);
        plot.getGraphWidget().getDomainLabelPaint().setColor(Color.BLACK);
        plot.getGraphWidget().getRangeLabelPaint().setColor(Color.BLACK);
        plot.getGraphWidget().getRangeLabelPaint().setTextSize(20);
        plot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.BLACK);
        plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
        plot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        //plot.getGraphWidget().getRangeGridLinePaint().setColor(Color.TRANSPARENT);

        formatter1       = new MyBarFormatter(getResources().getColor(R.color.othersLegendColor), Color.WHITE);
        openclFormatter  = new MyBarFormatter(getResources().getColor(R.color.oclLegendColor), Color.WHITE);
        cppFormatter     = new MyBarFormatter(getResources().getColor(R.color.cppLegendColor), Color.WHITE);
        currentFormatter = new MyBarFormatter(getResources().getColor(R.color.yoursLegendColor), Color.WHITE);

        plot.setTicksPerRangeLabel(1);

        //plot.getLayoutManager().remove(plot.getLegendWidget());
        plot.getLayoutManager().remove(plot.getTitleWidget());
        plot.getLayoutManager().remove(plot.getDomainLabelWidget());
        plot.getLayoutManager().remove(plot.getRangeLabelWidget());

        plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 0.5);

        plot.getGraphWidget().position(
                0, XLayoutStyle.ABSOLUTE_FROM_LEFT,
                0, YLayoutStyle.ABSOLUTE_FROM_BOTTOM,
                AnchorPosition.LEFT_BOTTOM);

        plot.getGraphWidget().setSize(new SizeMetrics(
                0, SizeLayoutType.FILL,
                0, SizeLayoutType.FILL));

        plot.setDomainValueFormat(new NumberFormat() {
            @Override
            public StringBuffer format(double value, StringBuffer buffer, FieldPosition field) {
                int index = (int) (value);
                //if ((index) == (value)) {
                //    return new StringBuffer(devices[index]);
                //}
                return new StringBuffer("");
            }

            @Override
            public StringBuffer format(long value, StringBuffer buffer, FieldPosition field) {
                throw new UnsupportedOperationException("Not yet implemented.");
            }

            @Override
            public Number parse(String string, ParsePosition position) {
                throw new UnsupportedOperationException("Not yet implemented.");
            }
        });

        // Setup our Series with the selected number of elements
        series1 = new SimpleXYSeries(Arrays.asList(values), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "devices");

        // add a new series' to the xyplot:
        plot.addSeries(series1, formatter1);

        // Setup the BarRenderer with our selected options
        MyBarRenderer renderer = ((MyBarRenderer)plot.getRenderer(MyBarRenderer.class));
        renderer.setBarRenderStyle(BarRenderer.BarRenderStyle.OVERLAID);
        renderer.setBarWidthStyle(BarRenderer.BarWidthStyle.VARIABLE_WIDTH);
        renderer.setBarWidth(BAR_GAP);
        renderer.setBarGap(BAR_GAP);
        plot.getGraphWidget().setDomainLabelOrientation(-90);



        plot.getGraphWidget().setGridPaddingRight(0);
        plot.getGraphWidget().setGridPaddingTop(0);
        plot.getGraphWidget().setGridPaddingLeft(0);
        plot.getGraphWidget().setGridPaddingBottom(0);


        plot.calculateMinMaxVals();

        PointF minXY = new PointF(plot.getCalculatedMinX().floatValue(), plot.getCalculatedMinY().floatValue());
        PointF maxXY = new PointF(plot.getCalculatedMaxX().floatValue(), plot.getCalculatedMaxY().floatValue());

        plot.setDomainBoundaries(minXY.x - 0.5, maxXY.x + 0.5, BoundaryMode.FIXED);
        plot.setRangeBoundaries(0, (int) (maxXY.y / 10) * 10f + 10f, BoundaryMode.FIXED);



        plot.redraw();


        return llLayout;
    }



}

