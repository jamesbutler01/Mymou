package mymou.task.individual_tasks;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;


import java.util.Random;

import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;

/**
 *
 * Evidence accumulation task
 *
 * A 'movie' is played, which consists of two 'progress bars' being flashed on the screen a specified number of times
 * On each flash the height of the two bars differs
 * After movie has finished, subjects must select which bar  had the greatest total height over the movie
 *
 * The amount of evidence shown, and the distance (difficulty) between options can be altered in the options menu
 *
 */
public class TaskEvidenceAccum extends Task {

    // Debug
    public static String TAG = "TaskEvidenceAccum";

    // Global task variables
    private static PreferencesManager prefManager;  // Load settings specified by experimenter
    private static int total1, total2;  // The total counts of how much each bar is filled over the sequence
    private static ProgressBar[] progressBars = new ProgressBar[2];  // The bar task objects, whose height will be altered on each movie frame
    private static int[] amounts1, amounts2;  // The sequence of amounts to fill option1 and option2
    private static Handler h0 = new Handler();  // Show object handler
    private static Handler h1 = new Handler();  // Hide object handler

    /**
     * Function called when task first loaded (before the UI is loaded)
     * Loads the UI components (cues, background etc)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_evidence_accum, container, false);
    }

    /**
     *
     * Function called after the UI has been loaded
     * Once this is called you can then make any UI changes you want (moving cues around etc)
     *
     */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent(TAG+" started", callback);

        // Instantiate task objects
        assignObjects();

        // Start the movie playing of the different bar heights for this trial
        startMovie(prefManager.ea_num_steps);

    }

    /**
     * Recursive function that manages the timing of the task
     * Flashes two bars onto the screen of different heights, then displays a blank screen, then
     * repeats until the desired number of bars have been displayed, at which points it asks subjects
     * to choose between the two options
     *
     * @param prefManager.ea_step_duration_on the amount of time to show each bar height (ms)
     * @param prefManager.ea_step_duration_off the amount of time to wait with blank screen before
     *                                         showing next bar heights
     * @param num_steps The current position in the sequence. Decremented on each iteration to
     *                  advance the sequence
     *
     */
    private void startMovie(int num_steps) {
        logEvent("Playing step: "+num_steps, callback);

        // First check its not the final step, which would the choice phase
        if (num_steps > 0) {

            // Set timer to switch on bars of appropriate heights after a certain duration
            h0.postDelayed(new Runnable() {
                @Override
                public void run() {
                    logEvent("Setting bars to " + amounts1[num_steps - 1] + " and " + amounts2[num_steps - 1], callback);

                    // Display bars on screen
                    progressBars[0].setVisibility(View.VISIBLE);
                    progressBars[1].setVisibility(View.VISIBLE);

                    // Set bars to the appropriate height
                    progressBars[0].setProgress(amounts1[num_steps - 1]);
                    progressBars[1].setProgress(amounts2[num_steps - 1]);

                    // Keep track of the total amounts so we know which answer is the correct answer
                    // at the end
                    total1 += amounts1[num_steps - 1];
                    total2 += amounts2[num_steps - 1];

                }
            }, prefManager.ea_step_duration_off);

            // At the same time, set a second timer to switch off the bars after a certain duration
            h1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    logEvent("Switching bars off", callback);

                    // Set both bars to invisible
                    progressBars[0].setVisibility(View.INVISIBLE);
                    progressBars[1].setVisibility(View.INVISIBLE);

                    // Call the function again and decrement where we are in the sequence
                    startMovie(num_steps - 1);
                }

            }, prefManager.ea_step_duration_on + prefManager.ea_step_duration_off);

        } else {

            logEvent("Choice phase enabled", callback);

            // Choice phase, simply switch on the choice buttons and activate the buttonClickListener
            getView().findViewById(R.id.ea_butt_1).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.ea_butt_2).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.ea_butt_1).setOnClickListener(buttonClickListener);
            getView().findViewById(R.id.ea_butt_2).setOnClickListener(buttonClickListener);
        }
    }

    /**
     * Load objects of the task
     *
     * Loads the following settings that the experiment has set through the settings menu:
     *
     * @param prefManager.ea_num_steps The number of different bar heights to display before choice
     * @param prefManager.ea_variance The variance in bar heights for each of the two options
     * @param prefManager.ea_distance The distance between the means of the gaussian distribution
     *                                for the two bar heights
     *
     */
    private void assignObjects() {
        // Load settings for this task
        prefManager = new PreferencesManager(getContext());
        prefManager.EvidenceAccum();

        // Make everything invisible at the start as startMovie handles the displaying of task objects
        getView().findViewById(R.id.ea_butt_1).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.ea_butt_2).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.ea_bar_1).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.ea_bar_2).setVisibility(View.INVISIBLE);

        // Assign the progress bars
        progressBars[0] = getView().findViewById(R.id.ea_bar_1);
        progressBars[1] = getView().findViewById(R.id.ea_bar_2);

        // Reset the total counts
        total1 = 0;
        total2 = 0;

        // Calculate amounts
        Random r = new Random();

        // Calculate the means of the bar heights for bar 1 and bar 2
        int range = 6 - prefManager.ea_distance;
        int mean1 = 3 + r.nextInt(range);
        int mean2 = mean1 + prefManager.ea_distance;

        // Swap which bar is on the top, and which is on the bottom, half the time
        // This eliminates any motor correlations for solving the task
        if (r.nextBoolean()) {
            int s = mean1;
            mean1 = mean2;
            mean2 = s;
        }

        // Instantiate arrays that store the height of each bar for each step
        amounts1 = new int[prefManager.ea_num_steps];
        amounts2 = new int[prefManager.ea_num_steps];

        // Now for each step, pick a value for each bar from the appropriate gaussian distribution
        for (int i = 0; i < prefManager.ea_num_steps; i++) {
            amounts1[i] = (int) getValue(r, mean1);
            amounts2[i] = (int) getValue(r, mean2);
            logEvent("For step "+i+": amount1 ="+amounts1[i]+", amount2 ="+amounts2[i], callback);

        }

    }

    /**
     * @param r Random number generator (instantiated only once in parent function to improve
     *          performance compared to instantiating a new generator each time function is called)
     * @param mean The mean value of the gaussian distribution from which the sample will be drawn
     * @param prefManager.ea_variance The variance of the gaussian distribution
     * @return a random number drawn a gaussian distribution of the specified mean and variance
     */
    private double getValue(Random r, int mean) {
        double a = ((r.nextGaussian() * prefManager.ea_variance) + mean) * 100;

        // Double check the number is not outside the limits of the progress bars
        if (a > 1000) {
            a = 1000;
        }
        if (a < 0) {
            a = 0;
        }
        Log.d(TAG, "Returning value: " + a);
        return a;
    }


    /**
     * Called whenever a cue is pressed by a subject
     * So then loads the next appropriate stage of the task depending on what cue was selected
     */
    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // Decide what to do based on which cue pressed
            boolean correct_chosen = false;
            switch (view.getId()) {
                case R.id.ea_butt_1:
                    // They pressed cue for bar '1', so total1 should be higher than total2
                    correct_chosen = total1 > total2;
                    logEvent("Button 1 pressed, correct trial="+correct_chosen, callback);
                    break;
                case R.id.ea_butt_2:
                    // They pressed cue for bar '2', so this time total2 should be higher
                    correct_chosen = total2 > total1;
                    logEvent("Button 2 pressed, correct trial="+correct_chosen, callback);
                    break;
            }

            // Tell parent (TrialManager.java) the outcome of the trial, which will then respond accordingly
            // (e.g. give reward if correct, set up for next trial, save photo etc)
            endOfTrial(correct_chosen, callback);

        }
    };

    /**
     * onPause called whenever a task is paused, interrupted, or cancelled
     *
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

     /**
     * This is static code repeated in each task that enables communication between the individual
     * task and the parent TaskManager.java which handles the backend utilities (reward delivery,
     * selfie processing, intertrial intervals, etc etc)
     */
    TaskInterface callback;
    public void setFragInterfaceListener(TaskInterface callback) {this.callback = callback;}

}
