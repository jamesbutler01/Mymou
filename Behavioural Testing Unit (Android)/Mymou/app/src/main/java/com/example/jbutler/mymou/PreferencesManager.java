package com.example.jbutler.mymou;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.preference.PreferenceManager;

public class PreferencesManager {
    private String TAG = "MyMouPreferencesManager";

    public static boolean bluetooth, camera, facerecog, restartoncrash, sound, autostart, autostop;
    public static int rewardduration, responseduration, timeoutduration, startuptime, shutdowntime;
    public static int taskbackground, rewardbackground, timeoutbackground;
    public static int num_monkeys;
    public static String ec_correct_trial, ec_incorrect_trial, ec_trial_timeout, ec_wrong_gocue_pressed;
    public static int[] colours_gocues;
    private SharedPreferences sharedPrefs;
    private int[] colors;
    private Context mContext;

    public PreferencesManager(Context context) {
        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        bluetooth = sharedPrefs.getBoolean("bluetooth", context.getResources().getBoolean(R.bool.default_bluetooth));
        camera = sharedPrefs.getBoolean("camera", context.getResources().getBoolean(R.bool.default_camera));
        facerecog = sharedPrefs.getBoolean("facerecog", context.getResources().getBoolean(R.bool.default_facerecog));
        restartoncrash = sharedPrefs.getBoolean("restartoncrash", context.getResources().getBoolean(R.bool.default_restartoncrash));
        sound = sharedPrefs.getBoolean("sound", context.getResources().getBoolean(R.bool.default_sound));
        autostart = sharedPrefs.getBoolean("autostart", context.getResources().getBoolean(R.bool.default_autostart));
        autostop = sharedPrefs.getBoolean("autostop", context.getResources().getBoolean(R.bool.default_autostop));

        rewardduration = Integer.valueOf(sharedPrefs.getString("rewardduration", context.getResources().getString(R.string.default_rewardduration)));
        responseduration = Integer.valueOf(sharedPrefs.getString("responseduration", context.getResources().getString(R.string.default_responseduration)));
        timeoutduration = Integer.valueOf(sharedPrefs.getString("timeoutduration", context.getResources().getString(R.string.default_timeoutduration)));
        startuptime = sharedPrefs.getInt("startuptime", context.getResources().getInteger(R.integer.default_startuptime));
        shutdowntime = sharedPrefs.getInt("shutdowntime", context.getResources().getInteger(R.integer.default_shutdowntime));

        int taskbackgroundcolour = Integer.valueOf(sharedPrefs.getString("taskbackgroundcolour", context.getResources().getString(R.string.default_taskbackgroundcolour)));
        int rewardbackgroundcolour = Integer.valueOf(sharedPrefs.getString("rewardbackgroundcolour", context.getResources().getString(R.string.default_rewardbackgroundcolour)));
        int timeoutbackgroundcolour = Integer.valueOf(sharedPrefs.getString("timeoutbackgroundcolour", context.getResources().getString(R.string.default_timeoutbackgroundcolour)));

        num_monkeys = sharedPrefs.getInt("num_monkeys", context.getResources().getInteger(R.integer.default_num_monkeys));

        colors = context.getResources().getIntArray(R.array.colorarray);
        taskbackground = colors[taskbackgroundcolour];
        rewardbackground = colors[rewardbackgroundcolour];
        timeoutbackground = colors[timeoutbackgroundcolour];

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

        ec_correct_trial = sharedPrefs.getString("eventcode_correct_trial", context.getResources().getString(R.string.default_eventcode_correct_trial));
        ec_incorrect_trial = sharedPrefs.getString("eventcode_error_trial", context.getResources().getString(R.string.default_eventcode_error_trial));
        ec_trial_timeout = sharedPrefs.getString("eventcode_timeout_trial", context.getResources().getString(R.string.default_eventcode_timeout_trial));
        ec_wrong_gocue_pressed = sharedPrefs.getString("eventcode_wrong_gocue", context.getResources().getString(R.string.default_eventcode_wrong_gocue));

        String keyprefix = "two_";


    }

    public int objectdiscrim_num_corr, objectdiscrim_num_incorr, objectdiscrim_num_corr_shown, objectdiscrim_num_incorr_shown, objectdiscim_num_steps;
    public int[] objectdiscrim_corr_colours, objectdiscrim_incorr_colours;
    public boolean repeatOnError;

    public void ObjectDiscrimination() {
        String keyprefix = "two_";
        objectdiscrim_num_corr_shown = sharedPrefs.getInt(keyprefix+"num_corr_cues", 1);
        objectdiscrim_num_incorr_shown = sharedPrefs.getInt(keyprefix+"num_incorr_cues", 1);
        objectdiscim_num_steps = sharedPrefs.getInt(keyprefix+"num_steps", 1);
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
