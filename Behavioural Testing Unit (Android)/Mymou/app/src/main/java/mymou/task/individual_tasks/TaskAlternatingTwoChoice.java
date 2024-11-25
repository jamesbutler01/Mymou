package mymou.task.individual_tasks;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Calendar;
import java.util.Random;

import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
    Show two cues, one of which is rewarded, each trial.
    Randomise cue position each time.
 */
public class TaskAlternatingTwoChoice extends Task {

    // Debug
    public static String TAG = "TaskAlternatingTwoChoice";

    // Global task variables
    PreferencesManager preferencesManager;
    private static ImageButton cue1, cue2;
    private final static int cue1_id = 1, cue2_id = 2;
    private final static int rewarded_cue = cue1_id;
    private final static int cue_1_image = R.drawable.aaaaa;
    private final static int int cue_2_image = R.drawable.aaaab;
    private static Random r;


    /**
     * Function called when task first loaded (before the UI is loaded)
     * Loads the UI components (cues, background etc)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_empty, container, false);
    }

    /**
     * Function called after the UI has been loaded
     * Once this is called you can then make any UI changes you want (moving cues around etc)
     */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent("0,,,"+TAG + " started", callback);

        // Instantiate task objects
        preferencesManager = new PreferencesManager(getContext());
        preferencesManager.DiscreteValueSpace();

        assignObjects();

    }

    /**
     * Load objects of the task
     */
    private void assignObjects() {

        // Determine choices
        r = new Random();

        // Create buttons
        ConstraintLayout layout = getView().findViewById(R.id.parent_task_empty);
        cue1 = UtilsTask.addImageCue(cue1_id, getContext(), layout, buttonClickListener);
        cue2 = UtilsTask.addImageCue(cue2_id, getContext(), layout, buttonClickListener);
        cue1.setImageResource(cue_1_image);
        cue2.setImageResource(cue_2_image);

        // Place buttons
        ImageButton[] cues = {cue1, cue2};
        UtilsTask.randomlyPositionCues(cues, getActivity());

        callback.logEvent_("1," + cue1.getX() + "," + cue1.getY() + ", cue1 position");
        callback.logEvent_("2," + cue2.getX() + "," + cue2.getY() + ", cue2 position");

    }

    /**
     * Called whenever a cue is pressed by a subject
     */
    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Reset timer for idle timeout on each press
            callback.resetTimer_();

            // Always disable all cues after a press as monkeys love to cheat
            UtilsTask.toggleCue(cue1, false);
            UtilsTask.toggleCue(cue2, false);

            // Reset timer for idle timeout on each press
            callback.resetTimer_();

            // Log screen press
            logEvent("3,"+view.getId()+",,cue " + view.getId() + " pressed", callback);

            // Figure out if they chose better option decide what to do based on what cue pressed
            switch (view.getId()) {
                case cue1_id:
                    // Show them option that they chose for the feedback period
                    UtilsTask.toggleCue(cue1, true);
                    cue1.setClickable(false);
                    break;
                case cue2_id:
                    // Show them option that they chose for the feedback period
                    UtilsTask.toggleCue(cue2, true);
                    cue2.setClickable(false);
                    break;
            }

            // Determine reward amount to be given
            int reward_amount = Math.round(preferencesManager.rewardduration);
            callback.logEvent_("4," + reward_amount + ",, amount of reward chosen");

            // Feedback
            if (view.getId()==rewarded_cue) {
                getActivity().findViewById(R.id.background_main).setBackgroundColor(preferencesManager.rewardbackground);
                callback.giveRewardFromTask_(reward_amount, true);
                callback.logEvent_("5,True,, correct trial");
            } else {
                getActivity().findViewById(R.id.background_main).setBackgroundColor(preferencesManager.timeoutbackground);
                callback.logEvent_("5,False,, incorrect trial");

            }

            // End trial a consistent amount of time after feedback
            final boolean finalSuccessfulTrial = view.getId()==rewarded_cue;
            Handler handlerOne = new Handler();
            handlerOne.postDelayed(new Runnable() {
                @Override
                public void run() {
                    endOfTrial(finalSuccessfulTrial, callback, preferencesManager);
                }
            }, 1000);

        }
    };

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
