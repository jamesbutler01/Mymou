package com.example.jbutler.mymou;

import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

// A basic object discrimination task showcasing the main features of the Mymou system:

public class TaskObjectDiscrimination extends Fragment implements View.OnClickListener {

    // Debug
    public static String TAG = "TaskExample";

    // Identifier for which monkey is currently playing the task
    private static int current_monkey;

    // Task settings
    private static int num_correct_cues = 5;
    private static int num_correct_cues_shown = 2;
    private static int num_incorrect_cues = 5;
    private static int num_incorrect_cues_shown = 2;
    private static int total_num_cues = num_correct_cues_shown + num_incorrect_cues_shown;
    private static int num_steps_needed = 2;
    private static boolean repeat_on_error = true;

    // Task objects
    private static Button[] cues;  // List of all trial objects for an individual monkey

    // Predetermined locations where cues can appear on screen, calculated by UtilsTask.calculateCueLocations()
    private static Point[] possible_cue_locs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_object_discrimination, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        // Instantiate task objects
        assignObjects();
        UtilsTask.randomlyPositionCues(cues,  new UtilsTask().getPossibleCueLocs(getActivity()));

    }

    private void assignObjects() {
        int i_buttons = 0;
        cues = new Button[total_num_cues];
        // Add correct cues
        for (int i_correct = 0; i_correct < num_correct_cues_shown; i_correct++) {
            addButton(i_correct, i_correct);
            i_buttons += 1;
        }
        for (int i_incorrect = 0; i_incorrect < num_incorrect_cues_shown; i_incorrect++) {
            addButton(i_buttons, i_incorrect);
            i_buttons += 1;
        }

        possible_cue_locs = new UtilsTask().getPossibleCueLocs(getActivity());
    }

    private void addButton(int id, int color) {
        int[] colors = this.getResources().getIntArray(R.array.colorarray);
        LinearLayout layout = getView().findViewById(R.id.container_object_discrim);
        Button button = new Button(getContext());
        button.setWidth(175);
        button.setHeight(175);
        button.setId(id);
        button.setBackgroundColor(colors[color]);
        button.setOnClickListener(this);
        layout.addView(button);
        cues[id] = button;
    }

    @Override
    public void onClick(View view) {

        // Always disable all cues after a press as monkeys love to bash repeatedly
        UtilsTask.toggleCues(cues, false);

         // Reset timer for idle timeout on each press
         ((TaskManager) getActivity()).resetTimer();

        // Now decide what to do based on what button pressed
        // The id of correct cues come first so this is how we determine if it's a correct cue or not
        boolean successfulTrial = false;

        if (Integer.valueOf(view.getId()) < num_correct_cues_shown) {
            successfulTrial = true;
        }
        endOfTrial(successfulTrial);
    }

    private void endOfTrial(boolean successfulTrial) {
        String outcome;
        PreferencesManager preferencesManager = new PreferencesManager(getContext());
        if (successfulTrial) {
            outcome = preferencesManager.ec_correct_trial;
        } else {
            outcome = preferencesManager.ec_incorrect_trial;
        }
        // Send outcome up to parent
        ((TaskManager) getActivity()).trialEnded(outcome);
    }

}
