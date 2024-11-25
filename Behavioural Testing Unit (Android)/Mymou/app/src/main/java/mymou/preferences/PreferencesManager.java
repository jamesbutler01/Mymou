package mymou.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import mymou.R;
import mymou.Utils.UtilsSystem;

import java.util.Arrays;

public class PreferencesManager {
    private String TAG = "MyMouPreferencesManager";

    public String base_error_message = "Error: Invalid settings configured so task cannot run. Please adjust settings and restart the task:\n\n";
    public String data_headers = "taskId, trialCounter, faceRecogPrediction, overallTrialOutcome, photoTimestamp, eventTimestamp, task manager code, task specific event codes";

    public static boolean debug, bluetooth, camera, facerecog, savefacerecogarrays, restartoncrash,
            sound, autostart, autostop, skip_go_cue = false, dimscreen, handle_feedback, run_timer = true;
    public static int sound_to_play, tone_dur, tone_freq, tone_strength;
    public static int dimscreenlevel, dimscreentime;
    public static int num_reward_chans, default_rew_chan, max_reward_channels;
    public static int rewardduration, responseduration, timeoutduration;
    public static int autostart_hour, autostop_hour, autostart_min, autostop_min;
    public static int taskbackground, rewardbackground, timeoutbackground;
    public static int border_colour, border_size, cue_size, cue_spacing;
    public static int num_monkeys;
    public static int camera_to_use;
    public static int[] colours_gocues;
    public static String tone_type, tone_filename;
    public static String ec_correct_trial, ec_incorrect_trial, ec_trial_timeout, ec_wrong_gocue_pressed, ec_trial_started, ec_trial_prepared;

    public SharedPreferences sharedPrefs;
    private int[] colors;
    public Resources r;
    public Context mContext;
    public Activity activity;

    public PreferencesManager(Context context) {
        mContext = context;
        activity = (Activity) context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        r = context.getResources();

        debug = sharedPrefs.getBoolean(r.getString(R.string.preftag_debug), r.getBoolean(R.bool.default_debug));
        bluetooth = sharedPrefs.getBoolean(r.getString(R.string.preftag_bluetooth), r.getBoolean(R.bool.default_bluetooth));
        camera = sharedPrefs.getBoolean(r.getString(R.string.preftag_camera), r.getBoolean(R.bool.default_camera));
        facerecog = sharedPrefs.getBoolean(r.getString(R.string.preftag_facerecog), r.getBoolean(R.bool.default_facerecog));
        savefacerecogarrays = sharedPrefs.getBoolean(r.getString(R.string.preftag_savefacerecogarrays), r.getBoolean(R.bool.default_savefacerecogarrays));
        dimscreen = sharedPrefs.getBoolean(r.getString(R.string.preftag_dimscreen), r.getBoolean(R.bool.default_dimscreen));
        restartoncrash = sharedPrefs.getBoolean(r.getString(R.string.preftag_restartoncrash), r.getBoolean(R.bool.default_restartoncrash));
        sound = sharedPrefs.getBoolean(r.getString(R.string.preftag_sound), r.getBoolean(R.bool.default_sound));
        autostart = sharedPrefs.getBoolean(r.getString(R.string.preftag_autostart), r.getBoolean(R.bool.default_autostart));
        autostop = sharedPrefs.getBoolean(r.getString(R.string.preftag_autostop), r.getBoolean(R.bool.default_autostop));

        camera_to_use = sharedPrefs.getInt(r.getString(R.string.preftag_camera_to_use), r.getInteger(R.integer.default_camera_to_use));
        sound_to_play = sharedPrefs.getInt(r.getString(R.string.preftag_sound_to_play), r.getInteger(R.integer.default_system_tone));
        tone_dur = sharedPrefs.getInt(r.getString(R.string.preftag_tone_dur), r.getInteger(R.integer.default_tone_duration));
        tone_freq = sharedPrefs.getInt(r.getString(R.string.preftag_tone_freq), r.getInteger(R.integer.default_tone_freq));
        tone_strength =  sharedPrefs.getInt("preftag_tone_strength", 100);

        dimscreenlevel = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_dimscreenlevel), "5"));
        dimscreentime = sharedPrefs.getInt(r.getString(R.string.preftag_dimscreentime), r.getInteger(R.integer.default_dimscreentime));

        max_reward_channels = Integer.valueOf(mContext.getString(R.string.max_reward_channels));
        num_reward_chans = sharedPrefs.getInt(r.getString(R.string.preftag_num_rew_chans), r.getInteger(R.integer.default_num_rew_chans));
        default_rew_chan = sharedPrefs.getInt(r.getString(R.string.preftag_default_rew_chan), r.getInteger(R.integer.default_rew_chan));
        if (default_rew_chan > num_reward_chans) {
            default_rew_chan = 0;
        }
        rewardduration = sharedPrefs.getInt(r.getString(R.string.preftag_rewardduration), r.getInteger(R.integer.default_rewardduration));
        responseduration = sharedPrefs.getInt(r.getString(R.string.preftag_responseduration), r.getInteger(R.integer.default_responseduration));
        responseduration *= 1000;
        timeoutduration = sharedPrefs.getInt(r.getString(R.string.preftag_timeoutduration), r.getInteger(R.integer.default_timeoutduration));

        autostart_hour = sharedPrefs.getInt(r.getString(R.string.preftag_autostart_hour), r.getInteger(R.integer.default_autostart_hour));
        autostart_min = sharedPrefs.getInt(r.getString(R.string.preftag_autostart_min), 0);
        autostop_hour = sharedPrefs.getInt(r.getString(R.string.preftag_autostop_hour), r.getInteger(R.integer.default_autostop_hour));
        autostop_min = sharedPrefs.getInt(r.getString(R.string.preftag_autostop_min), 0);

        cue_size = sharedPrefs.getInt(r.getString(R.string.preftag_cue_size), r.getInteger(R.integer.default_cuesize));
        cue_spacing = sharedPrefs.getInt(r.getString(R.string.preftag_cue_spacing), r.getInteger(R.integer.default_cuespacing));
        border_size = sharedPrefs.getInt(r.getString(R.string.preftag_cue_border_size), r.getInteger(R.integer.default_bordersize));
        num_monkeys = sharedPrefs.getInt(r.getString(R.string.preftag_num_monkeys), r.getInteger(R.integer.default_num_monkeys));

        int taskbackgroundcolour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_taskbackgroundcolour), Integer.toString(r.getInteger(R.integer.default_taskbackgroundcolour))));
        int rewardbackgroundcolour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_rewardbackgroundcolour), Integer.toString(r.getInteger(R.integer.default_rewardbackgroundcolour))));
        int timeoutbackgroundcolour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_timeoutbackgroundcolour), Integer.toString(r.getInteger(R.integer.default_timeoutbackgroundcolour))));
        int bordercolour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_cuebordercolors), Integer.toString(r.getInteger(R.integer.default_bordercolour))));

        colors = r.getIntArray(R.array.colorarray);
        taskbackground = colors[taskbackgroundcolour];
        rewardbackground = colors[rewardbackgroundcolour];
        timeoutbackground = colors[timeoutbackgroundcolour];
        border_colour = colors[bordercolour];

        // Cue colour settings
        colours_gocues = new int[4];
        int cue_colour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_gocuecolorone), Integer.toString(r.getInteger(R.integer.default_gocuecolone))));
        colours_gocues[0] = colors[cue_colour];
        cue_colour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_gocuecolortwo), Integer.toString(r.getInteger(R.integer.default_gocuecoltwo))));
        colours_gocues[1] = colors[cue_colour];
        cue_colour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_gocuecolorthree), Integer.toString(r.getInteger(R.integer.default_gocuecolthree))));
        colours_gocues[2] = colors[cue_colour];
        cue_colour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_gocuecolorfour), Integer.toString(r.getInteger(R.integer.default_gocuecolfour))));
        colours_gocues[3] = colors[cue_colour];

        ec_incorrect_trial = sharedPrefs.getString(r.getString(R.string.preftag_eventcode_error_trial), r.getString(R.string.default_eventcode_error_trial));
        ec_correct_trial = sharedPrefs.getString(r.getString(R.string.preftag_eventcode_correct_trial), r.getString(R.string.default_eventcode_correct_trial));
        ec_trial_timeout = sharedPrefs.getString(r.getString(R.string.preftag_eventcode_timeout_trial), r.getString(R.string.default_eventcode_timeout_trial));
        ec_wrong_gocue_pressed = sharedPrefs.getString(r.getString(R.string.preftag_eventcode_wrong_gocue), r.getString(R.string.default_eventcode_wrong_gocue));
        ec_trial_started = sharedPrefs.getString(r.getString(R.string.preftag_eventcode_start_trial), r.getString(R.string.default_eventcode_start_trial));
        ec_trial_prepared = sharedPrefs.getString(r.getString(R.string.preftag_eventcode_trial_prepared), r.getString(R.string.default_eventcode_trial_prepared));

        tone_type = sharedPrefs.getString(r.getString(R.string.preftag_tone_type), r.getString(R.string.preftag_system_tone));
        tone_filename = sharedPrefs.getString(r.getString(R.string.tone_filename), "");

        handle_feedback = true; // Default behaviour, individual tasks can adjust this parameter

    }

    public String[] strobes_on, strobes_off;

    public void RewardStrobeChannels() {

        strobes_on = new String[max_reward_channels];
        strobes_off = new String[max_reward_channels];
        strobes_on[0] = sharedPrefs.getString(r.getString(R.string.preftag_strobe_one_on), r.getString(R.string.default_strobe_one_on));
        strobes_off[0] = sharedPrefs.getString(r.getString(R.string.preftag_strobe_one_off), r.getString(R.string.default_strobe_one_off));
        strobes_on[1] = sharedPrefs.getString(r.getString(R.string.preftag_strobe_two_on), r.getString(R.string.default_strobe_two_on));
        strobes_off[1] = sharedPrefs.getString(r.getString(R.string.preftag_strobe_two_off), r.getString(R.string.default_strobe_two_off));
        strobes_on[2] = sharedPrefs.getString(r.getString(R.string.preftag_strobe_three_on), r.getString(R.string.default_strobe_three_on));
        strobes_off[2] = sharedPrefs.getString(r.getString(R.string.preftag_strobe_three_off), r.getString(R.string.default_strobe_three_off));
        strobes_on[3] = sharedPrefs.getString(r.getString(R.string.preftag_strobe_four_on), r.getString(R.string.default_strobe_four_on));
        strobes_off[3] = sharedPrefs.getString(r.getString(R.string.preftag_strobe_four_off), r.getString(R.string.default_strobe_four_off));
    }

    public int objectdiscrim_num_corr_options, objectdiscrim_num_incorr_options, objectdiscrim_num_corr_shown, objectdiscrim_num_incorr_shown, objectdiscrim_num_steps;
    public int[] objectdiscrim_corr_colours, objectdiscrim_incorr_colours;
    public int[] objectdiscrim_prev_cols_corr, objectdiscrim_prev_cols_incorr;
    public boolean objectdiscrim_repeatOnError, objectdiscrim_previous_error, objectdiscrim_valid_config;
    public String objectdiscrim_errormessage;

    public void ObjectDiscriminationCol() {
        objectdiscrim_num_corr_shown = sharedPrefs.getInt(r.getString(R.string.preftag_od_num_corr_cues), r.getInteger(R.integer.default_objdisc_num_corr_shown));
        objectdiscrim_num_incorr_shown = sharedPrefs.getInt(r.getString(R.string.preftag_od_num_incorr_cues), r.getInteger(R.integer.default_objdisc_num_incorr_shown));
        objectdiscrim_num_steps = sharedPrefs.getInt(r.getString(R.string.preftag_od_num_steps), r.getInteger(R.integer.default_objdisc_num_steps));
        objectdiscrim_repeatOnError = sharedPrefs.getBoolean(r.getString(R.string.preftag_od_repeat_error), r.getBoolean(R.bool.default_objdisc_repeaterror));

        // Colours
        int max_cues = 15;
        objectdiscrim_corr_colours = new int[max_cues];
        objectdiscrim_incorr_colours = new int[max_cues];

        int[] chosen_cols = UtilsSystem.loadIntArray(r.getString(R.string.preftag_od_corr_cols), sharedPrefs, mContext);
        int num_corr_options = 0;
        for (int i = 0; i < chosen_cols.length; i++) {
            if (chosen_cols[i] == 1) {
                objectdiscrim_corr_colours[num_corr_options] = colors[i];
                num_corr_options += 1;
            }
        }
        objectdiscrim_num_corr_options = num_corr_options;
        objectdiscrim_corr_colours = Arrays.copyOf(objectdiscrim_corr_colours, objectdiscrim_num_corr_options);


        chosen_cols = UtilsSystem.loadIntArray(r.getString(R.string.preftag_od_incorr_cols), sharedPrefs, mContext);
        int num_incorr_options = 0;
        for (int i = 0; i < chosen_cols.length; i++) {
            if (chosen_cols[i] == 1) {
                objectdiscrim_incorr_colours[num_incorr_options] = colors[i];
                num_incorr_options += 1;
            }
        }
        objectdiscrim_num_incorr_options = num_incorr_options;
        objectdiscrim_incorr_colours = Arrays.copyOf(objectdiscrim_incorr_colours, objectdiscrim_num_incorr_options);

        // Previous trial information
        objectdiscrim_previous_error = sharedPrefs.getBoolean(r.getString(R.string.preftag_od_previous_error), r.getBoolean(R.bool.default_objdisc_previouserror));
        objectdiscrim_prev_cols_corr = UtilsSystem.loadIntArray(r.getString(R.string.preftag_od_prev_cols_corr), sharedPrefs, mContext);
        objectdiscrim_prev_cols_incorr = UtilsSystem.loadIntArray(r.getString(R.string.preftag_od_prev_cols_incorr), sharedPrefs, mContext);

        // Check settings are valid
        objectdiscrim_errormessage = "You've selected ";
        objectdiscrim_valid_config = false;
        if (objectdiscrim_num_incorr_options < objectdiscrim_num_incorr_shown) {
            objectdiscrim_errormessage += "" + objectdiscrim_num_incorr_shown + " incorrect cue(s) to display each trial, but have only selected " + objectdiscrim_num_incorr_options + " different incorrect cue(s) in total";
        } else if (objectdiscrim_num_corr_options < objectdiscrim_num_corr_shown) {
            objectdiscrim_errormessage += "" + objectdiscrim_num_corr_shown + " correct cue(s) to display each trial, but have only selected " + objectdiscrim_num_corr_options + " different correct cue(s) in total";
        } else {
            objectdiscrim_valid_config = true;
        }

    }

    public boolean dm_repeat_on_error, dm_static_reward, dm_extra_step_timeout, dm_use_progress_bar;
    public int dm_min_start_distance, dm_max_start_distance, dm_max_dist_in_map, dm_map_selected, dm_num_extra_steps, dm_dist_to_target_needed;
    public int dm_choice_delay, dm_animation_duration, dm_booster_amount, dm_target_switch_freq;

    public void DiscreteMaze() {

        dm_repeat_on_error = sharedPrefs.getBoolean(r.getString(R.string.preftag_dm_repeat_error), r.getBoolean(R.bool.default_dm_repeat_error));
        dm_static_reward = sharedPrefs.getBoolean(r.getString(R.string.preftag_dm_static_reward), r.getBoolean(R.bool.default_dm_static_reward));
        dm_extra_step_timeout = sharedPrefs.getBoolean(r.getString(R.string.preftag_dm_extra_step_timout), r.getBoolean(R.bool.default_dm_extra_step_timout));
        dm_use_progress_bar = sharedPrefs.getBoolean(r.getString(R.string.preftag_dm_use_progress_bar), r.getBoolean(R.bool.default_dm_use_progress_bar));
        dm_map_selected = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_dm_map), Integer.toString(r.getInteger(R.integer.default_dm_map))));
        dm_min_start_distance = sharedPrefs.getInt(r.getString(R.string.preftag_dm_min_start_distance), r.getInteger(R.integer.default_dm_min_start_distance));
        dm_max_start_distance = sharedPrefs.getInt(r.getString(R.string.preftag_dm_max_start_distance), r.getInteger(R.integer.default_dm_max_start_distance));
        dm_num_extra_steps = sharedPrefs.getInt(r.getString(R.string.preftag_dm_num_extra_steps), r.getInteger(R.integer.default_dm_num_extra_steps));
        dm_choice_delay = sharedPrefs.getInt(r.getString(R.string.preftag_dm_choice_delay), r.getInteger(R.integer.default_dm_choice_delay));
        dm_animation_duration = sharedPrefs.getInt(r.getString(R.string.preftag_dm_animation_duration), r.getInteger(R.integer.default_dm_animation_duration));
        dm_dist_to_target_needed = sharedPrefs.getInt(r.getString(R.string.preftag_dm_dist_to_target), r.getInteger(R.integer.default_dm_dist_to_target));
        dm_target_switch_freq = sharedPrefs.getInt(r.getString(R.string.preftag_dm_target_switch_freq), r.getInteger(R.integer.default_dm_target_switch_freq));
        dm_booster_amount = sharedPrefs.getInt(r.getString(R.string.preftag_dm_booster_amount), r.getInteger(R.integer.default_dm_booster_amount));

        dm_choice_delay = 300;
        dm_animation_duration = 300;

        dm_max_dist_in_map = 4;
    }

    public int t_one_screen_colour, t_one_num_presses, t_random_reward_start_time, t_random_reward_stop_time, t_four_num_static_cue_pos;

    public void TrainingTasks() {
        int screen_colour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_t_one_screen_colour), Integer.toString(r.getInteger(R.integer.default_t_one_screen_colour))));
        t_one_screen_colour = colors[screen_colour];
        t_random_reward_start_time = sharedPrefs.getInt(r.getString(R.string.preftag_t_random_reward_start), r.getInteger(R.integer.default_random_reward_start));
        t_random_reward_stop_time = sharedPrefs.getInt(r.getString(R.string.preftag_t_random_reward_stop), r.getInteger(R.integer.default_random_reward_stop));
        t_one_num_presses = sharedPrefs.getInt(r.getString(R.string.preftag_t_one_num_presses), r.getInteger(R.integer.default_t_one_num_presses));
        t_four_num_static_cue_pos = sharedPrefs.getInt(r.getString(R.string.preftag_t_four_num_static_pos), r.getInteger(R.integer.default_t_four_num_static_pos));
        skip_go_cue = sharedPrefs.getBoolean(r.getString(R.string.preftag_skip_go_cue), r.getBoolean(R.bool.default_t_one_skip_go_cue));

        // TODO: Get this working
        int t_random_reward_stop_time_ms = t_random_reward_stop_time * 1000;
        responseduration += t_random_reward_stop_time_ms;

    }

    public int sl_seq_length, sl_map_selected, sl_max_seq_length;

    public void SequentialLearning() {
        sl_max_seq_length = 10;
        sl_seq_length = sharedPrefs.getInt(r.getString(R.string.preftag_sl_seq_length), r.getInteger(R.integer.default_sl_seq_length));
        sl_map_selected = sharedPrefs.getInt(r.getString(R.string.preftag_sl_map_selected), r.getInteger(R.integer.default_sl_map_selected));

    }

    public int sv_rew_duration, sv_iti, sv_timeout_duration_mins, sv_n_movie_repeats_allowed, sv_cue_size;
    public int sv_pos1_x, sv_pos1_y, sv_pos2_x, sv_pos2_y;
    public int sv_video_width, sv_video_height;
    public boolean sv_video_muted;

    public void SocialVideo() {
        sv_rew_duration = sharedPrefs.getInt(r.getString(R.string.preftag_sv_rew_duration), r.getInteger(R.integer.default_sv_rew_duration));
        sv_iti = sharedPrefs.getInt(r.getString(R.string.preftag_sv_iti), r.getInteger(R.integer.default_sv_iti));
        sv_timeout_duration_mins = sharedPrefs.getInt(r.getString(R.string.preftag_sv_timeout_duration_mins), r.getInteger(R.integer.default_sv_timeout_duration_mins));
        sv_n_movie_repeats_allowed = sharedPrefs.getInt(r.getString(R.string.preftag_sv_n_movie_repeats_allowed), r.getInteger(R.integer.default_sv_n_movie_repeats_allowed));
        sv_pos1_x = sharedPrefs.getInt(r.getString(R.string.preftag_sv_pos1_x), r.getInteger(R.integer.default_sv_pos1_x));
        sv_pos1_y = sharedPrefs.getInt(r.getString(R.string.preftag_sv_pos1_y), r.getInteger(R.integer.default_sv_pos1_y));
        sv_pos2_x = sharedPrefs.getInt(r.getString(R.string.preftag_sv_pos2_x), r.getInteger(R.integer.default_sv_pos2_x));
        sv_pos2_y = sharedPrefs.getInt(r.getString(R.string.preftag_sv_pos2_y), r.getInteger(R.integer.default_sv_pos2_y));
        sv_pos2_y = sharedPrefs.getInt(r.getString(R.string.preftag_sv_pos2_y), r.getInteger(R.integer.default_sv_pos2_y));
        sv_video_width = sharedPrefs.getInt(r.getString(R.string.preftag_sv_video_width), r.getInteger(R.integer.default_sv_video_width));
        sv_video_height = sharedPrefs.getInt(r.getString(R.string.preftag_sv_video_height), r.getInteger(R.integer.default_sv_video_height));
        sv_cue_size = sharedPrefs.getInt(r.getString(R.string.preftag_sv_cue_size), r.getInteger(R.integer.default_sv_cue_size));
        sv_video_muted = sharedPrefs.getBoolean(r.getString(R.string.preftag_sv_video_muted), r.getBoolean(R.bool.default_sv_video_muted));
        skip_go_cue=true;
    }

    public int dvs_feedback_duration;
    public boolean dvs_randomly_place_options, dvs_give_full_map;

    public void DiscreteValueSpace() {
        dvs_feedback_duration = sharedPrefs.getInt(r.getString(R.string.preftag_dvs_feedback_duration), r.getInteger(R.integer.default_dvs_feedback_duration));
        dvs_randomly_place_options = sharedPrefs.getBoolean(r.getString(R.string.preftag_dvs_randomly_place_options), r.getBoolean(R.bool.default_dvs_randomly_place_options));
        dvs_give_full_map = sharedPrefs.getBoolean(r.getString(R.string.preftag_dvs_give_full_map), r.getBoolean(R.bool.default_dvs_give_full_map));
        handle_feedback = false;
    }

    public void AlternatingTwoChoice() {
        handle_feedback = false;

    }

    public int ts_transition_prob, ts_go_cue_reward_amount, ts_trial_reward_amount, ts_intertrial_interval;
    public int ts_low_reward_percent, ts_high_reward_percent;
    public int ts_rew_change_interval;
    public int ts_c2_1_col, ts_c2_2_col;

    public void TrainingFiveTwoStep() {
        ts_transition_prob = 80;
        ts_go_cue_reward_amount = 750;
        ts_trial_reward_amount = 2500;
        ts_low_reward_percent = 20; // Percent of times rewarded
        ts_high_reward_percent = 80;  // Percent of times rewarded
        ts_intertrial_interval = 1000;
        ts_rew_change_interval = 40;
        ts_c2_1_col = ContextCompat.getColor(mContext, R.color.silver);
        ts_c2_2_col = ContextCompat.getColor(mContext, R.color.yellow);

    }

    public int t_sc_minrew, t_sc_maxrew, t_sc_miniti, t_sc_maxiti, t_sc_sesslength;
    public int t_sc_numpressesneeded, t_sc_numpressesneedediti;
    public int t_sc_cuex, t_sc_cuey, t_sc_cuextwo, t_sc_cueytwo, t_sc_cueheight, t_sc_cuewidth;
    public int t_sc_cue_colour, t_sc_border_colour, t_sc_cue_shape, t_sc_bordersize;
    public boolean t_sc_photo, t_sc_stopsess, t_sc_togglecue, t_sc_movecue;

    public void TrainingStaticCue() {
        t_sc_minrew = sharedPrefs.getInt(r.getString(R.string.preftag_t_sc_minrew), r.getInteger(R.integer.default_t_sc_minrew));
        t_sc_maxrew = sharedPrefs.getInt(r.getString(R.string.preftag_t_sc_maxrew), r.getInteger(R.integer.default_t_sc_maxrew));
        t_sc_miniti = sharedPrefs.getInt(r.getString(R.string.preftag_t_sc_miniti), r.getInteger(R.integer.default_t_sc_miniti));
        t_sc_maxiti = sharedPrefs.getInt(r.getString(R.string.preftag_t_sc_maxiti), r.getInteger(R.integer.default_t_sc_maxiti));
        t_sc_sesslength = sharedPrefs.getInt(r.getString(R.string.preftag_t_sc_sess_length), r.getInteger(R.integer.default_t_sc_sess_length));
        t_sc_numpressesneeded = sharedPrefs.getInt(r.getString(R.string.preftag_t_sc_npn), r.getInteger(R.integer.default_t_sc_npn));
        t_sc_numpressesneedediti = sharedPrefs.getInt(r.getString(R.string.preftag_t_sc_npniti), r.getInteger(R.integer.default_t_sc_npniti));
        t_sc_photo = sharedPrefs.getBoolean(r.getString(R.string.preftag_t_sc_photo), r.getBoolean(R.bool.default_t_sc_photo));
        t_sc_stopsess = sharedPrefs.getBoolean(r.getString(R.string.preftag_t_sc_stopsess), r.getBoolean(R.bool.default_t_sc_stopsess));
        t_sc_togglecue = sharedPrefs.getBoolean(r.getString(R.string.preftag_t_sc_togglecue), r.getBoolean(R.bool.default_t_sc_togglecue));
        t_sc_movecue = sharedPrefs.getBoolean(r.getString(R.string.preftag_t_sc_alternatecue), r.getBoolean(R.bool.default_t_sc_alternatecue));

        // Cue properties
        t_sc_cuex = sharedPrefs.getInt(r.getString(R.string.preftag_t_sc_cuex), 300);
        t_sc_cuey = sharedPrefs.getInt(r.getString(R.string.preftag_t_sc_cuey), 300);
        t_sc_cuextwo = sharedPrefs.getInt(r.getString(R.string.preftag_t_sc_cuextwo), 300);
        t_sc_cueytwo = sharedPrefs.getInt(r.getString(R.string.preftag_t_sc_cueytwo), 300);
        t_sc_cueheight = sharedPrefs.getInt(r.getString(R.string.preftag_t_sc_cueheight), 300);
        t_sc_cuewidth = sharedPrefs.getInt(r.getString(R.string.preftag_t_sc_cuewidth), 300);
        int cue_colour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_t_sc_cue_colour), Integer.toString(r.getInteger(R.integer.default_t_sc_cue_colour))));
        t_sc_cue_colour = colors[cue_colour];
        int border_colour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_t_sc_cue_border_colour), Integer.toString(r.getInteger(R.integer.default_t_sc_bordercolour))));
        t_sc_border_colour = colors[border_colour];
        t_sc_cue_shape = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_t_sc_cue_shape), Integer.toString(r.getInteger(R.integer.default_t_sc_shape))));
        t_sc_bordersize = sharedPrefs.getInt(r.getString(R.string.preftag_t_sc_bordersize), r.getInteger(R.integer.default_t_sc_bordersize));

    }

    public int pass_minrew, pass_maxrew, pass_miniti, pass_maxiti, pass_sesslength;
    public boolean pass_photo, pass_stopsess;

    public void PassiveReward() {
        pass_minrew = sharedPrefs.getInt(r.getString(R.string.preftag_pass_minrew), r.getInteger(R.integer.default_pass_minrew));
        pass_maxrew = sharedPrefs.getInt(r.getString(R.string.preftag_pass_maxrew), r.getInteger(R.integer.default_pass_maxrew));
        pass_miniti = sharedPrefs.getInt(r.getString(R.string.preftag_pass_miniti), r.getInteger(R.integer.default_pass_miniti));
        pass_maxiti = sharedPrefs.getInt(r.getString(R.string.preftag_pass_maxiti), r.getInteger(R.integer.default_pass_maxiti));
        pass_sesslength = sharedPrefs.getInt(r.getString(R.string.preftag_pass_sess_length), r.getInteger(R.integer.default_pass_sess_length));
        pass_photo = sharedPrefs.getBoolean(r.getString(R.string.preftag_pass_photo), r.getBoolean(R.bool.default_pass_photo));
        pass_stopsess = sharedPrefs.getBoolean(r.getString(R.string.preftag_pass_stopsess), r.getBoolean(R.bool.default_pass_stopsess));
    }

    public int pr_cue_colour, pr_animation_duration, pr_cuex, pr_cuey, pr_iti, pr_blinklength, pr_timeoutlength, pr_sess_length, pr_cue_shape, pr_cue_size, pr_border_size, pr_border_colour;
    public boolean pr_progress_bar, pr_move_cue, pr_skip_go_cue;

    public void ProgressiveRatio() {
        int cue_colour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_pr_cue_colour), Integer.toString(r.getInteger(R.integer.default_pr_cue_colour))));
        pr_cue_colour = colors[cue_colour];
        int border_colour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_pr_cue_border_colour), Integer.toString(r.getInteger(R.integer.default_pr_bordercolour))));
        pr_border_colour = colors[border_colour];

        pr_progress_bar = sharedPrefs.getBoolean(r.getString(R.string.preftag_pr_progress_bar), r.getBoolean(R.bool.default_pr_progress_bar));
        pr_move_cue = sharedPrefs.getBoolean(r.getString(R.string.preftag_pr_move_cue), r.getBoolean(R.bool.default_pr_move_cue));
        pr_skip_go_cue = sharedPrefs.getBoolean(r.getString(R.string.preftag_pr_skip_go_cue), r.getBoolean(R.bool.default_pr_skip_go_cue));

        if (pr_skip_go_cue) {
            run_timer = false; // Handle timings ourselves
        }

        pr_cuex = sharedPrefs.getInt(r.getString(R.string.preftag_pr_cuex), 300);
        pr_cuey = sharedPrefs.getInt(r.getString(R.string.preftag_pr_cuey), 300);
        pr_cue_shape = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_pr_cue_shape), Integer.toString(r.getInteger(R.integer.default_pr_shape))));
        pr_animation_duration = sharedPrefs.getInt(r.getString(R.string.preftag_pr_animation_duration), r.getInteger(R.integer.default_pr_animation_duration));
        pr_iti = sharedPrefs.getInt(r.getString(R.string.preftag_pr_iti), r.getInteger(R.integer.default_pr_iti));
        pr_blinklength = sharedPrefs.getInt(r.getString(R.string.preftag_pr_blink), r.getInteger(R.integer.default_pr_blink));
        pr_timeoutlength = sharedPrefs.getInt(r.getString(R.string.preftag_pr_timeout_length), r.getInteger(R.integer.default_pr_timeout_length));
        pr_sess_length = sharedPrefs.getInt(r.getString(R.string.preftag_pr_sess_length), r.getInteger(R.integer.default_pr_sess_length));

        pr_cue_size = sharedPrefs.getInt(r.getString(R.string.preftag_pr_cue_size), r.getInteger(R.integer.default_pr_cuesize));
        pr_border_size = sharedPrefs.getInt(r.getString(R.string.preftag_pr_cue_border_size), r.getInteger(R.integer.default_pr_bordersize));

    }

    public int ea_num_steps, ea_step_duration_on, ea_step_duration_off, ea_distance, ea_variance;

    public void EvidenceAccum() {
        ea_num_steps = sharedPrefs.getInt(r.getString(R.string.preftag_ea_num_steps), r.getInteger(R.integer.default_ea_num_steps));
        ea_step_duration_off = sharedPrefs.getInt(r.getString(R.string.preftag_ea_step_duration_off), r.getInteger(R.integer.default_ea_step_duration_off));
        ea_step_duration_on = sharedPrefs.getInt(r.getString(R.string.preftag_ea_step_duration_on), r.getInteger(R.integer.default_ea_step_duration_on));
        ea_distance = sharedPrefs.getInt(r.getString(R.string.preftag_ea_distance), r.getInteger(R.integer.default_ea_distance));
        ea_variance = sharedPrefs.getInt(r.getString(R.string.preftag_ea_variance), r.getInteger(R.integer.default_ea_variance));
    }

    public int sr_duration_on, sr_duration_off, sr_num_stim, sr_locations;

    public void SpatialResponse() {
        sr_duration_off = sharedPrefs.getInt(r.getString(R.string.preftag_sr_duration_off), r.getInteger(R.integer.default_sr_duration_off));
        sr_duration_on = sharedPrefs.getInt(r.getString(R.string.preftag_sr_duration_on), r.getInteger(R.integer.default_sr_duration_on));
        sr_num_stim = sharedPrefs.getInt(r.getString(R.string.preftag_sr_num_stimuli), r.getInteger(R.integer.default_sr_num_stimuli));
        sr_locations = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_sr_locations), "2"));
    }

    public int od_duration_on, od_duration_off, od_num_stim, od_num_distractors, od_start_delay;

    public void ObjectDiscrim() {
        od_start_delay = 500;
        od_duration_off = sharedPrefs.getInt(r.getString(R.string.preftag_od_duration_off), r.getInteger(R.integer.default_od_duration_off));
        od_duration_on = sharedPrefs.getInt(r.getString(R.string.preftag_od_duration_on), r.getInteger(R.integer.default_od_duration_on));
        od_num_distractors = sharedPrefs.getInt(r.getString(R.string.preftag_od_num_distractors), r.getInteger(R.integer.default_od_num_distractors));
        od_num_stim = 1;
    }

    public int rdm_num_dots, rdm_dot_size, rdm_coherence_min, rdm_coherence_max, rdm_movement_distance_min, rdm_movement_distance_max,
            rdm_movie_length, rdm_choice_delay, rdm_colour_bg, rdm_colour_dots, rdm_colour_choice;
    public boolean rdm_horizontal_layout;

    public void RandomDotMotion() {
        rdm_num_dots = sharedPrefs.getInt(r.getString(R.string.preftag_rdm_num_dots), r.getInteger(R.integer.default_rdm_num_dots));
        rdm_dot_size = sharedPrefs.getInt(r.getString(R.string.preftag_rdm_dot_size), r.getInteger(R.integer.default_rdm_dot_size));
        rdm_coherence_min = sharedPrefs.getInt(r.getString(R.string.preftag_rdm_coherence_min), r.getInteger(R.integer.default_rdm_coherence_min));
        rdm_coherence_max = sharedPrefs.getInt(r.getString(R.string.preftag_rdm_coherence_max), r.getInteger(R.integer.default_rdm_coherence_max));
        rdm_movement_distance_min = sharedPrefs.getInt(r.getString(R.string.preftag_rdm_dist_min), r.getInteger(R.integer.default_rdm_dist_min));
        rdm_movement_distance_max = sharedPrefs.getInt(r.getString(R.string.preftag_rdm_dist_max), r.getInteger(R.integer.default_rdm_dist_max));
        rdm_movie_length = sharedPrefs.getInt(r.getString(R.string.preftag_rdm_movie_length), r.getInteger(R.integer.default_rdm_movie_length));
        rdm_choice_delay = sharedPrefs.getInt(r.getString(R.string.preftag_rdm_choice_delay), r.getInteger(R.integer.default_rdm_choice_delay));
        rdm_horizontal_layout = sharedPrefs.getBoolean(r.getString(R.string.preftag_rdm_horizontal), r.getBoolean(R.bool.default_rdm_horizontal));
        int bg_colour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_rdm_colour_bg), Integer.toString(r.getInteger(R.integer.default_rdm_color_bg))));
        rdm_colour_bg = colors[bg_colour];
        int dot_colour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_rdm_colour_dots), Integer.toString(r.getInteger(R.integer.default_rdm_color_dots))));
        rdm_colour_dots = colors[dot_colour];
        int choice_colour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_rdm_colour_choice), Integer.toString(r.getInteger(R.integer.default_rdm_color_choice))));
        rdm_colour_choice = colors[choice_colour];
    }
    public static int csl_col_context_1, csl_col_context_2;
    public static int csl_choice_col_i, csl_choice_col_a;

    public static int csl_tone_delay, csl_pair_tone_delay, csl_rtBase;
    public static int csl_waitcuex, csl_waitcuey, csl_presscuex, csl_presscuey;

    public void ContextSequenceLearning() {
        handle_feedback = false;
        colors = r.getIntArray(R.array.colorarray);
        // this is stupid because it's hardcoded to be the the color in the current array set. will work fine if subsequent colors are only appended at the end
        // TODO: Implement this in preferences
        csl_col_context_1 = colors[9];
        csl_col_context_2 = colors[16];

        csl_choice_col_i = colors[0]; // inactive while listening to tone
        csl_choice_col_a = colors[12]; // active, subject can press cue

        csl_tone_delay = sharedPrefs.getInt(r.getString(R.string.preftag_csl_tone_delay), r.getInteger(R.integer.default_csl_tone_delay));
        csl_pair_tone_delay = sharedPrefs.getInt(r.getString(R.string.preftag_csl_pair_tone_delay), r.getInteger(R.integer.default_csl_pair_tone_delay));
        csl_rtBase = sharedPrefs.getInt(r.getString(R.string.preftag_csl_rtbase), r.getInteger(R.integer.default_csl_rtbase));

        csl_waitcuex = sharedPrefs.getInt(r.getString(R.string.preftag_csl_waitcuex), r.getInteger(R.integer.default_csl_waitcuex));
        csl_waitcuey = sharedPrefs.getInt(r.getString(R.string.preftag_csl_waitcuey), r.getInteger(R.integer.default_csl_waitcuey));
        csl_presscuex = sharedPrefs.getInt(r.getString(R.string.preftag_csl_presscuex), r.getInteger(R.integer.default_csl_presscuex));
        csl_presscuey = sharedPrefs.getInt(r.getString(R.string.preftag_csl_presscuey), r.getInteger(R.integer.default_csl_presscuey));

    }

    public int w_gocuesize, w_choicecuesize, w_rewcuesize, w_numcues, w_numcuesused;
    public int w_choicecuey1, w_choicecuey2, w_choicecuex1, w_choicecuex2;
    public int w_startdelay, w_probcuesdelay_low, w_probcuesdelay_high;
    public int w_probcuexloc1, w_probcueyloc1, w_probcuexloc2, w_probcueyloc2;
    public int w_fixcol, w_chcol1, w_chcol2;
    public boolean w_randposchoicecues;

    public void Walds() {
        w_numcues = sharedPrefs.getInt(r.getString(R.string.preftag_w_numcues), r.getInteger(R.integer.default_w_numcues));
        w_numcuesused = sharedPrefs.getInt(r.getString(R.string.preftag_w_numcuesused), r.getInteger(R.integer.default_w_numcuesused));
        w_startdelay = sharedPrefs.getInt(r.getString(R.string.preftag_w_startdelay), r.getInteger(R.integer.default_w_startdelay));
        w_probcuesdelay_low = sharedPrefs.getInt(r.getString(R.string.preftag_w_probcuesdelay_low), r.getInteger(R.integer.default_w_probcuesdelay_low));
        w_probcuesdelay_high = sharedPrefs.getInt(r.getString(R.string.preftag_w_probcuesdelay_high), r.getInteger(R.integer.default_w_probcuesdelay_high));
        w_gocuesize = sharedPrefs.getInt(r.getString(R.string.preftag_w_gocuesize), r.getInteger(R.integer.default_w_gocuesize));
        w_choicecuesize = sharedPrefs.getInt(r.getString(R.string.preftag_w_choicecuesize), r.getInteger(R.integer.default_w_choicecuesize));
        w_rewcuesize = sharedPrefs.getInt(r.getString(R.string.preftag_w_rewcuesize), r.getInteger(R.integer.default_w_rewcuesize));
        w_probcuexloc1 = sharedPrefs.getInt(r.getString(R.string.preftag_w_probcuexloc1), r.getInteger(R.integer.default_w_probcuexloc1));
        w_probcueyloc1 = sharedPrefs.getInt(r.getString(R.string.preftag_w_probcueyloc1), r.getInteger(R.integer.default_w_probcueyloc1));
        w_probcuexloc2 = sharedPrefs.getInt(r.getString(R.string.preftag_w_probcuexloc2), r.getInteger(R.integer.default_w_probcuexloc2));
        w_probcueyloc2 = sharedPrefs.getInt(r.getString(R.string.preftag_w_probcueyloc2), r.getInteger(R.integer.default_w_probcueyloc2));
        w_choicecuey1 = sharedPrefs.getInt(r.getString(R.string.preftag_w_choicecuey1), r.getInteger(R.integer.default_w_choicecuey1));
        w_choicecuey2 = sharedPrefs.getInt(r.getString(R.string.preftag_w_choicecuey2), r.getInteger(R.integer.default_w_choicecuey2));
        w_choicecuex1 = sharedPrefs.getInt(r.getString(R.string.preftag_w_choicecuex1), r.getInteger(R.integer.default_w_choicecuex1));
        w_choicecuex2 = sharedPrefs.getInt(r.getString(R.string.preftag_w_choicecuex2), r.getInteger(R.integer.default_w_choicecuex2));
        w_fixcol = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_w_colfix), Integer.toString(r.getInteger(R.integer.default_w_colfix))));
        w_fixcol = colors[w_fixcol];
        w_chcol1 = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_w_colchone), Integer.toString(r.getInteger(R.integer.default_w_colchone))));
        w_chcol1 = colors[w_chcol1];
        w_chcol2 = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_w_colchtwo), Integer.toString(r.getInteger(R.integer.default_w_colchtwo))));
        w_chcol2 = colors[w_chcol2];
        w_randposchoicecues = sharedPrefs.getBoolean(r.getString(R.string.preftag_w_randposchoicecues), r.getBoolean(R.bool.default_w_randposchoicecues));


    }


    public static int colgrat_green_cue, colgrat_red_cue, colgrat_blue_cue, colgrat_target_cue, colgrat_fix_cue;
    public static int colgrat_sizecolcue, colgrat_sizefixcue, colgrat_sizeindicatorcue;
    public static int colgrat_sizestripes, colgrat_grateoffset, colgrat_numstripes;
    public static int colgrat_red_x, colgrat_red_y, colgrat_green_x, colgrat_green_y, colgrat_blue_x, colgrat_blue_y;
    public static int colgrat_rt_limit, colgrat_catchtrialfreq;
    public static int colgrat_fixation_time_mi, colgrat_stimulus_time_mi, colgrat_cue_time_mi, colgrat_dim_time_mi;
    public static int colgrat_fixation_time_ma, colgrat_stimulus_time_ma, colgrat_cue_time_ma, colgrat_dim_time_ma;

    public static int colgrat_position_rand;
    public static int colgrat_background_color, colgrat_target_shape;
    public static float colgrat_start_dim, colgrat_end_dim;
    public static boolean colgrat_trainingmode;

    public void ColoredGrating() {

        colgrat_trainingmode = sharedPrefs.getBoolean(r.getString(R.string.preftag_colgrat_trainingmode), r.getBoolean(R.bool.default_colgrat_trainingmode));

        colors = r.getIntArray(R.array.colorarray); // not best because it directly takes from the color array

        colgrat_green_cue = colors[14];
        colgrat_red_cue  = colors[6];
        colgrat_blue_cue = colors[3];
        colgrat_target_cue = colors[6]; // not super important what color it is, just a placeholder for now
        colgrat_fix_cue  = colors[0]; // not super important what color it is, just a placeholder for now
        colgrat_background_color = colors[2];

        colgrat_red_x = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_red_x), r.getInteger(R.integer.default_colgrat_red_x));
        colgrat_red_y = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_red_y), r.getInteger(R.integer.default_colgrat_red_y));
        colgrat_green_x = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_green_x), r.getInteger(R.integer.default_colgrat_green_x));
        colgrat_green_y = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_green_y), r.getInteger(R.integer.default_colgrat_green_y));
        colgrat_blue_x = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_blue_x), r.getInteger(R.integer.default_colgrat_blue_x));
        colgrat_blue_y = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_blue_y), r.getInteger(R.integer.default_colgrat_blue_y));

        colgrat_sizecolcue = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_sizecolcue), r.getInteger(R.integer.default_colgrat_sizecolcue));
        colgrat_sizefixcue = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_sizefixcue), r.getInteger(R.integer.default_colgrat_sizefixcue));
        colgrat_sizeindicatorcue = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_sizeindicatorcue), r.getInteger(R.integer.default_colgrat_sizeindicatorcue));

        colgrat_sizestripes = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_sizestripes), r.getInteger(R.integer.default_colgrat_sizestripes));
        colgrat_grateoffset = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_grateoffset), r.getInteger(R.integer.default_colgrat_grateoffset));
        colgrat_numstripes = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_numstripes), r.getInteger(R.integer.default_colgrat_numstripes));

        // are blue/red/green always in the same position?
        colgrat_position_rand = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_position_rand), r.getInteger(R.integer.default_colgrat_position_rand));

        colgrat_start_dim = (float) sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_start_dim), r.getInteger(R.integer.default_colgrat_start_dim)) / 100;
        colgrat_end_dim   = (float) sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_end_dim),   r.getInteger(R.integer.default_colgrat_end_dim)) / 100;

        colgrat_rt_limit      = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_rt_limit), r.getInteger(R.integer.default_colgrat_rt_limit));
        colgrat_catchtrialfreq      = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_catchtrialfreq), r.getInteger(R.integer.default_colgrat_catchtrialfreq));

        colgrat_fixation_time_mi = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_fixation_time_mi), r.getInteger(R.integer.default_colgrat_fixation_time_mi));
        colgrat_stimulus_time_mi = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_stimulus_time_mi), r.getInteger(R.integer.default_colgrat_stimulus_time_mi));
        colgrat_cue_time_mi      = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_cue_time_mi), r.getInteger(R.integer.default_colgrat_cue_time_mi));
        colgrat_dim_time_mi      = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_dim_time_mi), r.getInteger(R.integer.default_colgrat_dim_time_mi));

        colgrat_fixation_time_ma = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_fixation_time_ma), r.getInteger(R.integer.default_colgrat_fixation_time_ma));
        colgrat_stimulus_time_ma = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_stimulus_time_ma), r.getInteger(R.integer.default_colgrat_stimulus_time_ma));
        colgrat_cue_time_ma      = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_cue_time_ma), r.getInteger(R.integer.default_colgrat_cue_time_ma));
        colgrat_dim_time_ma      = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_dim_time_ma), r.getInteger(R.integer.default_colgrat_dim_time_ma));

        colgrat_target_shape = sharedPrefs.getInt(r.getString(R.string.preftag_colgrat_target_shape), r.getInteger(R.integer.default_colgrat_target_shape));

    }
}

