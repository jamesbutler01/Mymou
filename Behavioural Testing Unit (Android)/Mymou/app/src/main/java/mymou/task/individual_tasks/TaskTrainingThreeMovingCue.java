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
import androidx.preference.PreferenceManager;
import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

import java.util.Random;

/**
 * Training task three: Shrinking and Moving Cue
 *
 * Valid touch area starts as the entire screen, and gets progressively smaller
 * The cue also moves randomly around the screen
 * An idle timeout resets size of the cue to the entire screen
 * Must get specified amount of presses in a row to receive reward
 *
 * @param  num_consecutive_corr the current number of consecutive presses
 *
 */
public class TaskTrainingThreeMovingCue extends Task {

    // Debug
    public static String TAG = "TaskTrainingThreeMovingCue";

    private String preftag_successful_trial = "t_three_successful_trial";
    private String preftag_num_consecutive_corr = "t_three_num_consecutive_corr";
    private static int rew_scalar = 1;
    private static int num_consecutive_corr;
    private static PreferencesManager prefManager;
    private SharedPreferences settings;

    // Task objects
    private static Button cue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_empty, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent(TAG+" started", callback);

        loadTrialParams();

        assignObjects();

    }

    private void assignObjects() {
        // Load preferences
        prefManager = new PreferencesManager(getContext());
        prefManager.TrainingTasks();

        // Create one giant cue
        cue = UtilsTask.addColorCue(0, prefManager.t_one_screen_colour,
                getContext(), buttonClickListener, getView().findViewById(R.id.parent_task_empty));

        // Figure out how big to make the cue
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        display.getSize(screen_size);
        int max_x = screen_size.x - prefManager.cue_size;
        int max_y = screen_size.y - prefManager.cue_size;

        float scalar;
        if (num_consecutive_corr > 9) {
            scalar = 0f;
        } else {
            scalar = (10-num_consecutive_corr) / 10f;
        }
        int x_length = (int) (prefManager.cue_size + (max_x * scalar));
        int y_length = (int) (prefManager.cue_size + (max_y * scalar));

        cue.setWidth(x_length);
        cue.setHeight(y_length);
        logEvent("Cue height set to "+x_length+" "+y_length, callback);

        // Put cue in random location
        Float x_range = (float) (screen_size.x - x_length);
        Float y_range = (float) (screen_size.y - y_length);

        Random r = new Random();
        int x_loc = (int) (r.nextFloat() * x_range);
        int y_loc = (int) (r.nextFloat() * y_range);

        cue.setX(x_loc);
        cue.setY(y_loc);

        UtilsTask.toggleCue(cue, true);
        logEvent("Cue toggled on at location "+x_loc+" "+y_loc, callback);

    }


    // Load previous trial params
    private void loadTrialParams() {
        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean prev_trial_correct = settings.getBoolean(preftag_successful_trial, false);
        num_consecutive_corr = settings.getInt(preftag_num_consecutive_corr, 0);
        if(!prev_trial_correct) {
            num_consecutive_corr = 0;
        }

        // Now save values, and they will be overwritten upon correct trial happening
        log_trial_outcome(false);

        Log.d(TAG, ""+num_consecutive_corr+" "+prev_trial_correct);
    }

    private void log_trial_outcome(boolean outcome) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(preftag_successful_trial, outcome);
        editor.putInt(preftag_num_consecutive_corr, num_consecutive_corr);
        editor.commit();
    }


    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            logEvent("Cue pressed", callback);

            // Always disable cues first
            UtilsTask.toggleCue(cue, false);

            // Reset timer for idle timeout on each press
            callback.resetTimer_();

            // Take photo of button press
            callback.takePhotoFromTask_();

            // Log that it was a correct trial
            num_consecutive_corr += 1;
            log_trial_outcome(true);

            // End trial
            endOfTrial(true, rew_scalar, callback, prefManager);

        }
    };

    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;
    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }


}
