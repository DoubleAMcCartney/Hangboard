package com.example.android.hangboard;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

//TODO: fix issue where activity disconnects from bluetooth when device is still connected
//TODO: Use weight to control timer

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
    private BluetoothGattCharacteristic HAGActual;
    private BluetoothGattCharacteristic HAGMove;
    private BluetoothGattCharacteristic HAGDesired;
    private BluetoothGattService HAGService;

    List <BluetoothGattCharacteristic> bluetoothGattCharacteristic = new ArrayList<>();
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

    private Workout currentWorkout;
    private int rep = 0;
    private int reps = 0;
    private int set = 0;
    private int sets = 0;
    private int exercise = 0;
    private int exercises = 0;

    private WorkoutViewModel mModel;
    protected TextView timerStatusText;
    protected TextView timerText;
    protected TextView repText;
    protected TextView setText;
    protected TextView exerciseText;
    protected TextView angleText;
    protected TextView depthText;
    protected TextView weightText;
    protected TextView timeRemainingText;
    protected Button startPauseButton;
    protected Button stopButton;
    protected Button skipButton;


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
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
                Toast.makeText(WorkoutActivity.this, R.string.hag_board_disconnect, Toast.LENGTH_SHORT).show();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
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


            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (HAGActual != null) {
                    int weight = HAGActual.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
                    weight += HAGActual.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3)*256;
                    weightText.setText(weight + "lbs");
                }
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

                // If landscape Set fullscreen flag
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }

                // Hide action bar
                ActionBar actionBar = getSupportActionBar();
                actionBar.hide();

                // Set keep screen on flag
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            else {
                startPauseButton.setText("Start");

                // Clear keep screen on and fullscreen flags
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // Show action bar
                ActionBar actionBar = getSupportActionBar();
                actionBar.show();
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
            byte[] value = new byte[2];
            value[0] = (byte)(int)currentWorkout.getAngles().get(exercise);
            value[1] = (byte)(int)currentWorkout.getDepths().get(exercise);
            HAGDesired.setValue(value);
            depthText.setText(currentWorkout.getDepths().get(exercise) + "mm");
            angleText.setText(currentWorkout.getAngles().get(exercise) + "°");
            updateText();
        }
    };

    private final Observer<Long> timeRemainingObserver = new Observer<Long>() {
        @Override
        public void onChanged(@Nullable final Long newValue) {
            int seconds =  (int)(newValue / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            timeRemainingText.setText("Remaining: " + String.format("%d:%02d", minutes, seconds));
        }
    };

    private final Observer<Workout> currentWorkoutObserver = new Observer<Workout>() {
        @Override
        public void onChanged(@Nullable Workout workout) {
            if (workout != null) {
                currentWorkout = workout;
                sets = workout.getSets();
                reps = workout.getReps();
                exercises = workout.getExercises();

                //Set activity title to the workout title
                //This will be displayed in the app bar
                setTitle(workout.getWorkoutTitle());

                byte[] value = new byte[2];
                value[0] = (byte)(int)currentWorkout.getAngles().get(0);
                value[1] = (byte)(int)currentWorkout.getDepths().get(0);
                HAGDesired.setValue(value);
                depthText.setText(currentWorkout.getDepths().get(0) + "mm");
                angleText.setText(currentWorkout.getAngles().get(0) + "°");
                updateText();

                if (startPauseButton.getText() == "Start") {
                    mModel.setTotalRep(currentWorkout.getReps());
                    mModel.setTotalSet(currentWorkout.getSets());
                    mModel.setTotalExercises(currentWorkout.getExercises());
                    mModel.setWorkTime(currentWorkout.getWorkTime());
                    mModel.setRestTime(currentWorkout.getRestTime());
                    mModel.setBreakTime(currentWorkout.getBreakTime());
                    mModel.setTimeRemaining();
                }
            }
            else {
                sets = 0;
                reps = 0;
                exercises = 0;
                updateText();
            }

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        final Toolbar myToolbar = findViewById(R.id.workout_toolbar);
        setSupportActionBar(myToolbar);

        //Bluetooth stuff
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        if (mDeviceName != null) {
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }

        timerStatusText = findViewById(R.id.timerStatusTextView);
        timerText = findViewById(R.id.timerTextView);
        repText = findViewById(R.id.repText);
        setText = findViewById(R.id.setText);
        exerciseText = findViewById(R.id.exerciseText);
        angleText = findViewById(R.id.angleText);
        depthText = findViewById(R.id.depthText);
        weightText = findViewById(R.id.weightText);
        timeRemainingText = findViewById(R.id.timeRemainingText);
        startPauseButton = findViewById(R.id.startPauseButton);
        stopButton = findViewById(R.id.stopButton);
        skipButton = findViewById(R.id.skipButton);

        //ViewModel and live data stuff
        mModel = ViewModelProviders.of(this).get(WorkoutViewModel.class);
        mModel.getTimerState().observe(this, timerStateObserver);
        mModel.getTimerValue().observe(this, timerValueObserver);
        mModel.getTimerStarted().observe(this, timerStartedObserver);
        mModel.getCurrentRep().observe(this, currentRepObserver);
        mModel.getCurrentSet().observe(this, currentSetObserver);
        mModel.getCurrentExercise().observe(this, currentExerciseObserver);
        mModel.getTimeRemaining().observe(this, timeRemainingObserver);
        mModel.getWorkout().observe(this, currentWorkoutObserver);


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

        skipButton.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                mModel.skipTimer();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        //if (mDeviceName != null) {
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        //}

        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
            if (!result) {
                Toast.makeText(this, R.string.hag_board_disconnect, Toast.LENGTH_SHORT).show();
            }
        }

        invalidateOptionsMenu();

        // Hide status and action bars if timer is started
        if (startPauseButton.getText().equals(getString(R.string.startButtonText))) {
            // Clear keep screen on and fullscreen flags
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // Show action bar
            ActionBar actionBar = getSupportActionBar();
            actionBar.show();
        }
        else {
            // If landscape Set fullscreen flag
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            // Hide action bar
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();

            // Set keep screen on flag
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //if (mConnected) {
            unregisterReceiver(mGattUpdateReceiver);
        //}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnected) {
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.removeItem(R.id.action_timer);
        if (mConnected ){
            menu.removeItem(R.id.action_connect);
        }
        else {
            menu.removeItem(R.id.action_disconnect);
            menu.removeItem(R.id.action_freeHang);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_editWorkout:
                // User chose the "edit_workout" item, show the app edit workout UI...
                final Intent intent1 = new Intent(this, ViewWorkoutsActivity.class);
                startActivity(intent1);
                return true;

            case R.id.action_workoutLog:
                final Intent intent2 = new Intent(this, LogActivity.class);
                startActivity(intent2);
                return true;

            case R.id.action_freeHang:
                final Intent intent3 = new Intent(this, MoveActivity.class);
                intent3.putExtra(MoveActivity.EXTRAS_DEVICE_NAME, mDeviceName);
                intent3.putExtra(MoveActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                startActivity(intent3);
                return true;

            case R.id.action_disconnect:
                unbindService(mServiceConnection);
                mBluetoothLeService = null;
                final Intent intent4 = new Intent(this, ConnectActivity.class);
                startActivity(intent4);
                return true;

            case R.id.action_connect:
                final Intent intent5 = new Intent(this, ConnectActivity.class);
                startActivity(intent5);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices)
    {
        for(BluetoothGattService service : gattServices)
        {
            Log.i(TAG, "Service UUID = " + service.getUuid());

            bluetoothGattCharacteristic = service.getCharacteristics();

            for(BluetoothGattCharacteristic character: bluetoothGattCharacteristic)
            {
                Log.i(TAG, "Service Character UUID = " + character.getUuid());

                // Add your **preferred characteristic** in a Queue
                mWriteCharacteristic.add(character);
            }
        }

        if(mWriteCharacteristic.size() > 0)
        {
            read_Characteristic();
        };
    };

    // make sure this method is called when there is more than one characteristic to read & set
    private void read_Characteristic()
    {

        mBluetoothLeService.readCharacteristic(mWriteCharacteristic.element());
        mBluetoothLeService.setCharacteristicNotification(mWriteCharacteristic.element(),true);
        mWriteCharacteristic.remove();
    };


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void updateText() {
        repText.setText("Rep: " + Integer.toString(rep) + " of " + Integer.toString(reps));
        setText.setText("Set: " + Integer.toString(set) + " of " + Integer.toString(sets));
        exerciseText.setText("Exercise: " + Integer.toString(exercise) + " of " + Integer.toString(exercises));
    }

}
