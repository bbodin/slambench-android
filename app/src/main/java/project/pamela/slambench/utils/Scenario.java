package project.pamela.slambench.utils;

import android.util.Log;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import project.pamela.slambench.SLAMBenchApplication;
import project.pamela.slambench.models.SLAMResult;
import project.pamela.slambench.models.SLAMTest;

/*
 * SLAMBench for Android
 * *********************
 * Author: Bruno Bodin.
 * Copyright (c) 2015 University of Edinburgh.
 * Developed in the PAMELA project, EPSRC Programme Grant EP/K008730/1
 * This code is licensed under the MIT License.
 */

public class Scenario {

    private final Lock _mutex = new ReentrantLock(true);
    private final SLAMResult _current_result;
    private ScenarioState _state = ScenarioState.START_OF_PROGRAM;
    private boolean _kfusion_initialized = false;
    private boolean _kfusion_end_of_file = false;
    private boolean _current_state_resolved = true; // The first state is supposed resolved
    private int _next_frame = 0;

    public Scenario(SLAMTest t) {

        Log.d(SLAMBenchApplication.LOG_TAG, "Scenario has been initialized with a test " + t.toString());
        this._current_result = new SLAMResult(t);
    }


    public SLAMTest getTest() {
        return _current_result.test;
    }

    public int getTotalFrame() {
        return _current_result.test.dataset.getFrameCount();
    }

    public int getCurrentFrame() {
        return _next_frame;
    }

    public boolean isFinsh() {
        return _state == ScenarioState.END_OF_PROGRAM;
    }

    /**
     * Process the computation result and finish the current state
     *
     * @param output line return by Kfusion object
     */


    public void process_computation_result(ComputeFrameResult output) throws Exception {

        // Here we need to check the order of frame, add the X,Y,Z position, and the total duration.

        if (!output.end_of_file) {

            if (_next_frame != output.frame) {
                throw new Exception("Bad frame number : expected = " + _next_frame + " while receive " + output.frame);
            }

            this._current_result.addFrameResult(output);


        }

        this.finishState(output.reply);

    }

    public void finishState(ScenarioReply result) throws Exception {

        if (_current_state_resolved) {
            throw new Exception("Scenario Error !");
        }


        _mutex.lock();

        switch (_state) {
            case FREE_MEMORY:
                if (result != ScenarioReply.SUCCESS) {
                    Log.e("SCENARIO", "You don't free memory correctly and you suppose I'd be happy ! ****");
                    throw new Exception("Scenario Error : memory freeing failed !");
                }
                _kfusion_initialized = false;
            case START_OF_PROGRAM:
                break;

            case KFUSION_FRAME_EXECUTION:
                _kfusion_end_of_file = (result == ScenarioReply.END_OF_FILE);
                _next_frame += (result == ScenarioReply.SUCCESS) ? 1 : 0;
                break;

            case KFUSION_INITILISATION:
                _kfusion_initialized = (result == ScenarioReply.SUCCESS);
                break;
            case END_OF_PROGRAM:
                break;
            default:
                Log.e("SCENARIO", "You're a bad programmer, you should not use strange scenario value like that ...");
                _mutex.unlock();
                throw new Exception("Unknown state found in the scenario system");
        }

        _current_state_resolved = true;

        _mutex.unlock();
    }

    public ScenarioState goNextState() throws Exception {

        if (!_current_state_resolved) {
            throw new Exception("Scenario Error : goNextState before finishState !");
        }

        _mutex.lock();

        _current_state_resolved = false;

        switch (_state) {

            case START_OF_PROGRAM:
                if (_kfusion_initialized) {
                    _state = ScenarioState.KFUSION_FRAME_EXECUTION;
                } else {
                    _state = ScenarioState.KFUSION_INITILISATION;
                }
                break;
            case FREE_MEMORY:
                _state = ScenarioState.END_OF_PROGRAM;
                break;

            case KFUSION_FRAME_EXECUTION:
                if (!_kfusion_end_of_file) {
                    _state = ScenarioState.KFUSION_FRAME_EXECUTION;
                } else {
                    _state = ScenarioState.FREE_MEMORY;
                }
                break;

            case KFUSION_INITILISATION:
                if (_kfusion_initialized) {
                    _state = ScenarioState.KFUSION_FRAME_EXECUTION;
                } else {
                    _state = ScenarioState.FREE_MEMORY;
                }
                break;
            case END_OF_PROGRAM:
                break;
            default:
                Log.e("SCENARIO", "You're a bad programmer, you should not use strange scenario value like that ...");
                _mutex.unlock();
                throw new Exception("Unknown state found in the scenario system");
        }

        _mutex.unlock();
        return _state;
    }

    public SLAMResult getResult(String traj) {
        this._current_result.finishIt(traj);
        return _current_result;
    }

    public enum ScenarioState {
        START_OF_PROGRAM,
        KFUSION_INITILISATION,
        KFUSION_FRAME_EXECUTION,
        FREE_MEMORY,
        END_OF_PROGRAM,
    }

    public enum ScenarioReply {
        SUCCESS,
        FAILURE,
        END_OF_FILE,
    }


}
