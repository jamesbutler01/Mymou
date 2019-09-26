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

import java.util.Random;

import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

public class TaskTrainingFourSmallMovingCue extends Task {

    // Debug
    public static String TAG = "TaskTrainingFourSmallMovingCue";

    private static int rew_scalar = 1;
    private static PreferencesManager prefManager;

    // Task objects
    private static Button cue;
    Float x_range, y_range;
    Random r;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_empty, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.d(TAG, "Task started");

        assignObjects();

        positionCue();

    }

    private void assignObjects() {
        // Load preferences
        prefManager = new PreferencesManager(getContext());
        prefManager.TrainingTasks();

        // Create cue
        cue = UtilsTask.addColorCue(0, prefManager.t_one_screen_colour,
                getContext(), buttonClickListener, getView().findViewById(R.id.parent_task_empty));

        // Figure out how big to make the cue
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        display.getSize(screen_size);

        // Put cue in random location
        x_range = (float) (screen_size.x - prefManager.cue_size);
        y_range = (float) (screen_size.y - prefManager.cue_size);

        r = new Random();

        UtilsTask.toggleCue(cue, true);

    }

    private void positionCue() {
        int x_loc = (int) (r.nextFloat() * x_range);
        int y_loc = (int) (r.nextFloat() * y_range);

        cue.setX(x_loc);
        cue.setY(y_loc);
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick");

            // Always disable cues first
            UtilsTask.toggleCue(cue, false);

            // Reset timer for idle timeout on each press
            callback.resetTimer_();

            // Take photo of subject
            callback.takePhotoFromTask_();

            // Reward subject
            callback.giveRewardFromTask_(prefManager.rewardduration);

            // Move cue
            positionCue();

        }
    };

    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;
    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }


}
