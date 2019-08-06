package mymou.task.individual_tasks;

import android.util.Log;
import androidx.fragment.app.Fragment;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;

public class Task extends Fragment {

    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;
    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }

    public void endOfTrial(boolean successfulTrial, double rew_scalar, TaskInterface callback) {
        String outcome;
        PreferencesManager preferencesManager = new PreferencesManager(getContext());
        if (successfulTrial) {
            outcome = preferencesManager.ec_correct_trial;
        } else {
            outcome = preferencesManager.ec_incorrect_trial;
        }
        // Send outcome up to parent
        callback.trialEnded_(outcome, rew_scalar);
    }

}
