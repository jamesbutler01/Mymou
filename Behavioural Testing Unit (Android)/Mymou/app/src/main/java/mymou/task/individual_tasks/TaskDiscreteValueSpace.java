package mymou.task.individual_tasks;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Random;

import mymou.R;
import mymou.Utils.UtilsSystem;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 * Discrete Value Space
 * <p>
 * Subjects are presented with two options, each of which are a  location in a 2D value space (magnitude x probability)
 * Subjects will then receive the magnitude * probability of whichever stimuli they choose and a secondary
 * reinforcer to indicate whether they chose the higher or lower option
 * <p>
 * Stimuli are taken from Brady, T. F., Konkle, T., Alvarez, G. A. and Oliva, A. (2008). Visual
 * long-term memory has a massive storage capacity for object details. Proceedings of the National
 * Academy of Sciences, USA, 105 (38), 14325-14329.
 */
public class TaskDiscreteValueSpace extends Task {

    // Debug
    public static String TAG = "TaskDiscreteValueSpace";


    // Global task variables
    private static ImageButton cue1, cue2;
    private final static int cue1_id = 1, cue2_id = 2;
    private static int cue1_x, cue1_y, cue2_x, cue2_y;
    private static Random r;

    private static int[][] imagelist = {
            {R.drawable.aafaa,
                    R.drawable.aafab,
                    R.drawable.aafac,
                    R.drawable.aafad,
                    R.drawable.aafae,
                    R.drawable.aafaf,
                    R.drawable.aafag,
                    R.drawable.aafah,
                    R.drawable.aafai,
                    R.drawable.aafaj,
            }, {
            R.drawable.aafak,
            R.drawable.aafal,
            R.drawable.aafam,
            R.drawable.aafan,
            R.drawable.aafao,
            R.drawable.aafap,
            R.drawable.aafaq,
            R.drawable.aafar,
            R.drawable.aafas,
            R.drawable.aafat,
    }, {
            R.drawable.aafau,
            R.drawable.aafav,
            R.drawable.aafaw,
            R.drawable.aafax,
            R.drawable.aafay,
            R.drawable.aafaz,
            R.drawable.aafba,
            R.drawable.aafbb,
            R.drawable.aafbc,
            R.drawable.aafbd,
    }, {
            R.drawable.aafbe,
            R.drawable.aafbf,
            R.drawable.aafbg,
            R.drawable.aafbh,
            R.drawable.aafbi,
            R.drawable.aafbj,
            R.drawable.aafbk,
            R.drawable.aafbl,
            R.drawable.aafbm,
            R.drawable.aafbn,
    }, {
            R.drawable.aafbo,
            R.drawable.aafbp,
            R.drawable.aafbq,
            R.drawable.aafbr,
            R.drawable.aafbs,
            R.drawable.aafbt,
            R.drawable.aafbu,
            R.drawable.aafbv,
            R.drawable.aafbw,
            R.drawable.aafbx,
    }, {
            R.drawable.aafby,
            R.drawable.aafbz,
            R.drawable.aafca,
            R.drawable.aafcb,
            R.drawable.aafcc,
            R.drawable.aafcd,
            R.drawable.aafce,
            R.drawable.aafcf,
            R.drawable.aafcg,
            R.drawable.aafch,
    }, {
            R.drawable.aafci,
            R.drawable.aafcj,
            R.drawable.aafck,
            R.drawable.aafcl,
            R.drawable.aafcm,
            R.drawable.aafcn,
            R.drawable.aafco,
            R.drawable.aafcp,
            R.drawable.aafcq,
            R.drawable.aafcr,
    }, {
            R.drawable.aafcs,
            R.drawable.aafct,
            R.drawable.aafcu,
            R.drawable.aafcv,
            R.drawable.aafcw,
            R.drawable.aafcx,
            R.drawable.aafcy,
            R.drawable.aafcz,
            R.drawable.aafda,
            R.drawable.aafdb,
    }, {
            R.drawable.aafdc,
            R.drawable.aafdd,
            R.drawable.aafde,
            R.drawable.aafdf,
            R.drawable.aafdg,
            R.drawable.aafdh,
            R.drawable.aafdi,
            R.drawable.aafdj,
            R.drawable.aafdk,
            R.drawable.aafdl,
    }, {
            R.drawable.aafdm,
            R.drawable.aafdn,
            R.drawable.aafdo,
            R.drawable.aafdp,
            R.drawable.aafdq,
            R.drawable.aafdr,
            R.drawable.aafds,
            R.drawable.aafdt,
            R.drawable.aafdu,
            R.drawable.aafdv,
    }
    };

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
        logEvent(TAG + " started", callback);

        // Instantiate task objects
        assignObjects();

    }

    /**
     * Load objects of the task
     */
    private void assignObjects() {

        // Randomly pick cue locations
        r = new Random();
        cue1_x = r.nextInt(10);
        cue2_x = r.nextInt(10);
        cue1_y = r.nextInt(10);
        cue2_y = r.nextInt(10);

        // Prevent same choice being presented on each side
        if (cue1_x == cue2_x && cue1_y == cue2_y) {
            while (cue1_x == cue2_x && cue1_y == cue2_y) {
                cue1_x = r.nextInt(10);
                cue2_x = r.nextInt(10);
                cue1_y = r.nextInt(10);
                cue2_y = r.nextInt(10);
            }
        }

        // Log the values
        callback.logEvent_("1," + cue1_x + "," + cue1_y + ", cue1 values");
        callback.logEvent_("2," + cue2_x + "," + cue2_y + ", cue2 values");


        // Create buttons
        ConstraintLayout layout = getView().findViewById(R.id.parent_task_from_paper);
        cue1 = UtilsTask.addImageCue(cue1_id, getContext(), layout, buttonClickListener);
        cue2 = UtilsTask.addImageCue(cue2_id, getContext(), layout, buttonClickListener);
        cue1.setImageResource(imagelist[cue1_x][cue1_y]);
        cue2.setImageResource(imagelist[cue2_x][cue2_y]);

        // Place buttons
        if (r.nextBoolean()) {
            callback.logEvent_("3,175,725, cue positions");
            cue1.setX(175);
            cue1.setY(750);
            cue2.setX(725);
            cue2.setY(750);
        } else {
            callback.logEvent_("3,725,175, cue positions");
            cue1.setX(725);
            cue1.setY(750);
            cue2.setX(175);
            cue2.setY(750);
        }
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
            logEvent("4,,," + view.getId() + " button pressed", callback);

            // Figure out if they chose better option decide what to do based on what cue pressed
            boolean successfulTrial = false;
            int chosen_prob = 0;
            float chosen_mag = 0;
            switch (view.getId()) {
                case cue1_id:
                    callback.logEvent_("5,1,, cue 1 pressed");

                    // Store reward values to give
                    chosen_mag = cue1_x;
                    chosen_prob = cue1_y;

                    // See if they chose better option
                    if (cue1_x + cue1_y >= cue2_x + cue2_y) {
                        successfulTrial = true;
                    }

                    // Show them option that they chose for the feedback period
                    UtilsTask.toggleCue(cue1, true);
                    cue1.setClickable(false);
                    break;
                case cue2_id:
                    callback.logEvent_("5,2,, cue 2 pressed");

                    // Store reward values to give
                    chosen_mag = cue2_x;
                    chosen_prob = cue2_y;

                    // See if they chose better option
                    if (cue1_x + cue1_y <= cue2_x + cue2_y) {
                        successfulTrial = true;
                    }

                    // Show them option that they chose for the feedback period
                    UtilsTask.toggleCue(cue1, true);
                    cue1.setClickable(false);

                    break;
            }

            // Determine reward amount to be given
            PreferencesManager preferencesManager = new PreferencesManager(getContext());
            preferencesManager.DiscreteValueSpace();
            float rew_scalar;
            int roll = r.nextInt(10);
            if (roll < chosen_prob) {
                rew_scalar = 0;
            } else {
                rew_scalar = chosen_mag / 10;
            }
            int reward_amount = Math.round(preferencesManager.rewardduration * rew_scalar);
            callback.logEvent_("6," + reward_amount + ",, amount of reward given");

            // Feedback
            if (successfulTrial) {
                getActivity().findViewById(R.id.background_main).setBackgroundColor(preferencesManager.timeoutbackground);
            } else {
                getActivity().findViewById(R.id.background_main).setBackgroundColor(preferencesManager.rewardbackground);
            }
            callback.giveRewardFromTask_(reward_amount);

            // End trial a consistent amount of time after feedback
            // This time is set quite long to encourage learning about the correct choices
            final boolean finalSuccessfulTrial = successfulTrial;
            Handler handlerOne = new Handler();
            handlerOne.postDelayed(new Runnable() {
                @Override
                public void run() {
                    endOfTrial(finalSuccessfulTrial, 0, callback);
                }
            }, preferencesManager.dvs_feedback_duration);


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
