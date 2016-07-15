package project.pamela.slambench.models;

import project.pamela.slambench.jni.KFusion;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class SLAMTest {

    public String name = null;
    public KFusion.ImplementationId implementation = null;
    public SLAMBench.Argument arguments = null;
    public SLAMBench.Dataset dataset = null;


    public SLAMTest(String name, KFusion.ImplementationId implementation,SLAMBench.Argument arguments, SLAMBench.Dataset dataset) {

        this.name = name;
        this.implementation = implementation;
        this.arguments = arguments;
        this.dataset = dataset;


    }

    @Override
    public String toString() {
        return "SLAMTest(" + this.name + "," + this.implementation + "," + this.arguments + "," + this.dataset.getFilename() + ")";
    }

    public String toXML() {
        return "<SLAMTest "
                + " name='" + this.name + "'"
                + " implementation='" + this.implementation + "'"
                // + " arguments='" +  this.arguments + "'"  // TODO : These arguments are not public yet
                + " dataset='" + this.dataset.getFilename() + "'" + " />";
    }
}
