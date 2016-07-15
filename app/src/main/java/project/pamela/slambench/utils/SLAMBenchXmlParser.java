package project.pamela.slambench.utils;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.jni.KFusion;
import project.pamela.slambench.models.SLAMBench;
import project.pamela.slambench.models.SLAMConfiguration;
import project.pamela.slambench.models.SLAMMode;
import project.pamela.slambench.models.SLAMRank;
import project.pamela.slambench.models.SLAMTest;


/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class SLAMBenchXmlParser {


    // We don't use namespaces
    private static final String ns = null;

    public  SLAMConfiguration parse(InputStream in) throws XmlPullParserException, IOException {
        try {

            Log.d(SLAMBenchApplication.LOG_TAG,"Start parsing ... ");

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            SLAMConfiguration config = readFile(parser);
            return config;
        } finally {
            in.close();
        }
    }
    private  SLAMConfiguration readFile(XmlPullParser parser) throws XmlPullParserException, IOException {

        List<SLAMBench.Argument> arguments = new ArrayList();
        List<SLAMBench.Dataset> datasets = new ArrayList();
        List<SLAMTest> tests = new ArrayList();
        List<SLAMRank> ranks = new ArrayList();
        List<SLAMMode> modes = new ArrayList();
        String uid     = null;
        String message = null;

        parser.require(XmlPullParser.START_TAG, ns, "SLAMBench");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            switch (name.toUpperCase()) {
                case "ARGUMENTS":
                    arguments.add(readArguments(parser));
                    break;
                case "DATASET":
                    datasets.add(readDataset(parser));
                    break;
                case "SLAMTEST":
                    tests.add(readSLAMTest(parser, arguments, datasets));
                    break;
                case "SLAMRANK" :
                    ranks.add(readSLAMRank(parser, tests));
                    break;
                case "SLAMMODE":
                    modes.add(readSLAMMode(parser, tests));
                    break;
                case "VERSION" :
                    skip(parser);
                    break;
                case "UID" :
                    uid = readStringTag(parser, name);
                    break;
                case "MESSAGE" :
                    message = readStringTag(parser,name);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return new SLAMConfiguration(uid, message, tests, ranks, modes);
    }

    private SLAMBench.Argument readArguments(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Arguments");

        String argument_name  = null;
        String argument_value = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name.toUpperCase()) {
                case "NAME":
                    argument_name = readStringTag(parser, name );
                    break;
                case "VALUE":
                    argument_value = readStringTag(parser, name );
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return new SLAMBench.Argument(argument_name,argument_value);
    }

    private SLAMTest readSLAMTest(XmlPullParser parser, List<SLAMBench.Argument> arguments, List<SLAMBench.Dataset>  datasets) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "SLAMTest");

        String test_name = null;
        KFusion.ImplementationId test_implementation = null;
        String test_arguments = null;
        String test_dataset = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name.toUpperCase()) {
                case "NAME":
                    test_name = readStringTag(parser,name);
                    break;
                case "IMPLEMENTATION":
                    test_implementation = KFusion.ImplementationId.valueOf(readStringTag(parser,name));
                    break;
                case "ARGUMENTS":
                    test_arguments = readStringTag(parser, name);
                    break;
                case "DATASET":
                    test_dataset = readStringTag(parser,name);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }

        SLAMBench.Argument test_arguments_real = null;
        SLAMBench.Dataset test_dataset_real = null;

        for (SLAMBench.Argument a : arguments) {
            if (a.name.equalsIgnoreCase(test_arguments)) {
                test_arguments_real = a;
            }
        }
        for (SLAMBench.Dataset d : datasets) {
            if (d.getName().equalsIgnoreCase(test_dataset)) {
                test_dataset_real = d;
            }
        }


        return new SLAMTest(test_name,test_implementation,test_arguments_real,test_dataset_real);
    }



    private SLAMMode readSLAMMode(XmlPullParser parser, List<SLAMTest> tests) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, ns, "SLAMMode");

        String mode_name = parser.getAttributeValue(null, "name");
        String mode_description = parser.getAttributeValue(null, "description");
        Log.d(SLAMBenchApplication.LOG_TAG,"SLAMMode name=" + mode_name + " description=" + mode_description);
        SLAMMode result = new SLAMMode(mode_name,mode_description);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            switch (name) {
                case "test":
                    String test_name = readStringTag(parser, name);
                    SLAMTest real_test = null;
                    for (SLAMTest a : tests) {
                        if (a.name.equalsIgnoreCase(test_name)) {
                            real_test = a;
                        }
                    }
                    if (real_test != null) {
                        result.addTest(real_test);
                    }
                    break;
                default:
                    skip(parser);
                    break;
            }
        }

        return result;
    }



    private SLAMRank readSLAMRank(XmlPullParser parser, List<SLAMTest> tests) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "SLAMRank");

        String   rank_test   = null;
        String   rank_device = null;
        Double   rank_result = 0.0;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name.toUpperCase()) {
                case "TEST":
                    rank_test = readStringTag(parser, name);
                    break;
                case "DEVICE":
                    rank_device = readStringTag(parser, name);
                    break;
                case "RESULT":
                    rank_result = readDoubleTag(parser, name);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }

        SLAMTest rank_test_real = null;
        for (SLAMTest a : tests) {
            if (a.name.equalsIgnoreCase(rank_test)) {
                rank_test_real= a;
            }
        }
        if (rank_test_real == null) {
            for (SLAMTest a : tests) {
                Log.d(SLAMBenchApplication.LOG_TAG,"Test " + a.name + "==" + rank_test);
            }

        }

        return new SLAMRank(rank_device,rank_test_real,rank_result);
    }

    private SLAMBench.Dataset readDataset(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Dataset");
        String dataset_name = null;
        String dataset_md5 = null;
        String dataset_trajmd5 = null;
        String dataset_filename = null;
        int    dataset_size = 0;
        int    dataset_trajsize = 0;
        int    dataset_filesize = 0;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name.toUpperCase()) {
                case "NAME":
                    dataset_name = readStringTag(parser, name);
                    break;
                case "FILENAME":
                    dataset_filename = readStringTag(parser,  name);
                    break;
                case "MD5":
                    dataset_md5 = readStringTag(parser, name);
                    break;
                case "TRAJ_MD5":
                    dataset_trajmd5 = readStringTag(parser, name);
                    break;
                case "SIZE":
                    dataset_size = readIntegerTag(parser, name);
                    break;
                case "FILESIZE":
                    dataset_filesize = readIntegerTag(parser, name);
                    break;
                case "TRAJSIZE":
                    dataset_trajsize = readIntegerTag(parser, name);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return new SLAMBench.Dataset(dataset_name,dataset_filename,dataset_filesize, dataset_trajsize,dataset_md5,dataset_trajmd5,dataset_size);
    }

    private Double readDoubleTag(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, name);
        String v = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, name);
        return Double.valueOf(v);
    }

    private int readIntegerTag(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, name);
        String v = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, name);
        return Integer.valueOf(v);
    }

    private String readStringTag(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, name);
        String v = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, name);
        return v;
    }





    // For the tags title and summary, extracts their text values.
private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
    String result = "";
    if (parser.next() == XmlPullParser.TEXT) {
        result = parser.getText();
        parser.nextTag();
    }
    return result;
}


private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
    if (parser.getEventType() != XmlPullParser.START_TAG) {
        throw new IllegalStateException();
    }
    int depth = 1;
    while (depth != 0) {
        switch (parser.next()) {
        case XmlPullParser.END_TAG:
            depth--;
            break;
        case XmlPullParser.START_TAG:
            depth++;
            break;
        }
    }
 }
}
