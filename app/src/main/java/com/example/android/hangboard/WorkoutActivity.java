package com.example.android.hangboard;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class WorkoutActivity extends AppCompatActivity {
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

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private int rep = 0;
    private int reps = 0;
    private int set = 0;
    private int sets = 0;
    private int exercise = 0;
    private int exercises = 0;

    protected TextView timerStatusText;
    protected TextView timerText;
    protected TextView repText;
    protected TextView setText;
    protected TextView exerciseText;
    protected TextView angleText;
    protected TextView depthText;
    protected Button startPauseButton;
    protected Button stopButton;


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

    //Live Data observers
    private final Observer<String> timerStateObserver = new Observer<String>() {
        @Override
        public void onChanged(@Nullable final String newValue) {
            timerStatusText.setText(newValue);
        }
    };

    private final Observer<Long> timerValueObserver = new Observer<Long>() {
        @Override
        public void onChanged(@Nullable final Long millUntilFinnished) {
            int seconds =  (int)(millUntilFinnished / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            timerText.setText(String.format("%d:%02d", minutes, seconds));
        }
    };

    private final Observer<Boolean> timerStartedObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable final Boolean newValue) {
            if (newValue) {
                startPauseButton.setText("Pause");
            }
            else {
                startPauseButton.setText("Start");
            }
        }
    };

    private final Observer<Integer> currentRepObserver = new Observer<Integer>() {
        @Override
        public void onChanged(@Nullable final Integer newValue) {
            rep = newValue;
            updateText();
        }
    };

    private final Observer<Integer> currentSetObserver = new Observer<Integer>() {
        @Override
        public void onChanged(@Nullable final Integer newValue) {
            set = newValue;
            updateText();
        }
    };

    private final Observer<Integer> currentExerciseObserver = new Observer<Integer>() {
        @Override
        public void onChanged(@Nullable final Integer newValue) {
            exercise = newValue;
            updateText();
        }
    };

    private final Observer<Integer> totalRepObserver = new Observer<Integer>() {
        @Override
        public void onChanged(@Nullable final Integer newValue) {
            reps = newValue;
            updateText();
        }
    };

    private final Observer<Integer> totalSetObserver = new Observer<Integer>() {
        @Override
        public void onChanged(@Nullable final Integer newValue) {
            sets = newValue;
            updateText();
        }
    };

    private final Observer<Integer> totalExerciseObserver = new Observer<Integer>() {
        @Override
        public void onChanged(@Nullable final Integer newValue) {
            exercises = newValue;
            updateText();
        }
    };


    private final Observer<Integer> angleObserver = new Observer<Integer>() {
        @Override
        public void onChanged(@Nullable final Integer newValue) {
            angleText.setText(newValue + "°");
        }
    };

    private final Observer<Integer> depthObserver = new Observer<Integer>() {
        @Override
        public void onChanged(@Nullable final Integer newValue) {
            depthText.setText(newValue + "mm");
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        //Bluetooth stuff
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        timerStatusText = findViewById(R.id.timerStatusTextView);
        timerText = findViewById(R.id.timerTextView);
        repText = findViewById(R.id.repText);
        setText = findViewById(R.id.setText);
        exerciseText = findViewById(R.id.exerciseText);
        angleText = findViewById(R.id.angleText);
        depthText = findViewById(R.id.depthText);
        startPauseButton = findViewById(R.id.startPauseButton);
        stopButton = findViewById(R.id.stopButton);

        //ViewModel and live data stuff
        final WorkoutViewModel mModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        mModel.getTimerState().observe(this, timerStateObserver);
        mModel.getTimerValue().observe(this, timerValueObserver);
        mModel.getTimerStarted().observe(this, timerStartedObserver);
        mModel.getCurrentRep().observe(this, currentRepObserver);
        mModel.getCurrentSet().observe(this, currentSetObserver);
        mModel.getCurrentExercise().observe(this, currentExerciseObserver);
        mModel.getTotalRep().observe(this, totalRepObserver);
        mModel.getTotalSet().observe(this, totalSetObserver);
        mModel.getTotalExercise().observe(this, totalExerciseObserver);
        mModel.getAngle().observe(this, angleObserver);
        mModel.getDepth().observe(this, depthObserver);


        startPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (b.getText().equals(getString(R.string.startButtonText))) {
                    mModel.startTimer();
                }
                else {
                    mModel.pauseTimer();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                mModel.stopTimer();
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
        repText.setText("Rep: " + Integer.toString(rep) + " of " + Integer.toString(reps));
        setText.setText("Set: " + Integer.toString(set) + " of " + Integer.toString(sets));
        exerciseText.setText("Exercise: " + Integer.toString(exercise) + " of " + Integer.toString(exercises));
    }
}
