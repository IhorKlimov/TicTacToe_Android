package com.example.igorklimov.tictactoe;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.igorklimov.tictactoe.databinding.ActivityStartBinding;
import com.example.igorklimov.tictactoe.res.ExpListAdapter;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity {
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothAdapter adapter = null;
    private BluetoothService service = null;
    private static MyHandler handler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityStartBinding binding = DataBindingUtil
                .setContentView(this, R.layout.activity_start);
        ArrayList<ArrayList<String>> groups = new ArrayList<>();
        ArrayList<String> children1 = new ArrayList<>();
        children1.add(getString(R.string.connect_with_bluetooth));
        children1.add(getString(R.string.connect_with_wifi));
        groups.add(children1);
        ExpListAdapter adapter = new ExpListAdapter(getApplicationContext(), groups);
        binding.options.setAdapter(adapter);
        binding.options.setGroupIndicator(null);
        binding.options.setDividerHeight(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    private void init() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null && defaultAdapter.isEnabled() && handler == null) {
            handler = new MyHandler(getApplicationContext());
            service = new BluetoothService(handler);
            handler.setService(service);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (service != null) service.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
        if (service != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (service.getState() == BluetoothService.STATE_NONE) {
                service.start();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode != Activity.RESULT_OK) {
                    Toast.makeText(
                            this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    init();
                }
        }
    }

    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = adapter.getRemoteDevice(address);
        // Attempt to connect to the device
        service.connect(device);
    }

    public void startOnePlayerGame(View view) {
        if (System.currentTimeMillis() % 2 == 0) {
            MainActivity.playersChar = MainActivity.Side.X;
            MainActivity.opponentChar = MainActivity.Side.O;
        } else {
            MainActivity.playersChar = MainActivity.Side.O;
            MainActivity.opponentChar = MainActivity.Side.X;
        }
        MainActivity.playersName = "You";
        MainActivity.opponentsName = "AI";
        Intent singleGame = new Intent(this, MainActivity.class);
        startActivity(singleGame);
        finish();
    }
}
