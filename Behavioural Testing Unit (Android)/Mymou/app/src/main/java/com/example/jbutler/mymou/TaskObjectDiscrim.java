package com.example.jbutler.mymou;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

// A basic object discrimination task showcasing the main features of the Mymou system:

public class TaskObjectDiscrim extends Fragment implements View.OnClickListener {

    // Debug
    public static String TAG = "TaskExample";

    private static int current_monkey;
    private static int num_steps;
    private static PreferencesManager prefManager;

    // Task objects
    private static Button[] cues;  // List of all trial objects for an individual monkey

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_object_discrim, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.d(TAG, "TaskObjectDiscrim started");

        assignObjects();

        positionAndDisplayCues();

    }

    private void positionAndDisplayCues() {
        UtilsTask.randomlyPositionCues(cues,  new UtilsTask().getPossibleCueLocs(getActivity()));
        UtilsTask.toggleCues(cues, true);
    }

    private void assignObjects() {
        prefManager = new PreferencesManager(getContext());
        prefManager.ObjectDiscrimination();

        int total_num_cues = prefManager.objectdiscrim_num_corr_shown + prefManager.objectdiscrim_num_incorr_shown;
        int i_buttons = 0;
        cues = new Button[total_num_cues];

        // Add correct cues
        for (int i_correct = 0; i_correct < prefManager.objectdiscrim_num_corr_shown; i_correct++) {
            cues[i_correct] = UtilsTask.addCue(i_correct, prefManager.objectdiscrim_corr_colours[i_correct], getContext(), this, getView().findViewById(R.id.parent_object_discrim));
            i_buttons += 1;
        }

        // Add distractor cues
        for (int i_incorrect = 0; i_incorrect < prefManager.objectdiscrim_num_incorr_shown; i_incorrect++) {
            cues[i_buttons] = UtilsTask.addCue(i_buttons, prefManager.objectdiscrim_incorr_colours[i_incorrect], getContext(), this, getView().findViewById(R.id.parent_object_discrim));
            i_buttons += 1;
        }

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
        if (Integer.valueOf(view.getId()) < prefManager.objectdiscrim_num_corr_shown) {
            successfulTrial = true;
            num_steps += 1;
        }

        // Check how many correct presses they've got and how many they need per trial
        if (!successfulTrial | num_steps == prefManager.objectdiscrim_num_steps) {
            endOfTrial(successfulTrial);
        } else {
            positionAndDisplayCues();
        }
    }

    private void endOfTrial(boolean successfulTrial) {
        String outcome;
        if (successfulTrial) {
            outcome = prefManager.ec_correct_trial;
        } else {
            outcome = prefManager.ec_incorrect_trial;
        }
        // Send outcome up to parent
        ((TaskManager) getActivity()).trialEnded(outcome);

    }

}
