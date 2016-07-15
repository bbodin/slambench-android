package project.pamela.slambench.utils;


import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.opengles.GL10;
import project.pamela.slambench.jni.KFusion;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */


/**
 * Proxy to the JNI function of rendering
 */
public class KFusionRenderer implements GLSurfaceView.Renderer {


    public KFusionRenderer() {
    }

    public void onDrawFrame(GL10 gl) {
        KFusion.render_view();
    }

    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {

        MessageLog.addInfo("GL_RENDERER = " + gl.glGetString(GL10.GL_RENDERER));
        MessageLog.addInfo("GL_VENDOR = " + gl.glGetString(GL10.GL_VENDOR));
        MessageLog.addInfo("GL_VERSION = " + gl.glGetString(GL10.GL_VERSION));
        MessageLog.addInfo("GL_EXTENSIONS = " + gl.glGetString(GL10.GL_EXTENSIONS));

        KFusion.init_view();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        KFusion.resize_view(width, height);
    }
}
