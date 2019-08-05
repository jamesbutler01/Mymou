package mymou.task.individual_tasks;

import mymou.R;

public class TaskDiscreteMazeMaps {

    public int[] LoadImageList(int map) {

        int[] imageListMapOne = {
                R.drawable.aaaaa,
                R.drawable.aaaab,
                R.drawable.aaaac,
                R.drawable.aaaad,
                R.drawable.aaaae,
                R.drawable.aaaaf,
                R.drawable.aaaag,
                R.drawable.aaaah,
                R.drawable.aaaai,
                R.drawable.aaaaj,
        };

        int[] imageListMapTwo = {
                R.drawable.aabaa,
                R.drawable.aabab,
                R.drawable.aabac,
                R.drawable.aabad,
                R.drawable.aabae,
                R.drawable.aabaf,
                R.drawable.aabag,
                R.drawable.aabah,
                R.drawable.aabai,
                R.drawable.aabaj,
                R.drawable.aabak,
                R.drawable.aabal,
                R.drawable.aabam,
                R.drawable.aaban,
                R.drawable.aabao,
                R.drawable.aabap,};


        int[][] maps = {imageListMapOne, imageListMapTwo};

        return maps[map];
    }

}

