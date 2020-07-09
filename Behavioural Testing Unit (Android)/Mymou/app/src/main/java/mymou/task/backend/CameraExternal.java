package mymou.task.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import mymou.R;

import static android.os.Looper.getMainLooper;

/**
 * USB camera module
 *
 * Adapted from "jiangdongguo on 2017/9/30."
 * Uses libusbcamera library
 *
 * jb 20200709
 */

public class CameraExternal extends Camera implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback {

    private static final String TAG = "CameraExternal";
    private static UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;

    private static String timestamp;
    private int width, height;
    private static boolean takingPhoto = false;
    private static Context mContext;
    private boolean isRequest;
    private boolean isPreview;
    public List<String> resolutions;

    // Error handling
    public static boolean camera_error = false;

    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            // request open permission
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    mCameraHelper.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                showShortMsg(device.getDeviceName() + " unplugged");
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                showShortMsg("Failed to connect");
                isPreview = false;
                camera_error = true;
            } else {
                isPreview = true;
                showShortMsg("Connecting to USB camera");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getView().findViewById(R.id.tvInsertUsbCam).setVisibility(View.INVISIBLE);
                    }
                });

                // Wait for UVCCamera to initialize
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Looper.prepare();
                        if(mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                            showShortMsg("Connected to USB camera");
                            getResolutionList();
                            if(width != -1) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mCameraHelper.updateResolution(width, height);
                                    }
                                });
                            }
                            callback.CameraLoaded();
                        }
                        Looper.loop();
                    }
                }).start();
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            showShortMsg("disconnecting");
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_usbcamera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "initialising ");
        mUVCCameraView = (CameraViewInterface) getView().findViewById(R.id.camera_view);

        // See if they have specified a resolution, and use it if they have
        SharedPreferences preferences =  PreferenceManager.getDefaultSharedPreferences(mContext);
        width = preferences.getInt(mContext.getResources().getString(R.string.preftag_camera_resolution_ext_width), -1);
        height = preferences.getInt(mContext.getResources().getString(R.string.preftag_camera_resolution_ext_height), -1);
        View cameraView = getView().findViewById(R.id.camera_view);

        if (width != -1) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cameraView.getLayoutParams();
            params.height = height;
            params.width = width;
            cameraView.setLayoutParams(params);
        }

        // Move view off screen if in task mode
        if (getArguments() != null && getArguments().getBoolean(getContext().getResources().getString(R.string.task_mode), false)) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cameraView.getLayoutParams();
            cameraView.setX(3000);
            cameraView.setY(3000);
            cameraView.setLayoutParams(params);
        }
        mUVCCameraView.setCallback(this);

        // Instantiate camera
        mCameraHelper = UVCCameraHelper.getInstance();

        // Activate USB listening
        mCameraHelper.initUSBMonitor(getActivity(), mUVCCameraView, listener);

        // Set up preview components
        mCameraHelper.setOnPreviewFrameListener(new AbstractUVCCameraHandler.OnPreViewResultListener() {
            @Override
            public void onPreviewResult(byte[] nv21Yuv) {
                Log.d(TAG, "onPreviewResult: "+nv21Yuv.length);
            }
        });

    }

    @Override
    public boolean captureStillPicture(String ts) {
        return captureStillPictureStatic(ts);
    }

    // Say cheese
    public static boolean captureStillPictureStatic(String ts) {
        Log.d(TAG, "Capture request started at" + ts);
        // If the camera is still in process of taking previous picture it will not take another one
        // If it took multiple photos the timestamp for saving/indexing the photos would be wrong
        // Tasks need to handle this behaviour
        // Perform action on click

        if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
            camera_error = true;
            return false;
        }

        if (takingPhoto) {
            return false;
        }

        // Update timestamp string, which will be used to save the photo once the photo is ready
        takingPhoto = true;
        timestamp = ts;
        CameraSavePhoto cameraSavePhoto = new CameraSavePhoto(timestamp, mContext);

        mCameraHelper.capturePicture(cameraSavePhoto.photoFile.getPath(), new AbstractUVCCameraHandler.OnCaptureListener() {
            @Override
            public void onCaptureResult(String path) {
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        takingPhoto = false;
                        Log.d(TAG, "Photo saved..");
                    }
                });
            }
        });

        return true;

    }

    @Override
    public void onStart() {
        super.onStart();
        // step.2 register USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // step.3 unregister USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
    }

    private void getResolutionList() {
        List<Size> list = mCameraHelper.getSupportedPreviewSizes();
        if (list != null && list.size() != 0) {
            resolutions = new ArrayList<>();
            for (Size size : list) {
                if (size != null) {
                    resolutions.add(size.width + "x" + size.height);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FileUtils.releaseFile();
        // step.4 release uvc camera resources
        if (mCameraHelper != null) {
            mCameraHelper.release();
        }
    }

    private static void showShortMsg(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            showShortMsg("Please enable USB permission");
        }
    }

    public boolean isCameraOpened() {
        return mCameraHelper.isCameraOpened();
    }

    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        if (!isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.startPreview(mUVCCameraView);
            isPreview = true;
            Log.d(TAG, "Preview start");
        }
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        Log.d(TAG, "Surface destroyed!");
        if (isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.stopPreview();
            isPreview = false;
            Log.d(TAG, "Preview stopped");
        }
    }

    // Add callback to enable parent activity to react when camera is loaded
    CameraInterface callback;
    public void setFragInterfaceListener(CameraInterface callback) {
        this.callback = callback;
    }

}
