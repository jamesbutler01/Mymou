package com.example.jbutler.mymou;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by jbutler on 04/07/2017.
 */

public class Permissions {

//        public static void main(String []args)throws Exception{
//            methodOne();
//        }

    public static void brightness(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
