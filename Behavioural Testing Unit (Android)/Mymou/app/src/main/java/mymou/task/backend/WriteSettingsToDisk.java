package mymou.task.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.text.SimpleDateFormat;

import mymou.R;
import mymou.Utils.FolderManager;
import mymou.preferences.PreferencesManager;

public class WriteSettingsToDisk implements Runnable {

    private PreferencesManager preferencesManager;
    private String fileName = "config.txt";
    private int taskId;

    private static String TAG = "MymouWriteSettingsToDisk";

    public WriteSettingsToDisk(PreferencesManager preferencesManagerIn, int taskIdIn) {
        preferencesManager = preferencesManagerIn;
        taskId = taskIdIn;
    }

    @Override
    public void run() {

        Log.d(TAG, "Writing settings to disk");
        String ts = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(System.currentTimeMillis());

        String[] taskNames = preferencesManager.r.getStringArray(R.array.available_tasks);

        new WriteDataToFile("Settings used for task '"+taskNames[taskId]+"' (loaded at "+ts+")", preferencesManager.mContext, fileName).run();
        Map<String, ?> allPrefs = preferencesManager.sharedPrefs.getAll(); //your sharedPreference
        Set<String> set = allPrefs.keySet();
        for(String s : set){
            String ss = s + " = "+ allPrefs.get(s).toString();
            Log.d(TAG, ss);
            new WriteDataToFile(ss, preferencesManager.mContext, fileName).run();
        }

        Log.d(TAG, "Finished writing settings to disk");


    }

}
