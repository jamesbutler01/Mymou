package com.example.jbutler.mymou;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import androidx.preference.PreferenceManager;

import javax.mail.Quota;

public class PreferencesManager {
    private String TAG = "MyMouPreferencesManager";

    public static boolean bluetooth, camera, facerecog, restartoncrash, sound, autostart, autostop;
    public static int rewardduration, responseduration, timeoutduration, startuptime, shutdowntime;
    public static int taskbackground, rewardbackground, timeoutbackground;
    public static int border_colour, border_size, cue_size, cue_spacing;
    public static int num_monkeys;
    public static String ec_correct_trial, ec_incorrect_trial, ec_trial_timeout, ec_wrong_gocue_pressed;
    public static int[] colours_gocues;
    public static boolean valid_configuration = true;
    public static String error_message;

    private SharedPreferences sharedPrefs;
    private int[] colors;
    private Context mContext;


    public PreferencesManager(Context context) {
        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Resources r = context.getResources();
        
        bluetooth = sharedPrefs.getBoolean("bluetooth", r.getBoolean(R.bool.default_bluetooth));
        camera = sharedPrefs.getBoolean("camera", r.getBoolean(R.bool.default_camera));
        facerecog = sharedPrefs.getBoolean("facerecog", r.getBoolean(R.bool.default_facerecog));
        restartoncrash = sharedPrefs.getBoolean("restartoncrash", r.getBoolean(R.bool.default_restartoncrash));
        sound = sharedPrefs.getBoolean("sound", r.getBoolean(R.bool.default_sound));
        autostart = sharedPrefs.getBoolean("autostart", r.getBoolean(R.bool.default_autostart));
        autostop = sharedPrefs.getBoolean("autostop", r.getBoolean(R.bool.default_autostop));

        rewardduration = sharedPrefs.getInt("rewardduration", r.getInteger(R.integer.default_rewardduration));
        rewardduration = sharedPrefs.getInt("responseduration", r.getInteger(R.integer.default_responseduration));
        rewardduration = sharedPrefs.getInt("timeoutduration", r.getInteger(R.integer.default_timeoutduration));
        startuptime = sharedPrefs.getInt("startuptime", r.getInteger(R.integer.default_startuptime));
        shutdowntime = sharedPrefs.getInt("shutdowntime", r.getInteger(R.integer.default_shutdowntime));

        cue_size = sharedPrefs.getInt("cue_size", r.getInteger(R.integer.default_cuesize));
        cue_spacing = sharedPrefs.getInt("cue_spacing", r.getInteger(R.integer.default_cuespacing));
        border_size = sharedPrefs.getInt("cue_border_size", r.getInteger(R.integer.default_bordersize));
        num_monkeys = sharedPrefs.getInt("num_monkeys", r.getInteger(R.integer.default_num_monkeys));

        int taskbackgroundcolour = Integer.valueOf(sharedPrefs.getString("taskbackgroundcolour", Integer.toString(r.getInteger(R.integer.default_taskbackgroundcolour))));
        int rewardbackgroundcolour = Integer.valueOf(sharedPrefs.getString("rewardbackgroundcolour", Integer.toString(r.getInteger(R.integer.default_taskbackgroundcolour))));
        int timeoutbackgroundcolour = Integer.valueOf(sharedPrefs.getString("timeoutbackgroundcolour", Integer.toString(r.getInteger(R.integer.default_taskbackgroundcolour))));
        int bordercolour = Integer.valueOf(sharedPrefs.getString(r.getString(R.string.preftag_cuebordercolors),Integer.toString(r.getInteger(R.integer.default_bordercolour))));


        colors = r.getIntArray(R.array.colorarray);
        taskbackground = colors[taskbackgroundcolour];
        rewardbackground = colors[rewardbackgroundcolour];
        timeoutbackground = colors[timeoutbackgroundcolour];
        border_colour = colors[bordercolour];

        // Cue colour settings
        String tag = context.getString(R.string.preftag_gocuecolors);
        int[] gocue_colors = UtilsSystem.loadIntArray(tag, colors.length, sharedPrefs);
        colours_gocues = new int[8];
        int i_monk=0;
        for (int i=0; i<gocue_colors.length; i++) {
            if (gocue_colors[i] == 1) {
                colours_gocues[i_monk] = colors[i];
                i_monk += 1;
            }
        }

        ec_correct_trial = sharedPrefs.getString("eventcode_correct_trial", r.getString(R.string.default_eventcode_correct_trial));
        ec_incorrect_trial = sharedPrefs.getString("eventcode_error_trial", r.getString(R.string.default_eventcode_error_trial));
        ec_trial_timeout = sharedPrefs.getString("eventcode_timeout_trial", r.getString(R.string.default_eventcode_timeout_trial));
        ec_wrong_gocue_pressed = sharedPrefs.getString("eventcode_wrong_gocue", r.getString(R.string.default_eventcode_wrong_gocue));

    }

    public int objectdiscrim_num_corr, objectdiscrim_num_incorr, objectdiscrim_num_corr_shown, objectdiscrim_num_incorr_shown, objectdiscrim_num_steps;
    public int[] objectdiscrim_corr_colours, objectdiscrim_incorr_colours;
    public boolean repeatOnError;

    public void ObjectDiscrimination() {
        String keyprefix = "two_";
        objectdiscrim_num_corr_shown = sharedPrefs.getInt(keyprefix+"num_corr_cues", 1);
        objectdiscrim_num_incorr_shown = sharedPrefs.getInt(keyprefix+"num_incorr_cues", 1);
        objectdiscrim_num_steps = sharedPrefs.getInt(keyprefix+"num_steps", 1);
        repeatOnError = sharedPrefs.getBoolean(keyprefix+"repeat_error", false);

        int max_cues = 15;
        objectdiscrim_corr_colours = new int[max_cues];
        objectdiscrim_incorr_colours = new int[max_cues];

        int[] chosen_cols = UtilsSystem.loadIntArray(mContext.getResources().getString(R.string.preftag_task_objdisc_corr), max_cues, sharedPrefs);
        int i_chosen=0;
        for (int i=0; i<chosen_cols.length; i++) {
            if (chosen_cols[i] == 1) {
                objectdiscrim_corr_colours[i_chosen] = colors[i];
                i_chosen += 1;
            }
        }
        objectdiscrim_num_corr = i_chosen;

        chosen_cols = UtilsSystem.loadIntArray(mContext.getResources().getString(R.string.preftag_task_objdisc_incorr), max_cues, sharedPrefs);
        i_chosen=0;
        for (int i=0; i<chosen_cols.length; i++) {
            if (chosen_cols[i] == 1) {
                objectdiscrim_incorr_colours[i_chosen] = colors[i];
                i_chosen += 1;
            }
        }
        objectdiscrim_num_incorr = i_chosen;

    }
    
}
