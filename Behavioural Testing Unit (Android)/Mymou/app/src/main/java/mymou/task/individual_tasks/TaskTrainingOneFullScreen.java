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

// A basic object discrimination task showcasing the main features of the Mymou system:

public class TaskTrainingOneFullScreen extends Task {

    // Debug
    public static String TAG = "TaskTrainingOneFullScreen";

    private static int num_steps = 0;
    private static int rew_scalar = 1;
    private static PreferencesManager prefManager;

    // Task objects
    private static Button cue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_training_one_full_screen, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.d(TAG, "Task started");

        assignObjects();

        positionAndDisplayCues();

    }

    private void positionAndDisplayCues() {
        Log.d(TAG, "Positioning cues around screen");
        UtilsTask.toggleCue(cue, true);
    }


    private void assignObjects() {
        // Load preferences
        prefManager = new PreferencesManager(getContext());
        prefManager.TrainingTasks();

        // Create one giant cue
        cue = UtilsTask.addColorCue(0, prefManager.t_one_screen_colour,
                getContext(), buttonClickListener, getView().findViewById(R.id.parent_t_one));
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        cue.setWidth(size.x);
        cue.setHeight(size.y);
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick");

            // Always disable cues first
            UtilsTask.toggleCue(cue, false);

            // Reset timer for idle timeout on each press
            callback.resetTimer_();

            // Increment number of steps
            num_steps += 1;

            // Check how many correct presses they've got and how many they need per trial
            if (num_steps >= prefManager.t_one_num_presses) {
                endOfTrial(true, rew_scalar, callback);
            } else {
                UtilsTask.toggleCue(cue, true);
            }
        }
    };

    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;
    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }


}
