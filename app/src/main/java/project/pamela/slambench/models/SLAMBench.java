package project.pamela.slambench.models;

import java.util.List;

import project.pamela.slambench.SLAMBenchApplication;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class SLAMBench {

    public static final Argument arg_live    = new Argument("live_arguments","-c 4 -l 32.8116 -r 24 -m 0.419017 -z 1 -y 4,2,0 -v 128");

    private String identifier = null;
    private SLAMResult bestResult = null;



    private final List<SLAMMode> availableModes;
    private final List<SLAMTest> availableTests;
    private final List<SLAMRank> rank;
    private final String uid;
    private final String message;


    public SLAMBench(SLAMConfiguration entries) {


        availableModes = entries.get_modes();
        availableTests = entries.get_tests();
        rank = entries.get_ranks();
        uid = entries.get_uid();
        message = entries.get_message();

    }

    public List<SLAMTest> getModeTests(int mode_index) {
        return availableModes.get(mode_index).getTests();
    }
    public List<SLAMMode> getAvailableModes() {
        return availableModes;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


    public double getBestSpeed() {
        if (this.getBestResult() != null) {
            return this.getBestResult().speed;
        } else {
            return 0;
        }
    }

    public double getMaxAccuracyError() {
        return 0.05;
    }
    public int getMinFrameCount() {
        return 50;
    }


    public List<SLAMRank> getDevicesRank() {
        return rank;
    }



    public SLAMResult getBestResult() {
        if (this.bestResult == null) {
            for (SLAMResult r : SLAMBenchApplication.getResults()) {
                if (r.test.dataset._frame_count > this.getMinFrameCount())
                    if (r.accuracy < this.getMaxAccuracyError()) {
                        if (this.bestResult == null) {
                            this.bestResult = r;
                        }
                        if (this.bestResult.speed > r.speed) {
                            this.bestResult = r;
                        }
                    }
            }
        }

        return bestResult;
    }

    public String getUid() {
        return uid;
    }

    public String getMessage() {
        return message;
    }

    public static class Argument {
        public final String name;
        public final String value;

        public Argument(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public static class Dataset {


        private final String _name;
        private final String _md5;
        private final String _trajmd5;
        private final String _filename;
        private final int _frame_count;
        private final int _fileSize;
        private final int _trajSize;

        public Dataset(String name,String filename, int fileSize,  int trajSize, String md5, String trajmd5, int frame_count) {
            this._name = name;
            this._filename = filename;
            this._frame_count = frame_count;
            this._md5 = md5;
            this._trajmd5 = trajmd5;
            this._fileSize = fileSize;
            this._trajSize = trajSize;
        }

        public String getFilename() {
            return this._filename;
        }

        public Integer getFrameCount() {
            return this._frame_count;
        }

        public String getRawMD5() {
            return this._md5;
        }

        public String getTrajMD5() {
            return this._trajmd5;
        }

        public String getName() {
            return this._name;
        }

        public int getFileSize() {
            return this._fileSize;
        }

        public int getTrajSize() {
            return this._trajSize;
        }
    }



}
