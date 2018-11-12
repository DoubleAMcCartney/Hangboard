package com.example.android.hangboard;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class MoveActivity extends AppCompatActivity {
    private final static String TAG = MoveActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic HAGActual;
    private BluetoothGattCharacteristic HAGDesired;
    private BluetoothGattCharacteristic HAGMove;
    private BluetoothGattService HAGService;

    List<BluetoothGattCharacteristic> bluetoothGattCharacteristic = new ArrayList<>();
    Queue<BluetoothGattCharacteristic> mWriteCharacteristic = new LinkedList<>();

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    public final static UUID UUID_HAG_SERVICE =
            UUID.fromString(GattAttributes.HAG_SERVICE);
    public final static UUID UUID_HAG_CURRENT =
            UUID.fromString(GattAttributes.HAG_CURRENT);
    public final static UUID UUID_HAG_DESIRED =
            UUID.fromString(GattAttributes.HAG_DESIRED);
    public final static UUID UUID_HAG_MOVE =
            UUID.fromString(GattAttributes.HAG_MOVE);

    public byte desiredAngle;
    public byte desiredDepth;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            if (mBluetoothLeService != null) {
                List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();
                for (BluetoothGattService gattService : gattServices) {
                    UUID serviceUUID = gattService.getUuid();


                    if (serviceUUID.equals(UUID_HAG_SERVICE)) {
                        HAGService = gattService;
                        List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

                        for (BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                            if (characteristic.getUuid().equals(UUID_HAG_CURRENT)) {
                                HAGActual = characteristic;
                                final int charaProp = characteristic.getProperties();
                                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                    mNotifyCharacteristic = characteristic;
                                    mBluetoothLeService.setCharacteristicNotification(
                                            characteristic, true);
                                }
                            }
                            else if (characteristic.getUuid().equals(UUID_HAG_MOVE)) {
                                HAGMove = characteristic;
                                final int charaProp = characteristic.getProperties();
                                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                    mNotifyCharacteristic = characteristic;
                                    mBluetoothLeService.setCharacteristicNotification(
                                            characteristic, true);
                                }
                            }
                            else if (characteristic.getUuid().equals(UUID_HAG_DESIRED)) {
                                HAGDesired = characteristic;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
                Toast.makeText(com.example.android.hangboard.MoveActivity.this, R.string.hag_board_disconnect, Toast.LENGTH_SHORT).show();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {



            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (HAGActual != null) {
                    int weight = HAGActual.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
                    weight += HAGActual.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3)*256;
                    //weightText.setText(weight + "lbs");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // run only in portrait mode
        setContentView(R.layout.activity_move);

        final Toolbar myToolbar = findViewById(R.id.move_toolbar);
        setSupportActionBar(myToolbar);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

      //  List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();

      //  desiredAngle = (byte)(int)HAGActual.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
       // desiredDepth = (byte)(int)HAGActual.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);

        ImageButton shallowerButton = findViewById(R.id.Shallower);
        ImageButton deeperButton = findViewById(R.id.Deeper);



        shallowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (desiredDepth >= 0) {
                    desiredDepth--;
                    byte[] value = new byte[2];
                    value[0] = desiredAngle;
                    value[1] = desiredDepth;
                    mBluetoothLeService.writeCharac(HAGDesired,value);
                }
            }
        });

        deeperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (desiredDepth <= 100) {
                    desiredDepth++;
                    byte[] value = new byte[2];
                    value[0] = desiredAngle;
                    value[1] = desiredDepth;
                    mBluetoothLeService.writeCharac(HAGDesired,value);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
            if (!result) {
                Toast.makeText(this, R.string.hag_board_disconnect, Toast.LENGTH_SHORT).show();
                final Intent intent = new Intent(this, ConnectActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.removeItem(R.id.action_connect);
        menu.removeItem(R.id.action_freeHang);
        menu.removeItem(R.id.action_editWorkout);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_disconnect:
                unbindService(mServiceConnection);
                mBluetoothLeService = null;
                final Intent intent1 = new Intent(this, ConnectActivity.class);
                startActivity(intent1);
                return true;

            case R.id.action_timer:
                final Intent intent2 = new Intent(this, TimerActivity.class);
                intent2.putExtra(TimerActivity.EXTRAS_DEVICE_NAME, mDeviceName);
                intent2.putExtra(TimerActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                startActivity(intent2);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}
