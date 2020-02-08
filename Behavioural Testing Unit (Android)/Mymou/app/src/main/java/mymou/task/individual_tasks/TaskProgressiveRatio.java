package mymou.task.individual_tasks;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
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
 *
 * After each successful press, you need 2^(successful presses) for reward
 * On idle timeout will then reset number of successful presses needed to 1
 *
 * @param num_consecutive_corr the current number of consecutive successful trials
 * @param num_presses_needed this trial's presses needed for reward
 * @param num_presses the current number of presses made in this trial
 */
public class TaskProgressiveRatio extends Task {

    // Debug
    public static String TAG = "TaskProgressiveRatio";

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_prog_ratio, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent(TAG+" started", callback);

        loadTrialParams();

        assignObjects();

        logTaskEvent(prefManager.ec_trial_started);

    }

    private void logTaskEvent(String event) {
        String msg = "" + num_consecutive_corr + "," + num_presses_needed + "," + num_presses + "," + event;
        logEvent(msg, callback);
    }

    private void assignObjects() {

        // Progress bar
        if (prefManager.pr_progress_bar) {
            configureProgressBar();
        } else {
            pb1.setEnabled(false);
            pb1.setVisibility(View.INVISIBLE);
        }

        // Create cue
        cue = UtilsTask.addColorCue(0, prefManager.pr_cue_colour,
                getContext(), buttonClickListener, getView().findViewById(R.id.parent_prog_ratio));
        randomlyPositionCue();
        UtilsTask.toggleCue(cue, true);

    }

    private void randomlyPositionCue() {
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
        logTaskEvent("cue moved to "+cue.getX()+" "+cue.getY());
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
        num_presses_needed = (int) Math.pow(2, num_consecutive_corr);

        // Now save values, and they will be overwritten upon correct trial happening
        log_trial_outcome(false);
        Log.d(TAG, "num_consecutive_corr=" + num_consecutive_corr + " " + prev_trial_correct);

        // Reset num presses
        num_presses = 0;

        // Load preferences
        prefManager = new PreferencesManager(getContext());
        prefManager.ProgressiveRatio();
    }

    private void log_trial_outcome(boolean outcome) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(preftag_successful_trial, outcome);
        editor.putInt(preftag_num_consecutive_corr, num_consecutive_corr);
        editor.commit();
    }

    private void configureProgressBar() {
        logEvent("Progress bar enabled", callback);
        pb1 = (ProgressBar) getView().findViewById(R.id.boosterBar);
        pb1.setMax(num_presses_needed * pb_scalar);
        pb1.setProgress(0);
    }

    private void updateProgressBar() {
        if (prefManager.pr_progress_bar) {
            ProgressBarAnimation anim = new ProgressBarAnimation(pb1, pb1.getProgress(), num_presses * pb_scalar);
            anim.setDuration(prefManager.dm_animation_duration);
            Log.d(TAG, "onClick " + prefManager.pr_animation_duration);
            pb1.startAnimation(anim);
        }
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            logTaskEvent("cue pressed,"+num_presses);

            // Always disable cues first
            UtilsTask.toggleCue(cue, false);

            // Reset timer for idle timeout on each press
            callback.resetTimer_();

            // Increment number of steps
            num_presses += 1;

            // Update progress bar
            updateProgressBar();

            // Log outcome
            logTaskEvent("-1");

            if (num_presses >= num_presses_needed) {

                // Log that it was a correct trial
                num_consecutive_corr += 1;
                log_trial_outcome(true);

                // End trial for reward
                endOfTrial(true, rew_scalar, callback, prefManager);

            } else {

                // Move cue
                randomlyPositionCue();

                // Let them press again
                UtilsTask.toggleCue(cue, true);
                logTaskEvent("Cues toggled on");
            }
        }
    };

    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;

    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }


}
