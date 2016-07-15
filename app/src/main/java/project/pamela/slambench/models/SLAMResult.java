package project.pamela.slambench.models;

import android.renderscript.Double3;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.jni.KFusion;
import project.pamela.slambench.utils.ComputeFrameResult;
import project.pamela.slambench.utils.MessageLog;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class SLAMResult {

    public SLAMTest test = null;
    public double speed;
    public double accuracy;
    private double power;
    private double voltage;
    private double current;
    private double temperature;
    private boolean _finish;
    private int _waitFor;
    private final ArrayList<Double> _current;
    private final ArrayList<Double> _power;
    private final ArrayList<Double> _temperature;
    private final ArrayList<Double> _voltage;
    private final ArrayList<String> _process_lines;
    private final ArrayList<int[]> _freqs;
    private Double[][] _result;
    private Double[]   _ate;

    public SLAMResult(SLAMTest test) {
        this.test = test;

        this._finish = false;
        this._waitFor = 0;

        int count = test.dataset.getFrameCount();
        if (count == 0) count = 9000;

        this._process_lines = new ArrayList<>(count);
        this._current = new ArrayList<>(count);
        this._power = new ArrayList<>(count);
        this._temperature  = new ArrayList<>(count);
        this._voltage  = new ArrayList<>(count);
        this._freqs  = new ArrayList<>(count);

    }

    public String powerToString() {
        return String.format("%.2f", power) + "W";
    }

    public String accuracyToString() {
        return String.format("%.2f", 100.0 * accuracy) + "cm";
    }

    public String speedToString() {
        return String.format("%.1f", (speed == 0) ? 0 : 1.0 / speed) + "fps";
    }

    public String getDescription() {

        if (!_finish) {
            return "This test has not been finished.";
        }

        return    "Speed " + String.format("%.1f", (speed == 0) ? 0 : 1.0 / speed) + "fps" +
                "\nAccuracy " + String.format("%.2f", 100.0 * accuracy) + "cm" +
                "\nPower " + String.format("%.2f", (power == 1) ? 0 : power / -1000) + "W" +
                "\ntemperature " + String.format("%.2f", temperature) + "°C" +
                "\nDataset " + String.valueOf(test.dataset.getFilename());
    }
    @Override
    public String toString() {

        if (!_finish) {
            return "This test has not been finished.";
        }

        return test.name + "\nSpeed " + String.format("%.1f", (speed == 0) ? 0 : 1.0 / speed) + "fps" +
                "\nAccuracy " + String.format("%.2f", 100.0 * accuracy) + "cm" +
                "\nPower " + String.format("%.2f", (power == 1) ? 0 : power / -1000) + "W" +
                "\ntemperature " + String.format("%.2f", temperature) + "°C" +
                "\nDataset " + String.valueOf(test.dataset.getFilename());
    }


    public void addFrameResult(ComputeFrameResult output) {
        this._process_lines.add(output.process_line);
        this._current.add(output.current);
        this._power.add(output.power);
        this._temperature.add(output.temperature);
        this._voltage.add(output.voltage);
        this._freqs.add(output.freqs);

        _waitFor += 1;
    }

    private double sumOfDoubles(ArrayList<Double> list) {

        double _average = 0;
        for (Double d : list) {
            _average += d;
        }
        return _average / list.size();
    }


    private Vector<Double3> loadGroundTruth(String gtfile) {

        Vector<Double3> gt = new Vector<>();

        BufferedReader br = null;
        String line;
        try {

            br = new BufferedReader(new FileReader(gtfile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] field = line.split(" ");
                gt.add(new Double3(Double.valueOf(field[1]), Double.valueOf(field[2]), Double.valueOf(field[3])));
            }

        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return gt;
    }


    public void finishIt(String gt_file) {

        if (_waitFor == 0) {
            Log.e(SLAMBenchApplication.LOG_TAG, "Skip a test which obviously has not been process.");
            return;
        }
        if (this.test.dataset.getFrameCount() == 0) {
            this.test.dataset = new SLAMBench.Dataset(this.test.dataset.getName(), this.test.dataset.getFilename(), this.test.dataset.getFileSize(), this.test.dataset.getTrajSize(), this.test.dataset.getRawMD5(), this.test.dataset.getTrajMD5(), _waitFor);
        }

        if (_waitFor != this.test.dataset.getFrameCount()) {
            MessageLog.addError("Wait for " + _waitFor + " before the end of " + this.test.dataset.getFrameCount() + "frames");
            return;
        }

        Vector<Double3> nuim_traj = null;
        if (gt_file != null) {
             nuim_traj = loadGroundTruth(gt_file);
        }

        try {

            int row_count = this._process_lines.size();
            int col_count = KFusion.FieldIndex.values().length;
            this._result = new Double[col_count][row_count];
            this._ate  = new Double[row_count];
            int line_index = 0;
            double max_ATE = 0;

            for (String process_line : this._process_lines) {

                String[] parts = process_line.split("\t");

                if (parts.length != KFusion.FieldIndex.values().length) {
                    MessageLog.addError("This test failed... wrong data format.");
                    return;
                }

                if (line_index != Integer.valueOf(parts[KFusion.FieldIndex.FRAME_NUMBER.getValue()])) {
                    MessageLog.addError("This test failed...wrong data values.");
                    return;
                }

                for (KFusion.FieldIndex i : KFusion.FieldIndex.values()) {
                    this._result[i.getValue()][line_index] = Double.valueOf(parts[i.getValue()]);
                }

                if (nuim_traj != null) {
                    Double3 p = new Double3(this._result[KFusion.FieldIndex.X_POSITION.getValue()][line_index], this._result[KFusion.FieldIndex.Y_POSITION.getValue()][line_index], this._result[KFusion.FieldIndex.Z_POSITION.getValue()][line_index]);
                    Double3 first = nuim_traj.get(0);

                    Double3 nkfusion_traj = new Double3(p.x + first.x, -(p.y + first.y), p.z + first.z);
                    Double3 diff = new Double3(Math.abs(nkfusion_traj.x - nuim_traj.get(line_index).x), Math.abs(nkfusion_traj.y - nuim_traj.get(line_index).y), Math.abs(nkfusion_traj.z - nuim_traj.get(line_index).z));
                    this._ate[line_index] = Math.sqrt(((diff.x * diff.x + diff.y * diff.y + diff.z * diff.z)));
                } else {
                    this._ate[line_index] = 0.0;
                }
                max_ATE = Math.max(this._ate[line_index], max_ATE);
                line_index += 1;

            }


            this.speed = sumOfDoubles(new ArrayList(Arrays.asList(_result[KFusion.FieldIndex.TOTAL_DURATION.getValue()])));
            this.power = sumOfDoubles(_power);
            this.current = sumOfDoubles(_current);
            this.temperature = sumOfDoubles(_temperature);
            this.voltage = sumOfDoubles(_voltage);
            this.accuracy = max_ATE;

        } catch (Exception e) {
            Log.d(SLAMBenchApplication.LOG_TAG,"test failed",e);
            MessageLog.addError("This test failed...");
            return;
        }

        this._finish = true;


    }

    public Double[] getField(KFusion.FieldIndex f) {
        return this._result[f.getValue()];
    }


    public boolean isFinished() {
        return this._finish;
    }

    public Double[] getATEList() {
        return _ate;
    }

    public Double[] getPowerList() {
        return _power.toArray(new Double[_power.size()]);
    }

    public Double[] gettemperatureList() {
        return _temperature.toArray(new Double[_temperature.size()]);
    }

    public int  getCPUCount() {
        return _freqs.get(0).length;
    }
    public Double[] getfrequencyList(int cpu_index) {
        Double[] list = new Double[_freqs.size()];
        int i = 0;
        for (int[] f_array : _freqs) {
            if (f_array == null)  {
                list[i] = 0.0;
            } else {
                list[i] = Double.valueOf(f_array[cpu_index]) / 1000000.0;
            }
            i++;
        }
        return list;
    }

    public String toXML() {

        String res = "<SLAMResult " +
                " speed='" + speed + "'" +
                " accuracy='" + accuracy + "'" +
                " power='" + power + "'" +
                " voltage='" + voltage + "'" +
                " current='" + current + "'" +
                " temperature='" + temperature + "'" +
                " _finish='" + _finish + "'" +
                " _waitFor='" + _waitFor + "'" + ">\n";

        res += test.toXML() + "\n";

        if (_finish) {

            res += "<currents>\n";
            for (Double d : _current) {
                res += "  " + (d == null ? "null" : d.toString()) + "\n";
            }
            res += "</currents>\n";


            res += "<temperature>\n";
            for (Double d : _temperature) {
                res += "  " + (d == null ? "null" : d.toString()) + "\n";
            }
            res += "</temperature>\n";


            res += "<voltage>\n";
            for (Double d : _voltage) {
                res += "  " + (d == null ? "null" : d.toString()) + "\n";
            }
            res += "</voltage>\n";

            res += "<frequencies>\n";
            for (int[] f_array : _freqs) {
                if (f_array == null)  {
                    res += "  null\n";
                } else for ( int f : f_array ) {
                    res += "  " + String.valueOf(f);
                }
                res += "\n";
            }
            res += "</frequencies>\n";

            res += "<ate>\n";
            for (Double d : _ate) {
                res += "  " + (d == null ? "null" : d.toString()) + "\n";
            }
            res += "</ate>\n";


            res += "<process_lines>\n";
            for (String d : _process_lines) {
                res += "  " + (d == null ? "null" : d) + "\n";
            }
            res += "</process_lines>\n";

        }

        res += "</SLAMResult>";

        return res;

    }
}
