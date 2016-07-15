package project.pamela.slambench.jni;

import android.util.Log;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import project.pamela.slambench.SLAMBenchApplication;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class KFusion {


    private static final Lock mutex = new ReentrantLock();

    private static final List<Version> current_implementations;
    private static boolean _is_initialized = false;
    private static Version _current_version = null;

    static {

        List<Version> possible_implementations = new Vector<>();
        possible_implementations.add(new CPPVersion());
        if (Commands.get_opencl_info() != null) {
            possible_implementations.add(new OCLVersion());
        }
        possible_implementations.add(new OMPVersion());
        if (android.os.Build.MODEL.equals("apq8084")) {
            possible_implementations.add(new MAREVersion());
        }
        possible_implementations.add(new NEONVersion());
        possible_implementations.add(new Recorder());

        current_implementations = new Vector<>(possible_implementations.size());
        for (Version v : possible_implementations) {

            Log.d(SLAMBenchApplication.LOG_TAG, "Load JNI KFusion " + v.name() + " Start.");
            try {
                System.loadLibrary(v.libname());
                Log.d(SLAMBenchApplication.LOG_TAG, "Load JNI KFusion " + v.name() + " : Result = OK");
                current_implementations.add(v);
            } catch (Throwable e) {
                Log.d(SLAMBenchApplication.LOG_TAG, "Load JNI KFusion " + v.name() + " failed : " + e.getMessage() + '.');
            }


        }


    }

    public static Iterable<ImplementationId> getImplementations() {
        Vector<ImplementationId> _result = new Vector<>(current_implementations.size());
        for (Version v : current_implementations) {
            _result.add(v.id());
        }

        return _result;
    }

    public static void init_view() {
        Log.d(SLAMBenchApplication.LOG_TAG, "init_view Start.");
        try {
            mutex.lock();
            Version v = _current_version;
            v.glinit();
            Log.d(SLAMBenchApplication.LOG_TAG, "RENDERING Init KFusion " + v.name());
        } catch (Throwable e) {
            Log.e(SLAMBenchApplication.LOG_TAG, "init_view : Error = " + e.getMessage());
        } finally {
            mutex.unlock();
        }
    }

    public static void resize_view(int w, int h) {
        try {
            mutex.lock();
            Version v = _current_version;
            v.glresize(w, h);
            Log.d(SLAMBenchApplication.LOG_TAG, "resize_view for " + v.name());
        } catch (Throwable e) {
            Log.e(SLAMBenchApplication.LOG_TAG, "resize_view : Error = " + e.getMessage());
        } finally {
            mutex.unlock();
        }
    }

    public static void render_view() {
        Log.d(SLAMBenchApplication.LOG_TAG, "render_view Start.");
        try {
            mutex.lock();
            Version v = _current_version;
            v.glrender();
            Log.d(SLAMBenchApplication.LOG_TAG, "RENDERING for KFusion " + v.name());
        } catch (Throwable e) {
            Log.d(SLAMBenchApplication.LOG_TAG, "render_view : Error = " + e.getMessage());
        } finally {
            mutex.unlock();
        }
    }

    public static boolean kfusion_init(ImplementationId version, String[] arguments) {


        if (_is_initialized) {
            kfusion_release();
            Log.e(SLAMBenchApplication.LOG_TAG, "Please don't init kfusion twice.");
        }

        for (Version v : current_implementations) {
            if (v.id().equals(version)) {
                _current_version = v;
            }
        }



        boolean res;
        try {
            mutex.lock();
            Log.d(SLAMBenchApplication.LOG_TAG, "Start INIT KFusion " + _current_version.name());
            res = _current_version.init(arguments);
            _current_version.glresize(0, 0);
            Log.d(SLAMBenchApplication.LOG_TAG, "kfusion_init : " + res);
            _is_initialized = true;

        } catch (Throwable e) {
            Log.e(SLAMBenchApplication.LOG_TAG, "kfusion_init " + version + " raise an Exception : " + e.getMessage());
            res = false;
        }finally {
            mutex.unlock();
        }
        return res;
    }

    public static String kfusion_info() {

        Log.d(SLAMBenchApplication.LOG_TAG, "kfusion_info Start.");

        String res;

        try {
            mutex.lock();
            Version v = _current_version;
            Log.d(SLAMBenchApplication.LOG_TAG, "Start INFO KFusion " + v.name());
            res = v.info();
            Log.d(SLAMBenchApplication.LOG_TAG, "kfusion_info : " + res);

            if (res == null) {
                Log.d(SLAMBenchApplication.LOG_TAG, "kfusion_info : Result = null");
            } else {
                Log.d(SLAMBenchApplication.LOG_TAG, "kfusion_info : Result = '" + res + '\'');
            }

        } catch (Throwable e) {
            Log.d(SLAMBenchApplication.LOG_TAG, "kfusion_info : Error = " + e.getMessage());
            res = null;
        }finally {
            mutex.unlock();
        }
        return res;
    }

    public static String kfusion_process() {

        Log.d(SLAMBenchApplication.LOG_TAG, "kfusion_process Start.");

        String res;

        try {
            mutex.lock();
            Version v = _current_version;
            Log.d(SLAMBenchApplication.LOG_TAG, "Start PROCESS KFusion " + v.name());
            res = v.process();
            Log.d(SLAMBenchApplication.LOG_TAG, "kfusion_process : " + res);
        } catch (Throwable e) {
            Log.d(SLAMBenchApplication.LOG_TAG, "kfusion_process : Error = " + e.getMessage());
            res = null;
        }finally {
            mutex.unlock();
        }
        return res;
    }

    public static boolean kfusion_release() {

        Log.d(SLAMBenchApplication.LOG_TAG, "kfusion_release Start.");


        boolean res;


        try {
            mutex.lock();
            Version v = _current_version;
            Log.d(SLAMBenchApplication.LOG_TAG, "Start RELEASE KFusion " + v.name());
            res = v.release();
            Log.d(SLAMBenchApplication.LOG_TAG, "kfusion_release : " + res);
            _current_version = null;
            _is_initialized = false;

        } catch (Throwable e) {
            Log.d(SLAMBenchApplication.LOG_TAG, "kfusion_release : Error = " + e.getMessage());
            res = false;
        }finally {
            mutex.unlock();
        }

        return res;
    }

    /*
    * JNI External Functions
    * */

    private static native boolean kfusioncppinit(String[] filename);
    private static native String kfusioncppprocess();
    private static native boolean kfusioncpprelease();
    private static native void kfusioncppglinit();
    private static native void kfusioncppglresize(int width, int height);
    private static native void kfusioncppglrender();
    private static native String kfusioncppinfo();

    private static native boolean kfusionompinit(String[] filename);
    private static native String kfusionompprocess();
    private static native boolean kfusionomprelease();
    private static native void kfusionompglinit();
    private static native void kfusionompglresize(int width, int height);
    private static native void kfusionompglrender();
    private static native String kfusionompinfo();

    private static native boolean kfusionmareinit(String[] filename);
    private static native String kfusionmareprocess();
    private static native boolean kfusionmarerelease();
    private static native void kfusionmareglinit();
    private static native void kfusionmareglresize(int width, int height);
    private static native void kfusionmareglrender();
    private static native String kfusionmareinfo();

    private static native boolean kfusionoclinit(String[] filename);
    private static native String kfusionoclprocess();
    private static native boolean kfusionoclrelease();
    private static native void kfusionoclglinit();
    private static native void kfusionoclglresize(int width, int height);
    private static native void kfusionoclglrender();
    private static native String kfusionoclinfo();

    private static native boolean kfusionneoninit(String[] filename);
    private static native String kfusionneonprocess();
    private static native boolean kfusionneonrelease();
    private static native void kfusionneonglinit();
    private static native void kfusionneonglresize(int width, int height);
    private static native void kfusionneonglrender();
    private static native String kfusionneoninfo();

    private static native boolean recorderinit(String[] filename);
    private static native String recorderprocess();
    private static native boolean recorderrelease();
    private static native void recorderglinit();
    private static native void recorderglresize(int width, int height);
    private static native void recorderglrender();
    private static native String recorderinfo();


    public enum ImplementationId {NONE, KFUSION_CPP, KFUSION_OMP, KFUSION_OCL, KFUSION_MARE, KFUSION_NEON,RECORDER}

    public enum FieldIndex {

        FRAME_NUMBER(0, "frame"),
        AQUISITION_DURATION(1, "Acquire"),
        PREPROCESSING_DURATION(2, "Preprocess"),
        TRACKING_DURATION(3, "Track"),
        INTEGRATION_DURATION(4, "Integrate"),
        RAYCASTING_DURATION(5, "Raycast"),
        RENDERING_DURATION(6, "Render"),
        COMPUTATION_DURATION(7, "compute"),
        TOTAL_DURATION(8, "Total"),
        X_POSITION(9, "X"),
        Y_POSITION(10, "Y"),
        Z_POSITION(11, "Z"),
        TRACKED_COUNT(12, "Tracked"),
        INTEGRATED_COUNT(13, "Integrated");

        private final int _value;
        private final String _name;

        FieldIndex(int value, String name) {
            _value = value;
            _name = name;
        }

        public int getValue() {
            return _value;
        }

        public String getName() {
            return _name;
        }
    }

    public interface Version {


        boolean init(String[] filename);

        void glinit();

        String name();

        ImplementationId id();

        void glresize(int w, int h);

        void glrender();

        String libname();

        String process();

        String info();

        boolean release();
    }

    public static class OMPVersion implements Version {
        public OMPVersion() {
        }


        @Override
        public boolean init(String[] filename) {
            return kfusionompinit(filename);
        }

        @Override
        public void glinit() {
            kfusionompglinit();
        }

        @Override
        public String name() {
            return "omp";
        }

        @Override
        public ImplementationId id() {
            return ImplementationId.KFUSION_OMP;
        }

        @Override
        public void glresize(int w, int h) {
            kfusionompglresize(w, h);

        }

        @Override
        public void glrender() {
            kfusionompglrender();
        }

        @Override
        public String libname() {
            return "kfusion-omp";
        }

        @Override
        public String process() {
            return kfusionompprocess();
        }

        @Override
        public boolean release() {
            return kfusionomprelease();
        }

        @Override
        public String info() {
            return kfusionompinfo();
        }
    }

    public static class MAREVersion implements Version {
        public MAREVersion() {
        }

        @Override
        public boolean init(String[] filename) {
            return kfusionmareinit(filename);
        }

        @Override
        public void glinit() {
            kfusionmareglinit();
        }

        @Override
        public ImplementationId id() {
            return ImplementationId.KFUSION_MARE;
        }

        @Override
        public String name() {
            return "mare";
        }

        @Override
        public void glresize(int w, int h) {
            kfusionmareglresize(w, h);
        }

        @Override
        public void glrender() {
            kfusionmareglrender();
        }

        @Override
        public String libname() {
            return "kfusion-mare";
        }

        @Override
        public String process() {
            return kfusionmareprocess();
        }

        @Override
        public boolean release() {
            return kfusionmarerelease();
        }

        @Override
        public String info() {
            return kfusionmareinfo();
        }
    }

    public static class OCLVersion implements Version {
        public OCLVersion() {
        }

        @Override
        public boolean init(String[] filename) {
            return kfusionoclinit(filename);
        }

        @Override
        public ImplementationId id() {
            return ImplementationId.KFUSION_OCL;
        }

        @Override
        public void glinit() {
            kfusionoclglinit();
        }

        @Override
        public String name() {
            return "ocl";
        }

        @Override
        public void glresize(int w, int h) {
            kfusionoclglresize(w, h);
        }

        @Override
        public void glrender() {
            kfusionoclglrender();
        }

        @Override
        public String libname() {
            return "kfusion-opencl";
        }

        @Override
        public String process() {
            return kfusionoclprocess();
        }

        @Override
        public boolean release() {
            return kfusionoclrelease();
        }

        @Override
        public String info() {
            return kfusionoclinfo();
        }
    }

    public static class NEONVersion implements Version {
        public NEONVersion() {
        }

        @Override
        public boolean init(String[] filename) {
            return kfusionneoninit(filename);
        }

        @Override
        public ImplementationId id() {
            return ImplementationId.KFUSION_NEON;
        }

        @Override
        public void glinit() {
            kfusionneonglinit();
        }

        @Override
        public String name() {
            return "neon";
        }

        @Override
        public void glresize(int w, int h) {
            kfusionneonglresize(w, h);
        }

        @Override
        public void glrender() {
            kfusionneonglrender();
        }

        @Override
        public String libname() {
            return "kfusion-neon";
        }

        @Override
        public String process() {
            return kfusionneonprocess();
        }

        @Override
        public boolean release() {
            return kfusionneonrelease();
        }

        @Override
        public String info() {
            return kfusionneoninfo();
        }
    }

    public static class CPPVersion implements Version {
        public CPPVersion() {
        }

        @Override
        public boolean init(String[] filename) {
            return kfusioncppinit(filename);
        }

        @Override
        public void glinit() {
            kfusioncppglinit();
        }

        @Override
        public ImplementationId id() {
            return ImplementationId.KFUSION_CPP;
        }

        @Override
        public String name() {
            return "CPP";
        }

        @Override
        public void glresize(int w, int h) {
            kfusioncppglresize(w, h);
        }

        @Override
        public void glrender() {
            kfusioncppglrender();
        }

        @Override
        public String libname() {
            return "kfusion-cpp";
        }

        @Override
        public String process() {
            return kfusioncppprocess();
        }

        @Override
        public boolean release() {
            return kfusioncpprelease();
        }

        @Override
        public String info() {
            return kfusioncppinfo();
        }
    }

    public static class Recorder implements Version {
        public Recorder() {
        }

        @Override
        public boolean init(String[] filename) {
            return recorderinit(filename);
        }

        @Override
        public void glinit() {
            recorderglinit();
        }

        @Override
        public ImplementationId id() {
            return ImplementationId.RECORDER;
        }

        @Override
        public String name() {
            return "RECORDER";
        }

        @Override
        public void glresize(int w, int h) {
            recorderglresize(w, h);
        }

        @Override
        public void glrender() {
            recorderglrender();
        }

        @Override
        public String libname() {
            return "recorder";
        }

        @Override
        public String process() {
            return recorderprocess();
        }

        @Override
        public boolean release() {
            return recorderrelease();
        }

        @Override
        public String info() {
            return recorderinfo();
        }
    }


}

