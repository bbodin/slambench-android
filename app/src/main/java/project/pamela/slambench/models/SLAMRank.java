package project.pamela.slambench.models;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class SLAMRank {

    private String   _device;
    private SLAMTest _test;
    private Double      _result;

    public SLAMRank(String rank_device, SLAMTest rank_test_real, Double rank_result) {
        this._device = rank_device;
        this._test   = rank_test_real;
        this._result = rank_result;
    }

    public String get_device() {
        return _device;
    }

    public SLAMTest get_test() {
        return _test;
    }

    public Double get_result() {
        return _result;
    }
}
