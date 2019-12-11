package mymou.task.individual_tasks;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 * Training task one
 *
 * Pressing anywhere on screen will trigger device
 * Must get specified amount of presses in a row to receive reward
 *
 * @param  num_steps the current number of presses made in this trial
 *
 */
public class TaskTrainingOneFullScreen extends Task {

    // Debug
    public static String TAG = "TaskTrainingOneFullScreen";

    private static int num_steps;
    private static int rew_scalar = 1;
    private static PreferencesManager prefManager;

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

        assignObjects();
    }

    private void assignObjects() {
        // Load preferences
        prefManager = new PreferencesManager(getContext());
        prefManager.TrainingTasks();

        // Create one giant cue
        cue = UtilsTask.addColorCue(0, prefManager.t_one_screen_colour,
                getContext(), buttonClickListener, getView().findViewById(R.id.parent_task_empty));
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        cue.setWidth(size.x);
        cue.setHeight(size.y);

        // Reset num steps
        num_steps = 0;

        // Enable cue
        UtilsTask.toggleCue(cue, true);
        logEvent("Cue toggled on", callback);
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // Always disable cues first
            UtilsTask.toggleCue(cue, false);

            // Reset timer for idle timeout on each press
            callback.resetTimer_();

            // Increment number of steps
            num_steps += 1;

            // Take photo of button press
            callback.takePhotoFromTask_();

            // Check how many correct presses they've got and how many they need per trial
            logEvent("Cue pressed (num steps: "+num_steps+"/"+prefManager.t_one_num_presses+")", callback);

            if (num_steps >= prefManager.t_one_num_presses) {
                endOfTrial(true, rew_scalar, callback);
            } else {
                UtilsTask.toggleCue(cue, true);
                logEvent("Cue toggled on", callback);
            }
        }
    };

    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;
    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }


}
