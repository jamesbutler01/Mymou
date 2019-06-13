package mymou.task.individual_tasks;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.content.ContextCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import mymou.task.backend.MatrixMaths;
import mymou.preferences.PreferencesManager;
import mymou.Utils.ProgressBarAnimation;
import mymou.R;
import mymou.task.backend.TaskInterface;

import java.util.Random;

// The task used in Butler & Kennerley (2018)
// Used to teach a 4x4 discrete world

public class TaskFromPaper extends Fragment
        implements View.OnClickListener {

    private int pathDistance = 1;
    private static Context mContext;
    private int numDistractors = 3;

    TextView textView;

    int[] imageList = {
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

    int[][] transitionMatrix;
    private int size = 4;
    private int numNeighbours = 4;
    private int yCenter, xCenter, distanceFromCenter;
    private int[] neighbours = new int[numNeighbours];
    private int[] xLocs = new int[8];
    private int[] yLocs = new int[8];
    private Random r = new Random();
    private ImageButton bgGreen, ibWait, ibTarget, ibCurrLoc;
    private ImageButton pos0, pos1, pos2, pos3, pos4, pos5, pos6, pos7;
    private ImageButton[] imageButtons = new ImageButton[numNeighbours];
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

        assignObjects();

        setOnClickListeners();

        calculateButtonLocations();

        setPosLocations();

        numStimulus = imageList.length;

        chooseTargetLoc();
        setStartingPosition();
        setMaxProgress();

        resetButtonPositionsAndBorders();
        pb1.setProgress((pbLength - currentDistanceFromTarget) * pbScalar);
        randomiseImageLocation();

        switchOffChoiceButtons();

        //Disable unused buttons
        bgGreen.setEnabled(false);
        bgGreen.setVisibility(View.INVISIBLE);
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
        bgGreen = (ImageButton) getView().findViewById(R.id.imageButtonGreen);
        ibWait = (ImageButton) getView().findViewById(R.id.imageButtonWaitCue);
        ibTarget = (ImageButton) getView().findViewById(R.id.imageButtonTarget);
        ibCurrLoc = (ImageButton) getView().findViewById(R.id.imageButtonCurrLoc);
        pb1 = (ProgressBar) getView().findViewById(R.id.boosterBar);
        imageButtons[0] = (ImageButton) getView().findViewById(R.id.imageButton0);
        imageButtons[1] = (ImageButton) getView().findViewById(R.id.imageButton1);
        imageButtons[2] = (ImageButton) getView().findViewById(R.id.imageButton2);
        imageButtons[3] = (ImageButton) getView().findViewById(R.id.imageButton3);
        pos0 = (ImageButton) getView().findViewById(R.id.posZero);
        pos1 = (ImageButton) getView().findViewById(R.id.posOne);
        pos2 = (ImageButton) getView().findViewById(R.id.posTwo);
        pos3 = (ImageButton) getView().findViewById(R.id.posThree);
        pos4 = (ImageButton) getView().findViewById(R.id.posFour);
        pos5 = (ImageButton) getView().findViewById(R.id.posFive);
        pos6 = (ImageButton) getView().findViewById(R.id.posSix);
        pos7 = (ImageButton) getView().findViewById(R.id.posSeven);
        textView = (TextView)getView().findViewById(R.id.textView2);
        transitionMatrix = MatrixMaths.generateTransitionMatrix(size, size, false);
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
        yLocs[0] = yCenter - distanceFromCenter;
        yLocs[1] = yCenter;
        yLocs[2] = yCenter + distanceFromCenter;
        yLocs[3] = yCenter;
        yLocs[4] = yCenter + distanceFromCenter;
        yLocs[5] = yCenter - distanceFromCenter;
        yLocs[6] = yCenter + distanceFromCenter;
        yLocs[7] = yCenter - distanceFromCenter;
        ibTarget.setY(425);
        ibWait.setY(yCenter + 100 - 2*distanceFromCenter);

        // X locations
        xLocs[0] = xCenter;
        xLocs[1] = xCenter - distanceFromCenter;
        xLocs[2] = xCenter;
        xLocs[3] = xCenter + distanceFromCenter;
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

    private void setPosLocations() {
        pos0.setY(yLocs[0]);
        pos0.setX(xLocs[0]);
        pos1.setY(yLocs[1]);
        pos1.setX(xLocs[1]);
        pos2.setY(yLocs[2]);
        pos2.setX(xLocs[2]);
        pos3.setY(yLocs[3]);
        pos3.setX(xLocs[3]);
        pos4.setY(yLocs[4]);
        pos4.setX(xLocs[4]);
        pos5.setY(yLocs[5]);
        pos5.setX(xLocs[5]);
        pos6.setY(yLocs[6]);
        pos6.setX(xLocs[6]);
        pos7.setY(yLocs[7]);
        pos7.setX(xLocs[7]);
        pos0.setEnabled(false);
        pos0.setVisibility(View.INVISIBLE);
        pos1.setEnabled(false);
        pos1.setVisibility(View.INVISIBLE);
        pos2.setEnabled(false);
        pos2.setVisibility(View.INVISIBLE);
        pos3.setEnabled(false);
        pos3.setVisibility(View.INVISIBLE);
        pos4.setEnabled(false);
        pos4.setVisibility(View.INVISIBLE);
        pos5.setEnabled(false);
        pos5.setVisibility(View.INVISIBLE);
        pos6.setEnabled(false);
        pos6.setVisibility(View.INVISIBLE);
        pos7.setEnabled(false);
        pos7.setVisibility(View.INVISIBLE);
    }

    private void setOnClickListeners() {
        for (int i = 0; i < numNeighbours; i++) {
            imageButtons[i].setOnClickListener(this);
        }
        ibWait.setOnClickListener(this);
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
    int startingLoc;

    @Override
    public void onClick(View view) {
         // Reset timer for idle timeout on each press
         callback.resetTimer_();


         switch (view.getId()) {
            case R.id.imageButton0:
                moveForwards(0);
                break;
            case R.id.imageButton1:
                moveForwards(1);
                break;
            case R.id.imageButton2:
                moveForwards(2);
                break;
            case R.id.imageButton3:
                moveForwards(3);
                break;
        }
    }

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
                currentDistanceFromTarget = pbLength;
                updateProgressBar(animationDuration + 400);
                arrivedAtWrongTarget();
            }
        }
    }

    private void logStep(int result) {
        String msg =
                numSteps + "," + result + "," + currentDistanceFromTarget + "," +
                targetPos + "," + currentPos + "," + startingLoc + "," + pathDistance;
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
                endOfTrial(new PreferencesManager(getContext()).ec_incorrect_trial);
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
                endOfTrial(new PreferencesManager(getContext()).ec_correct_trial);
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
        randomiseCurrentPos();
        while (distanceFromTarget(currentPos) != pathDistance) {
            randomiseCurrentPos();
        }
        startingLoc = currentPos;
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
        targetPos = r.nextInt(numStimulus);
        ibTarget.setImageResource(imageList[targetPos]);
    }

    private void setMaxProgress() {
        pbLength = pathDistance;
        pb1.setMax(pathDistance * pbScalar);
        pb1.setProgress(0);
    }

    private int distanceFromTarget(int currentPos) {
        return transitionMatrix[currentPos][targetPos];
    }

    private void randomiseImageLocation() {
        int[] chosen = {0, 0, 0, 0, 0, 0, 0, 0,};
        int choice = r.nextInt(4);
        for (int i = 0; i < numNeighbours; i++) {
            while (chosen[choice] == 1) {
                choice = r.nextInt(4);
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

    private void endOfTrial(String outcome) {
       h5.postDelayed(new Runnable() {
            @Override
            public void run() {
                        callback.trialEnded_(outcome);
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
