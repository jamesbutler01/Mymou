package com.example.jbutler.mymou;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

// A basic object discrimination task showcasing the main features of the Mymou system:

public class TaskObjectDiscrim extends Fragment implements View.OnClickListener {

    // Debug
    public static String TAG = "TaskExample";

    // Identifier for which monkey is currently playing the task
    private static int current_monkey;
    private static int num_steps;
    private static PreferencesManager preferencesManager;

    // Task objects
    private static Button[] cues;  // List of all trial objects for an individual monkey

    // Predetermined locations where cues can appear on screen, calculated by UtilsTask.calculateCueLocations()
    private static Point[] possible_cue_locs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_object_discrim, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        // Instantiate task objects
        assignObjects();
        UtilsTask.randomlyPositionCues(cues,  new UtilsTask().getPossibleCueLocs(getActivity()));

    }

    private void assignObjects() {
        preferencesManager = new PreferencesManager(getContext());
        preferencesManager.ObjectDiscrimination();

        int total_num_cues = preferencesManager.objectdiscrim_num_corr_shown + preferencesManager.objectdiscrim_num_incorr_shown;
        int i_buttons = 0;
        cues = new Button[total_num_cues];

        Log.d("asdf", "id:"+preferencesManager.objectdiscrim_num_corr_shown+" col:"+preferencesManager.objectdiscrim_num_incorr_shown);

        // Add correct cues
        for (int i_correct = 0; i_correct < preferencesManager.objectdiscrim_num_corr_shown; i_correct++) {
            addButton(i_correct, preferencesManager.objectdiscrim_corr_colours[i_correct]);
            i_buttons += 1;
        }
        for (int i_incorrect = 0; i_incorrect < preferencesManager.objectdiscrim_num_incorr_shown; i_incorrect++) {
            addButton(i_buttons, preferencesManager.objectdiscrim_incorr_colours[i_incorrect]);
            i_buttons += 1;
        }

        possible_cue_locs = new UtilsTask().getPossibleCueLocs(getActivity());
    }

    private void addButton(int id, int color) {
        Log.d("asdf", "id:"+id+" col:"+color);
        int[] colors = this.getResources().getIntArray(R.array.colorarray);
        LinearLayout layout = getView().findViewById(R.id.container_object_discrim);
        Button button = new Button(getContext());
        button.setWidth(175);
        button.setHeight(175);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setStroke(5, Color.MAGENTA);
        drawable.setColor(color);
        button.setBackgroundDrawable(drawable);
        button.setId(id);
        //button.setBackgroundColor(color);
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

        if (Integer.valueOf(view.getId()) < preferencesManager.objectdiscrim_num_corr_shown) {
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
