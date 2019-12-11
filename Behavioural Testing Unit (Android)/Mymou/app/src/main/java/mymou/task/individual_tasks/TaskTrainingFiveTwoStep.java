package mymou.task.individual_tasks;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Path;
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

import java.util.Random;

import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 * Training task five: Two step task
 *
 * Implementation of Thomas Akam's reduced two-step task (Akam et al. 2015)
 *
 * Stimuli are taken from Brady, T. F., Konkle, T., Alvarez, G. A. and Oliva, A. (2008). Visual
 *  long-term memory has a massive storage capacity for object details. Proceedings of the National
 *  Academy of Sciences, USA, 105 (38), 14325-14329.
 */
public class TaskTrainingFiveTwoStep extends Task {

    // Debug
    public static String TAG = "TaskTrainingFiveTwoStep";

    private static PreferencesManager prefManager;

    // Task objects
    private static Button go_cue;
    private static ImageButton[] choice_cues;
    private final static int id_go = 4, id_c1_1 = 0, id_c1_2 = 1, id_c2_1 = 2, id_c2_2 = 3;
    private static int trial_counter;
    private static int c2_1_rew_percent, c2_2_rew_percent, next_rew_change;
    private Float x_range, y_range;
    private View background;
    private Random r;
    private static Handler h0 = new Handler();  // Task trial_timer
    private static Handler h1 = new Handler();  // Inter-trial interval timer
    private static Handler h2 = new Handler();  // Dim brightness timer


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_empty, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent(TAG+" started", callback);

        assignObjects();

        // Randomly decide which state starts out high when task first started
        if (r.nextBoolean()) {
            updateRewardamounts();
        }

        prepareForNewTrial(0);

    }

    private void assignObjects() {
        // Load preferences
        prefManager = new PreferencesManager(getContext());
        prefManager.TrainingTasks();
        prefManager.TrainingFiveTwoStep();

        background = getView().findViewById(R.id.parent_task_empty);

        // Create go_cue
        ConstraintLayout layout = getView().findViewById(R.id.parent_task_empty);

        go_cue = UtilsTask.addColorCue(id_go, ContextCompat.getColor(getContext(), R.color.green),
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

        // Centre static Choice 2 cues and secondary reinforcer
        choice_cues[id_c2_1].setX(450);
        choice_cues[id_c2_1].setY(750);
        choice_cues[id_c2_2].setX(450);
        choice_cues[id_c2_2].setY(750);

        // Random number generator
        r = new Random();

        // Locations for go cue
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        display.getSize(screen_size);
        x_range = (float) (screen_size.x - prefManager.cue_size);
        y_range = (float) (screen_size.y - prefManager.cue_size);

        next_rew_change = 0;

        disableCues();
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            callback.logEvent_("17," + view.getId() + ",,,, cue clicked");

            // Always disable cues first
            disableCues();

            // Make sure screen bright
            callback.setBrightnessFromTask_(true);

            // Cancel timers
            h0.removeCallbacksAndMessages(null);
            h1.removeCallbacksAndMessages(null);
            h2.removeCallbacksAndMessages(null);

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

                    // Log press
                    callback.logEvent_(prefManager.ec_trial_started + ",,,,, trial started");

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
                    break;
                case id_c2_2:
                    outcomeStage(id_c2_2);
                    break;
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

                callback.resetTimer_();
                callback.logEvent_("18," + prefManager.ec_trial_prepared + ",,,, trial prepared");

                disableCues();

                UtilsTask.toggleCue(go_cue, true);

                randomRewardTimer();

                background.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));

                // Disabled for now
                // animateGoCue();

            }
        }, delay + prefManager.ts_intertrial_interval);
    }

    private void updateRewardamounts() {
        if (next_rew_change <= trial_counter) {

            if (c2_1_rew_percent == prefManager.ts_low_reward_percent) {
                c2_1_rew_percent = prefManager.ts_high_reward_percent;
                c2_2_rew_percent = prefManager.ts_low_reward_percent;
            } else {
                c2_1_rew_percent = prefManager.ts_low_reward_percent;
                c2_2_rew_percent = prefManager.ts_high_reward_percent;
            }

            // Pick next reward update
            next_rew_change = trial_counter + prefManager.ts_rew_change_interval;
            callback.logEvent_("10," + c2_1_rew_percent + "," + c2_2_rew_percent + "," + (next_rew_change - trial_counter) + ",, rewards changed");
        }
    }

    private void endOfTrial(String outcome, int delay) {
        h0.removeCallbacksAndMessages(null);
        callback.logEvent_("11," + outcome + ",,,, end of trial");

        // We have to commit the event as well as the trial never actually ends
        callback.commitTrialDataFromTask_(outcome);

        prepareForNewTrial(delay);
    }

    private void toggleC1() {
        callback.logEvent_("12,,,,, c1 toggled on,");

        UtilsTask.toggleCue(choice_cues[id_c1_1], true);
        UtilsTask.toggleCue(choice_cues[id_c1_2], true);
    }

    private void toggleC2(int c1_pressed) {

        // Roll the dice
        int roll = r.nextInt(100);

        if (roll >= prefManager.ts_transition_prob) {

            // If rare, flip c1_pressed
            if (c1_pressed == id_c1_1) {
                c1_pressed = id_c1_2;
            } else {
                c1_pressed = id_c1_1;
            }

            callback.logEvent_("13," + c1_pressed + "," + roll + ",,, rare transition");
        } else {
            callback.logEvent_("13," + c1_pressed + "," + roll + ",,, common transition");
        }

        if (c1_pressed == id_c1_1) {
            background.setBackgroundColor(prefManager.ts_c2_1_col);
        } else {
            background.setBackgroundColor(prefManager.ts_c2_2_col);
        }

        final int c1_pressed_final = c1_pressed;

        // Enable choice 1 after reward finished
        h1.removeCallbacksAndMessages(null);
        h1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (c1_pressed_final == id_c1_1) {
                    UtilsTask.toggleCue(choice_cues[id_c2_1], true);
                } else {
                    UtilsTask.toggleCue(choice_cues[id_c2_2], true);
                }
                callback.logEvent_("14," + c1_pressed_final + ",,,,c2 toggled on");
            }
        }, prefManager.ts_go_cue_reward_amount);

    }

    private void outcomeStage(int c2_pressed) {

        int percent_needed = c2_pressed == id_c2_1 ? c2_1_rew_percent : c2_2_rew_percent;
        int roll = r.nextInt(100);
        callback.logEvent_("15," + c2_pressed + "," + percent_needed + "," + roll + ",, outcome");

        if (roll < percent_needed) {

            callback.logEvent_("19, 1, ,,, pump activated");
            callback.giveRewardFromTask_(prefManager.ts_trial_reward_amount);
            background.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue));
            endOfTrial(prefManager.ec_correct_trial, prefManager.ts_trial_reward_amount);

        } else {

            callback.logEvent_("19, 0, ,,, no pump");
            background.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.fuchsia));
            endOfTrial(prefManager.ec_incorrect_trial, prefManager.ts_trial_reward_amount);

        }


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

        callback.logEvent_("16,"+x_loc+","+y_loc+",,,go cue x and y position");

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        display.getSize(screen_size);

        // Choice 1 cues
        // Hardcoded as for some reason using Point's didn't work
        if (r.nextBoolean()) {
            if (r.nextBoolean()) {
                callback.logEvent_("20,175,1200,725,300, choice 1 positions");
                choice_cues[id_c1_1].setX(175);
                choice_cues[id_c1_1].setY(1200);
                choice_cues[id_c1_2].setX(725);
                choice_cues[id_c1_2].setY(300);
            } else {
                callback.logEvent_("20,725,300,175,1200, choice 1 positions");
                choice_cues[id_c1_1].setX(725);
                choice_cues[id_c1_1].setY(300);
                choice_cues[id_c1_2].setX(175);
                choice_cues[id_c1_2].setY(1200);
            }
        } else {
            if (r.nextBoolean()) {
                callback.logEvent_("20,725,1200,175,300, choice 1 positions");
                choice_cues[id_c1_1].setX(725);
                choice_cues[id_c1_1].setY(1200);
                choice_cues[id_c1_2].setX(175);
                choice_cues[id_c1_2].setY(300);
            } else {
                callback.logEvent_("20,175,300,725,1200, choice 1 positions");
                choice_cues[id_c1_1].setX(175);
                choice_cues[id_c1_1].setY(300);
                choice_cues[id_c1_2].setX(725);
                choice_cues[id_c1_2].setY(1200);
            }
        }

    }

    private void randomRewardTimer() {

        // Pick reward time
        int random_reward_time = r.nextInt(prefManager.t_random_reward_stop_time - prefManager.t_random_reward_start_time);
        random_reward_time += prefManager.t_random_reward_start_time;
        Log.d(TAG, "Setting timer for " + random_reward_time);
        callback.logEvent_("33," + random_reward_time + ",,,, idle timer set");

        h0.removeCallbacksAndMessages(null);
        h0.postDelayed(new Runnable() {
            @Override
            public void run() {

                callback.logEvent_("25,,,,, random reward");
                callback.setBrightnessFromTask_(true);
                disableCues();
                if (r.nextInt(20) < 1) {
                    callback.takePhotoFromTask_();
                }
                callback.giveRewardFromTask_(prefManager.ts_go_cue_reward_amount * 2);
                endOfTrial(prefManager.ec_trial_timeout, prefManager.ts_go_cue_reward_amount);

                // Dim screen as well
                h2.removeCallbacksAndMessages(null);
                h2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        callback.setBrightnessFromTask_(false);
                    }
                }, 20000);


            }
        }, random_reward_time * 1000);
    }


    public void animateGoCue() {
            Path path = new Path();
        path.moveTo(175, 1200);
        path.lineTo(725, 300);
        path.moveTo(725, 300);
        path.lineTo(175, 300);
        path.moveTo(175, 300);
        path.lineTo(725, 1200);
        path.moveTo(725, 1200);
        path.lineTo(175, 1200);
        path.moveTo(175, 1200);
        ObjectAnimator mover = ObjectAnimator.ofFloat(go_cue, "x", "y", path);
        mover.setDuration(10000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(mover);
        animatorSet.start();
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
        h2.removeCallbacksAndMessages(null);
        callback.setBrightnessFromTask_(true);
    }


}
