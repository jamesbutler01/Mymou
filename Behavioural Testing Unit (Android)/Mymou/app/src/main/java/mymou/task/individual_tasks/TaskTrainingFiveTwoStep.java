/**
 * Training task four: Small moving go_cue
 * <p>
 * Cue  moves randomly around the screen
 * Instead of idle timeout, it randomly gives reward and then moves the go_cue
 * Different to all other tasks in that it never ends a trial, and so must handle data logging itself rather than using TaskManager
 */

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
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.Random;

import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

public class TaskTrainingFiveTwoStep extends Task {

    // Debug
    public static String TAG = "TaskTrainingFourSmallMovingCue";

    private static PreferencesManager prefManager;

    // Task objects
    private static Button go_cue;
    private static ImageButton[] choice_cues;
    private final static int id_go = 4, id_c1_1 = 0, id_c1_2 = 1, id_c2_1 = 2, id_c2_2 = 3;
    private static int trial_counter;
    private static int c2_1_rewamount, c2_2_rewamount, next_rew_change;
    private Float x_range, y_range;
    private Random r;
    private static Handler h0 = new Handler();  // Task trial_timer
    private static Handler h1 = new Handler();  // Inter-trial interval timer


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_empty, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.d(TAG, "Task started");

        assignObjects();

        prepareForNewTrial(0);

    }

    private void assignObjects() {
        // Load preferences
        prefManager = new PreferencesManager(getContext());
        prefManager.TrainingTasks();
        prefManager.TrainingFiveTwoStep();

        // Create go_cue
        ConstraintLayout layout = getView().findViewById(R.id.parent_task_empty);

        go_cue = UtilsTask.addColorCue(id_go, prefManager.t_one_screen_colour,
                getContext(), buttonClickListener, layout);

        // Create choice 1 and choice 2 cues
        choice_cues = new ImageButton[4];
        choice_cues[id_c1_1] = UtilsTask.addImageCue(id_c1_1, getContext(), layout, buttonClickListener);
        choice_cues[id_c1_1].setImageResource(R.drawable.tstc11);

        choice_cues[id_c1_2] = UtilsTask.addImageCue(id_c1_2, getContext(), layout, buttonClickListener);
        choice_cues[id_c1_2].setImageResource(R.drawable.tstc12);

        choice_cues[id_c2_1] = UtilsTask.addImageCue(id_c2_1, getContext(), layout, buttonClickListener);
        choice_cues[id_c2_1].setImageResource(R.drawable.tstc21);

        choice_cues[id_c2_2] = UtilsTask.addImageCue(id_c2_2, getContext(), layout, buttonClickListener);
        choice_cues[id_c2_2].setImageResource(R.drawable.tstc22);

        // Centre static Choice 2 cues
        UtilsTask.centreCue(choice_cues[id_c2_1], getActivity());
        UtilsTask.centreCue(choice_cues[id_c2_2], getActivity());

        // Random number generator
        r = new Random();

        // Locations for go cue
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        display.getSize(screen_size);
        x_range = (float) (screen_size.x - prefManager.cue_size);
        y_range = (float) (screen_size.y - prefManager.cue_size);

        // Switch everything off
        disableCues();
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick");
            callback.logEvent_("cue clicked," + view.getId());

            // Always disable cues first
            disableCues();

            // Cancel timers
            h0.removeCallbacksAndMessages(null);
            h1.removeCallbacksAndMessages(null);

            // Reset timer for idle timeout on each press
            callback.resetTimer_();
            randomRewardTimer();

            switch (view.getId()) {
                case id_go:
                    trial_counter += 1;

                    // Take photo of subject
                    callback.takePhotoFromTask_();

                    // Reward subject
                    callback.giveRewardFromTask_(prefManager.ts_go_cue_reward_amount);
                    getView().findViewById(R.id.parent_task_empty).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));

                    // Log press
                    callback.logEvent_(prefManager.ec_trial_started);

                    // Enable choice 1 after reward finished
                    h1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toggleC1();
                        }
                    }, prefManager.ts_go_cue_reward_amount);

                    break;

                case id_c1_1:
                    toggleC2(id_c1_1);
                    break;
                case id_c1_2:
                    toggleC2(id_c1_2);
                    break;
                case id_c2_1:
                    outcomeStage(id_c2_1);
                case id_c2_2:
                    outcomeStage(id_c2_2);
            }
        }
    };


    private void prepareForNewTrial(int delay) {
        // Move cues
        positionCues();

        // Determine reward amounts
        updateRewardamounts();

        // Re-enable go_cue and start idle timer after specified delay
        h1.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.logEvent_("trial prepared," + prefManager.ec_trial_prepared);
                UtilsTask.toggleCue(go_cue, true);
                randomRewardTimer();
                getView().findViewById(R.id.parent_task_empty).setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
            }
        }, delay + prefManager.ts_intertrial_interval);
    }

    private void updateRewardamounts() {
        if (next_rew_change <= trial_counter) {

            // Roll the dice
            boolean roll = r.nextBoolean();
            if (roll) {
                c2_1_rewamount = prefManager.ts_high_reward_amount;
                c2_2_rewamount = prefManager.ts_low_reward_amount;
            } else {
                c2_1_rewamount = prefManager.ts_low_reward_amount;
                c2_2_rewamount = prefManager.ts_high_reward_amount;
            }

            // Pick next reward update
            next_rew_change = trial_counter + prefManager.ts_low_rew_change + r.nextInt(prefManager.ts_high_rew_change - prefManager.ts_low_rew_change);

        }
    }

    private void endOfTrial(String outcome, int delay) {
        callback.logEvent_("end of trial," + outcome);

        // We have to commit the event as well as the trial never actually ends
        callback.commitTrialDataFromTask_(outcome);

        prepareForNewTrial(delay);
    }

    private void toggleC1() {
        callback.logEvent_("c1 toggled on,");

        UtilsTask.toggleCue(choice_cues[id_c1_1], true);
        UtilsTask.toggleCue(choice_cues[id_c1_2], true);
    }

    private void toggleC2(int c1_pressed) {
        callback.logEvent_("c2 toggled on," + c1_pressed);

        // Roll the dice
        int roll = r.nextInt(100);

        if (roll >= prefManager.ts_transition_prob) {

            // If rare, flip c1_pressed
            if (c1_pressed == id_c1_1) {
                c1_pressed = id_c1_2;
            } else {
                c1_pressed = id_c1_1;
            }

            callback.logEvent_("rare transition," + c1_pressed);
        } else {
            callback.logEvent_("common transition," + c1_pressed);

        }

        if (c1_pressed == id_c1_1) {
            UtilsTask.toggleCue(choice_cues[id_c2_1], true);
            getView().findViewById(R.id.parent_task_empty).setBackgroundColor(prefManager.ts_c2_1_col);
        } else {
            UtilsTask.toggleCue(choice_cues[id_c2_2], true);
            getView().findViewById(R.id.parent_task_empty).setBackgroundColor(prefManager.ts_c2_2_col);
        }
    }

    private void outcomeStage(int c2_pressed) {

        int amount = c2_pressed == id_c2_1 ? c2_1_rewamount : c2_2_rewamount;

        callback.logEvent_("c2_pressed," + c2_pressed + "," + amount);
        callback.giveRewardFromTask_(amount);
        endOfTrial(prefManager.ec_correct_trial, amount);

    }

    private void disableCues() {
        UtilsTask.toggleCue(go_cue, false);
        UtilsTask.toggleCues(choice_cues, false);
    }

    private void positionCues() {
        int x_loc = (int) (r.nextFloat() * x_range);
        int y_loc = (int) (r.nextFloat() * y_range);

        go_cue.setX(x_loc);
        go_cue.setY(y_loc);

        // Move choice 1 cues as well
        UtilsTask.randomlyPositionCues(Arrays.copyOf(choice_cues, 2),
                new UtilsTask().getPossibleCueLocs(getActivity()));
    }

    private void randomRewardTimer() {

        // Pick reward time
        int random_reward_time = r.nextInt(prefManager.t_random_reward_stop_time - prefManager.t_random_reward_start_time);
        random_reward_time += prefManager.t_random_reward_start_time;
        Log.d(TAG, "Setting timer for " + random_reward_time);

        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Giving random reward");
                disableCues();
                callback.giveRewardFromTask_(prefManager.ts_go_cue_reward_amount);
                endOfTrial(prefManager.ec_trial_timeout, prefManager.ts_go_cue_reward_amount);
            }
        }, random_reward_time * 1000);
    }


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
        h1.removeCallbacksAndMessages(null);
    }


}
