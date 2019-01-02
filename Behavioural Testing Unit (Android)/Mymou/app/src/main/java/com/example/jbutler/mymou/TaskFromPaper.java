package com.example.jbutler.mymou;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

// The task used in Butler & Kennerley (2018)
// Used to teach a 4x4 discrete world

public class TaskFromPaper extends Fragment
        implements View.OnClickListener {

    public ImageButton hideApplication;

    private int maxTrialDuration = 30000;
    private int pathDistance = 2;
    private static Context mContext;

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

    int[][] transitionMatrix = {
            {0,1,2,3,1,2,3,4,2,3,4,5,3,4,5,6},
            {1,0,1,2,2,1,2,3,3,2,3,4,4,3,4,5},
            {2,1,0,1,3,2,1,2,4,3,2,3,5,4,3,4},
            {3,2,1,0,4,3,2,1,5,4,3,2,6,5,4,3},
            {1,2,3,4,0,1,2,3,1,2,3,4,2,3,4,5},
            {2,1,2,3,1,0,1,2,2,1,2,3,3,2,3,4},
            {3,2,1,2,2,1,0,1,3,2,1,2,4,3,2,3},
            {4,3,2,1,3,2,1,0,4,3,2,1,5,4,3,2},
            {2,3,4,5,1,2,3,4,0,1,2,3,1,2,3,4},
            {3,2,3,4,2,1,2,3,1,0,1,2,2,1,2,3},
            {4,3,2,3,3,2,1,2,2,1,0,1,3,2,1,2},
            {5,4,3,2,4,3,2,1,3,2,1,0,4,3,2,1},
            {3,4,5,6,2,3,4,5,1,2,3,4,0,1,2,3},
            {4,3,4,5,3,2,3,4,2,1,2,3,1,0,1,2},
            {5,4,3,4,4,3,2,3,3,2,1,2,2,1,0,1},
            {6,5,4,3,5,4,3,2,4,3,2,1,3,2,1,0},
    };

    private int numNeighbours = 4;
    private int time = 0;
    private int yCenter, xCenter, distanceFromCenter, rewardChoice=0;
    private int[] neighbours = new int[numNeighbours];
    private int[] xLocs = new int[8];
    private int[] yLocs = new int[8];
    private Random r = new Random();
    private ImageButton ibRed, ibPink, ibGo, ibBrown, ibWait, ibTarget, ibCurrLoc;
    private ImageButton pos0, pos1, pos2, pos3, pos4, pos5, pos6, pos7;
    private ImageButton[] imageButtons = new ImageButton[numNeighbours];
    private ProgressBar pb1;
    public static boolean shutdown = false;

    Handler h0 = new Handler();
    Handler h1 = new Handler();
    Handler h2 = new Handler();
    Handler h3 = new Handler();
    Handler h4 = new Handler();
    Handler h5 = new Handler();
    Handler h6 = new Handler();
    Handler h7 = new Handler();
    Handler h8 = new Handler();
    Handler h9 = new Handler();
    private Handler backgroundLogHandler;
    private HandlerThread backgroundLogThread;

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

        //Disable unused buttons
        ibRed.setEnabled(false);
        ibRed.setVisibility(View.INVISIBLE);
        ibBrown.setEnabled(false);
        ibBrown.setVisibility(View.INVISIBLE);
        ibWait.setEnabled(false);
        ibWait.setVisibility(View.INVISIBLE);
        ibGo.setEnabled(true);
        ibGo.setVisibility(View.VISIBLE);

        TaskManager.setBrightness(255);

        numStimulus = imageList.length;

        // Start async handler to handle data logging
        backgroundLogThread = new HandlerThread("LogBackground");
        backgroundLogThread.start();
        backgroundLogHandler = new Handler(backgroundLogThread.getLooper());

        PrepareForNewTrial(0);
        timer();

    }

    private void assignObjects() {
        mContext = getActivity().getApplicationContext();
        ibPink = (ImageButton) getView().findViewById(R.id.imageButtonPink);
        ibRed = (ImageButton) getView().findViewById(R.id.imageButtonRed);
        ibBrown = (ImageButton) getView().findViewById(R.id.imageButtonBrown);
        ibGo = (ImageButton) getView().findViewById(R.id.imageButtonGo);
        ibWait = (ImageButton) getView().findViewById(R.id.imageButtonWaitCue);
        ibTarget = (ImageButton) getView().findViewById(R.id.imageButtonTarget);
        ibCurrLoc = (ImageButton) getView().findViewById(R.id.imageButtonCurrLoc);
        hideApplication = (ImageButton) getView().findViewById(R.id.imageButtonBigBlack);
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
        ibGo.setX(xCenter);
        ibGo.setY(yCenter);
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
        ibGo.setOnClickListener(this);
        ibWait.setOnClickListener(this);
    }

    static int rewardAmount = 1000;
    int currentPos = -1;
    int previousPos = -1;
    int timeout = 1000;
    int choiceDelay = 400;
    boolean choicePeriod = false;
    int animationDuration = 450;
    int trialCounter = 0;
    int[] chosenXlocs = {-1, -1, -1, -1};
    int[] chosenYlocs = {-1, -1, -1, -1};
    int targetPos;
    int numStimulus;
    int pbScalar = 1000;
    int pbLength;
    int currentDistanceFromTarget;
    int numSteps;
    int startingLoc;
    boolean timerRunning;

    @Override
    public void onClick(View view) {
        time = 0;
        TaskManager.setBrightness(255);
        switch (view.getId()) {
            case R.id.imageButtonGo:
                startTrial();
                break;
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

    private void startTrial() {

        TaskManager.takePhoto();

        logStep(5);

        if(!timerRunning) {
            timer();
        }

        ibGo.setEnabled(false);
        ibGo.setVisibility(View.INVISIBLE);
        //Task visible
        ibTarget.setEnabled(true);
        ibTarget.setVisibility(View.VISIBLE);
        unfadeButtons(0);
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
        } else {
            choicePeriod = false;
            bashingTimeout();
        }
    }

    private void arrivedAtWrongTarget() {
        //Highlight the correct two
        textView.setText("Feedback");
        h1.postDelayed(new Runnable() {
            @Override
            public void run() {
                ibCurrLoc.setVisibility(View.VISIBLE);
                ibCurrLoc.setEnabled(true);
                ibCurrLoc.setBackground(ContextCompat.getDrawable(mContext, R.drawable.outline_thick));
                ibTarget.setBackground(ContextCompat.getDrawable(mContext, R.drawable.outline_thick));
                ibRed.setVisibility(View.VISIBLE);
                ibRed.setEnabled(true);
                endOfTrial(0, timeout);
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
                ibPink.setEnabled(true);
                ibPink.setVisibility(View.VISIBLE);

                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

                TaskManager.deliverReward(rewardChoice, rewardAmount);
                endOfTrial(1, rewardAmount + 1000);
            }
        }, (animationDuration*2 + 400));

        Handler handlerOne = new Handler();
        handlerOne.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setText("Reward");
            }
        }, animationDuration*2 + 400);
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
        h9.postDelayed(new Runnable() {
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
        int j = 0;
        for (int i = 0; i < numStimulus; i++) {
            if (transitionMatrix[currentPos][i] == 1) {
                neighbours[j] = i;
                imageButtons[j].setEnabled(true);
                imageButtons[j].setVisibility(View.VISIBLE);
                imageButtons[j].setImageResource(imageList[i]);
                j++;
            }
        }
        Log.d("tag",j+"");
        //If on edge of maze set remaining neighbours to inactive
        while(j < numNeighbours) {
            Log.d("tag",j+"");
            neighbours[j] = -1;
            imageButtons[j].setEnabled(false);
            imageButtons[j].setVisibility(View.INVISIBLE);
            j++;
        }

    }

    private void PrepareForNewTrial(int delay) {

        //New trial data
        TaskManager.resetTrialData();

        h5.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (TaskManager.dateHasChanged()) {
                    trialCounter = 0;
                } else {
                    trialCounter++;
                }

                numSteps = 0;

                chooseTargetLoc();
                setStartingPosition();
                setMaxProgress();

                resetButtonPositionsAndBorders();
                pb1.setProgress((pbLength - currentDistanceFromTarget) * pbScalar);
                randomiseImageLocation();

                switchOffChoiceButtons();
                ibGo.setEnabled(true);
                ibGo.setVisibility(View.VISIBLE);

                //Task visible
                ibCurrLoc.setEnabled(false);
                ibCurrLoc.setVisibility(View.INVISIBLE);
                ibTarget.setEnabled(false);
                ibTarget.setVisibility(View.INVISIBLE);
                ibPink.setEnabled(false);
                ibPink.setVisibility(View.INVISIBLE);
                ibRed.setVisibility(View.INVISIBLE);
                ibRed.setEnabled(false);
                ibBrown.setEnabled(false);
                ibBrown.setVisibility(View.INVISIBLE);

                textView.setText("Initiation");

            }
        }, delay);
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
        currentPos = 6;
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
        targetPos = 4; // Static start target for demonstration
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

    private void bashingTimeout() {
        setChoiceButtonsClickable(false);
        cancelHandlers();
        ibBrown.setEnabled(true);
        ibBrown.setVisibility(View.VISIBLE);
        endOfTrial(2, timeout);
    }

    private void updateProgressBar(int delay) {
        h8.postDelayed(new Runnable() {
            @Override
            public void run() {
                ProgressBarAnimation anim = new ProgressBarAnimation(pb1, pb1.getProgress(), (pbLength - currentDistanceFromTarget) * pbScalar);
                anim.setDuration(animationDuration);
                pb1.startAnimation(anim);
            }
        }, delay);
    }

    private void endOfTrial(int outcome, int newTrialDelay) {
        logStep(outcome);

        //Save trial
        TaskManager.commitTrialData(outcome);

        PrepareForNewTrial(newTrialDelay);
    }

    private void timer() {
        Handler handlerOne = new Handler();
        handlerOne.postDelayed(new Runnable() {
            @Override
            public void run() {
                time += 1000;
                if (time > maxTrialDuration && !shutdown) {
                    time = 0;
                    endOfTrial(7, 0);
                    timerRunning = false;

                    //Decrease brightness while not in use
                    TaskManager.setBrightness(50);
                } else {
                    timer();
                    timerRunning = true;
                }
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
        h8.removeCallbacksAndMessages(null);
    }


    private void logStep(int result) {
        String timestamp = new SimpleDateFormat("HHmmss_SSS").format(Calendar.getInstance().getTime());
        String msg = "038," + TaskManager.photoTimestamp + "," + timestamp + "," + trialCounter + "," +
                numSteps + "," + result + "," + currentDistanceFromTarget + "," +
                targetPos + "," + currentPos + "," + startingLoc + "," + pathDistance + "," +
                rewardAmount + "," + timeout;
        TaskManager.logEvent(msg);
    }

    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }



}
