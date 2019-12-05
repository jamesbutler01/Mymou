package mymou.task.individual_tasks;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.Random;

import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;

import android.graphics.Path;
import android.graphics.Point;

/**
 * Random dot motion task
 * <p>
 * A 'movie' is played, with certain number of dots moving in certain direction
 * After movie has finished, subjects must select which direction had greatest movement
 */
public class TaskRandomDotMotion extends Task {

    // Debug
    public static String TAG = "TaskRandomDotMotion";

    // Global task variables
    private static PreferencesManager prefManager;  // Load settings specified by experimenter
    private static boolean upper_option_corr;
    private static Handler h0 = new Handler();  // Show object handler
    private static Handler h1 = new Handler();  // Hide object handler

    /**
     * Function called when task first loaded (before the UI is loaded)
     * Loads the UI components (cues, background etc)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_random_dot_motion, container, false);
    }

    /**
     * Function called after the UI has been loaded
     * Once this is called you can then make any UI changes you want (moving cues around etc)
     */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        // Instantiate task objects
        assignObjects();

        // Terrible Android design means have to execute this code in runnable to ensure view is actually created
        // Even though this function is literally called on view created...
        view.post(new Runnable() {
            @Override
            public void run() {

                // Start the movie playing of the different bar heights for this trial
                startMovie();
            }
        });


    }

    /**
     * Recursive function that manages the timing of the task
     * Flashes two bars onto the screen of different heights, then displays a blank screen, then
     * repeats until the desired number of bars have been displayed, at which points it asks subjects
     * to choose between the two options
     *
     * @param prefManager.ea_step_duration_on  the amount of time to show each bar height (ms)
     * @param prefManager.ea_step_duration_off the amount of time to wait with blank screen before
     *                                         showing next bar heights
     * @param num_steps                        The current position in the sequence. Decremented on each iteration to
     *                                         advance the sequence
     */
    private void startMovie() {

        Random r = new Random();

        // Decide on correct option
        upper_option_corr = r.nextBoolean();

        // Get view properties
        RelativeLayout movie_bg = getActivity().findViewById(R.id.ll_rdm_movie);
        int max_x = movie_bg.getWidth() - (prefManager.rdm_dot_size);
        int max_y = movie_bg.getHeight() - (prefManager.rdm_dot_size);

        // Figure out how many dots to move in correct direction
        float coherence = r.nextFloat() * (prefManager.rdm_coherence_max - prefManager.rdm_coherence_min);
        coherence += prefManager.rdm_coherence_min;
        coherence /= 100;  // As a fraction
        float num_dots_in_corr_dir = prefManager.rdm_num_dots * coherence;

        // Add dots
        ObjectAnimator[] animations = new ObjectAnimator[prefManager.rdm_num_dots];
        for (int i = 0; i < prefManager.rdm_num_dots; i++) {

            // Draw object in random location
            Button myButton = new Button(getActivity());
            myButton.setLayoutParams(new LinearLayout.LayoutParams(
                    prefManager.rdm_dot_size,
                    prefManager.rdm_dot_size));
            myButton.setX(r.nextInt(max_x));
            myButton.setY(r.nextInt(max_y));
            movie_bg.addView(myButton);

            // Calculate distance to move object
            float rand_dist = r.nextFloat() * (prefManager.rdm_movement_distance_max - prefManager.rdm_movement_distance_min);
            rand_dist += prefManager.rdm_movement_distance_min;
            rand_dist *= 2; // As formula uses radius, not diameter

            // Calculate translation
            double angle;
            if (i > num_dots_in_corr_dir) {
                // Pick random angle and calculate translation in terms of x and y
                angle = (2.0 * Math.PI) * r.nextFloat();  // Pick random angle between 0 and 2 pi
            } else {
                if (upper_option_corr) {
                    angle = 0.5 * Math.PI;
                } else {
                    angle = 1.5 * Math.PI;
                }
            }
            float xtransloc = (float) (rand_dist * Math.cos(angle));
            float ytransloc = (float) (rand_dist * Math.sin(angle));

            // Build animation
            Path path = new Path();
            path.moveTo(myButton.getX(), myButton.getY());
            path.lineTo(myButton.getX() + xtransloc, myButton.getY() + ytransloc);
            animations[i] = ObjectAnimator.ofFloat(myButton, "x", "y", path);
            animations[i].setDuration(prefManager.rdm_movie_length);

            Log.d(TAG, "Adding dot " + i + " " + angle + " " + prefManager.rdm_coherence_max + " " + coherence + " " + num_dots_in_corr_dir + " " + (myButton.getX() + rand_dist) + " " + (myButton.getX() + xtransloc) + " " + (myButton.getX() + ytransloc));
        }

        // We're now ready to play animations
        for (int i = 0; i < prefManager.rdm_num_dots; i++) {
            animations[i].start();
        }

    }

    /**
     * Load objects of the task
     * <p>
     * Loads the following settings that the experiment has set through the settings menu:
     *
     * @param prefManager.ea_num_steps The number of different bar heights to display before choice
     * @param prefManager.ea_variance  The variance in bar heights for each of the two options
     * @param prefManager.ea_distance  The distance between the means of the gaussian distribution
     *                                 for the two bar heights
     */
    private void assignObjects() {
        // Load settings for this task
        prefManager = new PreferencesManager(getContext());
        prefManager.RandomDotMotion();

        // Make everything invisible at the start as startMovie handles the displaying of task objects
        getView().findViewById(R.id.rdm_butt_1).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.rdm_butt_2).setVisibility(View.INVISIBLE);

    }

    /**
     * @param r                       Random number generator (instantiated only once in parent function to improve
     *                                performance compared to instantiating a new generator each time function is called)
     * @param mean                    The mean value of the gaussian distribution from which the sample will be drawn
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
                    correct_chosen = upper_option_corr;
                    break;
                case R.id.ea_butt_2:
                    // They pressed cue for bar '2', so this time total2 should be higher
                    correct_chosen = !upper_option_corr;
                    break;
            }

            // Tell parent (TrialManager.java) the outcome of the trial, which will then respond accordingly
            // (e.g. give reward if correct, set up for next trial, save photo etc)
            endOfTrial(correct_chosen, callback);

        }
    };

    /**
     * onPause called whenever a task is paused, interrupted, or cancelled
     * <p>
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

    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }

}
