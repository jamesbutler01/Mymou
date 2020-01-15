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

/**
 * Spatial response task
 *
 * Subjects are shown movie where certain cues are highlighted sequentially
 * Must then repeat the sequence that they saw in correct order to receive reward
 *
 * The length of each sequence, and timing properties of the movie, can be altered in the options menu
 *
 * TODO: Implement logging of task variables
 */
public class TaskSpatialResponse extends Task {

    // Debug
    public static String TAG = "TaskSpatialResponse";

    private static Button[] cues;
    private static int[] chosen_cues;
    private static int choice_counter;
    private static int task_phase;
    private GradientDrawable drawable_red, drawable_grey;
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
        logEvent(TAG+" started", callback);

        assignObjects();

        startMovie(prefManager.sr_num_stim);
    }


    private void startMovie(int num_steps) {
        logEvent("Playing movie, frame: " + num_steps + "/" + prefManager.sr_num_stim, callback);

        if (num_steps > 0) {
            h0.postDelayed(new Runnable() {
                @Override
                public void run() { // turn ON the cue
                    task_phase = 1;
                    UtilsTask.toggleCues(cues, true);
                    cues[chosen_cues[num_steps - 1]].setBackgroundDrawable(drawable_red);
                    logEvent("Cues toggled on (frame: " + num_steps + "/" + prefManager.sr_num_stim +")", callback);
                }
            }, prefManager.sr_duration_off);

            h1.postDelayed(new Runnable() { // turn off the cue
                @Override
                public void run() {
                    task_phase = 2;
                    cues[chosen_cues[num_steps - 1]].setBackgroundDrawable(drawable_grey);
                    UtilsTask.toggleCues(cues, false);
                    logEvent("Cues toggled off (frame: " + num_steps + "/" + prefManager.sr_num_stim +")", callback);
                    startMovie(num_steps - 1);
                }
            }, prefManager.sr_duration_on + prefManager.sr_duration_off);

        } else {

            // Choice phase
            h2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    task_phase = 3;
                    for (int i = 0; i < cues.length; i++) {

                        UtilsTask.toggleCue(cues[i], true);

                        // Change colour to not reveal answer!
                        GradientDrawable drawable = new GradientDrawable();
                        drawable.setShape(GradientDrawable.OVAL); //use different shape to denote a response cue
                        drawable.setColor(ContextCompat.getColor(getContext(), R.color.white));
                        drawable.setStroke(5, ContextCompat.getColor(getContext(), R.color.black));
                        cues[i].setBackgroundDrawable(drawable);
                    }
                    logEvent("Choice cues toggled on (frame: " + num_steps + "/" + prefManager.sr_num_stim +")", callback);
                }
            }, prefManager.sr_duration_off);
        }

    }

    private void assignObjects() {
        prefManager = new PreferencesManager(getContext());
        prefManager.SpatialResponse();

        int locations = prefManager.sr_locations;
        if (locations<3) {locations=2;}else if (locations<5) {locations=4;}else {locations = 8;}

        choice_counter = 0;
        task_phase = 0;
        cues = new Button[locations];

        // Choose cues (without replacement)
        chosen_cues = new int[prefManager.sr_num_stim];
        boolean[] chosen_cues_b = UtilsSystem.getBooleanFalseArray(locations);

        for (int i = 0; i < prefManager.sr_num_stim; i++) {
            chosen_cues[i] = UtilsTask.chooseValueNoReplacement(chosen_cues_b);
            chosen_cues_b[chosen_cues[i]] = true;
            logEvent("Cue "+i+" set to "+chosen_cues[i], callback);
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

        switch (locations) {
            case 2:
                cues[0].setX(575);
                cues[1].setX(575);
                cues[0].setY(400);
                cues[1].setY(1400);
                break;
            case 4:
                // Position cues clockwise from 12:00
                cues[0].setX(575);
                cues[1].setX(975);
                cues[2].setX(575);
                cues[3].setX(175);
                cues[0].setY(400);
                cues[1].setY(900);
                cues[2].setY(1400);
                cues[3].setY(900);
                break;
            default:
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
                break;
        }

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

            boolean correct_chosen = view.getId() == chosen_cues[(prefManager.sr_num_stim - choice_counter) - 1];
            logEvent(""+view.getId()+" cue pressed ("+correct_chosen+" answer)", callback);
            choice_counter += 1;

            if (task_phase < 3) {
                logEvent("Stimulus cue rather than decision cue clicked!!!", callback);
                endOfTrial(false, callback);
            }
            else if (choice_counter == prefManager.sr_num_stim | !correct_chosen) {
                logEvent("End of trial, correct outcome:"+correct_chosen, callback);
                endOfTrial(correct_chosen, callback);
            } else {
                logEvent("Disabling cue"+view.getId(), callback);
                UtilsTask.toggleCue(cues[view.getId()], false);
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
