package com.example.jbutler.mymou;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import androidx.preference.PreferenceManager;

import java.util.Random;

public class UtilsSystem {
   // Debug
    public static String TAG = "MymouUtilsSystem";

    public static void setOnClickListenerLoop(Button[] buttons, View.OnClickListener view) {
        for (Button button: buttons) {
            button.setOnClickListener(view);
        }
    }



}
