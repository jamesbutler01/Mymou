/**
 * Spatial response task
 * <p>
 * Subjects are shown movie where certain cues are highlighted sequentially
 * Must then repeat the sequence that they saw in correct order to receive reward
 * <p>
 * The length of each sequence, and timing properties of the movie, can be altered in the options menu
 * <p>
 * TODO: Implement logging of task variables
 */

package mymou.task.individual_tasks;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.Random;

import mymou.R;
import mymou.Utils.UtilsSystem;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;


public class TaskSpatialResponse extends Task {

    // Debug
    public static String TAG = "MyMouEvidenceAccum";

    private static int num_positions = 8;
    private static int[] chosen_cues;
    private static int choice_counter;
    private static boolean[] chosen_cues_b;
    private static Button[] cues = new Button[num_positions];
    GradientDrawable drawable_red, drawable_grey;
    private static PreferencesManager prefManager;
    private static Handler h0 = new Handler();  // Show object
    private static Handler h1 = new Handler();  // Hide object
    private static Handler h2 = new Handler();  // Choice phase


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_empty, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.d(TAG, "Task started");

        assignObjects();

        startMovie(prefManager.sr_num_stim);
    }


    private void startMovie(int num_steps) {
        Log.d(TAG, "Playing movie, frame: " + num_steps + "/" + prefManager.sr_num_stim);
        if (num_steps > 0) {
            h0.postDelayed(new Runnable() {
                @Override
                public void run() {
                    UtilsTask.toggleCues(cues, true);
                    cues[chosen_cues[num_steps - 1]].setBackgroundDrawable(drawable_red);

                }
            }, prefManager.sr_duration_off);

            h1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cues[chosen_cues[num_steps - 1]].setBackgroundDrawable(drawable_grey);

                    UtilsTask.toggleCues(cues, false);

                    startMovie(num_steps - 1);

                }
            }, prefManager.sr_duration_on + prefManager.sr_duration_off);

        } else {

            // Choice phase
            h2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < cues.length; i++) {

                        UtilsTask.toggleCue(cues[i], true);

                        // Make clickable
                        cues[i].setOnClickListener(buttonClickListener);

                        // Change colour to not reveal answer!
                        GradientDrawable drawable = new GradientDrawable();
                        drawable.setShape(GradientDrawable.RECTANGLE);
                        drawable.setColor(ContextCompat.getColor(getContext(), R.color.white));
                        drawable.setStroke(5, ContextCompat.getColor(getContext(), R.color.black));
                        cues[i].setBackgroundDrawable(drawable);
                    }
                }
            }, prefManager.sr_duration_off);
        }

    }


    private void assignObjects() {
        prefManager = new PreferencesManager(getContext());
        prefManager.SpatialResponse();

        choice_counter = 0;

        // Choose cues (without replacement)
        chosen_cues = new int[prefManager.sr_num_stim];
        chosen_cues_b = UtilsSystem.getBooleanFalseArray(num_positions);

        for (int i = 0; i < prefManager.sr_num_stim; i++) {
            chosen_cues[i] = UtilsTask.chooseValueNoReplacement(chosen_cues_b);
            chosen_cues_b[chosen_cues[i]] = true;
        }

        // Cue colours
        drawable_grey = new GradientDrawable();
        drawable_grey.setShape(GradientDrawable.RECTANGLE);
        drawable_grey.setColor(ContextCompat.getColor(getContext(), R.color.grey));
        drawable_red = new GradientDrawable();
        drawable_red.setShape(GradientDrawable.RECTANGLE);
        drawable_red.setColor(ContextCompat.getColor(getContext(), R.color.red));

        ConstraintLayout layout = getView().findViewById(R.id.parent_task_empty);

        for (int i = 0; i < cues.length; i++) {
            cues[i] = new Button(getContext());
            cues[i].setWidth(75);
            cues[i].setHeight(75);
            cues[i].setBackgroundDrawable(drawable_grey);
            cues[i].setId(i);
            cues[i].setOnClickListener(buttonClickListener);
            layout.addView(cues[i]);
        }

        // Position cues clockwise from 12:00
        cues[0].setX(575);
        cues[1].setX(775);
        cues[2].setX(975);
        cues[3].setX(775);
        cues[4].setX(575);
        cues[5].setX(375);
        cues[6].setX(175);
        cues[7].setX(375);

        cues[0].setY(400);
        cues[1].setY(650);
        cues[2].setY(900);
        cues[3].setY(1150);
        cues[4].setY(1400);
        cues[5].setY(1150);
        cues[6].setY(900);
        cues[7].setY(650);

        UtilsTask.toggleCues(cues, false);

    }

    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;

    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick " + view.getId());

            boolean correct_chosen = Integer.valueOf(view.getId()) == chosen_cues[(prefManager.sr_num_stim - choice_counter) - 1];

            choice_counter += 1;

            if (choice_counter == prefManager.sr_num_stim | !correct_chosen) {
                endOfTrial(correct_chosen, callback);
            } else {
                UtilsTask.toggleCue(cues[Integer.valueOf(view.getId())], false);
            }

        }
    };

    @Override
    public void onPause() {
        super.onPause();
        super.onDestroy();
        h0.removeCallbacksAndMessages(null);
        h1.removeCallbacksAndMessages(null);
        h2.removeCallbacksAndMessages(null);
    }

}
