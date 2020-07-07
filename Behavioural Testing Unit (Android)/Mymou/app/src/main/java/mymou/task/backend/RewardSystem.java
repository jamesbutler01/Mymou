package mymou.task.backend;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import mymou.Utils.PermissionManager;
import mymou.preferences.PreferencesManager;
import mymou.R;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by jbutler on 06/12/2018.
 */

public class RewardSystem {

    private static String TAG = "MymouRewardSystem";

    public static boolean bluetoothConnection = false;
    public static String status = "Disabled";
    private static Handler connectionLoopHandler;
    private static boolean active = false;
    private static PreferencesManager preferencesManager;
    private static Context context;
    private static Activity activity;
    private static final int REQUEST_ENABLE_BT = 1;
    private static BluetoothAdapter btAdapter = null;
    private static BluetoothSocket btSocket = null;
    private static OutputStream outStream = null;

    // Replace with your devices UUID and address
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public RewardSystem(Context context_in, Activity activity_in) {

        context = context_in;
        activity = activity_in;

        initialiseRewardChannelStrings();

        this.listener = null; // set null listener

    }

    private static void registerBluetoothReceivers() {
            IntentFilter bluetoothIntent = new IntentFilter();
            bluetoothIntent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            bluetoothIntent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            context.registerReceiver(bluetoothReceiver, bluetoothIntent);
    }

    public static void connectToBluetooth() {

        if (new PreferencesManager(context).bluetooth) {  // Has user activated bluetooth in settings?
            if (new PermissionManager(context, activity).checkPermissions()) {  // Has user allowed permission to access the bt?
                if (checkBluetoothEnabled()) {  // Is the tablet's bluetooth enabled?
                    establishConnection();
                    return;
                }
            }
        }

        // Failed one of the checks
        bluetoothConnectedBool(false, "Disabled");

    }

    private static void log(String msg) {
        Log.d(TAG, msg);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    private static void establishConnection() {
        Toast.makeText(context, "Connecting to bluetooth..", Toast.LENGTH_SHORT).show();
        bluetoothConnectedBool(false, "Connecting..");

        // Get list of paired bluetooth devices
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        // Check device is paired with tablet
        if (pairedDevices.size() == 0) {
            log("No bluetooth devices paired");
            bluetoothConnectedBool(false, "Connection failed");
            return;
        }

        // Check only one device paired
        if (pairedDevices.size() > 1) {
            log("Too many bluetooth devices paired to device, please unpair other Bluetooth devices");
            bluetoothConnectedBool(false, "Connection failed");
            return;
        }

        // Find device with correct name
        BluetoothDevice device = pairedDevices.iterator().next();

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            log("Error: Could not create socket");
            bluetoothConnectedBool(false, "Connection failed");
            return;
        }

        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        try {
            btSocket.connect();
            Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
            bluetoothConnectedBool(true, "Connected");
            registerBluetoothReceivers();
        } catch (IOException e) {
            bluetoothConnectedBool(false, "Connection failed");
            log("Error: Failed to establish bluetooth connection");
            try {
                btSocket.close();
            } catch (IOException e2) {
                log("Error: Failed to close socket");
            }
        }

        // Create data stream to talk to server.
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            bluetoothConnectedBool(false, "Connection failed");
            log("Error: Failed to create output stream");
        }
    }


    private static boolean checkBluetoothEnabled() {

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null) {
            Log.d(TAG,"Error: No Bluetooth support found");
        } else if (!btAdapter.isEnabled()) {
            //Prompt user to turn on Bluetooth
            Log.d(TAG, "Error: Bluetooth not enabled");
            Toast.makeText(context, "Bluetooth is disabled, please enable and restart", Toast.LENGTH_LONG).show();
            Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
            Activity activity = (Activity) context;
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }

        return true;
    }

    private static void stopAllChannels() {
        for (int i=0; i<preferencesManager.num_reward_chans; i++) {
            stopChannel(i);
        }
    }

    public static void stopChannel(int Ch) {
        Log.d(TAG, "Stopping channel "+Ch);
        sendData(preferencesManager.strobes_off[Ch]);
    }

    public static void startChannel(int Ch) {
        Log.d(TAG, "Starting channel "+Ch);
        sendData(preferencesManager.strobes_on[Ch]);
    }

    public static void activateChannel(final int Ch, int amount) {

        // Don't allow multiple calls to the reward system
        if (!active) {
            Log.d(TAG,"Giving reward "+amount+" ms on channel "+Ch);
            active = true;

            startChannel(Ch);

            new CountDownTimer(amount, 100) {
                public void onTick(long ms) {
                }

                public void onFinish() {
                    stopChannel(Ch);
                    active=false;
                }
            }.start();
        }
    }

    public static void sendData(String message) {
        if(bluetoothConnection) {
            byte[] msgBuffer = message.getBytes();
            try {
                outStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "Error: No socket");
            }
        } else {
            Log.d(TAG, "Error: No connection");
        }
    }

    public static void quitBt() {
        Log.d(TAG, "Quitting bluetooth");
        if (connectionLoopHandler != null) {
            connectionLoopHandler.removeCallbacksAndMessages(null);
        }
        try {
            context.unregisterReceiver(bluetoothReceiver);
        } catch (IllegalArgumentException e) {
            // No receiver registered
        } catch (NullPointerException e) {
            // Invalid ref
        }
        if (bluetoothConnection) {
            stopAllChannels();
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    log("Bluetooth reconnected");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    log("Lost bluetooth connection..");
                    bluetoothConnectedBool(false, "Disconnected");
                    loopUntilConnected();
                    break;
            }
        }
    };

    private static void loopUntilConnected() {

        connectToBluetooth();

        if(!bluetoothConnection) {
            bluetoothConnectedBool(false, "Attempting to reconnect");
            connectionLoopHandler = new Handler();
            connectionLoopHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loopUntilConnected();
                }
            }, 10000);
        } else {
            stopAllChannels();
        }

    }

    private static void initialiseRewardChannelStrings() {
        preferencesManager = new PreferencesManager(context);
        preferencesManager.RewardStrobeChannels();
    }

    private static void  bluetoothConnectedBool(boolean statusswitch, String statusstring) {
        Log.d(TAG, "New status: " + statusstring);
        bluetoothConnection = statusswitch;
        status = statusstring;
        try {
            listener.onChangeListener();
        } catch (NullPointerException e) {
            Log.d(TAG, "No listener registered");
        }
    }

    public interface MyCustomObjectListener {
        // These methods are the different events and need to pass relevant arguments with the event
        public void onChangeListener();
    }

    // Step 2- This variable represents the listener passed in by the owning object
    // The listener must implement the events interface and passes messages up to the parent.
    private static MyCustomObjectListener listener;

    // Assign the listener implementing events interface that will receive the events (passed in by the owner)
    public void setCustomObjectListener(MyCustomObjectListener listener) {
        this.listener = listener;
    }


}
