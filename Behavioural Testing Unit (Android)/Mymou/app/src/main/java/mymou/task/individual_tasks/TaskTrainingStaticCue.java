package mymou.task.individual_tasks;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.preference.PreferenceManager;

import java.util.Random;

import mymou.R;
import mymou.Utils.ProgressBarAnimation;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 * Training: Static cue task
 * <p>
 * Static cue on screen that subject can press for reward
 */
public class TaskTrainingStaticCue extends Task {

    // Debug
    public static String TAG = "MymouTaskTrainingStaticCue";

    // Task objects
    private Random r = new Random();

    private static PreferencesManager prefManager;
    private static Button cue;
    private Handler hNextTrial = new Handler();
    private Handler hSessionTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_empty, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        // Load preferences
        prefManager = new PreferencesManager(getContext());
        prefManager.TrainingStaticCue();

        // Stop TaskManager doing idle timeout
        callback.disableTrialTimeout();

        assignObjects();

        // Create sess timer
        if (hSessionTimer == null) {
            Log.d(TAG, "starting session timer");
            hSessionTimer = new Handler();
        } else {
            hSessionTimer.removeCallbacksAndMessages(null);
        }

        // Start sess timer
        if (prefManager.t_sc_stopsess) {
            hSessionTimer.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hNextTrial.removeCallbacksAndMessages(null);
                    UtilsTask.toggleCue(cue, false);
                }
            }, prefManager.t_sc_sesslength * 1000 * 60); // Minutes
        }

    }

    private void logTaskEvent(String event) {
        String msg = "" + event;
        logEvent(msg, callback);
    }

    private void assignObjects() {

        // Create and position cue
        cue = UtilsTask.addColorCue(0, prefManager.t_sc_cue_colour,
                getContext(), buttonClickListener, getView().findViewById(R.id.parent_task_empty), prefManager.t_sc_cue_shape, 100, prefManager.t_sc_bordersize, prefManager.t_sc_border_colour);
        cue.setX(prefManager.t_sc_cuex);
        cue.setY(prefManager.t_sc_cuey);
        cue.setWidth(prefManager.t_sc_cuewidth);
        cue.setHeight(prefManager.t_sc_cueheight);

        moveCue();

        UtilsTask.toggleCue(cue, true);
        logTaskEvent("Cues toggled on");

    }


    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // Always disable cues first
            if (prefManager.t_sc_togglecue) {
                UtilsTask.toggleCue(cue, false);
            } else {
                cue.setClickable(false);
            }

            // Pick reward length
            int amount = prefManager.t_sc_maxrew - prefManager.t_sc_minrew;
            if (amount < 1) {
                amount = 1;
            }
            int rewardlength = r.nextInt(amount) + prefManager.t_sc_minrew;
            callback.giveRewardFromTask_(rewardlength);
            logEvent("Giving "+rewardlength+" ms reward", callback);

            // Reactivate cue after reward finished and ITI occurred
            int amount2 = prefManager.t_sc_maxiti - prefManager.t_sc_miniti;
            if (amount2 < 1) {
                amount2 = 1;
            }
            int timeuntilreward = r.nextInt(amount2) + prefManager.t_sc_miniti;

            hNextTrial.postDelayed(new Runnable() {
                @Override
                public void run() {
                    moveCue();
                    UtilsTask.toggleCue(cue, true);
                    logEvent("Cue activated", callback);
                }
            }, (rewardlength) + (timeuntilreward * 1000)); // Minutes


        }
    };

    private void moveCue() {

        if (!prefManager.t_sc_movecue) {
            return;
        }

        int[] xlocs = {prefManager.t_sc_cuex, prefManager.t_sc_cuextwo};
        int[] ylocs = {prefManager.t_sc_cuey, prefManager.t_sc_cueytwo};

        int pos = r.nextInt(2);
        cue.setX(xlocs[pos]);
        cue.setY(ylocs[pos]);
        logEvent("Cue moved to position "+pos, callback);

    }

    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;

    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }

    @Override
    public void onPause() {
        super.onPause();
        logEvent("Task paused/stopped", callback);
        callback.commitTrialDataFromTask_(prefManager.ec_trial_timeout);
        hNextTrial.removeCallbacksAndMessages(null);
        hSessionTimer.removeCallbacksAndMessages(null);
    }
}
