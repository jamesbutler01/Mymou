package mymou.task.individual_tasks;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import mymou.preferences.PreferencesManager;
import mymou.R;
import mymou.Utils.UtilsSystem;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

// A basic object discrimination task showcasing the main features of the Mymou system:

public class TaskExample extends Fragment implements View.OnClickListener {

    // Debug
    public static String TAG = "TaskExample";

     // Identifier for which monkey is currently playing the task
    private static int current_monkey;

    // Task objects
    private static int num_cues = 2;
    private static Button[] cues;  // List of all trial objects for an individual monkey
    private static Button[][] cues_all = {new Button[num_cues], new Button[num_cues]};  // All cues across all monkeys

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_example, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

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

    private void assignObjects() {
        // Monkey 0 cues
        cues_all[0][0] = getView().findViewById(R.id.buttonCue1MonkO);
        cues_all[0][1] = getView().findViewById(R.id.buttonCue2MonkO);

        // Monkey 1's cues
        cues_all[1][0] = getView().findViewById(R.id.buttonCue1MonkV);
        cues_all[1][1] = getView().findViewById(R.id.buttonCue2MonkV);
    }

    @Override
    public void onClick(View view) {

        // Always disable all cues after a press as monkeys love to bash repeatedly
        UtilsTask.toggleCues(cues, false);

         // Reset timer for idle timeout on each press
        callback.resetTimer_();

        // Now decide what to do based on what menu_button pressed
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
        callback.trialEnded_(outcome);
    }

        // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;
    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }


}
