package project.pamela.slambench.utils;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class ComputeFrameResult {

    public boolean success;
    public boolean end_of_file;
    public String message;
    public Scenario.ScenarioReply reply;
    public int frame;
    public double temperature;
    public double voltage;
    public double current;
    public double power;
    public int[] freqs;
    public String process_line;


    public ComputeFrameResult() {
        this.end_of_file = true;
        this.success = false;
        this.message = null;
    }

    @Override
    public String toString() {
        return "temperature = " + temperature
                + " voltage = " + voltage
                + " current = " + current
                + " power = " + power
                + " process_line = " + process_line;
    }

}

