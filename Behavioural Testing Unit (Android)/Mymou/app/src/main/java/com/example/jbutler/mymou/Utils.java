package com.example.jbutler.mymou;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

import java.util.List;

public class Utils extends Activity {

    // Make a predetermined list of the locations on the screen where cues can be placed
    private Object getPossibleCueLocs() {
        int imageWidths = 175 + 175/2;
        int border = 30;  // Spacing between different task objects
        int totalImageSize = imageWidths + border;

        // Find centre of screen in pixels
        Display display =  getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int xCenter = screenWidth / 2;
        int screenHeight = size.y;

        // Find how many images will fill on the screen
        int[] xlocs = calculateLocs(totalImageSize, screenWidth);
        int[] ylocs = calculateLocs(totalImageSize, screenHeight);

        return new Object[]{xlocs, ylocs};

    }

    private int[] calculateLocs(int totalLength, int totalImageSize) {
        int num_locs = totalLength / totalImageSize; // floor division

        int[] locs = new int[num_locs];

        int offset = totalLength - (num_locs * totalImageSize);  // Centre images on screen
        offset = offset / 2;

        for (int i = 0; i < num_locs; i++) {
            locs[i] = offset + i * totalImageSize;
        }

        return locs;

    }


}
