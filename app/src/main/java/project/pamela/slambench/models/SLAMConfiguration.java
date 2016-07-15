package project.pamela.slambench.models;

import java.util.List;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class SLAMConfiguration {
    private List<SLAMTest> _tests;
    private List<SLAMRank> _ranks;
    private List<SLAMMode> _modes;
    private String _uid;
    private String _message;

    public SLAMConfiguration(String uid, String message, List<SLAMTest> tests, List<SLAMRank> ranks ,  List<SLAMMode> modes) {
        this._ranks = ranks;
        this._tests = tests;
        this._modes = modes;
        this._uid = uid;
        this._message = message;
    }

    public List<SLAMTest> get_tests() {
        return _tests;
    }

    public List<SLAMRank> get_ranks() { return _ranks; }

    public List<SLAMMode> get_modes() { return _modes; }

    public String get_uid() {
        return _uid;
    }

    public String get_message() {
        return _message;
    }
}
