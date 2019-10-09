/**
 * Object discrimination task
 * <p>
 * Subjects shown specified number of CS+ and CS- cues
 * Must get certain amount of correct CS+ presses in a row to receive reward
 * <p>
 * The number of CS-, CS+, and number needed for reward can all be altered in preferences menu
 */

package mymou.task.individual_tasks;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.preference.PreferenceManager;

import java.util.Random;

import mymou.R;
import mymou.Utils.UtilsSystem;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.MatrixMaths;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

// An evidence accumulation task where the height of bars must be integrated to receive reward
// TODO: Implement logging of task variables

public class TaskEvidenceAccum extends Task {

    // Debug
    public static String TAG = "MyMouEvidenceAccum";

    private static int pb_scalar = 100;
    private static int total1, total2;
    private static PreferencesManager prefManager;
    private static ProgressBar[] progressBars = new ProgressBar[2];
    private static int[] amounts1, amounts2;
    private static Handler h0 = new Handler();  // Show object
    private static Handler h1 = new Handler();  // Hide object


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_evidence_accum, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.d(TAG, "Task started");

        assignObjects();

        startMovie(prefManager.ea_num_steps);

    }

    private void startMovie(int num_steps) {
        if (num_steps > 0) {
            h0.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Setting bars to " + amounts1[num_steps - 1] + " and " + amounts2[num_steps - 1]);
                    progressBars[0].setVisibility(View.VISIBLE);
                    progressBars[1].setVisibility(View.VISIBLE);
                    progressBars[0].setProgress(amounts1[num_steps - 1]);
                    progressBars[1].setProgress(amounts2[num_steps - 1]);

                    total1 += amounts1[num_steps - 1];
                    total2 += amounts2[num_steps - 1];
                    Log.d(TAG, "totals: " + total1 + "," + total2);

                }
            }, prefManager.ea_step_duration_off);

            h1.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBars[0].setVisibility(View.INVISIBLE);
                    progressBars[1].setVisibility(View.INVISIBLE);

                    startMovie(num_steps - 1);

                }
            }, prefManager.ea_step_duration_on + prefManager.ea_step_duration_off);

        } else {

            // Choice phase
            getView().findViewById(R.id.ea_butt_1).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.ea_butt_2).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.ea_butt_1).setOnClickListener(buttonClickListener);
            getView().findViewById(R.id.ea_butt_2).setOnClickListener(buttonClickListener);


        }
    }


    private void assignObjects() {
        prefManager = new PreferencesManager(getContext());
        prefManager.EvidenceAccum();

        getView().findViewById(R.id.ea_butt_1).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.ea_butt_2).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.ea_bar_1).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.ea_bar_2).setVisibility(View.INVISIBLE);

        progressBars[0] = getView().findViewById(R.id.ea_bar_1);
        progressBars[1] = getView().findViewById(R.id.ea_bar_2);

        total1 = 0;
        total2 = 0;

        // Calculate amounts
        Random r = new Random();

        // Find means
        int range = 6 - prefManager.ea_distance;
        int start1 = 3 + r.nextInt(range);
        int start2 = start1 + prefManager.ea_distance;

        // Swap the two bars half the time
        if (r.nextBoolean()) {
            int s = start1;
            start1 = start2;
            start2 = s;
        }

        amounts1 = new int[prefManager.ea_num_steps];
        amounts2 = new int[prefManager.ea_num_steps];
        for (int i = 0; i < prefManager.ea_num_steps; i++) {
            amounts1[i] = (int) getValue(r, start1);
            amounts2[i] = (int) getValue(r, start2);
        }

    }

    private double getValue(Random r, int start) {
        double a = ((r.nextGaussian() * prefManager.ea_variance) + start) * pb_scalar;
        if (a > 1000) {
            a = 1000;
        }
        if (a < 0) {
            a = 0;
        }
        Log.d(TAG, "Returning value: " + a);
        return a;
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
            Log.d(TAG, "totals: " + total1 + "," + total2);

            boolean correct_chosen = false;
            switch (view.getId()) {
                case R.id.ea_butt_1:
                    correct_chosen = total1 > total2;
                    break;
                case R.id.ea_butt_2:
                    correct_chosen = total2 > total1;
                    break;
            }

            endOfTrial(correct_chosen, callback);

        }
    };

    @Override
    public void onPause() {
        super.onPause();
        super.onDestroy();
        h0.removeCallbacksAndMessages(null);
        h1.removeCallbacksAndMessages(null);
    }

}
