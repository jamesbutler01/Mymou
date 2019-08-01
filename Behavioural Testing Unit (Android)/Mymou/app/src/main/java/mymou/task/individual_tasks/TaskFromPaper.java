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

public class TaskFromPaper extends Fragment {

    private String TAG = "TaskFromPaper";

    private int min_starting_distance = 3;  // Inclusive
    private int max_starting_distance = 6; // Inclusive
    private static Context mContext;
    private int numDistractors = 3;
    private boolean prev_trial_correct;
    private int prev_target, prev_start;

    TextView textView;
    int[] imageList;
    int[] imageListMapTwo = {
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
            R.drawable.aabap,};

    int[] imageListMapOne = {
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

    int[][] transitionMatrix;
    private int x_size, y_size, numNeighbours;
    private boolean torus;
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

        // Decide on map
        imageList = imageListMapOne;
        x_size = 1;
        y_size = 10;
        torus = false;
        numNeighbours = 2;

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
        pb1.setProgress((pbLength - currentDistanceFromTarget) * pbScalar);
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

        imageButtons = new ImageButton[numNeighbours];
        for (int i = 0; i < numNeighbours; i++) {
            imageButtons[i] = UtilsTask.addImageCue(i, getContext(), buttonClickListener, layout, true);
        }
        neighbours = new int[numNeighbours];
        transitionMatrix = MatrixMaths.generateTransitionMatrix(y_size, y_size, torus);
        numStimulus = imageList.length;


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
    int targetPos;
    int numStimulus;
    int pbScalar = 1000;
    int pbLength;
    int currentDistanceFromTarget;
    int numSteps = 0;
    int startPos;

        // Save outcome of this trial and the cues used so that it can be repeated if it was unsuccessful
    private void saveTrialParams(boolean successfulTrial) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("frompaper_previous_error", successfulTrial);
        editor.putInt("frompaper_prev_target", targetPos);
        editor.putInt("frompaper_prev_start", startPos);
        editor.commit();
    }

    // Load previous trial params
    private void loadTrialParams() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        prev_trial_correct = settings.getBoolean("frompaper_previous_error", true);
        prev_target = settings.getInt("frompaper_prev_target", -1);
        prev_start = settings.getInt("frompaper_prev_start", -1);
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
            numSteps++;
            int previousDistance = currentDistanceFromTarget;
            updateCurrLoc(chosenOne);
            animateStep(chosenOne);
            fadeButtons(chosenOne);
            currentDistanceFromTarget = distanceFromTarget(currentPos);
            if(previousDistance > currentDistanceFromTarget) { // If right direction
                updateProgressBar(animationDuration + 400);
                if (currentPos == targetPos) {
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
                numSteps + "," + result + "," + currentDistanceFromTarget + "," +
                targetPos + "," + currentPos + "," + startPos + "," + min_starting_distance;
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

        for (int i = 0; i < numNeighbours; i++) {
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
        for (int i = 0; i < numNeighbours; i++) {
            if (i != chosenOne) {
                imageButtons[i].animate().alpha(0).setDuration(animationDuration);
            }
        }

        //Quickly switch chosenLoc (which is now over currLoc) with currLoc
        h2.postDelayed(new Runnable() {
            @Override
            public void run() {
                ibCurrLoc.setImageResource(imageList[currentPos]);
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
                for (int i = 0; i < numNeighbours; i++) {
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
        for (int i = 0; i < numStimulus; i++) {
            if (transitionMatrix[currentPos][i] == 1) {
                // Is it not in correct direction
                if (transitionMatrix[i][targetPos] >= transitionMatrix[currentPos][targetPos]) {
                    distractorCount += 1;
                }

                // Only add if it's in correct direction or a distracter fewer than specified amount
                if (transitionMatrix[i][targetPos] < transitionMatrix[currentPos][targetPos] | distractorCount < numDistractors+1) {
                    neighbours[j] = i;
                    imageButtons[j].setEnabled(true);
                    imageButtons[j].setVisibility(View.VISIBLE);
                    imageButtons[j].setImageResource(imageList[i]);
                    j++;
                }
            }
        }

        //If on edge of maze set remaining neighbours to inactive
        while(j < numNeighbours) {
            neighbours[j] = -1;
            imageButtons[j].setEnabled(false);
            imageButtons[j].setVisibility(View.INVISIBLE);
            j++;
        }

    }


    private void switchOffChoiceButtons() {
        for(int i = 0; i < numNeighbours; i++) {
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
            randomiseCurrentPos();
            while (distanceFromTarget(currentPos) < min_starting_distance | distanceFromTarget(currentPos) > max_starting_distance) {
                randomiseCurrentPos();
            }

        } else {

            currentPos = prev_start;
            currentDistanceFromTarget = distanceFromTarget(currentPos);
            ibCurrLoc.setImageResource(imageList[currentPos]);
        }

        startPos = currentPos;
        rew_scalar = 1 + ((distanceFromTarget(currentPos) - min_starting_distance) * 0.5);  // Scale reward by difficulty
        updateChoiceButtons();
    }

    private void randomiseCurrentPos() {
        currentPos = r.nextInt(numStimulus);
        currentDistanceFromTarget = distanceFromTarget(currentPos);
        ibCurrLoc.setImageResource(imageList[currentPos]);
    }

    private void resetButtonPositionsAndBorders() {
        for (int i = 0; i < numNeighbours; i++) {
            imageButtons[i].animate().alpha(0).setDuration(1);
        }
        ibCurrLoc.setBackground(ContextCompat.getDrawable(mContext, R.drawable.outline));
        ibTarget.setBackground(ContextCompat.getDrawable(mContext, R.drawable.outline));
        ibCurrLoc.setY(yCenter);
        ibCurrLoc.setX(xCenter);
    }

    private void chooseTargetLoc() {
        if(prev_trial_correct) {
            targetPos = r.nextInt(numStimulus);
        } else {
            targetPos = prev_target;
        }
        ibTarget.setImageResource(imageList[targetPos]);
    }

    private void setMaxProgress() {
        pbLength = max_starting_distance + 1;
        pb1.setMax(pbLength * pbScalar);
        pb1.setProgress(pbScalar);
    }

    private int distanceFromTarget(int currentPos) {
        return transitionMatrix[currentPos][targetPos];
    }

    private void randomiseImageLocation() {
        int[] chosen = {0, 0, 0, 0, 0, 0, 0, 0,};
        int bound = 2;
        int choice = r.nextInt(bound);
        for (int i = 0; i < numNeighbours; i++) {
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
        for(int i = 0; i < numNeighbours; i++) {
            imageButtons[i].setClickable(status);
        }
    }


    private void updateProgressBar(int delay) {
        h6.postDelayed(new Runnable() {
            @Override
            public void run() {
                ProgressBarAnimation anim = new ProgressBarAnimation(pb1, pb1.getProgress(), (pbLength - currentDistanceFromTarget) * pbScalar);
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
