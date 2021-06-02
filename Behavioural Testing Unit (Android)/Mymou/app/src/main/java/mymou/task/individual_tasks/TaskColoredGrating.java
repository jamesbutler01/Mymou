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
    private int is_target, target_cue_val;
    private static Button red_cue, green_cue, blue_cue, fix_cue, targ_cue, hold_cue;
    private long startTime;
    private float startDimLevel, endDimLevel;
    private int fixation_time, stimulus_time, cue_time, dim_time;
    private int cumulative_reward, cumulative_reward_multiplier;
    private int rewAmount;
    private int rt_limit;
    private int target_shape;
    private SharedPreferences settings;
    private String preftag_colgrat_cumulative_reward;

    /**
     * Function called when task first loaded (before the UI is loaded)
     * Loads the UI components (cues, background etc)
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_colored_grating, container, false);
    }

    /**
     * Function called after the UI has been loaded
     * Once this is called you can then make any UI changes you want (moving cues around etc)
     */

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent(TAG + " started", callback);
        getActivity().findViewById(R.id.background_main).setBackgroundColor(prefManager.colgrat_background_color);

        // Instantiate task objects with curr config
        assignObjects();

        // assign the whole thing here programatically
        startMovie();

    }

    /**
     * Recursive function that manages the timing of the task
     * Flashes two bars onto the screen of different heights, then displays a blank screen, then
     * repeats until the desired number of bars have been displayed, at which points it asks subjects
     * to choose between the two options
     */

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

        // then we present the target color cue
        h1.postDelayed(new Runnable() {
            @Override
            public void run() {

                UtilsTask.toggleCue(targ_cue, true);
                UtilsTask.toggleCue(fix_cue, true); // overlay again because it otherwise overwrites it
                // here we start diming them pseudorandomly
                onDim();

                // once the target starts to dim
                // subject has 700 msec to provide a response for a reward
                // on 15 % trials the target does not dim

                // grating should have pre-dimmming and post-dimming luminance levels

            }
        }, fixation_time + stimulus_time);
    }

    private void assignObjects() {
        prefManager = new PreferencesManager(getContext());
        prefManager.ColoredGrating();

        // get the number of consecutive trials that were correct
        cumulative_reward_multiplier = loadTrialParams();
        if (cumulative_reward_multiplier == 0) { // if something went wrong set starting value
            cumulative_reward_multiplier = 1;
        }

        logEvent(TAG + " current reward level" + cumulative_reward_multiplier, callback);

        // fetch the screen size to be able to center cues
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        display.getSize(screen_size);

        // initialize the starting reward amount and rt limit
        rewAmount = (int) (prefManager.colgrat_starting_reward);
        rt_limit  = (int) (prefManager.colgrat_rt_limit);

        // cue shape type
        target_shape = prefManager.colgrat_target_shape;

        // generate the stuff
        red_cue = UtilsTask.addColorCue(1, R.color.red, getContext(), responseClickListener, getView().findViewById(R.id.parent_task_colgrat), GradientDrawable.OVAL);
        green_cue = UtilsTask.addColorCue(2, R.color.green, getContext(), responseClickListener, getView().findViewById(R.id.parent_task_colgrat), GradientDrawable.OVAL);
        blue_cue = UtilsTask.addColorCue(3, R.color.blue, getContext(), responseClickListener, getView().findViewById(R.id.parent_task_colgrat), GradientDrawable.OVAL);
        fix_cue = UtilsTask.addColorCue(5, R.color.white, getContext(), nullListener, getView().findViewById(R.id.parent_task_colgrat), GradientDrawable.OVAL);

        hold_cue = UtilsTask.addColorCue(6, R.color.white, getContext(), nullListener, getView().findViewById(R.id.parent_task_colgrat), GradientDrawable.RECTANGLE);

        // initialize time information - these need to be sampled from a distribution between those bounds
        int t1, t2, t3, t4;

        fixation_time = new Random().nextInt((prefManager.colgrat_fixation_time_ma - prefManager.colgrat_fixation_time_mi) + 1) + prefManager.colgrat_fixation_time_mi; // how long the fixation cues should be on for in the beginning
        stimulus_time = new Random().nextInt((prefManager.colgrat_stimulus_time_ma - prefManager.colgrat_stimulus_time_mi) + 1) + prefManager.colgrat_stimulus_time_mi; // indicates how long cues should be on the screen
        cue_time      = new Random().nextInt((300+prefManager.colgrat_cue_time_ma - prefManager.colgrat_cue_time_mi) + 1) + prefManager.colgrat_cue_time_mi; // indicates how long the target cue should be on the screen before dimming
        dim_time      = new Random().nextInt((prefManager.colgrat_dim_time_ma - prefManager.colgrat_dim_time_mi) + 1) + prefManager.colgrat_dim_time_mi; // indicates how long a cue should be dimmed for

        startDimLevel = prefManager.colgrat_start_dim;
        endDimLevel = prefManager.colgrat_end_dim;

        // starting dimness
        logEvent(TAG + " check starting dim level" + startDimLevel, callback);

        red_cue.setAlpha(startDimLevel);
        green_cue.setAlpha(startDimLevel);
        blue_cue.setAlpha(startDimLevel);

        // determine which cue is the target cue on this trial
        int max = 3;
        int min = 1;

        Random rand = new Random();
        target_cue_val = rand.nextInt((max - min) + 1) + min;

        logEvent(TAG + " check current target cue val" + target_cue_val, callback);

        switch (target_cue_val) {
            case 1:
                targ_cue = UtilsTask.addColorCue(4, R.color.red, getContext(), nullListener, getView().findViewById(R.id.parent_task_colgrat), target_shape);
                break;
            case 2:
                targ_cue = UtilsTask.addColorCue(4, R.color.green, getContext(), nullListener, getView().findViewById(R.id.parent_task_colgrat), target_shape);
                break;
            case 3:
                targ_cue = UtilsTask.addColorCue(4, R.color.blue, getContext(), nullListener, getView().findViewById(R.id.parent_task_colgrat), target_shape);
                break;
        }

        // new size
        int center_x = (int) (prefManager.cue_size / 4.5);
        int center_y = (int) (prefManager.cue_size / 4.5);

        int center_x_target = (int) (prefManager.cue_size / 2);
        int center_y_target = (int) (prefManager.cue_size / 2);

        fix_cue.setWidth(center_x);
        fix_cue.setHeight(center_y);

        targ_cue.setWidth(center_x_target);
        targ_cue.setHeight(center_y_target);

        // Centre cue on screen
        float x_loc_fix = (screen_size.x / 2) - (center_x / 2);
        float y_loc_fix = (screen_size.y / 2) - (center_y / 2);

        float x_loc_targ = (screen_size.x / 2) - (center_x_target / 2);
        float y_loc_targ = (screen_size.y / 2) - (center_y_target / 2);

        // fixation cue
        fix_cue.setX(x_loc_fix);
        fix_cue.setY(y_loc_fix);

        targ_cue.setX(x_loc_targ);
        targ_cue.setY(y_loc_targ);

        float x_loc_hold = (screen_size.x / 2) - (center_x_target / 3);
        float y_loc_hold = 2000; //(screen_size.y / 2) - (center_y_target / 2);

        hold_cue.setX(x_loc_hold);
        hold_cue.setY(y_loc_hold);

        int hold_x_target = (int) (prefManager.cue_size);
        int hold_y_target = (int) (prefManager.cue_size);

        hold_cue.setWidth(hold_x_target);
        hold_cue.setHeight(hold_y_target);

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

        // turn off all cues
        UtilsTask.toggleCue(red_cue, false);
        UtilsTask.toggleCue(green_cue, false);
        UtilsTask.toggleCue(blue_cue, false);
        UtilsTask.toggleCue(fix_cue, false);
        UtilsTask.toggleCue(targ_cue, false);
    }

    private void onDim()
    {
        // we need a vector of three cues which represents an arbitrary assignment / map to the task
        int[] cue_map;

        // we first get an arbitrary assignment. we always dim them as c1, c2, c3  but the actual values of those
        cue_map = returnDimOrder();

        int c1, c2, c3;
        c1 = cue_map[0]; // 1 - red, 2 - green, 3 - blue
        c2 = cue_map[1];
        c3 = cue_map[2];

        logEvent(TAG + " check current dimming sequence" + c1 + c2 + c3, callback);

        int currTrial; // is this a catch trial?
        currTrial = isCatchTrial();

        logEvent(TAG + " is current trial a catch trial?" + currTrial  + target_cue_val, callback);
        executeDimSequence(currTrial, target_cue_val, c1);

        logEvent(TAG + " after catch check, what is the dime tim?" + dim_time, callback);

        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                executeDimSequence(currTrial, target_cue_val, c2);
            }
        }, dim_time); // this will be delay between reward delivery and next door coming on

        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                executeDimSequence(currTrial, target_cue_val, c3);
            }
        }, dim_time*2); // this will be delay between reward delivery and next door coming on

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
        curr_prob = 0 + Math.random() * (1);
        if (curr_prob > .75) {
            catchTrial = 1;
        } else {
            catchTrial = 0;
        }
        return catchTrial;
    }

    private void executeDimSequence(int currTrial, int target_cue_val, int current_cue)
    {
        if (currTrial == 1 & target_cue_val == current_cue) // don't dim because catch trial
        {
            is_target = 1;
            logEvent(TAG + " no dimming because catch trial, this is the target cue " + is_target, callback);
        }

        else if (currTrial == 1 & target_cue_val != current_cue)
        {
            is_target = 0;
            logEvent(TAG + " no dimming because catch trial, this is not the target cue " + is_target, callback);
        }
        else if((currTrial == 0) & (target_cue_val == current_cue)) // dim it
        {
            logEvent(TAG + " we are dimming cue and its a target cue" + current_cue + target_cue_val, callback);
            is_target = selectDimCue(current_cue, target_cue_val);
        }
        else if((currTrial == 0) & (target_cue_val != current_cue)) // dim it
        {
            logEvent(TAG + " we are dimming cue and its not a target cue" + current_cue + target_cue_val, callback);
            is_target = selectDimCue(current_cue, target_cue_val);
        }
    };

    private int selectDimCue(int cue_map, int curr_target)
    {
        int target_found = 0;
        target_found = startCueDim(cue_map, curr_target);
        return target_found;
    };

    private int startCueDim(int cue_map, int curr_target)
    {

        int target_found = 0;

        logEvent(TAG + " We are about to start dimming cue: " + cue_map, callback);
        logEvent(TAG + "With following dim parameters (end dim level) and (dim time): " + endDimLevel + dim_time, callback);

            switch (cue_map) {
                case 1:
                    red_cue.animate().alpha(endDimLevel).setDuration(dim_time).start();
                    break;
                case 2:
                    green_cue.animate().alpha(endDimLevel).setDuration(dim_time).start();
                    break;
                case 3:
                    blue_cue.animate().alpha(endDimLevel).setDuration(dim_time).start();
                    break;
        }

        if (curr_target == cue_map) {
            target_found = 1;
            startTime = System.currentTimeMillis(); // if this was the target then we start the countdown
            logEvent(TAG + " we are starting countdown because current dimming cue is target cue " + startTime, callback);
        }
        else {
            target_found = 0;
        }

        return target_found;
    }

    // helper functions for saving trial responses

    // Load previous trial params
    private int loadTrialParams() {
        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        return settings.getInt(preftag_colgrat_cumulative_reward, 0); // load previous save
    }

    private void log_trial_outcome(int cumulative_reward) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(preftag_colgrat_cumulative_reward, cumulative_reward);
        editor.commit();
    }

    // this is our listened for the response that computes the reaction time and dictates strength of sound that comes out
    private View.OnClickListener responseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int rewarded_trial = 0;
            // if they clicked on the correct target within the interval, they will be rewarded
            logEvent(TAG + " what is the id of picked target " + view.getId(), callback);
            if ((is_target == 1) & (target_cue_val == view.getId())) // initialize the RT countdown
            {
                float rtTime = System.currentTimeMillis() - startTime;

                logEvent(TAG + " reaction time " + rtTime, callback);

                if (rtTime <= rt_limit)
                {
                    logEvent(TAG + " was the reaction time within bounds?  " + rtTime, callback);

                    rewarded_trial = 1;
                    rewAmount = rewAmount + cumulative_reward_multiplier * 5; // 10x multiplier for each correct trial
                    cumulative_reward += 1;

                    log_trial_outcome(cumulative_reward); // save if it was correct

                }
                else if (rtTime > rt_limit) 
                {
                    logEvent(TAG + " rt was higher than expected" + rtTime, callback);
                    rewarded_trial = 0;
                }
            }
            if (rewarded_trial == 1) {
                // they get the rewarded
                logEvent(TAG + " subject is rewarded now " + rewAmount, callback);
                callback.giveRewardFromTask_(rewAmount, true);
                endOfTrial(true, callback, prefManager);
            }
            else if (rewarded_trial == 0)
            {
                logEvent(TAG + " subject gets no reward " + rewarded_trial, callback);
//                endOfTrial(false, callback, prefManager);
            }
        }
    };

    private View.OnClickListener nullListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int rewarded_trial = 0;
            // if they released within 700 msec of target cue starting to dim they will be rewarded
            float rtTime = System.currentTimeMillis() - startTime;
            logEvent(TAG + " reaction time " + rtTime, callback);

            if (rtTime <= rt_limit) {
                logEvent(TAG + " was the reaction time within bounds?  " + rtTime, callback);
                rewarded_trial = 1;
                rewAmount = rewAmount + cumulative_reward_multiplier * 5; // 10x multiplier for each correct trial
                cumulative_reward += 1;
                log_trial_outcome(cumulative_reward); // save if it was correct
            } else if (rtTime > rt_limit) {
                logEvent(TAG + " rt was higher than expected" + rtTime, callback);
                rewarded_trial = 0;
            }

            if (rewarded_trial == 1) {
                // they get the rewarded
                logEvent(TAG + " subject is rewarded now " + rewAmount, callback);
                callback.giveRewardFromTask_(rewAmount, true);
                endOfTrial(true, callback, prefManager);
            } else if (rewarded_trial == 0) {
                logEvent(TAG + " subject gets no reward " + rewarded_trial, callback);
//                endOfTrial(false, callback, prefManager);
            }

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
  public void setFragInterfaceListener(TaskInterface callback) {this.callback = callback;}

}

