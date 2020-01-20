package mymou.task.individual_tasks;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.preference.PreferenceManager;
import mymou.task.backend.MatrixMaths;
import mymou.preferences.PreferencesManager;
import mymou.R;
import mymou.Utils.UtilsSystem;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 * Object discrimination (color) task
 *
 * Subjects shown specified number of CS+ and CS- cues
 * Must get certain amount of correct CS+ presses in a row to receive reward
 *
 * In this implmentation only monocoloured stimuli are used
 *
 * The number of CS-, CS+, and number needed for reward can all be altered in preferences menu
 *
 */
public class TaskObjectDiscrimCol extends Task {

    // Debug
    public static String TAG = "MyMouObjDiscrim";

    private static int num_steps = 0;
    private static int rew_scalar = 1;
    private static PreferencesManager prefManager;

    // Task objects
    private static Button[] cues;  // List of all trial objects for an individual monkey
    private static int[] random_cols_corr, random_cols_incorr;  // Cues selected for certain trial

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_object_discrim, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.d(TAG, "TaskObjectDiscrimCol started");

        num_steps = 0;

        assignObjects();

        positionAndDisplayCues();

    }

    private void positionAndDisplayCues() {
        Log.d(TAG, "Positining cues around screen");
        UtilsTask.randomlyPositionCues(cues, new UtilsTask().getPossibleCueLocs(getActivity()));
        UtilsTask.toggleCues(cues, true);
    }


    private void assignObjects() {
        prefManager = new PreferencesManager(getContext());
        prefManager.ObjectDiscriminationCol();

        int total_num_cues = prefManager.objectdiscrim_num_corr_shown + prefManager.objectdiscrim_num_incorr_shown;
        int i_cues = 0;
        cues = new Button[total_num_cues];

        // Check to see if we should reload the previous trial's cues
        if (!prefManager.objectdiscrim_repeatOnError | !prefManager.objectdiscrim_previous_error) {
            random_cols_corr = MatrixMaths.randomNoRepeat(prefManager.objectdiscrim_num_corr_shown, prefManager.objectdiscrim_num_corr_options);
            random_cols_incorr = MatrixMaths.randomNoRepeat(prefManager.objectdiscrim_num_incorr_shown, prefManager.objectdiscrim_num_incorr_options);
        } else {
            Log.d(TAG, "Use cue colours from previous trial");
            random_cols_corr = prefManager.objectdiscrim_prev_cols_corr;
            random_cols_incorr = prefManager.objectdiscrim_prev_cols_incorr;
        }

        // Add correct cues
        Log.d(TAG, "Adding correct cues");
        for (int i_corr = 0; i_corr < prefManager.objectdiscrim_num_corr_shown; i_corr++) {
            cues[i_corr] = UtilsTask.addColorCue(i_corr, prefManager.objectdiscrim_corr_colours[random_cols_corr[i_corr]],
                   getContext() , buttonClickListener, getView().findViewById(R.id.parent_object_discrim));
            i_cues += 1;
        }

        // Add distractor cues
        Log.d(TAG, "Adding incorrect cues");
        for (int i_incorr = 0; i_incorr < prefManager.objectdiscrim_num_incorr_shown; i_incorr++) {
            cues[i_cues] = UtilsTask.addColorCue(i_cues, prefManager.objectdiscrim_incorr_colours[random_cols_incorr[i_incorr]],
                    getContext(), buttonClickListener, getView().findViewById(R.id.parent_object_discrim));
            i_cues += 1;
        }

    }

    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;

    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick");

            // Always disable all cues after a press as monkeys love to bash repeatedly
            UtilsTask.toggleCues(cues, false);

            // Reset timer for idle timeout on each press
            callback.resetTimer_();

            // Now decide what to do based on what menu_button pressed
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
    };

    // Save outcome of this trial and the cues used so that it can be repeated if it was unsuccessful
    private void saveTrialParams(boolean successfulTrial) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(getString(R.string.preftag_od_previous_error), successfulTrial);
        editor.putString(getString(R.string.preftag_od_prev_cols_corr), UtilsSystem.convertIntArrayToString(random_cols_corr));
        editor.putString(getString(R.string.preftag_od_prev_cols_incorr), UtilsSystem.convertIntArrayToString(random_cols_incorr));
        editor.commit();
    }

    private void endOfTrial(boolean successfulTrial) {
        Log.d(TAG, "endOfTrial");

        saveTrialParams(successfulTrial);

        String outcome;
        if (successfulTrial) {
            outcome = prefManager.ec_correct_trial;
        } else {
            outcome = prefManager.ec_incorrect_trial;
        }
        // Send outcome up to parent
        callback.trialEnded_(outcome, rew_scalar);

    }

}
