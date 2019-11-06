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
    private static Handler connectionLoopHandler;
    private static boolean bluetoothEnabled = false;
    private static boolean active = false;
    private static String allChanOff, chanZeroOn, chanZeroOff, chanOneOn, chanOneOff, chanTwoOn,
            chanTwoOff, chanThreeOn, chanThreeOff;
    private static Context context;
    private static final int REQUEST_ENABLE_BT = 1;
    private static BluetoothAdapter btAdapter = null;
    private static BluetoothSocket btSocket = null;
    private static OutputStream outStream = null;
    // Replace with your devices UUID and address
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public RewardSystem(Context context_in, Activity activity) {

        context = context_in;

        initialiseRewardChannelStrings();

        this.listener = null; // set null listener

        if (new PreferencesManager(context).bluetooth && new PermissionManager(context_in, activity).checkPermissions()) {
            loopUntilConnected();
        }

    }

    private static void registerBluetoothReceivers() {
            IntentFilter bluetoothIntent = new IntentFilter();
            bluetoothIntent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            bluetoothIntent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            context.registerReceiver(bluetoothReceiver, bluetoothIntent);
    }

    private static void connectToBluetooth() {
        Log.d(TAG, "Connecting to bluetooth");
        checkBluetoothEnabled();
        if (bluetoothEnabled) {
            establishConnection();
        }
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    private static void establishConnection() {
        Log.d(TAG,"Connecting to bluetooth..");

        // Get list of paired bluetooth devices
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        // Check device is paired with tablet
        if (pairedDevices.size() == 0) {
            log("No bluetooth devices paired");
            return;
        }

        // Check only one device paired
        if (pairedDevices.size() > 1) {
            log("Too many bluetooth devices paired to device, please unpair other Bluetooth devices");
            return;
        }

        // Find device with correct name
        BluetoothDevice device = pairedDevices.iterator().next();

        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.d(TAG,"Error: Could not create socket");
            return;
        }

        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        try {
            btSocket.connect();
            Log.d(TAG, "Connected to Bluetooth");
            bluetoothConnectedBool(true);
            registerBluetoothReceivers();
        } catch (IOException e) {
            Log.d(TAG,"Error: Failed to establish connection");
            try {
                btSocket.close();
            } catch (IOException e2) {
                Log.d(TAG,"Error: Failed to close socket");
            }
        }

        // Create data stream to talk to server.
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            Log.d(TAG,"Error: Failed to create output stream");
        }
    }


    private static void checkBluetoothEnabled() {

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
            return;
        }

        bluetoothEnabled = true;
    }

    private static void stopAllChannels() {
        sendData(allChanOff);
    }

    private static String getStopString(int Ch) {
        String offString="";
        if(Ch == 0) {
            offString = chanZeroOff;
        } else if (Ch == 1) {
            offString = chanOneOff;
        } else if (Ch == 2) {
            offString = chanTwoOff;
        } else if (Ch == 3) {
            offString = chanThreeOff;
        } else {
            Log.d(TAG, "Error: No valid Ch specified");
        }
        return offString;
    }

    private static String getStartString(int Ch) {
        String onString="";
        if(Ch == 0) {
            onString = chanZeroOn;
        } else if (Ch == 1) {
            onString = chanOneOn;
        } else if (Ch == 2) {
            onString = chanTwoOn;
        } else if (Ch == 3) {
            onString = chanThreeOn;
        } else {
            Log.d(TAG, "Error: Invalid ch specified");
        }
        return onString;
    }

    public static void stopChannel(int Ch) {
        Log.d(TAG, "Stopping channel "+Ch);
        String stopString = getStopString(Ch);
        sendData(stopString);
    }

    public static void startChannel(int Ch) {
        Log.d(TAG, "Starting channel "+Ch);
        String startString = getStartString(Ch);
        sendData(startString);
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
                    //Bluetooth connected, no action needed
                    Log.d(TAG,"Bluetooth reconnected");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    //Bluetooth disconnected
                    Log.d(TAG,"Lost bluetooth connection..");
                    bluetoothConnectedBool(false);
                    loopUntilConnected();
                    break;
            }
        }
    };

    private static void loopUntilConnected() {

        connectToBluetooth();

        if(!bluetoothConnection) {
            connectionLoopHandler = new Handler();
            connectionLoopHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loopUntilConnected();
                }
            }, 15000);
        } else {
            stopAllChannels();
        }

    }

    private static void initialiseRewardChannelStrings() {
        allChanOff = context.getString(R.string.allChanOff);
        chanZeroOn = context.getString(R.string.chanZeroOn);
        chanZeroOff = context.getString(R.string.chanZeroOff);
        chanOneOn = context.getString(R.string.chanOneOn);
        chanOneOff = context.getString(R.string.chanOneOff);
        chanTwoOn = context.getString(R.string.chanTwoOn);
        chanTwoOff = context.getString(R.string.chanTwoOff);
        chanThreeOn = context.getString(R.string.chanThreeOn);
        chanThreeOff = context.getString(R.string.chanThreeOff);
    }

    private static void bluetoothConnectedBool(boolean status) {
        bluetoothConnection = status;
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
