package mymou.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import androidx.preference.PreferenceManager;
import mymou.R;
import mymou.Utils.UtilsSystem;

import java.util.Arrays;

public class PreferencesManager {
    private String TAG = "MyMouPreferencesManager";

    public String base_error_message = "Error: Invalid settings configured so task cannot run. Please adjust settings and restart the task:\n\n";

    public static boolean debug, bluetooth, camera, facerecog, restartoncrash, sound, autostart, autostop;
    public static int sound_to_play;
    public static int num_reward_chans, default_rew_chan;
    public static int rewardduration, responseduration, timeoutduration;
    public static int autostart_hour, autostop_hour, autostart_min, autostop_min;
    public static int taskbackground, rewardbackground, timeoutbackground;
    public static int border_colour, border_size, cue_size, cue_spacing;
    public static int num_monkeys;
    public static String ec_correct_trial, ec_incorrect_trial, ec_trial_timeout, ec_wrong_gocue_pressed;
    public static int[] colours_gocues;

    private SharedPreferences sharedPrefs;
    private int[] colors;
    private Resources r;
    private Context mContext;

    public PreferencesManager(Context context) {
        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        r = context.getResources();

        debug = sharedPrefs.getBoolean(r.getString(R.string.preftag_debug), r.getBoolean(R.bool.default_debug));
        bluetooth = sharedPrefs.getBoolean(r.getString(R.string.preftag_bluetooth), r.getBoolean(R.bool.default_bluetooth));
        camera = sharedPrefs.getBoolean(r.getString(R.string.preftag_camera), r.getBoolean(R.bool.default_camera));
        facerecog = sharedPrefs.getBoolean(r.getString(R.string.preftag_facerecog), r.getBoolean(R.bool.default_facerecog));
        restartoncrash = sharedPrefs.getBoolean(r.getString(R.string.preftag_restartoncrash), r.getBoolean(R.bool.default_restartoncrash));
        sound = sharedPrefs.getBoolean(r.getString(R.string.preftag_sound), r.getBoolean(R.bool.default_sound));
        autostart = sharedPrefs.getBoolean(r.getString(R.string.preftag_autostart), r.getBoolean(R.bool.default_autostart));
        autostop = sharedPrefs.getBoolean(r.getString(R.string.preftag_autostop), r.getBoolean(R.bool.default_autostop));

        sound_to_play = sharedPrefs.getInt(r.getString(R.string.preftag_sound_to_play), 0);

        num_reward_chans = sharedPrefs.getInt(r.getString(R.string.preftag_num_rew_chans), r.getInteger(R.integer.default_num_rew_chans));
        default_rew_chan = sharedPrefs.getInt(r.getString(R.string.preftag_default_rew_chan), r.getInteger(R.integer.default_rew_chan));
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
        int bordercolour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_cuebordercolors),Integer.toString(r.getInteger(R.integer.default_bordercolour))));

        colors = r.getIntArray(R.array.colorarray);
        taskbackground = colors[taskbackgroundcolour];
        rewardbackground = colors[rewardbackgroundcolour];
        timeoutbackground = colors[timeoutbackgroundcolour];
        border_colour = colors[bordercolour];

        // Cue colour settings
        String tag = context.getString(R.string.preftag_gocuecolors);
        int[] gocue_colors = UtilsSystem.loadIntArray(tag, sharedPrefs, context);
        colours_gocues = new int[8];
        int i_monk=0;
        for (int i=0; i<gocue_colors.length; i++) {
            if (gocue_colors[i] == 1) {
                colours_gocues[i_monk] = colors[i];
                i_monk += 1;
            }
        }

        ec_correct_trial = sharedPrefs.getString(r.getString(R.string.preftag_eventcode_correct_trial), r.getString(R.string.default_eventcode_correct_trial));
        ec_incorrect_trial = sharedPrefs.getString(r.getString(R.string.preftag_eventcode_error_trial), r.getString(R.string.default_eventcode_error_trial));
        ec_trial_timeout = sharedPrefs.getString(r.getString(R.string.preftag_eventcode_timeout_trial), r.getString(R.string.default_eventcode_timeout_trial));
        ec_wrong_gocue_pressed = sharedPrefs.getString(r.getString(R.string.preftag_eventcode_wrong_gocue), r.getString(R.string.default_eventcode_wrong_gocue));

    }

    public int objectdiscrim_num_corr_options, objectdiscrim_num_incorr_options, objectdiscrim_num_corr_shown, objectdiscrim_num_incorr_shown, objectdiscrim_num_steps;
    public int[] objectdiscrim_corr_colours, objectdiscrim_incorr_colours;
    public int[] objectdiscrim_prev_cols_corr, objectdiscrim_prev_cols_incorr;
    public boolean objectdiscrim_repeatOnError, objectdiscrim_previous_error, objectdiscrim_valid_config;
    public String objectdiscrim_errormessage;

    public void ObjectDiscrimination() {
        objectdiscrim_num_corr_shown = sharedPrefs.getInt(r.getString(R.string.preftag_od_num_corr_cues), r.getInteger(R.integer.default_objdisc_num_corr_shown));
        objectdiscrim_num_incorr_shown = sharedPrefs.getInt(r.getString(R.string.preftag_od_num_incorr_cues), r.getInteger(R.integer.default_objdisc_num_incorr_shown));
        objectdiscrim_num_steps = sharedPrefs.getInt(r.getString(R.string.preftag_od_num_steps), r.getInteger(R.integer.default_objdisc_num_steps));
        objectdiscrim_repeatOnError = sharedPrefs.getBoolean(r.getString(R.string.preftag_od_repeat_error), r.getBoolean(R.bool.default_objdisc_repeaterror));

        // Colours
        int max_cues = 15;
        objectdiscrim_corr_colours = new int[max_cues];
        objectdiscrim_incorr_colours = new int[max_cues];

        int[] chosen_cols = UtilsSystem.loadIntArray(r.getString(R.string.preftag_od_corr_cols), sharedPrefs, mContext);
        int num_corr_options=0;
        for (int i=0; i<chosen_cols.length; i++) {
            if (chosen_cols[i] == 1) {
                objectdiscrim_corr_colours[num_corr_options] = colors[i];
                num_corr_options += 1;
            }
        }
        objectdiscrim_num_corr_options = num_corr_options;
        objectdiscrim_corr_colours = Arrays.copyOf(objectdiscrim_corr_colours, objectdiscrim_num_corr_options);


        chosen_cols = UtilsSystem.loadIntArray(r.getString(R.string.preftag_od_incorr_cols), sharedPrefs, mContext);
        int num_incorr_options=0;
        for (int i=0; i<chosen_cols.length; i++) {
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
            objectdiscrim_errormessage += ""+num_incorr_options+" incorrect cue(s) to display each trial, but have only selected "+num_incorr_options+" different incorrect cue(s) in total";
        } else if (objectdiscrim_num_corr_options < objectdiscrim_num_corr_shown) {
            objectdiscrim_errormessage += ""+num_corr_options+" correct cue(s) to displayeach trial, but have only selected "+num_corr_options+" different correct cue(s) in total";
        } else {
            objectdiscrim_valid_config = true;
        }

    }

    public boolean dm_repeatOnError;
    public int dm_min_start_distance, dm_max_start_distance, dm_max_dist_in_map, dm_map_selected, dm_num_maps;

    public void DiscreteMaze() {
        dm_repeatOnError = true;
        dm_map_selected = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_dm_map), Integer.toString(r.getInteger(R.integer.default_dm_map))));
        dm_min_start_distance = sharedPrefs.getInt(r.getString(R.string.preftag_dm_min_start_distance), r.getInteger(R.integer.default_dm_min_start_distance));
        dm_max_start_distance = sharedPrefs.getInt(r.getString(R.string.preftag_dm_max_start_distance), r.getInteger(R.integer.default_dm_max_start_distance));
        dm_max_dist_in_map = 6;
        dm_num_maps = 2;
    }
    
}
