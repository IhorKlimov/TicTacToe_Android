package com.example.igorklimov.tictactoe;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
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
    private static final String TAG = "BluetoothChatFragment";
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;
    private BluetoothAdapter adapter = null;
    private StringBuffer outStringBuffer;
    private BluetoothService service = null;
    private static MyHandler handler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.elvMain);
        ArrayList<ArrayList<String>> groups = new ArrayList<>();
        ArrayList<String> children1 = new ArrayList<>();
        children1.add("Connect with Bluetooth");
        children1.add("Connect with Wi-Fi");
        groups.add(children1);
        ExpListAdapter adapter = new ExpListAdapter(getApplicationContext(), groups);
        listView.setAdapter(adapter);
        listView.setGroupIndicator(null);
        listView.setDividerHeight(0);
        outStringBuffer = new StringBuffer("");
        TextView title = (TextView) findViewById(R.id.title);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("Hello");
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
        if (service != null) {
            service.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
        if (service != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (service.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                service.start();
            }
        }
    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (adapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (service.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send = message.getBytes();
            service.write(send);
            // Reset out string buffer to zero and clear the edit text field
            outStringBuffer.setLength(0);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != Activity.RESULT_OK) {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getApplicationContext(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                } else {
                    init();
                }
        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     */
    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = adapter.getRemoteDevice(address);
        // Attempt to connect to the device
        service.connect(device);
    }
}
