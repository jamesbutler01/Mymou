package mymou.task.individual_tasks;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Random;

import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 * Training task four: Small moving cue
 *
 * Cue moves randomly around the screen
 * Instead of idle timeout, it randomly gives reward and then moves the cue
 * Different to all other tasks in that it never ends a trial, and so must handle data logging itself rather than using TaskManager
 *
 */
public class TaskTrainingFourSmallMovingCue extends Task {

    // Debug
    public static String TAG = "TaskTrainingFourSmallMovingCue";

    private static PreferencesManager prefManager;

    // Task objects
    private static Button cue;
    private Float x_range, y_range;
    private Random r;
    private int random_reward_time;
    private static Handler h0 = new Handler();  // Task trial_timer


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_empty, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent(TAG+" started", callback);

        assignObjects();

        positionCue();

        randomRewardTimer(0);

    }

    private void randomRewardTimer(int time) {
        Log.d(TAG, "trial_timer "+time);

        // If reset then pick next reward time
        if (time == 0) {
            random_reward_time = r.nextInt(prefManager.t_random_reward_stop_time - prefManager.t_random_reward_start_time);
            random_reward_time += prefManager.t_random_reward_start_time;
        }

        time += 1;
        final int time_final = time;

        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (time_final > random_reward_time) {

                    Log.d(TAG, "Giving random reward");
                    buttonClickListener.onClick(getView());

                } else {

                    randomRewardTimer(time_final);

                }
            }
        }, 1000);
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
        x_range = (float) (screen_size.x - prefManager.cue_size);
        y_range = (float) (screen_size.y - prefManager.cue_size);

        r = new Random();

        UtilsTask.toggleCue(cue, true);

    }

    private void positionCue() {
        int x_loc = (int) (r.nextFloat() * x_range);
        int y_loc = (int) (r.nextFloat() * y_range);

        logEvent("Moving cue to "+x_loc+" "+y_loc, callback);

        cue.setX(x_loc);
        cue.setY(y_loc);
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            logEvent("Cue clicked", callback);

            // Always disable cues first
            UtilsTask.toggleCue(cue, false);

            // Cancel random reward timer
            h0.removeCallbacksAndMessages(null);


            // Reset timer for idle timeout on each press
            callback.resetTimer_();

            // Take photo of subject
            callback.takePhotoFromTask_();

            // Reward subject
            callback.giveRewardFromTask_(prefManager.rewardduration);

            // Log press
            callback.logEvent_(prefManager.ec_correct_trial);

            // We have to commit the event as well as the trial never actually ends 
            callback.commitTrialDataFromTask_(prefManager.ec_correct_trial);

            // Move cue
            positionCue();

            // Re-enable cue after specified delay
            h0.postDelayed(new Runnable() {
                @Override
                public void run() {
                    UtilsTask.toggleCue(cue, true);
                    logEvent("Cue toggled on", callback);
                    randomRewardTimer(0);
                }
            }, 2000);

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
        super.onDestroy();
        h0.removeCallbacksAndMessages(null);
    }


}
