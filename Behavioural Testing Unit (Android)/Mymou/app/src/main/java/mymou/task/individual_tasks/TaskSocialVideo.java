package mymou.task.individual_tasks;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.VideoView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Random;

import mymou.R;
import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;
import mymou.task.backend.UtilsTask;

/**
 * Social video preference task
 *
 * Two options are shown on the screen. Each option is tied to a specific collection of videos, from
 * which one is played before a reward is delivered.
 *
 * TODO: Add configurable settings
 *
 */
public class TaskSocialVideo extends Task {

    // Debug
    public static String TAG = "TaskSocialVideo";

    private static PreferencesManager prefManager;

    // Task objects
    private static ImageButton[] choice_cues;
    private final static int id_c1_1 = 0, id_c1_2 = 1;
    private final int[] previous_movie_played={-1,-1}, num_repeats_of_movie={0,0};
    private View background;
    private Random r;
    private static Uri[] social_video_paths, nonsocial_video_paths;
    private static Handler handler_start_next_trial = new Handler();  // Task trial_timer
    private static Handler handler_timeout_task = new Handler();  // Inter-trial interval timer
    private static Handler handler_loop_indefinitely = new Handler();  // Dim brightness timer

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_empty, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        logEvent(TAG + " started", callback);

        // Load preferences
        prefManager = new PreferencesManager(getContext());
        prefManager.SocialVideo();

        background = getView().findViewById(R.id.parent_task_empty);

        social_video_paths = new Uri[2];
        nonsocial_video_paths = new Uri[2];
        social_video_paths[0] = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" +
                    R.raw.sv_social_1);
        social_video_paths[1] = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" +
                    R.raw.sv_social_2);
        nonsocial_video_paths[0] = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" +
                R.raw.sv_nonsocial_1);
        nonsocial_video_paths[1] = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" +
                R.raw.sv_nonsocial_2);

        // Create go_cue
        ConstraintLayout layout = getView().findViewById(R.id.parent_task_empty);

        // Create choice cues
        choice_cues = new ImageButton[2];
        choice_cues[id_c1_1] = UtilsTask.addImageCue(id_c1_1, getContext(), layout, buttonClickListener);
        choice_cues[id_c1_1].setImageResource(R.drawable.sv_cue1);

        choice_cues[id_c1_2] = UtilsTask.addImageCue(id_c1_2, getContext(), layout, buttonClickListener);
        choice_cues[id_c1_2].setImageResource(R.drawable.sv_cue2);

        // Random number generator
        r = new Random();

        // Idle timeout is handled within task, so we never want to bubble up
        loop_indefinitely();

        start_task();
    }

    private void position_cues() {

        // Position cues
        if (r.nextBoolean()) {
            callback.logEvent_("02,175,1200,725,300, choice 1 positions");
            choice_cues[id_c1_1].setX(175);
            choice_cues[id_c1_1].setY(1200);
            choice_cues[id_c1_2].setX(725);
            choice_cues[id_c1_2].setY(300);
        } else {
            callback.logEvent_("02,725,300,175,1200, choice 1 positions");
            choice_cues[id_c1_1].setX(725);
            choice_cues[id_c1_1].setY(300);
            choice_cues[id_c1_2].setX(175);
            choice_cues[id_c1_2].setY(1200);
        }
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            callback.logEvent_("03," + view.getId() + ",,,, cue clicked");

            // Always disable cues first
            UtilsTask.toggleCues(choice_cues, false);

            // Make sure screen bright
            callback.setBrightnessFromTask_(true);

            callback.resetTimer_();

            // Cancel timers
            handler_start_next_trial.removeCallbacksAndMessages(null);
            handler_timeout_task.removeCallbacksAndMessages(null);

            // Reset timer for idle timeout on each press
            callback.resetTimer_();

            switch (view.getId()) {
                case id_c1_1:
                    choice_made(id_c1_1);
                    break;
                case id_c1_2:
                    choice_made(id_c1_2);
                    break;
            }
        }
    };
    private void end_task(){
        callback.logEvent_("06,,,,, timeout length reached - disabling task");
        handler_start_next_trial.removeCallbacksAndMessages(null);
        handler_timeout_task.removeCallbacksAndMessages(null);
        UtilsTask.toggleCues(choice_cues, false);
    }

    private void loop_indefinitely(){
        // End trial a consistent amount of time after feedback
        handler_loop_indefinitely.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.resetTimer_();
                loop_indefinitely();
            }
        }, 1000);
    }

    private void start_task(){
        callback.logEvent_("01,,,,, starting task");

        callback.resetTimer_();

        position_cues();

        handler_timeout_task.postDelayed(new Runnable() {
            @Override
            public void run() {
                end_task();
            }
        }, prefManager.sv_timeout_duration_mins*60*1000);

        UtilsTask.toggleCues(choice_cues, true);

    }

    private int select_movie(int social_or_nonsocial, int n_movies) {
        int roll = r.nextInt(n_movies);
        if (roll == previous_movie_played[social_or_nonsocial]) {
            num_repeats_of_movie[social_or_nonsocial] = num_repeats_of_movie[social_or_nonsocial] + 1;
        } else{
            num_repeats_of_movie[social_or_nonsocial] = 0;
        }

        if (num_repeats_of_movie[social_or_nonsocial] > prefManager.sv_n_movie_repeats_allowed) {
            roll = (roll + 1) % n_movies;
        }
        previous_movie_played[social_or_nonsocial] = roll;
        return roll;
    }
    private void choice_made(int c1_pressed) {
        callback.resetTimer_();

        // Roll the dice
        Uri stringUri = null;
        if (r.nextBoolean()) {
            int i_movie = select_movie(0, social_video_paths.length);
            stringUri = social_video_paths[i_movie];
            callback.logEvent_("04,0," + i_movie + ",,, social movie chosen");
        } else {
            int i_movie = select_movie(0, nonsocial_video_paths.length);
            stringUri = nonsocial_video_paths[i_movie];
            callback.logEvent_("04,1," + i_movie + ",,, nonsocial movie chosen");
        }

        // Play movie
        VideoView videoView = (VideoView) getView().findViewById(R.id.parent_task_empty);

        // Lets stop the movie from being stretched
        getActivity().findViewById(R.id.parent_task_empty).setBackgroundColor(getActivity().getResources().getColor(R.color.black));
        videoView.setMediaController(null); // Disable controls so monkeys can't stop the movie!
        videoView.setVideoURI(stringUri);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                reward_stage();
            }
        });
        videoView.start();
    }

    private void reward_stage() {

        // Give reward upon movie finishing
        callback.logEvent_("05,,,,, movie finished");

        callback.resetTimer_();
        callback.giveRewardFromTask_(prefManager.sv_rew_duration, false);

        // End trial a consistent amount of time after feedback
        handler_start_next_trial.postDelayed(new Runnable() {
            @Override
            public void run() {
                // We have to commit the event as well as the trial never actually ends
                callback.commitTrialDataFromTask_(prefManager.ec_correct_trial);
                start_task();
            }
        }, prefManager.sv_iti+prefManager.sv_rew_duration);

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
        callback.commitTrialDataFromTask_(prefManager.ec_incorrect_trial);

        handler_start_next_trial.removeCallbacksAndMessages(null);
        handler_timeout_task.removeCallbacksAndMessages(null);
        handler_loop_indefinitely.removeCallbacksAndMessages(null);
        callback.setBrightnessFromTask_(true);
    }


}
