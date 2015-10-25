package com.example.igorklimov.tictactoe;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.example.igorklimov.tictactoe.BluetoothService;
import com.example.igorklimov.tictactoe.MainActivity;
import com.example.igorklimov.tictactoe.res.Constants;

/**
 * Created by Igor Klimov on 10/25/2015.
 */
public class MyHandler extends Handler {
    private Context context;
    private BluetoothService service;

    public MyHandler(Context context) {
        this.context = context;
    }

    public void setService(BluetoothService service) {
        this.service = service;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                    case BluetoothService.STATE_CONNECTED:
                        break;
                    case BluetoothService.STATE_CONNECTING:
                        Toast.makeText(context, "Establishing connection", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothService.STATE_LISTEN:
                    case BluetoothService.STATE_NONE:
                        break;
                }
                break;
            case Constants.MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                break;
            case Constants.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0, msg.arg1);
                if (readMessage.contains("You")) {
                    if (readMessage.substring(4, readMessage.length()).equals("O")) {
                        MainActivity.playersChar = MainActivity.Side.O;
                        MainActivity.opponentChar = MainActivity.Side.X;
                    } else {
                        MainActivity.playersChar = MainActivity.Side.X;
                        MainActivity.opponentChar = MainActivity.Side.O;
                    }
                    MainActivity.playerFirst = 1;
                    runGame();
                } else {
                    Toast.makeText(context, readMessage, Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.MESSAGE_DEVICE_NAME:
                String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                Toast.makeText(context, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case Constants.MESSAGE_TOAST:
                Toast.makeText(context, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                break;
            case Constants.RUN_BT_GAME:
                MainActivity.playerFirst = 2;
                runGame();
        }
    }

    private void runGame() {
        MainActivity.btGame = true;
        MainActivity.service = service;
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
//        finish();
    }
}
