package mymou.task.individual_tasks;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import mymou.R;
import mymou.Utils.UtilsSystem;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 *
 * Task Example
 *
 * A basic object discrimination task showcasing the main features of the Mymou system
 * Displays two cues on the screen, one of which leads to a reward and one of which leads to a
 * timeout
 * Actually uses 4 cues in total, 2 for 'monkey_o' and two for 'monkey_v'. To acheive this it uses
 * facial recognition to load different cues on a trial-by-trial basis depending on which monkey
 * started the trial.
 * This results in 'monkey_o' learning to differentiate between different cues compared to 'monkey_v'
 *
 */
public class TaskExample extends Task implements View.OnClickListener {

    // Debug
    public static String TAG = "TaskExample";

    // Identifier for which monkey is currently playing the task
    private static int current_monkey, monkey_o =0, monkey_v =1;

    // Scalar if you want to increase the reward on a particular trial (by multiplication)
    private static int rew_scalar = 1;

    // Global task variables
    private static int num_cues = 2;  // The number of cues to be presented on each trial
    private static Button[] cues;  // List of all trial objects for an individual monkey
    private static Button[][] cues_all = {new Button[num_cues], new Button[num_cues]};  // All cues across all monkeys

    private static PreferencesManager preferencesManager;

    /**
     * Function called when task first loaded (before the UI is loaded)
     * Loads the UI components (cues, background etc)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_example, container, false);
    }

    /**
     *
     * Function called after the UI has been loaded
     * Once this is called you can then make any UI changes you want (moving cues around etc)
     *
     */
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent(TAG+" started", callback);

        // Instantiate task objects
        assignObjects();

        // Find which monkey is playing (faceRecog result)
        current_monkey = getArguments().getInt("current_monkey");

        // Load cues for specific monkey, disable cues for other monkeys
        UtilsTask.toggleMonkeyCues(current_monkey, cues_all);
        cues = cues_all[current_monkey];

        // Activate cues
        UtilsSystem.setOnClickListenerLoop(cues, this);

        // Randomise cue locations
        UtilsTask.randomlyPositionCues(cues,  new UtilsTask().getPossibleCueLocs(getActivity()));
    }

    /**
     * Load objects of the task
     * In this case, it loads the different cues that have been specified for each monkey
     */
    private void assignObjects() {
        // Load preferences
        preferencesManager = new PreferencesManager(getContext());

        // Monkey 0 cues
        cues_all[monkey_o][0] = getView().findViewById(R.id.buttonCue1MonkO);
        cues_all[monkey_o][1] = getView().findViewById(R.id.buttonCue2MonkO);

        // Monkey 1's cues
        cues_all[monkey_v][0] = getView().findViewById(R.id.buttonCue1MonkV);
        cues_all[monkey_v][1] = getView().findViewById(R.id.buttonCue2MonkV);
    }

    /**
     * Called whenever a cue is pressed by a subject
     * So then loads the next appropriate stage of the task depending on what cue was selected
     */
    @Override
    public void onClick(View view) {

        // Always disable all cues after a press as monkeys love to cheat
        UtilsTask.toggleCues(cues, false);

         // Reset timer for idle timeout on each press
        callback.resetTimer_();

        // Log screen press
        logEvent(""+view.getId()+" button pressed", callback);

        // Now decide what to do based on what cue pressed
        boolean successfulTrial = false;
        switch (view.getId()) {
            // If they pressed the correct cue, then set the bool to true
            case R.id.buttonCue1MonkO:
                successfulTrial = true;
                break;
            case R.id.buttonCue2MonkV:
                successfulTrial = true;
                break;
        }

        // Tell parent (TrialManager.java) the outcome of the trial, which will then respond accordingly
        // (e.g. give reward if correct, set up for next trial etc)

        endOfTrial(successfulTrial, rew_scalar, callback, preferencesManager);
    }

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
