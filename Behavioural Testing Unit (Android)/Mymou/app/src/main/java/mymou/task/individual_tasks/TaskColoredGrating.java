package mymou.task.individual_tasks;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.graphics.drawable.GradientDrawable;

import androidx.preference.PreferenceManager;

import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

import android.graphics.Point;
import android.view.Display;

import java.util.Random;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Colored Grating Task
 */
public class TaskColoredGrating extends Task {

    // Debug
    public static String TAG = "MymouTaskColoredGrating";

    // Global task variables
    private static PreferencesManager prefManager;  // Load settings specified by experimenter
    private static Handler h0 = new Handler();  // Show object handler
    private static Handler h1 = new Handler();  // Hide object handler
    private int dimmed_target, target_cue_val;
    private static Button red_cue, green_cue, blue_cue;
    private final int red_id=1, green_id=2, blue_id=3;
    //    private static Button hold_cue;  // Have to hold this for duration of trial
    private static Button fix_cue;  // Central fixation spot
    private static Button targ_cue;  // Overlaid cue indicating which cue to attend
    private long startTime;
    private int fixation_time, stimulus_time, cue_time, dim_time;
    private int cumulative_reward;
    private int target_shape;
    private int targ_size;
    private SharedPreferences settings;
    private String preftag_colgrat_cumulative_reward = "preftag_colgratcumrew";

    /**
     * Function called when task first loaded (before the UI is loaded)
     * Loads the UI components (cues, background etc)
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_empty, container, false);
    }

    /**
     * Function called after the UI has been loaded
     * Once this is called you can then make any UI changes you want (moving cues around etc)
     */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent(TAG + " started", callback);
        getActivity().findViewById(R.id.parent_task_empty).setBackgroundColor(prefManager.colgrat_background_color);

        // Instantiate task objects with curr config
        assignObjects();

        // assign the whole thing here programatically
        startMovie();

    }

    private void startMovie() {

        // fixation on immediately
        UtilsTask.toggleCue(fix_cue, true);

        // three main cues come on after variable delay between 600 to 1000 msec
        h0.postDelayed(new Runnable() {
            @Override
            public void run() {

                UtilsTask.toggleCue(blue_cue, true);
                UtilsTask.toggleCue(green_cue, true);
                UtilsTask.toggleCue(red_cue, true);

            }
        }, fixation_time);

        // Target has not been dimmed yet!
        dimmed_target = 0;

        // then we present the target color cue
        h1.postDelayed(new Runnable() {
            @Override
            public void run() {

                UtilsTask.toggleCue(targ_cue, true);
                UtilsTask.toggleCue(fix_cue, true); // overlay again because it otherwise overwrites it

                // here we start diming them pseudorandomly
                startDim();

            }
        }, fixation_time + stimulus_time);
    }

    private void assignObjects() {
        prefManager = new PreferencesManager(getContext());
        prefManager.ColoredGrating();

        // get the number of consecutive trials that were correct
        cumulative_reward = loadTrialHistory();
        logEvent(TAG + " current reward level" + cumulative_reward, callback);

        // fetch the screen size to be able to center cues
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        display.getSize(screen_size);

        // cue shape type
        target_shape = prefManager.colgrat_target_shape;

        // determine which cue is the target cue on this trial
        int max = 3;
        int min = 1;

        Random rand = new Random();
        target_cue_val = rand.nextInt((max - min) + 1) + min;

        logEvent(TAG + " check current target cue val" + target_cue_val, callback);
        // Define this first so it is always behind the other cues
        switch (target_cue_val) {
            case 1:
                targ_cue = UtilsTask.addColorCue(4, prefManager.colgrat_red_cue, getContext(), null, getView().findViewById(R.id.parent_task_empty), target_shape, false);
                break;
            case 2:
                targ_cue = UtilsTask.addColorCue(4, prefManager.colgrat_green_cue, getContext(), null, getView().findViewById(R.id.parent_task_empty), target_shape, false);
                break;
            case 3:
                targ_cue = UtilsTask.addColorCue(4, prefManager.colgrat_blue_cue, getContext(), null, getView().findViewById(R.id.parent_task_empty), target_shape, false);
                break;
        }

        // generate col cues
        red_cue = UtilsTask.addColorCue(red_id, prefManager.colgrat_red_cue, getContext(), responseClickListener, getView().findViewById(R.id.parent_task_empty), GradientDrawable.OVAL, false);
        green_cue = UtilsTask.addColorCue(green_id, prefManager.colgrat_green_cue, getContext(), responseClickListener, getView().findViewById(R.id.parent_task_empty), GradientDrawable.OVAL, false);
        blue_cue = UtilsTask.addColorCue(blue_id, prefManager.colgrat_blue_cue, getContext(), responseClickListener, getView().findViewById(R.id.parent_task_empty), GradientDrawable.OVAL, false);
        red_cue.setHeight(prefManager.colgrat_sizecolcue);
        red_cue.setWidth(prefManager.colgrat_sizecolcue);
        green_cue.setHeight(prefManager.colgrat_sizecolcue);
        green_cue.setWidth(prefManager.colgrat_sizecolcue);
        blue_cue.setHeight(prefManager.colgrat_sizecolcue);
        blue_cue.setWidth(prefManager.colgrat_sizecolcue);

        fixation_time = new Random().nextInt((prefManager.colgrat_fixation_time_ma - prefManager.colgrat_fixation_time_mi) + 1) + prefManager.colgrat_fixation_time_mi; // how long the fixation cues should be on for in the beginning
        stimulus_time = new Random().nextInt((prefManager.colgrat_stimulus_time_ma - prefManager.colgrat_stimulus_time_mi) + 1) + prefManager.colgrat_stimulus_time_mi; // indicates how long cues should be on the screen
        cue_time = new Random().nextInt((300 + prefManager.colgrat_cue_time_ma - prefManager.colgrat_cue_time_mi) + 1) + prefManager.colgrat_cue_time_mi; // indicates how long the target cue should be on the screen before dimming
        dim_time = new Random().nextInt((prefManager.colgrat_dim_time_ma - prefManager.colgrat_dim_time_mi) + 1) + prefManager.colgrat_dim_time_mi; // indicates how long a cue should be dimmed for

        red_cue.setAlpha(prefManager.colgrat_start_dim);
        green_cue.setAlpha(prefManager.colgrat_start_dim);
        blue_cue.setAlpha(prefManager.colgrat_start_dim);

        if (prefManager.colgrat_trainingmode) {
            targ_cue.setAlpha(prefManager.colgrat_start_dim);
            targ_size = prefManager.colgrat_sizecolcue + prefManager.colgrat_sizeindicatorcue;
        } else {
            targ_size = prefManager.colgrat_sizefixcue + prefManager.colgrat_sizeindicatorcue;
        }
        targ_cue.setHeight(targ_size);
        targ_cue.setWidth(targ_size);

        fix_cue = UtilsTask.addColorCue(5, prefManager.colgrat_fix_cue, getContext(), null, getView().findViewById(R.id.parent_task_empty), GradientDrawable.OVAL, false);
        fix_cue.setHeight(prefManager.colgrat_sizefixcue);
        fix_cue.setWidth(prefManager.colgrat_sizefixcue);

        // Centre cues on screen
        float x_loc_fix = (screen_size.x / 2) - (prefManager.colgrat_sizefixcue / 2);
        float y_loc_fix = (screen_size.y / 2) - (prefManager.colgrat_sizefixcue / 2);
        fix_cue.setX(x_loc_fix);
        fix_cue.setY(y_loc_fix);

        float x_loc_targ = (screen_size.x / 2) - (targ_size / 2);
        float y_loc_targ = (screen_size.y / 2) - (targ_size / 2);
        targ_cue.setX(x_loc_targ);
        targ_cue.setY(y_loc_targ);

        randomlyPositionCues();

        // turn off all cues
        UtilsTask.toggleCue(red_cue, false);
        UtilsTask.toggleCue(green_cue, false);
        UtilsTask.toggleCue(blue_cue, false);
        UtilsTask.toggleCue(fix_cue, false);
        UtilsTask.toggleCue(targ_cue, false);
    }

    private void randomlyPositionCues() {

        // create location vector to sample from
        int[] x_locs = new int[]{prefManager.colgrat_red_x, prefManager.colgrat_green_x, prefManager.colgrat_blue_x};
        int[] y_locs = new int[]{prefManager.colgrat_red_y, prefManager.colgrat_green_y, prefManager.colgrat_blue_y};

        Integer[] array_descr_x = {0, 1, 2};
        Integer[] array_descr_y = {0, 1, 2};

        if (prefManager.colgrat_position_rand == 1) {
            List<Integer> intList_x = Arrays.asList(array_descr_x);
            Collections.shuffle(intList_x);
            array_descr_x = intList_x.toArray(array_descr_x);

            List<Integer> intList_y = Arrays.asList(array_descr_y);
            Collections.shuffle(intList_y);
            array_descr_y = intList_x.toArray(array_descr_y);
        }

        // doing it this way ensures we can randomize positions of individual cues independently in x and y
        // if we want to randomize them across trials while retaining their original positions to stay in RF.
        // importantly by doing randomization the 'color_x' and 'color_y' become meaningless as they randomized

        red_cue.setX(x_locs[array_descr_x[0]]);
        red_cue.setY(y_locs[array_descr_y[0]]);

        green_cue.setX(x_locs[array_descr_x[1]]);
        green_cue.setY(y_locs[array_descr_y[1]]);

        blue_cue.setX(x_locs[array_descr_x[2]]);
        blue_cue.setY(y_locs[array_descr_y[2]]);

        // Now draw the stripes over the top of it
        int[] ids = {red_id, green_id, blue_id};
        for (int j=0; j<ids.length; j++) {
            for (int i = 0; i < prefManager.colgrat_numstripes; i++) {
                Button stripe = UtilsTask.addColorCue(ids[j], prefManager.colgrat_background_color, getContext(), responseClickListener, getView().findViewById(R.id.parent_task_empty), GradientDrawable.RECTANGLE, false);
                stripe.setWidth(PreferencesManager.colgrat_sizestripes);
                if (PreferencesManager.colgrat_trainingmode) {
                    stripe.setHeight(targ_size);
                    stripe.setX(x_locs[array_descr_x[j]] + (i * prefManager.colgrat_sizestripes * 2) + prefManager.colgrat_grateoffset - (prefManager.colgrat_sizeindicatorcue / 2));
                    stripe.setY(y_locs[array_descr_x[j]] - (prefManager.colgrat_sizeindicatorcue / 2));
                } else {
                    stripe.setHeight(prefManager.colgrat_sizecolcue);
                    stripe.setX(x_locs[array_descr_x[j]] + (i * prefManager.colgrat_sizestripes * 2) + prefManager.colgrat_grateoffset);
                    stripe.setY(y_locs[array_descr_x[j]]);
                }
            }
        }

        // If training mode then we need to also move target cue over the relevant spatial cue
        if (prefManager.colgrat_trainingmode) {
                targ_cue.setX(x_locs[array_descr_x[target_cue_val-1]]- (prefManager.colgrat_sizeindicatorcue / 2));
                targ_cue.setY(y_locs[array_descr_y[target_cue_val-1]]- (prefManager.colgrat_sizeindicatorcue / 2));
        }

    }

    private void startDim() {
        // we need a vector of three cues which represents an arbitrary assignment / map to the task
        int[] cue_map;

        // we first get an arbitrary assignment. we always dim them as c1, c2, c3  but the actual values of those
        cue_map = returnDimOrder();

        int c1, c2, c3;
        c1 = cue_map[0]; // 1 - red, 2 - green, 3 - blue
        c2 = cue_map[1];
        c3 = cue_map[2];

        logEvent(TAG + " check current dimming sequence" + c1 + c2 + c3, callback);

        int catchTrial; // is this a catch trial?
        catchTrial = isCatchTrial();

        logEvent(TAG + " is current trial a catch trial?" + catchTrial + target_cue_val, callback);
        executeDimSequence(catchTrial, target_cue_val, c1);

        logEvent(TAG + " after catch check, what is the dime tim?" + dim_time, callback);

        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                executeDimSequence(catchTrial, target_cue_val, c2);
            }
        }, dim_time); // this will be delay between reward delivery and next door coming on

        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                executeDimSequence(catchTrial, target_cue_val, c3);
            }
        }, dim_time * 2); // this will be delay between reward delivery and next door coming on

        // If dont press anything
        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                trialEnded(catchTrial);  // Correct if catch trial, incorrect if not catch trial
            }
        }, dim_time * 3 + prefManager.colgrat_rt_limit); // After all dims + reaction time limit

    }

    private int[] returnDimOrder() {
        // random number
        int max = 6; // for now we constrain to only probe trials
        int min = 1;

        Random rand = new Random();
        int trialType = rand.nextInt((max - min) + 1) + min;

        // initialize code
        int[] cue_config = new int[3];

        switch (trialType)
        // trialConfig returns trial configuration
        {
            case 1:
                cue_config = new int[]{1, 2, 3};
                break;
            case 2:
                cue_config = new int[]{1, 3, 2};
                break;
            case 3:
                cue_config = new int[]{2, 1, 3};
                break;
            case 4:
                cue_config = new int[]{2, 3, 1};
                break;
            case 5:
                cue_config = new int[]{3, 1, 2};
                break;
            case 6:
                cue_config = new int[]{3, 2, 1};
                break;
        }
        return cue_config;
    }

    private int isCatchTrial() {
        int catchTrial;
        double curr_prob;
        curr_prob = Math.random() * (100);
        if (curr_prob < prefManager.colgrat_catchtrialfreq) {
            catchTrial = 1;
        } else {
            catchTrial = 0;
        }
        return catchTrial;
    }

    private void executeDimSequence(int catchTrial, int target_cue_val, int current_cue) {
        if (catchTrial == 1) {// don't dim because catch trial
            logEvent(TAG + " no dimming because catch trial, this is the target cue " + dimmed_target, callback);
        } else if ((catchTrial == 0) & (target_cue_val == current_cue)) { // dim it
            logEvent(TAG + " we are dimming cue and its a target cue" + current_cue + target_cue_val, callback);
            startCueDim(current_cue, target_cue_val);
        } else if ((catchTrial == 0) & (target_cue_val != current_cue)) { // dim it
            logEvent(TAG + " we are dimming cue and its not a target cue" + current_cue + target_cue_val, callback);
            startCueDim(current_cue, target_cue_val);
        }
    }

    // Dims relevant cue and toggles target_found if it's the target cue
    private void startCueDim(int cue_map, int curr_target) {

        logEvent(TAG + " We are about to start dimming cue: " + cue_map, callback);
        logEvent(TAG + "With following dim parameters (end dim level) and (dim time): " + prefManager.colgrat_end_dim + dim_time, callback);

        switch (cue_map) {
            case 1:
                red_cue.animate().alpha(prefManager.colgrat_end_dim).setDuration(dim_time).start();
                break;
            case 2:
                green_cue.animate().alpha(prefManager.colgrat_end_dim).setDuration(dim_time).start();
                break;
            case 3:
                blue_cue.animate().alpha(prefManager.colgrat_end_dim).setDuration(dim_time).start();
                break;
        }

        if (curr_target == cue_map) {

            if (prefManager.colgrat_trainingmode) {
                targ_cue.animate().alpha(prefManager.colgrat_end_dim).setDuration(dim_time).start();
            }

            dimmed_target = 1;
            startTime = System.currentTimeMillis(); // if this was the target then we start the countdown
            logEvent(TAG + " we are starting countdown because current dimming cue is target cue " + startTime, callback);
        }

    }

    // Load previous trial params
    private int loadTrialHistory() {
        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        return settings.getInt(preftag_colgrat_cumulative_reward, 0); // load previous save
    }

    private void log_trial_outcome(int cumulative_reward) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(preftag_colgrat_cumulative_reward, cumulative_reward);
        editor.commit();
    }

    private void trialEnded(int correctTrial) {
        if (correctTrial == 1) {

            cumulative_reward += 1;
            log_trial_outcome(cumulative_reward); // save if it was correct

            // they get the rewarded
            logEvent(TAG + " subject is rewarded now " + prefManager.rewardduration+" x "+cumulative_reward, callback);
            endOfTrial(true, cumulative_reward, callback, prefManager);

        } else if (correctTrial == 0) {
            cumulative_reward = 0;
            log_trial_outcome(cumulative_reward); // save if it was correct

            logEvent(TAG + " subject gets no reward " + correctTrial, callback);
            endOfTrial(false, callback, prefManager);

        }

    }

    // this is our listened for the response that computes the reaction time and dictates strength of sound that comes out
    private View.OnClickListener responseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            float rtTime = System.currentTimeMillis() - startTime;

            int correctTrial = 0;

            if ((dimmed_target == 1) & (target_cue_val == view.getId())) {

                logEvent(TAG + " clicked correct target" + view.getId(), callback);

                if (rtTime <= prefManager.colgrat_rt_limit) {
                    logEvent(TAG + " reaction time quick enough! " + rtTime, callback);

                    correctTrial = 1;

                } else {
                    logEvent(TAG + " rt too slow" + rtTime, callback);
                }
            } else {
                logEvent(TAG + " clicked target at wrong time " + view.getId(), callback);
            }

            trialEnded(correctTrial);
        }
    };


    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            return false;
        }
    };

    /**
     * onPause called whenever a task is paused, interrupted, or cancelled
     * If task aborted for some reason (e.g. they did not respond quick enough), then cancel the handlers to stop the movie playing
     * This prevents task objects being loaded AFTER a trial has finished
     */
    @Override
    public void onPause() {
        super.onPause();
        super.onDestroy();
        h0.removeCallbacksAndMessages(null);
        h1.removeCallbacksAndMessages(null);
    }

    TaskInterface callback;

    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }

}

