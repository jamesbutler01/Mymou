package com.example.jbutler.mymou;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Arrays;

// A basic object discrimination task showcasing the main features of the Mymou system:

public class TaskObjectDiscrim extends Fragment implements View.OnClickListener {

    // Debug
    public static String TAG = "MyMouTaskExample";

    private static int current_monkey = -1;
    private static int num_steps = 0;
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
        num_steps=0;
        assignObjects();

        positionAndDisplayCues();

    }

    private void positionAndDisplayCues() {
        UtilsTask.randomlyPositionCues(cues,  new UtilsTask().getPossibleCueLocs(getActivity()));
        UtilsTask.toggleCues(cues, true);
    }

    private void pickCueColours() {

    }

    private void assignObjects() {
        prefManager = new PreferencesManager(getContext());
        prefManager.ObjectDiscrimination();

        int total_num_cues = prefManager.objectdiscrim_num_corr_shown + prefManager.objectdiscrim_num_incorr_shown;
        int i_cues = 0;
        cues = new Button[total_num_cues];

        // Add correct cues
        int[] random_cols_corr = MatrixMaths.randomNoRepeat(prefManager.objectdiscrim_num_corr_shown, prefManager.objectdiscrim_num_corr);
        for (int i_corr = 0; i_corr < prefManager.objectdiscrim_num_corr_shown; i_corr++) {
            cues[i_corr] = UtilsTask.addCue(i_corr, prefManager.objectdiscrim_corr_colours[random_cols_corr[i_corr]],
                    getContext(), this, getView().findViewById(R.id.parent_object_discrim));
            i_cues += 1;
        }

        // Add distractor cues
        int[] random_cols_incorr = MatrixMaths.randomNoRepeat(prefManager.objectdiscrim_num_corr_shown, prefManager.objectdiscrim_num_corr);
        for (int i_incorr = 0; i_incorr < prefManager.objectdiscrim_num_incorr_shown; i_incorr++) {
            cues[i_cues] = UtilsTask.addCue(i_cues, prefManager.objectdiscrim_incorr_colours[random_cols_incorr[i_incorr]],
                    getContext(), this, getView().findViewById(R.id.parent_object_discrim));
            i_cues += 1;
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
