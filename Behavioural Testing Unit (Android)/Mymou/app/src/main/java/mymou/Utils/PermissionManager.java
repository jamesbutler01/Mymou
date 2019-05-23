package mymou.Utils;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class PermissionManager {

    private static String TAG = "PermissionManager";
    private Context mContext;
    private Activity activity;

    public PermissionManager(Context context, Activity activity) {
        this.mContext = context;
        this.activity = activity;
    }

    String[] permissionCodes = {
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.WRITE_SETTINGS,
    };

    // Check if all permissions granted
    public boolean checkPermissions() {
        boolean permissionFlag = true;
        for (int i = 0; i < permissionCodes.length; i++){
            if(!checkPermissionNested(i)) {
                permissionFlag = false;
                break;
            }
        }
        return permissionFlag;
    }

    private boolean checkPermissionNested(int i_perm) {
        final String permissionItem = permissionCodes[i_perm];
        int hasPermission= PackageManager.PERMISSION_DENIED;
        if (i_perm<5) {
            hasPermission = activity.checkSelfPermission(permissionItem);
        } else {
            if (Settings.System.canWrite(mContext)) {
                hasPermission = PackageManager.PERMISSION_GRANTED;
            }
        }
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mContext, "All permissions must be enabled before task can run", Toast.LENGTH_LONG).show();
            requestPermission(i_perm);
            return false;
        } else {
            return true;
        }
    }

    private void requestPermission(int i_perm){
        if (i_perm==5) {  // This one is handled differently
            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } else {
            activity.requestPermissions(new String[] {permissionCodes[i_perm]},123);
        }

    }


}
