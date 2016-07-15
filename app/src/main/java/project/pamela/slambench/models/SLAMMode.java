package project.pamela.slambench.models;

import java.util.ArrayList;
import java.util.List;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class SLAMMode {
    private String _name;
    private String _description;
    private List<SLAMTest> _tests;

    public SLAMMode (String name, String description) {
        _name = name;
        _description = description;
        _tests = new ArrayList<>();
    }
    public void addTest(SLAMTest t) {
        _tests.add(t);
    }


    public List<SLAMTest> getTests () {
        return _tests;
    }

    public String get_name() {
        return _name;
    }
    public String get_description() {
        return _description;
    }
}
