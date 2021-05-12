package mymou.task.individual_tasks;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Random;

import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 * Training task five: Two step task
 *
 * Implementation of Thomas Akam's reduced two-step task (Akam et al. 2015)
 *
 * Stimuli are taken from Brady, T. F., Konkle, T., Alvarez, G. A. and Oliva, A. (2008). Visual
 *  long-term memory has a massive storage capacity for object details. Proceedings of the National
 *  Academy of Sciences, USA, 105 (38), 14325-14329.
 */
public class TaskWalds extends Task {

    // Debug
    public static String TAG = "TaskWalds";

    // Task objects
    private static PreferencesManager prefManager;
    private ImageButton cue1, cue2;
    private Button gocue;
    private ImageButton[] probcues;
    private int[] probcueinds;
    private final static int id_cue1 = 0, id_cue2 = 1;
    private static int num_cues, rewardthresh, numsteps=0;
    private Float x_range, y_range;
    private Random r;
    private static Handler h0 = new Handler();  // Task trial_timer
    private static Handler h1 = new Handler();  // Inter-trial interval timer
    private static Handler h2 = new Handler();  // Dim brightness timer

    // Images to be used as probability cues
    int[] allprobcues = {
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

    double[] allprobweights = {-1, -0.9, -0.7, -0.5, -0.3, 0.3, 0.5, 0.7, 0.9, 1};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_empty, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent(TAG+" started", callback);

        assignObjects();

        nextStep(0);

    }

    private void nextStep(int numsteps) {

        int randomduration;
        if (numsteps==0) {
            // Find variable duration of prob cues showing
            randomduration = r.nextInt(prefManager.w_probcuesdelay_high - prefManager.w_probcuesdelay_low) + prefManager.w_probcuesdelay_low;
        } else {
            randomduration = prefManager.w_startdelay;
        }
        callback.logEvent_("Showing step "+numsteps+" after "+randomduration);

        h1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (numsteps > num_cues){
                    UtilsTask.toggleCue(gocue, false);
                    cue1.setClickable(true);
                    cue2.setClickable(true);

                } else {
                    UtilsTask.toggleView(probcues[numsteps], true);
                    callback.logEvent_("Showing step "+numsteps);
                    nextStep(numsteps + 1);
                }

            }
        }, randomduration);

    }

    private void assignObjects() {
        // Load preferences
        prefManager = new PreferencesManager(getContext());
        prefManager.Walds();

        // Random number generator
        r = new Random();

        // Locations for choice cues
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        display.getSize(screen_size);
        x_range = (float) (screen_size.x - prefManager.cue_size);
        y_range = (float) (screen_size.y - prefManager.cue_size);

        ConstraintLayout layout = getView().findViewById(R.id.parent_task_empty);

        // Create go cue
        gocue = UtilsTask.addColorCue(-1,  0, getContext(), null, layout, 1);
        gocue.setWidth(prefManager.w_gocuesize);
        gocue.setHeight(prefManager.w_gocuesize);
        float halfx = (float) (screen_size.x - prefManager.w_gocuesize) / 2;
        gocue.setX(halfx);
        float halfy = (float) (screen_size.y - prefManager.w_gocuesize) / 2;
        gocue.setY(halfy);

        // Create choice 1 and choice 2 cues
        cue1 = UtilsTask.addImageCue(id_cue1, getContext(), layout, buttonClickListener);
        cue1.setImageResource(R.drawable.tstc11);
        int x_loc = (int) (r.nextFloat() * x_range);
        cue1.setX(x_loc);
        cue1.setY(1550);
        cue1.setClickable(false);
        cue2 = UtilsTask.addImageCue(id_cue2, getContext(), layout, buttonClickListener);
        cue2.setImageResource(R.drawable.tstc12);
        x_loc = (int) (r.nextFloat() * x_range);
        cue2.setX(x_loc);
        cue2.setY(100);
        cue2.setClickable(false);

        // TODO: Implement later stages
        num_cues = 4;
        probcues = new ImageButton[num_cues];
        probcueinds = new int[num_cues];
        int summedcueval = 0;
        int[] chosenpositions = {0, 0, 0, 0};
        int[] xpositions = {prefManager.w_probcuexloc1, prefManager.w_probcuexloc2, prefManager.w_probcuexloc1, prefManager.w_probcuexloc2};
        int[] ypositions = {prefManager.w_probcueyloc1, prefManager.w_probcueyloc1, prefManager.w_probcueyloc2, prefManager.w_probcueyloc2};

        int pos = -1;
        for (int i=0; i<num_cues; i++) {
            probcues[i] = UtilsTask.addImageCue(num_cues+2, getContext(), layout, buttonClickListener);

            // Randomly pick cue value
            probcueinds[i] = r.nextInt(allprobcues.length);
            summedcueval += allprobweights[probcueinds[i]];
            probcues[i].setImageResource(allprobcues[probcueinds[i]]);
            callback.logEvent_("Prob. cue "+i+" set to "+probcueinds[i]+" (weight="+ allprobweights[probcueinds[i]]+")");
            callback.logEvent_("Current summed weight = "+summedcueval);

            // Randomly position cue
            pos = r.nextInt(chosenpositions.length);
            while (chosenpositions[pos]==1) {
                pos = r.nextInt(chosenpositions.length);
            }
            chosenpositions[pos] = 1;
            probcues[i].setX(xpositions[pos]);
            probcues[i].setY(ypositions[pos]);
            callback.logEvent_("Prob. cue "+i+" set to position"+pos+"(x="+xpositions[pos]+", y="+ypositions[pos]+")");

            UtilsTask.toggleCue(probcues[i], false);
        }

        double thresh = Math.pow(10, summedcueval);
        double rewardthreshd = thresh / (1 + thresh);
        rewardthreshd = rewardthreshd * 100;
        rewardthresh = (int) rewardthreshd;

        callback.logEvent_("Reward probability of A set to "+rewardthresh);

        numsteps = 0;
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            callback.logEvent_("Cue clicked, id = " + view.getId());

            // Always disable cues first
            disableCues();

            // Make sure screen bright
            callback.setBrightnessFromTask_(true);

            // Cancel timers
            h0.removeCallbacksAndMessages(null);
            h1.removeCallbacksAndMessages(null);
            h2.removeCallbacksAndMessages(null);

            // Reset timer for idle timeout on each press
            callback.resetTimer_();

            switch (view.getId()) {
                // If pressed B then flip the probability
                case id_cue2:
                    rewardthresh = 100 - rewardthresh;
                    callback.logEvent_("Reward probability of B set to "+rewardthresh);
                    break;
            }

            int roll = r.nextInt(100);
            boolean correct = false;
            if (roll > rewardthresh) {
                correct = true;
            }
            callback.logEvent_("Rolled "+roll+" so correct trial = "+correct);
            endOfTrial(correct, callback, prefManager);

        }
    };


    private void disableCues() {
        UtilsTask.toggleCue(cue1, false);
        UtilsTask.toggleCue(cue2, false);
        UtilsTask.toggleCues(probcues, false);
    }

    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;
    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }

    @Override
    public void onPause() {
        super.onPause();
        super.onDestroy();
        h0.removeCallbacksAndMessages(null);
        h1.removeCallbacksAndMessages(null);
        h2.removeCallbacksAndMessages(null);
    }


}
