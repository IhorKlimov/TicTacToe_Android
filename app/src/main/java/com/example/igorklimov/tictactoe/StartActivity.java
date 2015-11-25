package com.example.igorklimov.tictactoe;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
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
        setContentView(R.layout.activity_start);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) supportActionBar.hide();
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.elvMain);
        ArrayList<ArrayList<String>> groups = new ArrayList<>();
        ArrayList<String> children1 = new ArrayList<>();
        children1.add("Connect with Bluetooth");
        groups.add(children1);
        ExpListAdapter adapter = new ExpListAdapter(getApplicationContext(), groups);
        listView.setAdapter(adapter);
        listView.setGroupIndicator(null);
        listView.setDividerHeight(0);
        TextView onePlayerGame = (TextView) findViewById(R.id.one_player);
        onePlayerGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() % 2 == 0) {
                    MainActivity.playersChar = MainActivity.Side.X;
                    MainActivity.opponentChar = MainActivity.Side.O;
                } else {
                    MainActivity.playersChar = MainActivity.Side.O;
                    MainActivity.opponentChar = MainActivity.Side.X;
                }
                MainActivity.playersName = "You";
                MainActivity.opponentsName = "AI";
                Intent singleGame = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(singleGame);
                finish();
            }
        });
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (childPosition == 0) {
                    StartActivity.this.adapter = BluetoothAdapter.getDefaultAdapter();
                    if (StartActivity.this.adapter == null) {
                        Toast.makeText(StartActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (!StartActivity.this.adapter.isEnabled()) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    } else {
                        Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    private void init() {
        if (BluetoothAdapter.getDefaultAdapter().isEnabled() && handler == null) {
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
            if (service.getState() == BluetoothService.STATE_NONE) service.start();
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
                    Toast.makeText(getApplicationContext(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
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
}
