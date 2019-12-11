package mymou.task.individual_tasks;

import android.animation.ObjectAnimator;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.Random;

import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;

import android.graphics.Path;

/**
 * Random dot motion task
 *
 * A 'movie' is played, with certain number of dots moving in certain direction
 * After movie has finished, subjects must select which direction had greatest movement
 */
public class TaskRandomDotMotion extends Task {

    // Debug
    public static String TAG = "TaskRandomDotMotion";

    // Global task variables
    private static PreferencesManager prefManager;  // Load settings specified by experimenter
    private static boolean option_one_correct;
    private static Button butt_option_one, butt_option_two;
    private static Handler h0 = new Handler();  // Hide movie handler
    private static Handler h1 = new Handler();  // Show choice buttons handler

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
        logEvent(TAG+" started", callback);

        // Instantiate task objects
        assignObjects();

        // Terrible Android design means have to execute this code in runnable to ensure view is actually created
        // Even though this function is literally called on view created...
        view.post(new Runnable() {
            @Override
            public void run() {

                // Start the movie playing of the different bar heights for this trial
                startMovie();

                // And then present choice once we're finished with the movie
                toggleChoice();
            }
        });


    }

    /**
     * Builds and plays the random dot motion movie
     * Draws specified number of dots on the screen
     * Then animates each dot to move either in the direction of choice, or in a random direction
     * The proportion of dots moving in each of these two conditions is drawn from a random uniform
     * distribution between the percentages specified by the user
     * Angles sampled uniformly from 0 - 2pi radians
     *
     * @param prefManager.rdm_num_dots              Number of dots to be created
     * @param prefManager.rdm_dot_size              Size of dots to be created
     * @param prefManager.rdm_coherence_min         Minimum percentage of dots to move in correct direction
     * @param prefManager.rdm_coherence_max         Maximum percentage of dots to move in correct direction
     * @param prefManager.rdm_movement_distance_min Minimum distance for each dot to move
     * @param prefManager.rdm_movement_distance_max Maximum distance for each dot to move
     * @param prefManager.rdm_movie_length          Duration of the movie (ms)
     */
    private void startMovie() {

        Random r = new Random();

        // Decide on correct option
        option_one_correct = r.nextBoolean();

        // Get view properties
        RelativeLayout movie_bg = getActivity().findViewById(R.id.ll_rdm_movie);
        movie_bg.setBackgroundColor(prefManager.rdm_colour_bg);

        int max_x = movie_bg.getWidth() + (prefManager.rdm_movement_distance_max * 2);
        int max_y = movie_bg.getHeight() + (prefManager.rdm_movement_distance_max * 2);

        // Figure out how many dots to move in correct direction
        float coherence = r.nextFloat() * (prefManager.rdm_coherence_max - prefManager.rdm_coherence_min);
        coherence += prefManager.rdm_coherence_min;
        coherence /= 100;  // As a fraction
        float num_dots_in_corr_dir = prefManager.rdm_num_dots * coherence;

        // Create drawable to colour in dots as they are made
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(prefManager.rdm_colour_dots);

        // Add dots
        ObjectAnimator[] animations = new ObjectAnimator[prefManager.rdm_num_dots];
        for (int i = 0; i < prefManager.rdm_num_dots; i++) {

            // Draw object in random location
            Button dot = new Button(getActivity());
            dot.setLayoutParams(new LinearLayout.LayoutParams(
                    prefManager.rdm_dot_size,
                    prefManager.rdm_dot_size));
            dot.setBackgroundDrawable(drawable);

            // Choose random position
            dot.setX(r.nextInt(max_x) - prefManager.rdm_movement_distance_max);
            dot.setY(r.nextInt(max_y) - prefManager.rdm_movement_distance_max);

            // Add dot to view
            movie_bg.addView(dot);

            // Calculate distance to move object
            float rand_dist = r.nextFloat() * (prefManager.rdm_movement_distance_max - prefManager.rdm_movement_distance_min);
            rand_dist += prefManager.rdm_movement_distance_min;
            rand_dist *= 2; // As formula uses radius, not diameter

            // Calculate angle for dot to move along
            double angle;
            if (i > num_dots_in_corr_dir) {
                angle = (2.0 * Math.PI) * r.nextFloat();  // Pick random angle between 0 and 2 pi
            } else {
                if (option_one_correct) {
                    angle = prefManager.rdm_horizontal_layout ? 0 * Math.PI : 0.5 * Math.PI;
                } else {
                    angle = prefManager.rdm_horizontal_layout ? 1 * Math.PI : 1.5 * Math.PI;
                }
            }
            // Convert angle and radius to polar coordinates
            float xtransloc = (float) (rand_dist * Math.cos(angle));
            float ytransloc = (float) (rand_dist * Math.sin(angle));

            // Build animation
            Path path = new Path();
            path.moveTo(dot.getX(), dot.getY());
            path.lineTo(dot.getX() + xtransloc, dot.getY() + ytransloc);
            animations[i] = ObjectAnimator.ofFloat(dot, "x", "y", path);
            animations[i].setDuration(prefManager.rdm_movie_length);

        }

        // Log trial variables
        logEvent("Coherence set to:"+coherence, callback);
        logEvent("Option one set to:"+option_one_correct, callback);
        logEvent("Max movement distance ="+prefManager.rdm_movement_distance_max, callback);
        logEvent("Min movement distance ="+prefManager.rdm_movement_distance_min, callback);
        logEvent("Movie length ="+prefManager.rdm_movie_length, callback);

        // We're now ready to play animations
        for (int i = 0; i < prefManager.rdm_num_dots; i++) {
            animations[i].start();
        }

        logEvent("Movie started", callback);

    }

    /**
     * Load objects of the task
     * Set choice buttons to invisible, as these will be enabled after movie has finished playing
     */
    private void assignObjects() {
        // Load settings for this task
        prefManager = new PreferencesManager(getContext());
        prefManager.RandomDotMotion();

        // Pick which buttons to use
        if (prefManager.rdm_horizontal_layout) {
            butt_option_one = getView().findViewById(R.id.rdm_butt_left);
            butt_option_two = getView().findViewById(R.id.rdm_butt_right);
        } else {
            butt_option_one = getView().findViewById(R.id.rdm_butt_upper);
            butt_option_two = getView().findViewById(R.id.rdm_butt_lower);
        }

        // Set colours of choice buttons
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(prefManager.rdm_colour_choice);
        butt_option_one.setBackgroundDrawable(drawable);
        butt_option_two.setBackgroundDrawable(drawable);

        // Make everything invisible at the start as startMovie handles the displaying of task objects
        getView().findViewById(R.id.rdm_butt_left).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.rdm_butt_right).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.rdm_butt_upper).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.rdm_butt_lower).setVisibility(View.INVISIBLE);

    }

    /**
     * After a specified delay, toggles the movie off, and the choice buttons on
     *
     * @param prefManager.rdm_choice_delay Delay between movie ending, and choice buttons being
     *                                     presented (ms)
     */
    private void toggleChoice() {
        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Make movie invisible
                getView().findViewById(R.id.ll_rdm_movie).setVisibility(View.INVISIBLE);
                logEvent("Movie finished", callback);

            }
        }, prefManager.rdm_movie_length);

        h1.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Make choices visible
                butt_option_one.setVisibility(View.VISIBLE);
                butt_option_two.setVisibility(View.VISIBLE);
                butt_option_one.setOnClickListener(buttonClickListener);
                butt_option_two.setOnClickListener(buttonClickListener);
                logEvent("Choice options enabled", callback);
            }
        }, prefManager.rdm_movie_length + prefManager.rdm_choice_delay);

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
                case R.id.rdm_butt_left:
                    logEvent("Left button clicked", callback);

                    // They pressed cue for the left option
                    correct_chosen = !option_one_correct;
                    break;
                case R.id.rdm_butt_right:
                    logEvent("Right button clicked", callback);

                    // They pressed cue for the right option
                    correct_chosen = option_one_correct;
                    break;
                case R.id.rdm_butt_upper:
                    logEvent("Upper button clicked", callback);

                    // They pressed cue for the upper option
                    correct_chosen = !option_one_correct;
                    break;
                case R.id.rdm_butt_lower:
                    logEvent("Lower button clicked", callback);

                    // They pressed cue for the lower option
                    correct_chosen = option_one_correct;
                    break;
            }

            // Tell parent (TrialManager.java) the outcome of the trial, which will then respond accordingly
            // (e.g. give reward if correct, set up for next trial, save photo etc)
            logEvent("Trial ended, correct trial="+correct_chosen, callback);
            endOfTrial(correct_chosen, callback);

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
