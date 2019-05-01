package com.example.jbutler.mymou;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.util.Pair;
import android.view.Display;

public class Utils {
       // Debug
    public static String TAG = "MymouUtils";

    // Make a predetermined list of the locations on the screen where cues can be placed
    public Point[] getPossibleCueLocs(Activity activity) {
        int imageWidths = 175 + 175/2;
        int border = 30;  // Spacing between different task objects
        int totalImageSize = imageWidths + border;

        // Find centre of screen in pixels
        Display display =  activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        // Find how many images will fill on the screen
        int[] xlocs = calculateLocs(screenWidth, totalImageSize);
        int[] ylocs = calculateLocs(screenHeight, totalImageSize);
        Point[] locs = new Point[xlocs.length * ylocs.length];
        int i_loc = 0;
        for (int i_x = 0; i_x < xlocs.length; i_x++) {
            for (int i_y = 0; i_y < ylocs.length; i_y++) {
                locs[i_loc] = new Point(xlocs[i_x], ylocs[i_y]);
                i_loc += 1;
            }
        }

        return locs;

    }

    private static int[] calculateLocs(int screenLength, int totalImageSize) {
        int num_locs = screenLength / totalImageSize; // floor division

        int[] locs = new int[num_locs];

        int offset = screenLength - (num_locs * totalImageSize);  // Centre images on screen
        offset = offset / 2;

        for (int i = 0; i < num_locs; i++) {
            locs[i] = offset + i * totalImageSize;
        }

        return locs;

    }


}
