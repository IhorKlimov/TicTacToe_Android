package com.example.igorklimov.tictactoe.res;

/**
 * Created by Igor Klimov on 10/24/2015.
 */

import com.example.igorklimov.tictactoe.bluetooth.BluetoothService;

/**
 * Defines several constants used between {@link BluetoothService} and the UI.
 */
public interface Constants {
    // Message types sent from the BluetoothService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;
    int RUN_BT_GAME = 6;

    // Key names received from the BluetoothService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

}
