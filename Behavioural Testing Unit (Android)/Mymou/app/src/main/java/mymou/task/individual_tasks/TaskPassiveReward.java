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
 * Passive reward task
 * <p>
 * Reward is randomly switched on and off according to the experimenter's schedule
 */
public class TaskPassiveReward extends Task {

    // Debug
    public static String TAG = "MymouTaskPassiveReward";

    // Task objects
    private static PreferencesManager prefManager;
    private Random r = new Random();
    private Handler hNextReward = new Handler();
    private Handler hNextIti = new Handler();
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
        prefManager.PassiveReward();

        // Stop TaskManager doing idle timeout
        callback.disableTrialTimeout();

        // Define length of session
        if (hSessionTimer == null) {
            hSessionTimer = new Handler();
        } else {
            hSessionTimer.removeCallbacksAndMessages(null);
        }

        if (prefManager.pass_stopsess) {
            hSessionTimer.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hNextIti.removeCallbacksAndMessages(null);
                    hNextReward.removeCallbacksAndMessages(null);
                }
            }, prefManager.pass_sesslength * 1000 * 60); // Minutes
        }

        //Start task
        startTimer();

    }

    private void startTimer() {

        // Pick length until reward
        int amount = prefManager.pass_maxiti - prefManager.pass_miniti;
        if (amount < 1) {
            amount = 1;
        }
        int timeuntilreward = r.nextInt(amount) + prefManager.pass_miniti;

        // Set handler
        hNextReward.postDelayed(new Runnable() {
            @Override
            public void run() {
                giveReward();
            }
        }, timeuntilreward * 1000);  // Seconds

    }

    private void giveReward() {

        // Pick reward length
        int amount = prefManager.pass_maxrew - prefManager.pass_minrew;
        if (amount < 1) {
            amount = 1;
        }

        int rewardlength = r.nextInt(amount) + prefManager.pass_minrew;
        callback.giveRewardFromTask_(rewardlength, true);
        logEvent(" "+rewardlength+" ms reward given", callback);

        // Set handler
        hNextIti.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (prefManager.pass_photo) {
                    callback.takePhotoFromTask_();
                }
                startTimer();
            }
        }, rewardlength);  // Ms
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
        hNextReward.removeCallbacksAndMessages(null);
        hNextIti.removeCallbacksAndMessages(null);
        hSessionTimer.removeCallbacksAndMessages(null);
    }
}
