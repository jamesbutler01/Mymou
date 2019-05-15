package com.example.jbutler.mymou;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import androidx.preference.PreferenceManager;

import java.util.Random;
import java.util.StringTokenizer;

import static android.content.Context.MODE_PRIVATE;

public class UtilsSystem {
   // Debug
    public static String TAG = "MymouUtilsSystem";

    public static void setOnClickListenerLoop(Button[] buttons, View.OnClickListener view) {
        for (Button button : buttons) {
            button.setOnClickListener(view);
        }
    }

    public static String convertIntArrayToString(int[] list) {
        StringBuilder str = new StringBuilder();
        for (int s : list) {
            str.append(s).append(",");
        }
        return str.toString();
    }

    public static int[] loadIntArray(String tag, int n , SharedPreferences prefs) {
        String savedString = prefs.getString(tag, convertIntArrayToString(new int[n]));
        Log.d(TAG, "Loaded "+savedString+"from "+tag);
        StringTokenizer st = new StringTokenizer(savedString, ",");
        int[] savedList = new int[n];
        for (int i = 0; i < n; i++) {
            savedList[i] = Integer.parseInt(st.nextToken());
        }
        return savedList;
    }


}




