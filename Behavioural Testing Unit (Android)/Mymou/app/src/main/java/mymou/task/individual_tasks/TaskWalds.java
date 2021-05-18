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
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Random;

import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 * Task: Sequential Probability Ratio Test
 *
 * Implementation of Kira S, Yang T, Shadlen MN. 2015. A Neural Implementation of Wald’s
 * Sequential Probability Ratio Test. Neuron 85:861–873.
 *
 * Briefly, two options are on the screen, and cues appear sequentially informing you of the value
 * of each of the options. The value cues must be integrated to make the appropriate action
 *
 */
public class TaskWalds extends Task {

    // Debug
    public static String TAG = "TaskWalds";

    // Task objects
    private static PreferencesManager prefManager;
    private Button gocue, cue1, cue2;
    private ImageButton[] probcues;
    private int[] probcueinds;
    private final static int id_cue1 = 0, id_cue2 = 1;
    private static int rewardthresh, numsteps=0;
    private Float x_range, y_range;
    private Random r;
    private static Handler h0 = new Handler();  // Task trial_timer
    private static Handler h1 = new Handler();  // Inter-trial interval timer
    private static Handler h2 = new Handler();  // Dim brightness timer

    // Images to be used as probability cues
    int[] allprobcues = {
            R.drawable.w_circle,
            R.drawable.w_triangle,
            R.drawable.w_pie,
            R.drawable.w_star,
            R.drawable.w_lettert,
            R.drawable.w_vase,
            R.drawable.w_pieslice,
            R.drawable.w_hourglass,
            R.drawable.w_hexagon,
            R.drawable.w_rec,
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
                if (numsteps >= prefManager.w_numcues){
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
        callback.logEvent_("Setting go cue size to "+prefManager.w_gocuesize);
        float halfx = (float) (screen_size.x - prefManager.w_gocuesize) / 2;
        gocue.setX(halfx);
        float halfy = (float) (screen_size.y - prefManager.w_gocuesize) / 2;
        gocue.setY(halfy);
        gocue.setClickable(false);

        // Create choice 1 and choice 2 cues
        cue1 = UtilsTask.addColorCue(id_cue1, R.color.yellow, getContext(), buttonClickListener, layout, 1, false);

        cue1.setX(prefManager.w_choicecuex1);

        cue1.setY(prefManager.w_choicecuey1);
        cue1.setClickable(false);
        cue1.setWidth(prefManager.w_choicecuesize);
        cue1.setHeight(prefManager.w_choicecuesize);
        cue2 = UtilsTask.addColorCue(id_cue2, R.color.blue, getContext(), buttonClickListener, layout, 1, false);

        cue2.setX(prefManager.w_choicecuex2);

        cue2.setY(prefManager.w_choicecuey2);
        cue2.setWidth(prefManager.w_choicecuesize);
        cue2.setHeight(prefManager.w_choicecuesize);
        cue2.setClickable(false);

        // Randomly position x location of choice cues
        if (prefManager.w_randposchoicecues) {
            int x_loc = (int) (r.nextFloat() * x_range);
            cue1.setX(x_loc);
            x_loc = (int) (r.nextFloat() * x_range);
            cue2.setX(x_loc);
        }

        // TODO: Implement later stages
        probcues = new ImageButton[prefManager.w_numcues];
        probcueinds = new int[prefManager.w_numcues];
        int summedcueval = 0;
        int[] chosenpositions = {0, 0, 0, 0};
        int[] xpositions = {prefManager.w_probcuexloc1, prefManager.w_probcuexloc2, prefManager.w_probcuexloc1, prefManager.w_probcuexloc2};
        int[] ypositions = {prefManager.w_probcueyloc1, prefManager.w_probcueyloc1, prefManager.w_probcueyloc2, prefManager.w_probcueyloc2};

        int pos = -1;
        for (int i=0; i<prefManager.w_numcues; i++) {
            probcues[i] = UtilsTask.addImageCue(-1, getContext(), layout, buttonClickListener, prefManager.w_rewcuesize, 0);

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
