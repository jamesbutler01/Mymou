package com.example.jbutler.mymou;

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
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by jbutler on 06/12/2018.
 */

public class RewardSystem {

    public static boolean bluetoothConnection = false;
    private static Handler connectionLoopHandler;
    private static boolean bluetoothEnabled = false;
    private static String allChanOff, chanZeroOn, chanZeroOff, chanOneOn, chanOneOff, chanTwoOn,
            chanTwoOff, chanThreeOn, chanThreeOff;
    private static Context context;
    private static final int REQUEST_ENABLE_BT = 1;
    private static BluetoothAdapter btAdapter = null;
    private static BluetoothSocket btSocket = null;
    private static OutputStream outStream = null;
    // Replace with your devices UUID and address
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = "20:16:06:08:64:22";

    public RewardSystem(Context context_in) {

        context = context_in;

        initialiseRewardChannelStrings();
        if (MainMenu.useBluetooth) {
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
        checkBluetoothEnabled();
        if (bluetoothEnabled) {
            establishConnection();
        }
    }


    private static void establishConnection() {
        Log.d("RewardSystem","Connecting to bluetooth..");
        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.d("RewardSystem","Error: Could not create socket");
            return;
        }

        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        try {
            btSocket.connect();
            Log.d("RewardSystem", "Connected to Bluetooth");
            bluetoothConnection = true;
            registerBluetoothReceivers();
        } catch (IOException e) {
            Log.d("RewardSystem","Error: Failed to establish connection");
            try {
                btSocket.close();
            } catch (IOException e2) {
                Log.d("RewardSystem","Error: Failed to close socket");
            }
        }

        // Create data stream to talk to server.
        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {
            Log.d("RewardSystem","Error: Failed to create output stream");
        }
    }


    private static void checkBluetoothEnabled() {

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null) {
            Log.d("RewardSystem","Error: No Bluetooth support found");
        } else if (!btAdapter.isEnabled()) {
            //Prompt user to turn on Bluetooth
            Log.d("RewardSystem", "Error: Bluetooth not enabled");
            Toast.makeText(context, "Bluetooth is disabled, please enable and restart", Toast.LENGTH_LONG).show();
            Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
            Activity activity = (Activity) context;
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        bluetoothEnabled = true;
    }

    public static void stopAllChannels() {
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
            Log.d("RewardSystem", "Error: No valid Ch specified");
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
            Log.d("RewardSystem", "Error: Invalid ch specified");
        }
        return onString;
    }

    public static void stopChannel(int Ch) {
        Log.d("RewardSystem", "Stopping channel "+Ch);
        String stopString = getStopString(Ch);
        sendData(stopString);
    }

    public static void startChannel(int Ch) {
        Log.d("RewardSystem", "Starting channel "+Ch);
        String startString = getStartString(Ch);
        sendData(startString);
    }

    public static void activateChannel(final int Ch, int amount) {
        Log.d("RewardSystem","Giving reward "+amount+" ms on channel "+Ch);
        startChannel(Ch);

        new CountDownTimer(amount, 100) {
            public void onTick(long ms) {}
            public void onFinish() { stopChannel(Ch); }
        }.start();
    }

    public static void sendData(String message) {
        if(bluetoothConnection) {
            byte[] msgBuffer = message.getBytes();
            try {
                outStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d("RewardSystem", "Error: No socket");
            }
        } else {
            Log.d("RewardSystem", "Error: No connection");
        }
    }

    public static void quitBt() {
        Log.d("RewardSystem", "Quitting bluetooth");
        if (connectionLoopHandler != null) {
            connectionLoopHandler.removeCallbacksAndMessages(null);
        }
        try {
            context.unregisterReceiver(bluetoothReceiver);
        } catch (IllegalArgumentException e) {
            // No receiver registered
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
                    //Bluetooth connected
                    Log.d("RewardSystem","Bluetooth reconnected");
                    TaskManager.enableApp(true);
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    //Bluetooth disconnected
                    Log.d("RewardSystem","Lost bluetooth connection..");
                    bluetoothConnection = false;
                    TaskManager.enableApp(false);
                    loopUntilConnected();
                    break;
            }
        }
    };

    public static void loopUntilConnected() {

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


}
