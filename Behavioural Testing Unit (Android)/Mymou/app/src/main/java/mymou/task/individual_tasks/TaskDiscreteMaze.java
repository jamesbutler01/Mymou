package mymou.task.individual_tasks;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import mymou.task.backend.MatrixMaths;
import mymou.preferences.PreferencesManager;
import mymou.Utils.ProgressBarAnimation;
import mymou.R;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

import java.util.Random;

// The task used in Butler & Kennerley (2018)
// Used to teach a 4x4 discrete world

public class TaskDiscreteMaze extends Task {

    private String TAG = "TaskDiscreteMaze";

    private PreferencesManager preferencesManager;
    private TaskDiscreteMazeMapParams mapParams;
    private static Context mContext;
    private int numDistractors = 3;
    private boolean prev_trial_correct;
    private int prev_target, prev_start;

    TextView textView;

    int[][] transitionMatrix;
    private int yCenter, xCenter, distanceFromCenter;
    private static double rew_scalar = 1;

    private int[] neighbours;
    private int[] xLocs = new int[8];
    private int[] yLocs = new int[8];
    private Random r = new Random();
    private ImageButton ibWait, ibTarget, ibCurrLoc;
    private ImageButton[] imageButtons;
    private ProgressBar pb1;

    Handler h0 = new Handler();
    Handler h1 = new Handler();
    Handler h2 = new Handler();
    Handler h3 = new Handler();
    Handler h4 = new Handler();
    Handler h5 = new Handler();
    Handler h6 = new Handler();
    Handler h7 = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_from_paper, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.d(TAG, "Task started..");
        // Load settings
        preferencesManager = new PreferencesManager(getContext());
        preferencesManager.DiscreteMaze();

        mapParams = new TaskDiscreteMazeMapParams(preferencesManager);

        assignObjects();
        loadTrialParams();

        calculateButtonLocations();

        chooseTargetLoc();
        setStartingPosition();
        setStartingPosition();
        setStartingPosition();
        setStartingPosition();
        setStartingPosition();

        setMaxProgress();

        resetButtonPositionsAndBorders();
        pb1.setProgress((pb_length - currentDistanceFromTarget) * pb_scalar);
        randomiseImageLocation();

        switchOffChoiceButtons();

        //Disable unused buttons
        ibWait.setEnabled(false);
        ibWait.setVisibility(View.INVISIBLE);

        // Log starting parameters
        logStep(5);

        // Finally make task visual
        ibTarget.setEnabled(true);
        ibTarget.setVisibility(View.VISIBLE);
        unfadeButtons(0);

    }

    private void assignObjects() {
        mContext = getActivity().getApplicationContext();
        ibWait = (ImageButton) getView().findViewById(R.id.imageButtonWaitCue);
        pb1 = (ProgressBar) getView().findViewById(R.id.boosterBar);

        ConstraintLayout layout = getView().findViewById(R.id.parent_task_from_paper);
        ibTarget = UtilsTask.addImageCue(-1, getContext(), null, layout, true);
        ibCurrLoc = UtilsTask.addImageCue(-1, getContext(), null, layout, true);

        imageButtons = new ImageButton[mapParams.numNeighbours];
        for (int i = 0; i < mapParams.numNeighbours; i++) {
            imageButtons[i] = UtilsTask.addImageCue(i, getContext(), buttonClickListener, layout, true);
        }
        neighbours = new int[mapParams.numNeighbours];
        transitionMatrix = MatrixMaths.generateTransitionMatrix(mapParams.y_size, mapParams.y_size, mapParams.torus);
        num_stimulus = mapParams.imageList.length;


        textView = (TextView)getView().findViewById(R.id.textView2);
        textView.setVisibility(View.INVISIBLE);
    }

    private void calculateButtonLocations() {
        int imageWidths = 350;
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        xCenter = screenWidth / 2;
        xCenter -= imageWidths / 2;
        distanceFromCenter = imageWidths + 30;
        // Y locations
        yCenter = 1200;
        yLocs[0] = yCenter;
        yLocs[1] = yCenter;
        yLocs[2] = yCenter - distanceFromCenter;
        yLocs[3] = yCenter + distanceFromCenter;
        yLocs[4] = yCenter + distanceFromCenter;
        yLocs[5] = yCenter - distanceFromCenter;
        yLocs[6] = yCenter + distanceFromCenter;
        yLocs[7] = yCenter - distanceFromCenter;
        ibTarget.setY(425);
        ibWait.setY(yCenter + 100 - 2*distanceFromCenter);

        // X locations
        xLocs[0] = xCenter - distanceFromCenter;
        xLocs[1] = xCenter + distanceFromCenter;
        xLocs[2] = xCenter;
        xLocs[3] = xCenter;
        xLocs[4] = xCenter - distanceFromCenter;
        xLocs[5] = xCenter - distanceFromCenter;
        xLocs[6] = xCenter + distanceFromCenter;
        xLocs[7] = xCenter + distanceFromCenter;

        //Centre
        ibCurrLoc.setY(yCenter);
        ibCurrLoc.setX(xCenter);
        ibTarget.setX(xCenter);
        ibWait.setX(screenWidth / 2 - 50 - distanceFromCenter);
    }

    int currentPos = -1;
    int previousPos = -1;
    int choiceDelay = 400;
    boolean choicePeriod = false;
    int animationDuration = 450;
    int[] chosenXlocs = {-1, -1, -1, -1};
    int[] chosenYlocs = {-1, -1, -1, -1};
    int target_pos;
    int num_stimulus;
    int pb_scalar = 1000;
    int pb_length;
    int currentDistanceFromTarget;
    int num_steps = 0;
    int start_pos, start_dist;

        // Save outcome of this trial and the cues used so that it can be repeated if it was unsuccessful
    private void saveTrialParams(boolean successfulTrial) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("dm_previous_error", successfulTrial);
        editor.putInt("dm_prev_target", target_pos);
        editor.putInt("dm_prev_start", start_pos);
        editor.commit();
    }

    // Load previous trial params
    private void loadTrialParams() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        prev_trial_correct = settings.getBoolean("dm_previous_error", true);
        prev_target = settings.getInt("dm_prev_target", -1);
        prev_start = settings.getInt("dm_prev_start", -1);
    }



    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Reset timer for idle timeout on each press
            callback.resetTimer_();

            moveForwards(Integer.valueOf(view.getId()));
        }
    };

    private void moveForwards(final int chosenOne) {
        textView.setText("Feedback");

        if (choicePeriod) {
            choicePeriod = false;
            num_steps++;
            int previousDistance = currentDistanceFromTarget;
            updateCurrLoc(chosenOne);
            animateStep(chosenOne);
            fadeButtons(chosenOne);
            currentDistanceFromTarget = distanceFromTarget(currentPos);
            if(previousDistance > currentDistanceFromTarget) { // If right direction
                updateProgressBar(animationDuration + 400);
                if (currentPos == target_pos) {
                    //Reached target
                    arrivedAtTarget(chosenOne);
                } else {
                    //Right direction
                    logStep(3);
                    unfadeButtons(animationDuration*2 + 400 + 50);
                }
            } else {
                //Wrong direction
                logStep(0);
                updateProgressBar(animationDuration + 400);
                arrivedAtWrongTarget();
            }
        }
    }

    private void logStep(int result) {
        String msg =
                num_steps + "," + result + "," + currentDistanceFromTarget + "," +
                        target_pos + "," + currentPos + "," + start_pos + "," + start_dist;
         callback.logEvent_(msg);
    }

    private void arrivedAtWrongTarget() {
        //Highlight the correct two
        textView.setText("Feedback");
        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                ibCurrLoc.setVisibility(View.VISIBLE);
                ibCurrLoc.setEnabled(true);
                ibCurrLoc.setBackground(ContextCompat.getDrawable(mContext, R.drawable.outline_thick));
                ibTarget.setBackground(ContextCompat.getDrawable(mContext, R.drawable.outline_thick));
                endOfTrial(false);
            }
        }, (animationDuration));
    }

    private void arrivedAtTarget(int chosenOne) {
        //Fade out buttons that weren't chosen
        textView.setText("Feedback");

        for (int i = 0; i < mapParams.numNeighbours; i++) {
            if (i != chosenOne) {
                imageButtons[i].animate().alpha(0).setDuration(animationDuration);
            }
        }
        //Highlight the correct two
        h1.postDelayed(new Runnable() {
            @Override
            public void run() {
                ibCurrLoc.setVisibility(View.VISIBLE);
                ibCurrLoc.setEnabled(true);
                ibCurrLoc.setBackground(ContextCompat.getDrawable(mContext, R.drawable.double_border));
                ibTarget.setBackground(ContextCompat.getDrawable(mContext, R.drawable.double_border));
                endOfTrial(true);
            }
        }, (animationDuration*2 + 400));

    }


    private void animateStep(final int direction) {
        //Moves chosen image to centre loc
        Path path = new Path();
        path.moveTo(chosenXlocs[direction], chosenYlocs[direction]);
        path.lineTo(xCenter, yCenter);
        ObjectAnimator mover = ObjectAnimator.ofFloat(imageButtons[direction], "x", "y", path);
        mover.setDuration(animationDuration);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(mover);
        animatorSet.start();
        h7.postDelayed(new Runnable() {
            @Override
            public void run() {
                ibCurrLoc.setBackground(ContextCompat.getDrawable(mContext, R.drawable.outline_thick));
                ibTarget.setBackground(ContextCompat.getDrawable(mContext, R.drawable.outline_thick));
            }
        }, animationDuration);
    }

    private void fadeButtons(final int chosenOne) {
        //Fade out currLoc as newLoc moves into currLoc pos
        ibCurrLoc.animate().alpha(0).setDuration(animationDuration);
        for (int i = 0; i < mapParams.numNeighbours; i++) {
            if (i != chosenOne) {
                imageButtons[i].animate().alpha(0).setDuration(animationDuration);
            }
        }

        //Quickly switch chosenLoc (which is now over currLoc) with currLoc
        h2.postDelayed(new Runnable() {
            @Override
            public void run() {
                ibCurrLoc.setImageResource(mapParams.imageList[currentPos]);
                ibCurrLoc.animate().alpha(1).setDuration(1);
                ibCurrLoc.setEnabled(true);
                ibCurrLoc.setVisibility(View.VISIBLE);
                imageButtons[chosenOne].animate().alpha(0).setDuration(1);
                setChoiceButtonsClickable(false);
            }
        }, animationDuration);
    }

    private void unfadeButtons(final int delay) {
        h3.postDelayed(new Runnable() {
            @Override
            public void run() {
                resetButtonPositionsAndBorders();
                updateChoiceButtons();
                randomiseImageLocation();
                toggleDelayCue(true);
                setChoiceButtonsClickable(true);
                for (int i = 0; i < mapParams.numNeighbours; i++) {
                    imageButtons[i].animate().alpha(1).setDuration(animationDuration);
                }
            }
        }, delay);

        h4.postDelayed(new Runnable() {
            @Override
            public void run() {
                toggleDelayCue(false);
                textView.setText("Choice");
            }
        }, delay + animationDuration + choiceDelay);
    }


    private void toggleDelayCue(boolean toggle) {
        if (toggle) {
            ibWait.setEnabled(true);
            ibWait.setVisibility(View.VISIBLE);
            choicePeriod = false;
        } else {
            ibWait.setEnabled(false);
            ibWait.setVisibility(View.INVISIBLE);
            choicePeriod = true;
        }
    }


    private void updateCurrLoc(final int chosenOne) {
        previousPos = currentPos;
        currentPos = neighbours[chosenOne];
    }

    private void updateChoiceButtons() {
        //Find neighbouring images
        int distractorCount = 0;
        int j = 0;
        for (int i = 0; i < num_stimulus; i++) {
            if (transitionMatrix[currentPos][i] == 1) {
                // Is it not in correct direction
                if (transitionMatrix[i][target_pos] >= transitionMatrix[currentPos][target_pos]) {
                    distractorCount += 1;
                }

                // Only add if it's in correct direction or a distracter fewer than specified amount
                if (transitionMatrix[i][target_pos] < transitionMatrix[currentPos][target_pos] | distractorCount < numDistractors+1) {
                    neighbours[j] = i;
                    imageButtons[j].setEnabled(true);
                    imageButtons[j].setVisibility(View.VISIBLE);
                    imageButtons[j].setImageResource(mapParams.imageList[i]);
                    j++;
                }
            }
        }

        //If on edge of maze set remaining neighbours to inactive
        while(j < mapParams.numNeighbours) {
            neighbours[j] = -1;
            imageButtons[j].setEnabled(false);
            imageButtons[j].setVisibility(View.INVISIBLE);
            j++;
        }

    }


    private void switchOffChoiceButtons() {
        for(int i = 0; i < mapParams.numNeighbours; i++) {
            toggleButton(imageButtons[i], false);
        }
    }

    private void toggleButton(ImageButton ib, boolean status) {
        if (status) {
            ib.setVisibility(View.VISIBLE);
        } else {
            ib.setVisibility(View.INVISIBLE);
        }
        ib.setEnabled(status);
        ib.setClickable(status);
    }

    private void setStartingPosition() {
        if (prev_trial_correct) {
            // Randomly pick locations until we find one a correct distance away
            randomiseCurrentPos();
            while (distanceFromTarget(currentPos) < preferencesManager.dm_min_start_distance | distanceFromTarget(currentPos) > preferencesManager.dm_max_start_distance) {
                randomiseCurrentPos();
            }

        } else {

            currentPos = prev_start;
            currentDistanceFromTarget = distanceFromTarget(currentPos);
            ibCurrLoc.setImageResource(mapParams.imageList[currentPos]);
        }
        start_dist = currentDistanceFromTarget;
        start_pos = currentPos;
        rew_scalar = 1 + ((distanceFromTarget(currentPos) - preferencesManager.dm_min_start_distance) * 0.5);  // Scale reward by difficulty
        updateChoiceButtons();
    }

    private void randomiseCurrentPos() {
        currentPos = r.nextInt(num_stimulus);
        currentDistanceFromTarget = distanceFromTarget(currentPos);
        ibCurrLoc.setImageResource(mapParams.imageList[currentPos]);
    }

    private void resetButtonPositionsAndBorders() {
        for (int i = 0; i < mapParams.numNeighbours; i++) {
            imageButtons[i].animate().alpha(0).setDuration(1);
        }
        ibCurrLoc.setBackground(ContextCompat.getDrawable(mContext, R.drawable.outline));
        ibTarget.setBackground(ContextCompat.getDrawable(mContext, R.drawable.outline));
        ibCurrLoc.setY(yCenter);
        ibCurrLoc.setX(xCenter);
    }

    private void chooseTargetLoc() {
        if(prev_trial_correct) {
            target_pos = r.nextInt(num_stimulus);
        } else {
            target_pos = prev_target;
        }
        ibTarget.setImageResource(mapParams.imageList[target_pos]);
    }

    private void setMaxProgress() {
        pb_length = preferencesManager.dm_max_start_distance + 1;
        pb1.setMax(pb_length * pb_scalar);
        pb1.setProgress(pb_scalar);
    }

    private int distanceFromTarget(int currentPos) {
        return transitionMatrix[currentPos][target_pos];
    }

    private void randomiseImageLocation() {
        int[] chosen = {0, 0, 0, 0, 0, 0, 0, 0,};
        int bound = 2;
        int choice = r.nextInt(bound);
        for (int i = 0; i < mapParams.numNeighbours; i++) {
            while (chosen[choice] == 1) {
                choice = r.nextInt(bound);
            }
            // choice = i;
            imageButtons[i].setX(xLocs[choice]);
            imageButtons[i].setY(yLocs[choice]);
            chosenXlocs[i] = xLocs[choice];
            chosenYlocs[i] = yLocs[choice];
            chosen[choice] = 1;
        }
    }

    private void setChoiceButtonsClickable(boolean status) {
        for(int i = 0; i < mapParams.numNeighbours; i++) {
            imageButtons[i].setClickable(status);
        }
    }


    private void updateProgressBar(int delay) {
        h6.postDelayed(new Runnable() {
            @Override
            public void run() {
                ProgressBarAnimation anim = new ProgressBarAnimation(pb1, pb1.getProgress(), (pb_length - currentDistanceFromTarget) * pb_scalar);
                anim.setDuration(animationDuration);
                pb1.startAnimation(anim);
            }
        }, delay);
    }

    private void endOfTrial(boolean outcome) {

        saveTrialParams(outcome);

        String ec;
        if (outcome) {
            ec = new PreferencesManager(getContext()).ec_correct_trial;
        } else {
            ec = new PreferencesManager(getContext()).ec_incorrect_trial;
        }
       h5.postDelayed(new Runnable() {
            @Override
            public void run() {
                        callback.trialEnded_(ec, rew_scalar);
            }
        }, 1000);
    }


    private void cancelHandlers() {
        h0.removeCallbacksAndMessages(null);
        h1.removeCallbacksAndMessages(null);
        h2.removeCallbacksAndMessages(null);
        h3.removeCallbacksAndMessages(null);
        h4.removeCallbacksAndMessages(null);
        h5.removeCallbacksAndMessages(null);
        h6.removeCallbacksAndMessages(null);
        h7.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelHandlers();
    }

    // Implement interface and listener to enable communication up to TaskManager
    TaskInterface callback;
    public void setFragInterfaceListener(TaskInterface callback) {
        this.callback = callback;
    }


}
