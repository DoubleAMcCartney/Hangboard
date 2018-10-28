package com.example.android.hangboard;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class WorkoutActivity extends AppCompatActivity {
    private final static String TAG = MoveActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    public int sets = 2;
    public int set = 1;
    public int reps = 2;
    public int rep = 1;
    public int exercises = 2;
    public int exercise = 1;

    public int prepareTime = 10000;
    public int workTime = 7000;
    public int restTime = 4000;
    public int breakTime = 3000*60;

    public long prepareTimerValue = prepareTime;
    public long workTimerValue = workTime;
    public long restTimerValue = restTime;
    public long breakTimerValue = breakTime;

    private CountDownTimer prepareTimer;
    private CountDownTimer workTimer;
    private CountDownTimer breakTimer;
    private CountDownTimer restTimer;


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

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                final Intent intent2 = new Intent(WorkoutActivity.this, ConnectActivity.class);
                startActivity(intent2);

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        final TextView timerStatusText;
        final TextView timerText;
        final Button startPauseButton;
        final Button resetButton;

        timerStatusText = findViewById(R.id.timerStatusTextView);
        timerText = findViewById(R.id.timerTextView);
        startPauseButton = findViewById(R.id.startStopButton);
        resetButton = findViewById(R.id.resetButton);


        timerStatusText.setText("Prepare");
        int seconds =  (prepareTime / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        timerText.setText(String.format("%d:%02d", minutes, seconds));
        updateText();


        restTimer = new CountDownTimer(restTimerValue, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                restTimerValue = millisUntilFinished;
                int seconds = (int) (restTimerValue / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                timerText.setText(String.format("%d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                restTimerValue = restTime;
                timerFinished();
            }
        };

        breakTimer = new CountDownTimer(breakTimerValue, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                breakTimerValue = millisUntilFinished;
                int seconds = (int) (breakTimerValue / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                timerText.setText(String.format("%d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                breakTimerValue = breakTime;
                timerFinished();
            }
        };

        workTimer = new CountDownTimer(workTimerValue, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                workTimerValue = millisUntilFinished;
                int seconds = (int) (workTimerValue / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                timerText.setText(String.format("%d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                workTimerValue = workTime;
                timerFinished();
            }
        };

        prepareTimer = new CountDownTimer(prepareTimerValue, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                prepareTimerValue = millisUntilFinished;
                int seconds = (int) (prepareTimerValue / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                timerText.setText(String.format("%d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                prepareTimerValue = prepareTime;
                timerFinished();
            }
        };



        startPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (b.getText().equals("Start")) {
                    if (timerStatusText.getText()=="Prepare") {
                        prepareTimer.start();
                    }
                    else if (timerStatusText.getText()=="Work"){
                        workTimer.start();
                    }
                    else if (timerStatusText.getText()=="Break"){
                        breakTimer.start();
                    }
                    else if (timerStatusText.getText()=="Rest"){
                        restTimer.start();
                    }
                    b.setText("Pause");
                    b.setTextColor(getResources().getColor(R.color.pauseButtonColor));
                }
                else {
                    long time;
                    if (timerStatusText.getText()=="Prepare") {
                        time = prepareTimerValue;
                        prepareTimer.cancel();
                        prepareTimerValue = time;
                    }
                    else if (timerStatusText.getText()=="Work"){
                        time = workTimerValue;
                        workTimer.cancel();
                        workTimerValue = time;
                    }
                    else if (timerStatusText.getText()=="Break"){
                        time = breakTimerValue;
                        breakTimer.cancel();
                        breakTimerValue = time;
                    }
                    else if (timerStatusText.getText()=="Rest"){
                        time = restTimerValue;
                        restTimer.cancel();
                        restTimerValue = time;
                    }
                    b.setText("Start");
                    b.setTextColor(getResources().getColor(R.color.stertButtonColor));
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (timerStatusText.getText()=="Prepare") {
                    prepareTimer.cancel();
                    prepareTimerValue = prepareTime;
                }
                else if (timerStatusText.getText()=="Work"){
                    workTimer.cancel();
                    workTimerValue = prepareTime;
                }
                else if (timerStatusText.getText()=="Break"){
                    breakTimer.cancel();
                    breakTimerValue = prepareTime;
                }
                else if (timerStatusText.getText()=="Rest"){
                    restTimer.cancel();
                    restTimerValue = prepareTime;
                }

                int seconds = (int) (prepareTimerValue / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                timerText.setText(String.format("%d:%02d", minutes, seconds));
                startPauseButton.setText("Start");
                startPauseButton.setTextColor(getResources().getColor(R.color.stertButtonColor));
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
                Toast.makeText(this, R.string.hag_board_dissconnect, Toast.LENGTH_SHORT).show();
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


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void editWorkout(View view) {
        final Intent intent = new Intent(this, EditWorkoutActivity.class);
        startActivity(intent);
    }

    public void workoutLog(View view) {
        final Intent intent = new Intent(this, LogActivity.class);
        startActivity(intent);
    }

    public void freeHang(View view) {
        final Intent intent = new Intent(this, MoveActivity.class);
        intent.putExtra(MoveActivity.EXTRAS_DEVICE_NAME, mDeviceName);
        intent.putExtra(MoveActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);

        startActivity(intent);
    }

    private void updateText() {
        final TextView exerciseText;
        final TextView setText;
        final TextView repText;

        exerciseText = findViewById(R.id.exerciseText);
        repText = findViewById(R.id.repText);
        setText = findViewById(R.id.setText);

        setText.setText("Set: " + Integer.toString(set) + " of " + Integer.toString(sets));
        repText.setText("Rep: " + Integer.toString(rep) + " of " + Integer.toString(reps));
        exerciseText.setText("Exercise: " + Integer.toString(exercise) + " of " + Integer.toString(exercises));
    }

    private void timerFinished() {
        final TextView timerStatusText = findViewById(R.id.timerStatusTextView);

        if (timerStatusText.getText() == "Work") {
            if (rep < reps) {
                rep++;
                timerStatusText.setText("Break");
                breakTimer.start();
            }
            else if (set < sets) {
                set++;
                rep = reps;
                timerStatusText.setText("Rest");
                restTimer.start();
            }
            else if (exercise < exercises) {
                exercise++;
                rep = reps;
                set = sets;
                timerStatusText.setText("Rest");
                restTimer.start();
            }
            else {
                rep = reps;
                set = sets;
                exercise = exercises;
                timerStatusText.setText("Done!");
            }
        }
        else {
            timerStatusText.setText("Work");
            workTimer.start();
        }

    }



}
