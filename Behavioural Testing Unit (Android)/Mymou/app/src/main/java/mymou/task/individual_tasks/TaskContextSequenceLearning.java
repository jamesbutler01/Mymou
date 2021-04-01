package mymou.task.individual_tasks;

import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.media.MediaPlayer;
import android.media.AudioManager;
import android.media.ToneGenerator;

import android.widget.VideoView;

import java.util.Random;

import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 * Context sequence learning
 */
public class TaskContextSequenceLearning extends Task {

    // Debug
    public static String TAG = "MymouTaskContextSequenceLearning";

    // Global task variables
    private static PreferencesManager prefManager;  // Load settings specified by experimenter
    private static Handler h0 = new Handler();  // Show object handler
    private static Handler h1 = new Handler();  // Hide object handler
    private long startTime;
    private static Button cue1, cue2, waitcue, gocue;
    private ToneGenerator toneGenerator;
    private int sound_1, sound_2;
    private int[] currConfig;

    private int seqNr = 0;
    private int curr_context;

    /**
     * Function called when task first loaded (before the UI is loaded)
     * Loads the UI components (cues, background etc)
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_context_sequence_learning, container, false);
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

        // currConfig holds the trial sequence that needs to be played out
        currConfig = returnTrialConfiguration();

        // Instantiate task objects with curr config
        assignObjects();

        // assign the whole thing here programatically
        startMovie();

    }

    /**
     * Recursive function that manages the timing of the task
     * Flashes two bars onto the screen of different heights, then displays a blank screen, then
     * repeats until the desired number of bars have been displayed, at which points it asks subjects
     * to choose between the two options
     */

    private void startMovie() {

        // first define relevant bits for this trial

        if (seqNr == 0) {

            curr_context = currConfig[0];
            sound_1 = currConfig[2];
            sound_2 = currConfig[3];

            // now we play out the relevant parts of the trial in sequence
            // first 'door' comes on
            VideoView mVideoView  = (VideoView) getActivity().findViewById(R.id.videoview);
            int movie_length = 2000;
            h0.postDelayed(new Runnable() {
                @Override
                public void run() {

                    mVideoView.setVisibility(View.INVISIBLE);

                    if (curr_context == 1) {
                        getActivity().findViewById(R.id.background_main).setBackgroundColor(prefManager.csl_col_context_1);
                    } else if (curr_context == 2) {
                        getActivity().findViewById(R.id.background_main).setBackgroundColor(prefManager.csl_col_context_2);
                    }

                    UtilsTask.toggleCue(waitcue, true);
                    turnOnSound();

                }
            }, movie_length);

            Uri stringUri=null;
            if (curr_context == 1) {
                stringUri= Uri.parse("android.resource://" + getActivity().getPackageName() + "/" +
                        R.raw.purple_door);
            } else if (curr_context == 2) {
                stringUri= Uri.parse("android.resource://" + getActivity().getPackageName() + "/" +
                        R.raw.orange_door);
            }
            // Lets stop the movie from being stretched
            getActivity().findViewById(R.id.background_main).setBackgroundColor(getActivity().getResources().getColor(R.color.black));
            mVideoView.setMediaController(null); // Disable controls so monkeys can't stop the movie!
            mVideoView.setVideoURI(stringUri);
            mVideoView.start();

        } else if (seqNr == 1){

            curr_context = currConfig[1];
            sound_1 = currConfig[4]; // 1 becomes 3
            sound_2 = currConfig[5];// 2 becomes 4}
            UtilsTask.toggleCue(waitcue, true);
            turnOnSound();

        } else {
            getActivity().findViewById(R.id.background_main).setBackgroundColor(prefManager.taskbackground);
            endOfTrial(true, callback, prefManager);
            return;

        }

        logEvent("Trial information [sequence, context, sound1, sound2]: " +seqNr + ","  + curr_context + "," + sound_1 + "," + sound_2, callback);

    }

    private int[] returnTrialConfiguration()
    {
        // random number between 1 to 8 to correspond to all possible trial types
        int max = 2; // for now we constrain to only probe trials
        int min = 1;

        Random rand = new Random();
        int trialType = rand.nextInt((max - min) + 1) + min;

        // depending on random number, we generate a number of relevant things: col context 1, col context 2, and individual sounds

        // sound 1 = bik, sound 2 = fop, sound 3 = hig, sound 4 = tef

        // number convention is as follows. trialConfig = {context color 1, context color 2, sound 1, sound 2, sound 3, sound 4} - this removes any ambiguity
        // context color is either 1 (orange) or 2 (purple) - in line with the document
        // sound 1 2 3 4 correspond to (A, B, C, D) | BIK, FOP, HIG, TEF
        // so trialConfig = [1, 1, 1, 2, 3, 4]; corresponds orange background repeated throughout the trial with (A B), (C D) being played out
        // so trialConfig = [2, 1, 4, 1, 2, 3]; corresponds purple played for first chain and orange for second chain, with (D A), (B C) being played out

        // initialize code
        int[] trialConfig = new int[8];

        switch (trialType)
        // trialConfig returns trial configuration
        {
            case 1:
                trialConfig = new int[] {1, 1, 1, 2, 3, 4};
                break;
            case 2:
                trialConfig = new int[] {2, 2, 2, 1, 4, 3};
                break;
            // 1 and 2 are training / habituation trials
            case 3:
                trialConfig = new int[] {1, 2, 1, 2, 3, 4};
                break;
            case 4:
                trialConfig = new int[] {2, 1, 2, 1, 4, 3};
                break;
            case 5:
                trialConfig = new int[] {1, 1, 1, 2, 4, 3};
                break;
            case 6:
                trialConfig = new int[] {2, 2, 2, 1, 3, 4};
                break;
            case 7:
                trialConfig = new int[] {1, 2, 1, 2, 4, 3};
                break;
            case 8:
                trialConfig = new int[] {2, 1, 2, 1, 3, 4};
                break;
        }
        return trialConfig;
    }

    private void assignObjects()
    {
        prefManager = new PreferencesManager(getContext());
        prefManager.ContextSequenceLearning();

        seqNr = 0;

        // generate the stuff
        waitcue = UtilsTask.addColorCue(1, prefManager.csl_choice_col_i, getContext(), null, getView().findViewById(R.id.parent_task_csl), GradientDrawable.OVAL);
        gocue = UtilsTask.addColorCue(2, prefManager.csl_choice_col_a, getContext(), responseClickListener, getView().findViewById(R.id.parent_task_csl), GradientDrawable.OVAL);

        waitcue.setX(prefManager.csl_waitcuex);
        waitcue.setY(prefManager.csl_waitcuey);

        gocue.setX(prefManager.csl_presscuex);
        gocue.setY(prefManager.csl_presscuey);

        // turn off all cues
        UtilsTask.toggleCue(waitcue, false);
        UtilsTask.toggleCue(gocue, false);

    }


    private int soundMap(int soundNr)

    {

        // this is a random ID for now
        int tone_id = 10;

        switch (soundNr)
        {
            case 1:
                tone_id = R.raw.bik;
                break;
            case 2:
                tone_id = R.raw.fop;
                break;
            case 3:
                tone_id = R.raw.hig;
                break;
            case 4:
                tone_id = R.raw.tef;
                break;
        };

        int soundConfig = tone_id;
        return soundConfig;
    }

    private void turnOnSound()
    {
        // sound1 corresponds to first sound in sequence / this can be any of the four sounds
        // sound2 corresponds to second sound in sequence

        // modify first sound
        int out = soundMap(sound_1);
        MediaPlayer mediaPlayer = MediaPlayer.create(prefManager.mContext, out);
        mediaPlayer.start();

        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                // modify second sound
                int out = soundMap(sound_2);
                MediaPlayer mediaPlayer = MediaPlayer.create(prefManager.mContext, out);
                mediaPlayer.start();

            }
        }, prefManager.csl_tone_delay); // delay between sound 1 and 2

        h0.postDelayed(new Runnable() {
            @Override
            public void run() {
                UtilsTask.toggleCue(waitcue, false);
                UtilsTask.toggleCue(gocue, true);
                startTime = System.currentTimeMillis(); // this should be once the last tone is played

            }
        }, prefManager.csl_tone_delay + 1000); // this delay denotes the differences when sound 2 stops playing and go cue turning green
    }

    // this is our listened for the response that computes the reaction time and dictates strength of sound that comes out
    private View.OnClickListener responseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            long rtTime = System.currentTimeMillis() - startTime;

            // turn off cue
            UtilsTask.toggleCue(gocue, false);

            // we need some rtTime that we subtract it from
            int rewAmount = (int) (prefManager.csl_rtBase - rtTime);

            logEvent("Behavior information [Reward, RT]: " + rewAmount + "," + rtTime, callback);
            if (rewAmount > 0) {
                callback.giveRewardFromTask_(rewAmount, false); // needs to be modified to reflect sound strength
                // Play sound depending on amount of reward
                float maxVolume = 100;
                float ratio = ((float) rewAmount / (float) prefManager.csl_rtBase);
                float soundVol = (maxVolume * ratio);
                logEvent("Behavior information [reward, sound strength]: " +rewAmount+","+ soundVol, callback);
                try {
                    toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, (int) soundVol);
                    toneGenerator.startTone(1, 200);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (toneGenerator != null) {
                                toneGenerator.release();
                                toneGenerator = null;
                            }
                        }
                    }, 200);
                } catch (Exception e) {
                    Log.d(TAG, "Exception while playing sound:" + e);
                }
            } else {
                logEvent("No reward as RT ("+rtTime+") > max allowed ("+prefManager.csl_rtBase+")", callback);
                rewAmount = 0;
            }

            // After reward start next phase of task
            h0.postDelayed(new Runnable() {
                @Override
                public void run() {

                    // startMovie for 2nd part
                    seqNr = seqNr + 1;

                    startMovie();

                }
            }, rewAmount + prefManager.csl_pair_tone_delay); // this will be delay between reward delivery and next door coming on

        }
    };


    /**
     * onPause called whenever a task is paused, interrupted, or cancelled
     * If task aborted for some reason (e.g. they did not respond quick enough), then cancel the handlers to stop the movie playing
     * This prevents task objects being loaded AFTER a trial has finished
     */
    @Override
    public void onPause() {
        super.onPause();
        super.onDestroy();
        h0.removeCallbacksAndMessages(null);
        h1.removeCallbacksAndMessages(null);
    }


    /**
     * This is static code repeated in each task that enables communication between the individual
     * task and the parent TaskManager.java which handles the backend utilities (reward delivery,
     * selfie processing, intertrial intervals, etc etc)
     */

    TaskInterface callback;
    public void setFragInterfaceListener(TaskInterface callback) {this.callback = callback;}

}

