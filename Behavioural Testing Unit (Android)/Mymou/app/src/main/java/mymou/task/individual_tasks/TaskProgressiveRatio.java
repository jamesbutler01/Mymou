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
 * Progressive Ratio task
 * <p>
 * After each successful press, you need 2^(successful presses) for reward
 * On idle timeout will then reset number of successful presses needed to 1
 *
 * @param num_consecutive_corr the current number of consecutive successful trials
 * @param num_presses_needed   this trial's presses needed for reward
 * @param num_presses          the current number of presses made in this trial
 */
public class TaskProgressiveRatio extends Task {

    // Debug
    public static String TAG = "MymouTaskProgressiveRatio";

    // Task objects
    private String preftag_successful_trial = "pr_successful_trial";
    private String preftag_num_consecutive_corr = "pr_num_consecutive_corr";
    private static int rew_scalar = 1;
    private static int num_presses, num_consecutive_corr, num_presses_needed;
    private static PreferencesManager prefManager;
    private SharedPreferences settings;
    private ProgressBar pb1;
    private int pb_scalar = 1000;
    private static Button cue;
    private Handler hNextTrial = new Handler();
    private Handler hTrialTimer = new Handler();
    private Handler hSessionTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_prog_ratio, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        loadTrialParams();

        assignObjects();

        logTaskEvent(prefManager.ec_trial_started + "," + TAG + " started");

        resetTimer();

        if (hSessionTimer == null) {
            Log.d(TAG, "starting session timer");
            hSessionTimer = new Handler();
        } else {
            hSessionTimer.removeCallbacksAndMessages(null);
        }

        hSessionTimer.postDelayed(new Runnable() {
            @Override
            public void run() {
                endOfSession();
            }
        }, prefManager.pr_sess_length * 1000 * 60); // Minutes

    }

    private void logTaskEvent(String event) {
        String msg = "" + num_consecutive_corr + "," + num_presses_needed + "," + num_presses + "," + event;
        logEvent(msg, callback);
    }

    private void assignObjects() {
        preftag_successful_trial = getResources().getString(R.string.pr_successful_trial);
        preftag_num_consecutive_corr = getResources().getString(R.string.pr_num_consecutive_corr);

        // Progress bar
        pb1 = (ProgressBar) getView().findViewById(R.id.boosterBar);
        if (prefManager.pr_progress_bar) {
            configureProgressBar();
        } else {
            pb1.setEnabled(false);
            pb1.setVisibility(View.INVISIBLE);
        }

        // Create and position cue
        cue = UtilsTask.addColorCue(0, prefManager.pr_cue_colour,
                getContext(), buttonClickListener, getView().findViewById(R.id.parent_prog_ratio), prefManager.pr_cue_shape, prefManager.pr_cue_size, prefManager.pr_border_size, prefManager.pr_border_colour);
        cue.setX(prefManager.pr_cuex);
        cue.setY(prefManager.pr_cuey);
        randomlyPositionCue();

        UtilsTask.toggleCue(cue, true);
        logTaskEvent("Cues toggled on");

    }

    private void randomlyPositionCue() {

        // Only move cue if user specified
        if (!prefManager.pr_move_cue) {
            return;
        }

        // Get size of screen
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // Find possible locs along each dimension
        int imageWidths = prefManager.cue_size;
        int border = prefManager.cue_spacing;  // Spacing between different task objects
        int totalImageSize = imageWidths + border;
        int[] xlocs = UtilsTask.calculateLocs(size.x, totalImageSize);
        int[] ylocs = UtilsTask.calculateLocs(size.y - 300, totalImageSize);

        Random r = new Random();
        cue.setX(xlocs[r.nextInt(xlocs.length)]);
        cue.setY(ylocs[r.nextInt(ylocs.length)] + 300);
        logTaskEvent("cue moved to " + cue.getX() + " " + cue.getY());
    }


    // Load previous trial params
    private void loadTrialParams() {
        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean prev_trial_correct = settings.getBoolean(preftag_successful_trial, false);
        num_consecutive_corr = settings.getInt(preftag_num_consecutive_corr, 0);
        if (!prev_trial_correct) {
            num_consecutive_corr = 0;
        }

        // Calculate number of presses needed
        if (num_consecutive_corr < 9) {
            num_presses_needed = num_consecutive_corr + 1;
        } else if (num_consecutive_corr < 17) {
            num_presses_needed = 11 + ((num_consecutive_corr - 9) * 2);
        } else {
            num_presses_needed = 29 + ((num_consecutive_corr - 17) * 4);
        }

        // Now save values, and they will be overwritten upon correct trial happening
        log_trial_outcome(false);

        // Reset num presses
        num_presses = 0;

        // Load preferences
        prefManager = new PreferencesManager(getContext());
        prefManager.ProgressiveRatio();

        if (prefManager.pr_skip_go_cue) {
            // Stop TaskManager doing idle timeout
            callback.disableTrialTimeout();
        }
    }

    private void log_trial_outcome(boolean outcome) {
        Log.d(TAG, "log_trial_outcome: " + outcome + "," + num_consecutive_corr);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(preftag_successful_trial, outcome);
        editor.putInt(preftag_num_consecutive_corr, num_consecutive_corr);
        editor.commit();
    }

    private void configureProgressBar() {
        logTaskEvent("Progress bar enabled");
        pb1.setMax(num_presses_needed * pb_scalar);
        pb1.setProgress(0);
    }

    private void updateProgressBar() {
        if (prefManager.pr_progress_bar) {
            ProgressBarAnimation anim = new ProgressBarAnimation(pb1, pb1.getProgress(), num_presses * pb_scalar);
            anim.setDuration(prefManager.pr_animation_duration);
            pb1.startAnimation(anim);
        }
    }

    private void resetTimer() {
        hTrialTimer.removeCallbacksAndMessages(null);
        hTrialTimer.postDelayed(new Runnable() {
            @Override
            public void run() {
                endOfSession();
            }
        }, prefManager.pr_timeoutlength * 1000);
    }

    private void endOfSession() {
        if (prefManager.pr_skip_go_cue) {

            Log.d("TAG", "Stopping task");
            callback.commitTrialDataFromTask_(prefManager.ec_trial_timeout);

            hNextTrial.removeCallbacksAndMessages(null);
            hTrialTimer.removeCallbacksAndMessages(null);
            hSessionTimer.removeCallbacksAndMessages(null);

            UtilsTask.toggleCue(cue, false);
            if (prefManager.pr_progress_bar) {
                pb1.setEnabled(false);
                pb1.setVisibility(View.INVISIBLE);
            }
        }
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // Always disable cues first
            UtilsTask.toggleCue(cue, false);

            // Reset timer for idle timeout on each press
            callback.resetTimer_();
            resetTimer();

            // Increment number of steps
            num_presses += 1;

            // Update progress bar
            updateProgressBar();

            // Log press
            logTaskEvent("cue pressed (" + num_presses + ")");

            if (num_presses >= num_presses_needed) {

                // Log that it was a correct trial
                num_consecutive_corr += 1;
                log_trial_outcome(true);

                // End trial for reward
                if (!prefManager.pr_skip_go_cue) {
                    endOfTrial(true, rew_scalar, callback, prefManager);
                } else {

                    callback.commitTrialDataFromTask_(prefManager.ec_correct_trial);

                    hTrialTimer.removeCallbacksAndMessages(null);

                    callback.giveRewardFromTask_(prefManager.rewardduration);

                    // Restart task after x delay
                    hNextTrial.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onViewCreated(null, null);
                        }
                    }, prefManager.pr_iti);
                }

            } else {

                // Move cue
                randomlyPositionCue();

                // Let them press again
                // Restart task after x delay
                hNextTrial.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UtilsTask.toggleCue(cue, true);
                        logTaskEvent("Cues toggled on");
                    }
                }, prefManager.pr_blinklength);

            }
        }
    };

    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;

    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }

    @Override
    public void onPause() {
        super.onPause();
        hNextTrial.removeCallbacksAndMessages(null);
        hTrialTimer.removeCallbacksAndMessages(null);
        hSessionTimer.removeCallbacksAndMessages(null);
    }
}
