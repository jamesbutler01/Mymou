package mymou;

import android.app.Application;

import com.jiangdg.usbcamera.UVCCameraHelper;
import mymou.CrashHandler;

/**application class
 *
 * Created by jianddongguo on 2017/7/20.
 */

public class MainActivity extends Application {

    private CrashHandler mCrashHandler;

    // File Directory in sd card
    public static final String DIRECTORY_NAME = "USBCamera";

    @Override
    public void onCreate() {
        super.onCreate();
//        mCrashHandler = CrashHandler.getInstance();
//        mCrashHandler.init(getApplicationContext(), getClass());
    }
}
