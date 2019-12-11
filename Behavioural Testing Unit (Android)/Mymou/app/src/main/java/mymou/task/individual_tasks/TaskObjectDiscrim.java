package mymou.task.individual_tasks;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Random;

import mymou.R;
import mymou.Utils.UtilsSystem;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 * Task Object Discrimination (stimulus)
 * <p>
 * Subjects shown a stimuli, and then must choose them from up to 3 distractors
 * <p>
 * Stimuli are taken from Brady, T. F., Konkle, T., Alvarez, G. A. and Oliva, A. (2008). Visual
 * long-term memory has a massive storage capacity for object details. Proceedings of the National
 * Academy of Sciences, USA, 105 (38), 14325-14329.
 * <p>
 * TODO: Implement logging of task variables
 */
public class TaskObjectDiscrim extends Task {

    // Debug
    public static String TAG = "TaskObjectDiscrim";

    private static ImageButton[] cues, choice_cues;
    private static int chosen_cue_id;
    private static ConstraintLayout layout;
    private static PreferencesManager prefManager;
    private static Handler h0 = new Handler();  // Show object
    private static Handler h1 = new Handler();  // Hide object
    private static Handler h2 = new Handler();  // Show choices

    // The stimuli
    private static int[] stims = {
            R.drawable.aabaa,
            R.drawable.aabab,
            R.drawable.aabac,
            R.drawable.aabad,
            R.drawable.aabae,
            R.drawable.aabaf,
            R.drawable.aabag,
            R.drawable.aabah,
            R.drawable.aabai,
            R.drawable.aabaj,
            R.drawable.aabak,
            R.drawable.aabal,
            R.drawable.aabam,
            R.drawable.aaban,
            R.drawable.aabao,
            R.drawable.aabap,
            R.drawable.aaaaa,
            R.drawable.aaaab,
            R.drawable.aaaac,
            R.drawable.aaaad,
            R.drawable.aaaae,
            R.drawable.aaaaf,
            R.drawable.aaaag,
            R.drawable.aaaah,
            R.drawable.aaaai,
            R.drawable.aaaaj,
    };
    private static int num_stimuli = stims.length;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_empty, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent(TAG+" started", callback);

        assignObjects();

        startMovie(prefManager.od_num_stim);
    }


    private void startMovie(int num_steps) {
        Log.d(TAG, "Playing movie, frame: " + num_steps + "/" + prefManager.od_num_stim);
        if (num_steps > 0) {
            h0.postDelayed(new Runnable() {
                @Override
                public void run() {
                    UtilsTask.toggleCues(cues, true);
                }
            }, prefManager.od_start_delay);

            h1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    UtilsTask.toggleCues(cues, false);

                }
            }, prefManager.od_start_delay + prefManager.od_duration_on);

            h2.postDelayed(new Runnable() {
                @Override
                public void run() {

                    startMovie(num_steps - 1);

                }
            }, prefManager.od_start_delay + prefManager.od_duration_on + prefManager.od_duration_off);

        } else {

            // Choice phase
            Random r = new Random();

            // First make array of chosen positions
            int num_dirs = 4;
            boolean[] chosen_pos_bool = UtilsSystem.getBooleanFalseArray(num_dirs);

            choice_cues = new ImageButton[prefManager.od_num_stim + prefManager.od_num_distractors];
            // Add correct answer
            for (int i = 0; i < cues.length; i++) {
                choice_cues[i] = UtilsTask.addImageCue(chosen_cue_id, getContext(), layout, buttonClickListener);
                choice_cues[i].setImageResource(stims[chosen_cue_id]);
                int chosen_dir = r.nextInt(num_dirs);
                positionObject(chosen_dir, choice_cues[i]);
                chosen_pos_bool[chosen_dir] = true;
            }

            // Now add distractors (without replacement)

            // Array to track chosen stimuli
            boolean[] chosen_cues_bool = UtilsSystem.getBooleanFalseArray(num_stimuli);
            chosen_cues_bool[chosen_cue_id] = true;

            // For each distractor
            for (int i = 0; i < prefManager.od_num_distractors; i++) {
                // Choose stimuli
                int chosen_cue = UtilsTask.chooseValueNoReplacement(chosen_cues_bool);
                chosen_cues_bool[chosen_cue] = true;

                // Add cue to the UI
                choice_cues[i + prefManager.od_num_stim] = UtilsTask.addImageCue(-1, getContext(), layout, buttonClickListener);
                choice_cues[i + prefManager.od_num_stim].setImageResource(stims[chosen_cue]);

                // choose position of cue
                int chosen_pos = UtilsTask.chooseValueNoReplacement(chosen_pos_bool);
                chosen_pos_bool[chosen_pos] = true;
                positionObject(chosen_pos, choice_cues[i + prefManager.od_num_stim]);
            }

        }
    }

    private void positionObject(int pos, ImageButton cue) {
        switch (pos) {
            case 0:
                cue.setX(175);
                cue.setY(1200);
                break;
                case 1:
                cue.setX(725);
                cue.setY(300);
                break;
                case 2:
                cue.setX(725);
                cue.setY(1200);
                break;
                case 3:
                cue.setX(175);
                cue.setY(300);
                break;
        }
    }


    private void assignObjects() {
        prefManager = new PreferencesManager(getContext());
        prefManager.ObjectDiscrim();

        layout = getView().findViewById(R.id.parent_task_empty);

        // Choose cues (without replacement)
        cues = new ImageButton[prefManager.od_num_stim];
        Random r = new Random();
        chosen_cue_id = r.nextInt(num_stimuli);
        cues[0] = UtilsTask.addImageCue(chosen_cue_id, getContext(), layout);
        cues[0].setImageResource(stims[chosen_cue_id]);

        // Position cue in centre
        cues[0].setX(450);
        cues[0].setY(750);

        UtilsTask.toggleCues(cues, false);

    }


    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick "+view.getId());

            // Did they select the appropriate cue
            boolean correct_chosen = Integer.valueOf(view.getId()) == chosen_cue_id;

            endOfTrial(correct_chosen, callback);

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

    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;
    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }

}
