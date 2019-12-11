package mymou.task.individual_tasks;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.Random;

import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 * Task Sequential Learning
 * <p>
 * Must click on cue to progress through a sequence of images
 * On second lap of the sequence, at a random point a choice will be given to see if subjects can
 * correctly guess next image in sequence
 */
public class TaskSequentialLearning extends Task {

    // Debug
    public static String TAG = "TaskSequentialLearning";

    private static PreferencesManager prefManager;

    // Task objects
    private int[] seq_one = {
            R.drawable.slaaa,
            R.drawable.slaab,
            R.drawable.slaac,
            R.drawable.slaad,
            R.drawable.slaae,
            R.drawable.slaaf,
            R.drawable.slaag,
            R.drawable.slaah,
            R.drawable.slaai,
            R.drawable.slaaj,
    };
    private int[] seq_two = {
            R.drawable.slaba,
            R.drawable.slabb,
            R.drawable.slabc,
            R.drawable.slabd,
            R.drawable.slabe,
            R.drawable.slabf,
            R.drawable.slabg,
            R.drawable.slabh,
            R.drawable.slabi,
            R.drawable.slabj,
    };
    private static int[] cue_images;  // The images to be used in the task
    private static ImageButton cue_forced_choice, cue_choice_corr, cue_choice_incorr;  // The 3 buttons in the task
    private final static int id_forced_choice = 0, id_corr_choice = 1, id_incorr_choice = 2;  // ID's of the cues of the 3 buttons in the task
    private static int i_sequence, num_laps;  // Current position in sequence, and number of laps round sequence (choice is given on second lap)
    private static int corr_choice, incorr_choice;  // Where the choice phase will be
    private Float x_range, y_range;  // Limits of the screen (x and y coordinates) where images are to be placed
    private Random r;  // Random number generator

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_empty, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent(TAG + " started", callback);

        assignObjects();

        forced_choice_step();

    }

    private void forced_choice_step() {
        logEvent("Enabling forced choice cue (position " + i_sequence + ")", callback);
        cue_forced_choice.setImageResource(cue_images[i_sequence]);
        randomlyPositionCue(cue_forced_choice);
    }

    private void choice_step() {
        logEvent("Enabling choice cues at position (" + i_sequence + ")", callback);
        UtilsTask.toggleCue(cue_forced_choice, false);
        UtilsTask.toggleCue(cue_choice_incorr, true);
        UtilsTask.toggleCue(cue_choice_corr, true);
    }

    private void assignObjects() {
        // Load preferences
        prefManager = new PreferencesManager(getContext());
        prefManager.SequentialLearning();

        // Load the images to be used for the cues
        logEvent("Map loaded: " + prefManager.sl_map_selected, callback);
        cue_images = new int[prefManager.sl_seq_length];
        for (int i = 0; i < prefManager.sl_seq_length; i++) {
            int image_name = prefManager.sl_map_selected == 0 ? seq_one[i] : seq_two[i];
            cue_images[i] = image_name;
        }

        // Reset sequence
        i_sequence = 0;
        num_laps = 0;

        // Random number generator
        r = new Random();

        // Pick where the choice phase will be
        // This is anywhere from position 2 until the second from end of the sequence
        corr_choice = r.nextInt(prefManager.sl_seq_length - 1) + 1;
        incorr_choice = corr_choice;
        while (incorr_choice == corr_choice) {
            incorr_choice = r.nextInt(prefManager.sl_seq_length);
        }
        logEvent("Choice point: " + corr_choice, callback);
        logEvent("Incorrect choice stimulus: " + incorr_choice, callback);

        // Create cues
        cue_forced_choice = UtilsTask.addImageCue(id_forced_choice, getContext(), getView().findViewById(R.id.parent_task_empty), buttonClickListener);
        cue_choice_corr = UtilsTask.addImageCue(id_corr_choice, getContext(), getView().findViewById(R.id.parent_task_empty), buttonClickListener);
        cue_choice_corr.setImageResource(cue_images[corr_choice]);
        cue_choice_incorr = UtilsTask.addImageCue(id_incorr_choice, getContext(), getView().findViewById(R.id.parent_task_empty), buttonClickListener);
        cue_choice_incorr.setImageResource(cue_images[incorr_choice]);

        randomlyPositionChoiceCues(cue_choice_corr, cue_choice_incorr);

        // Switch off choice cues which come at end
        UtilsTask.toggleCue(cue_choice_corr, false);
        UtilsTask.toggleCue(cue_choice_incorr, false);

        // Locations for go cue_images
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        display.getSize(screen_size);
        x_range = (float) (screen_size.x - prefManager.cue_size);
        y_range = (float) (screen_size.y - prefManager.cue_size);

    }

    private void next_step() {
        // Increment our position in the sequence
        i_sequence += 1;
        if (i_sequence == prefManager.sl_seq_length) {
            i_sequence = 0;
            num_laps += 1;
        }

        // Figure out if its forced choice or choice stage next
        if (num_laps == 1 && i_sequence == corr_choice) {
            choice_step();
        } else {
            forced_choice_step();
        }

    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // Reset timer for idle timeout on each press
            callback.resetTimer_();

            switch (view.getId()) {
                case id_forced_choice:
                    logEvent("Forced choice button pressed", callback);
                    next_step();
                    break;
                case id_corr_choice:
                    logEvent("Correct choice button pressed", callback);
                    endOfTrial(true, callback);
                    break;
                case id_incorr_choice:
                    logEvent("Incorrect choice button pressed", callback);
                    endOfTrial(false, callback);
                    break;
            }
        }
    };

    private void randomlyPositionCue(ImageButton cue) {
        int x_loc = (int) (r.nextFloat() * x_range);
        int y_loc = (int) (r.nextFloat() * y_range);

        cue.setX(x_loc);
        cue.setY(y_loc);

    }

    private void randomlyPositionChoiceCues(ImageButton choice1, ImageButton choice2) {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        display.getSize(screen_size);

        // Hardcoded as for some reason using Point's didn't work
        if (r.nextBoolean()) {
            if (r.nextBoolean()) {
                logEvent("Choice locations set to combinatin #0", callback);
                choice1.setX(175);
                choice1.setY(1200);
                choice2.setX(725);
                choice2.setY(300);
            } else {
                logEvent("Choice locations set to combinatin #1", callback);
                choice1.setX(725);
                choice1.setY(300);
                choice2.setX(175);
                choice2.setY(1200);
            }
        } else {
            if (r.nextBoolean()) {
                logEvent("Choice locations set to combinatin #2", callback);
                choice1.setX(725);
                choice1.setY(1200);
                choice2.setX(175);
                choice2.setY(300);
            } else {
                logEvent("Choice locations set to combinatin #3", callback);
                choice1.setX(175);
                choice1.setY(300);
                choice2.setX(725);
                choice2.setY(1200);
            }
        }

    }


    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;

    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }

}
