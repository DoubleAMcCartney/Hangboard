/*
This is the ConnectActivity class. It controls the UI for the connect activity. The connect activity
is the first activity that opens when the app is opened. It searches and connects to the HAG Board.
 */

package com.example.android.hangboard;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class ConnectActivity extends AppCompatActivity {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION = 2;
    private static final long SCAN_PERIOD = 10000; // 10 seconds
    private static final String BLeDeviceName = "H.A.G. Board";
    private Handler mHandler;
    private boolean mScanning;

    // UI elements
    private TextView statusText;
    private Button connectButton;
    private ProgressBar spinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // run only in portrait mode
        setContentView(R.layout.activity_connect);

        final Toolbar myToolbar = findViewById(R.id.connect_toolbar);
        setSupportActionBar(myToolbar);

        statusText = findViewById(R.id.statusText);
        connectButton = findViewById(R.id.connectButton);
        spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mLeDeviceListAdapter = new LeDeviceListAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Location Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);
        }
        else {
            // permission has been granted, continue as usual
            scanLeDevice(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLeDeviceListAdapter.clear();
        scanLeDevice(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.removeItem(R.id.action_connect);
        menu.removeItem(R.id.action_disconnect);
        menu.removeItem(R.id.action_freeHang);
        menu.removeItem(R.id.action_editWorkout);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_workoutLog:
                final Intent intent1 = new Intent(this, LogActivity.class);
                startActivity(intent1);
                return true;

            case R.id.action_timer:
                final Intent intent2 = new Intent(this, TimerActivity.class);
                startActivity(intent2);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted. Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied. Disable the
                    // functionality that depends on this permission.
                    finish();
                    return;
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter  {
        private ArrayList<BluetoothDevice> mLeDevices;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                if(device.getName() != null) {
                    if(device.getName().equals(BLeDeviceName)) {
                        if(getCount()!=0) {
                            statusText.setText(R.string.multiple_hag_boards);
                            Toast.makeText(getApplicationContext(), R.string.multiple_hag_boards, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            statusText.setText(R.string.found_status);
                        }
                        mLeDevices.add(device);
                        connectButton.setText(R.string.connect);
                        connectButton.setEnabled(true);
                    }
                }
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        public int getCount() {
            return mLeDevices.size();
        }

    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                    spinner.setVisibility(View.INVISIBLE);
                    if (mLeDeviceListAdapter.getCount() == 0) {
                        statusText.setText(R.string.not_found_status);
                        connectButton.setText(R.string.connect_button_search);
                        connectButton.setEnabled(true);
                    }
                    else {
                        statusText.setText(R.string.found_status);
                        connectButton.setText(R.string.connect_button_connect);
                        connectButton.setEnabled(true);
                    }
                }
            }, SCAN_PERIOD);

            mScanning = true;
            if (mLeDeviceListAdapter.getCount() == 0) {
                connectButton.setEnabled(false);
                connectButton.setText(R.string.connect_button_searching);
                statusText.setText(R.string.scanning_status);
                spinner.setVisibility(View.VISIBLE);
            }
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            if (mLeDeviceListAdapter.getCount() == 0) {
                connectButton.setEnabled(true);
                statusText.setText(R.string.not_found_status);
                connectButton.setText(R.string.connect_button_search);
                spinner.setVisibility(View.INVISIBLE);
            }

            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                        }
                    });
                }
            };

    public void connect(View view) {
        // One HAG Board found
        if(mLeDeviceListAdapter.getCount() == 1) {
            if (mScanning) {
                scanLeDevice(false);
            }

            final BluetoothDevice device = mLeDeviceListAdapter.getDevice(0);
            final Intent intent = new Intent(this, TimerActivity.class);

            intent.putExtra(TimerActivity.EXTRAS_DEVICE_NAME, device.getName());
            intent.putExtra(TimerActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
            startActivity(intent);
        }
        // No or multiple HAG Boards found
        else {
            scanLeDevice(true);
        }
    }

}