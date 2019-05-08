package com.example.jbutler.mymou;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.ArrayMap;
import android.util.Log;
import androidx.preference.PreferenceManager;

public class PreferencesManager {
    private String TAG = "MyMouPreferencesManager";
    public static boolean bluetooth, camera, facerecog, restartoncrash, sound, autostart, autostop;
    public static int rewardduration, responseduration, timeoutduration, startuptime, shutdowntime;
    public static String taskbackground, rewardbackground, timeoutbackground;

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

        rewardduration = sharedPrefs.getInt("rewarduration", context.getResources().getInteger(R.integer.default_rewardduration));
        responseduration = sharedPrefs.getInt("rewarduration", context.getResources().getInteger(R.integer.default_responseduration));
        timeoutduration = sharedPrefs.getInt("rewarduration", context.getResources().getInteger(R.integer.default_timeoutduration));
        startuptime = sharedPrefs.getInt("rewarduration", context.getResources().getInteger(R.integer.default_startuptime));
        shutdowntime = sharedPrefs.getInt("rewarduration", context.getResources().getInteger(R.integer.default_shutdowntime));
    }
    
}
