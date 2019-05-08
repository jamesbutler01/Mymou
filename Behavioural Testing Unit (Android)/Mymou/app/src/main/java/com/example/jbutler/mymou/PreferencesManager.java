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

    public PreferencesManager(Context context) {
        // Get sharedpreferences
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(context);

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

        int[] colors = context.getResources().getIntArray(R.array.colorarray);
        taskbackground = colors[taskbackgroundcolour];
        rewardbackground = colors[rewardbackgroundcolour];
        timeoutbackground = colors[timeoutbackgroundcolour];

        ec_correct_trial = sharedPrefs.getString("eventcode_correct_trial", context.getResources().getString(R.string.default_eventcode_correct_trial));
        ec_incorrect_trial = sharedPrefs.getString("eventcode_error_trial", context.getResources().getString(R.string.default_eventcode_error_trial));
        ec_trial_timeout = sharedPrefs.getString("eventcode_timeout_trial", context.getResources().getString(R.string.default_eventcode_timeout_trial));
        ec_wrong_gocue_pressed = sharedPrefs.getString("eventcode_wrong_gocue", context.getResources().getString(R.string.default_eventcode_wrong_gocue));
    }
    
}
