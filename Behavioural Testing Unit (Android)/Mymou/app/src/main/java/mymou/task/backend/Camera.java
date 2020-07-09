package mymou.task.backend;

import androidx.fragment.app.Fragment;

import mymou.preferences.PreferencesManager;
import mymou.task.backend.TaskInterface;

/**
 * Parent Camera Fragment
 *
 * All cameras must inherit from this Fragment
 * Enables communiation with TaskManager parent
 *
 */
public abstract class Camera extends Fragment {

    public boolean camera_error;

    public abstract void setFragInterfaceListener(CameraInterface callback);
    public abstract boolean captureStillPicture(String ts);

}
