/*
This is the TimerActivity class. It controls the UI for the timer activity. The timer activity
provides the user with workout timers and controls the depth and angle of the HAG Board according to
the current workout.
 */

//todo record weight; calculate score; add dialog at end of workout

package com.example.android.hangboard;

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
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.hangboard.ChooseWorkout.ViewWorkoutsActivity;
import com.example.android.hangboard.WorkoutDB.Workout;
import com.example.android.hangboard.WorkoutLog.LogActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

//TODO: fix issue where activity disconnects from bluetooth when device is still connected

public class TimerActivity extends AppCompatActivity {
    private final static String TAG = MoveActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic HAGActual;
    private BluetoothGattCharacteristic HAGMove;
    private BluetoothGattCharacteristic HAGDesired;
    private BluetoothGattService HAGService;

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
    private int weight = 0;
    private List<Integer> weightList;
    private List<Boolean> workList;
    private boolean timerStarted = false;
    private boolean workState = false;
    private int actualWorkTime = 0;
    private int avgWeight = 0;
    private int score = 0;
    private int workTime = 0;

    private TimerViewModel mModel;
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
    private DialogFragment addWorkout;

    // Timer sounds
    private MediaPlayer pitch1;
    private MediaPlayer pitch2;


    // Code to manage Bluetooth LE Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        // Code called every time the service is connected
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);

            // Go through the list of supported services on the connected device
            List<BluetoothGattService> gattServices =
                    mBluetoothLeService.getSupportedGattServices();
            for (BluetoothGattService gattService : gattServices) {
                UUID serviceUUID = gattService.getUuid();

                // HAG service
                if (serviceUUID.equals(UUID_HAG_SERVICE)) {
                    HAGService = gattService;
                    List<BluetoothGattCharacteristic> gattCharacteristics =
                            gattService.getCharacteristics();

                    // Go through the list of characteristics in the service
                    for (BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                        // HAG Current characteristic
                        if (characteristic.getUuid().equals(UUID_HAG_CURRENT)) {
                            HAGActual = characteristic;
                            final int charaProp = characteristic.getProperties();

                            // Set notify to true so that the app will be notified
                            // when the data is changed
                            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                mNotifyCharacteristic = characteristic;
                                mBluetoothLeService.setCharacteristicNotification(
                                        characteristic, true);
                            }
                        }
                        // HAG Move characteristic
                        else if (characteristic.getUuid().equals(UUID_HAG_MOVE)) {
                            HAGMove = characteristic;
                            final int charaProp = characteristic.getProperties();

                            // Set notify to true so that the app will be notified
                            // when the data is changed
                            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                                mNotifyCharacteristic = characteristic;
                                mBluetoothLeService.setCharacteristicNotification(
                                        characteristic, true);
                            }
                        }
                        // HAG Desired characteristic
                        else if (characteristic.getUuid().equals(UUID_HAG_DESIRED)) {
                            HAGDesired = characteristic;
                        }
                    }
                }
            }
        }

        // Called every time the Bluetooth LE service is disconnected
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
                invalidateOptionsMenu(); // Reset toolbar to replace connect with disconnect

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu(); // Reset toolbar to replace disconnect with connect
                Toast.makeText(TimerActivity.this, R.string.hag_board_disconnect,
                        Toast.LENGTH_SHORT).show();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                if (mBluetoothLeService != null) {
                    // Go through the list of supported services on the connected device
                    List<BluetoothGattService> gattServices =
                            mBluetoothLeService.getSupportedGattServices();
                    for (BluetoothGattService gattService : gattServices) {
                        UUID serviceUUID = gattService.getUuid();

                        // HAG service
                        if (serviceUUID.equals(UUID_HAG_SERVICE)) {
                            HAGService = gattService;
                            List<BluetoothGattCharacteristic> gattCharacteristics =
                                    gattService.getCharacteristics();

                            // Go through the list of characteristics in the service
                            for (BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                                // HAG Current characteristic
                                if (characteristic.getUuid().equals(UUID_HAG_CURRENT)) {
                                    HAGActual = characteristic;
                                    final int charaProp = characteristic.getProperties();

                                    // Set notify to true so that the app will be notified
                                    // when the data is changed
                                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) >
                                            0) {
                                        mNotifyCharacteristic = characteristic;
                                        mBluetoothLeService.setCharacteristicNotification(
                                                characteristic, true);
                                    }
                                }
                                // HAG Move characteristic
                                else if (characteristic.getUuid().equals(UUID_HAG_MOVE)) {
                                    HAGMove = characteristic;
                                    final int charaProp = characteristic.getProperties();

                                    // Set notify to true so that the app will be notified
                                    // when the data is changed
                                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) >
                                            0) {
                                        mNotifyCharacteristic = characteristic;
                                        mBluetoothLeService.setCharacteristicNotification(
                                                characteristic, true);
                                    }
                                }
                                // HAG Desired characteristic
                                else if (characteristic.getUuid().equals(UUID_HAG_DESIRED)) {
                                    HAGDesired = characteristic;
                                }
                            }
                        }
                    }
                }


            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if (HAGActual != null) {
                    byte data[] = HAGActual.getValue();
                    byte weightArray[] = new byte[4];
                    for (int i=2; i<6; i++) {
                       weightArray[i-2] = data[i];
                    }
                    weight = ByteBuffer.wrap(weightArray).getInt();

                    weightText.setText(weight + "lbs"); // update weight text with new value
                }

                // Auto-skip timer if user hangs early and there is less than one second remaining
                if (weight > 10 & timerStarted & mModel.getTimeRemaining().getValue()<1000) {
                    String timerStatus = (String)timerStatusText.getText();
                    if (timerStatus.equals("Prepare") || timerStatus.equals("Rest") ||
                            timerStatus.equals("Break")) {
                        mModel.skipTimer();
                    }
                }
            }
        }
    };



    //Live Data observers
    private final Observer<String> timerStateObserver = new Observer<String>() {
        @Override
        public void onChanged(@Nullable final String newValue) {
            timerStatusText.setText(newValue); // Update timer text
            if (newValue == "Done") {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // run only in portrait mode
                workOutComplete();
                workState = false;
            }
            else if (newValue == "Work") {
                workState = true;
            }
            else {
                workState = false;
            }
        }
    };

    private final Observer<Long> timerValueObserver = new Observer<Long>() {
        @Override
        public void onChanged(@Nullable final Long millUntilFinnished) {
            // Calculate seconds and minutes from milliseconds
            int seconds =  (int)(millUntilFinnished / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            // Update text with new value
            timerText.setText(String.format("%d:%02d", minutes, seconds));

            // Play sound at two, one and zero seconds if timer is started
            if (startPauseButton.getText()=="Pause") {
                if (seconds == 1 & minutes == 0 || seconds==2 & minutes == 0) {
                    pitch1.start();
                }
                else if (seconds==0 & minutes == 0) {
                    pitch2.start();
                }
            }
        }
    };

    private final Observer<Boolean> timerStartedObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable final Boolean newValue) {
            timerStarted = newValue;
            if (newValue) {
                startPauseButton.setText("Pause");

                // If landscape Set fullscreen flag
                if(getResources().getConfiguration().orientation ==
                        Configuration.ORIENTATION_LANDSCAPE){
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
            updateText(); // Update text
        }
    };

    private final Observer<Integer> currentSetObserver = new Observer<Integer>() {
        @Override
        public void onChanged(@Nullable final Integer newValue) {
            set = newValue;
            updateText(); // Update text
        }
    };

    private final Observer<Integer> currentExerciseObserver = new Observer<Integer>() {
        @Override
        public void onChanged(@Nullable final Integer newValue) {
            if (currentWorkout != null) {
                exercise = newValue;

                // Update HAG Desired characteristic with new depth and angle
                if (HAGDesired!= null) {
                    byte[] value = new byte[2];
                    value[0] = (byte)(int)currentWorkout.getAngles().get(exercise-1);
                    value[1] = (byte)(int)currentWorkout.getDepths().get(exercise-1);
                    mBluetoothLeService.writeCharac(HAGDesired,value);
                }
                // Update text with new values
                depthText.setText(currentWorkout.getDepths().get(exercise-1) + "mm");
                angleText.setText(currentWorkout.getAngles().get(exercise-1) + "°");
                updateText();
            }
            else {
                exercise = newValue;
                updateText();
            }
        }
    };

    private final Observer<Long> timeRemainingObserver = new Observer<Long>() {
        @Override
        public void onChanged(@Nullable final Long newValue) {
            // calculate seconds and minutes from milliseconds
            int totSeconds =  (int)(newValue / 1000);
            int minutes = totSeconds / 60;
            int seconds = totSeconds % 60;
            timeRemainingText.setText("Remaining: " + String.format("%d:%02d", minutes, seconds));

            // Every second store the current weight in a list
            if (weightList==null) {
                return;
            }
            if (weightList.size()>totSeconds && totSeconds >=0) {
                if ((weightList.get(totSeconds)==0) & (weight >= 0)) {
                    weightList.set(totSeconds, weight);
                    workList.set(totSeconds, workState);
                }
            }

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

                // Update HAG Desired characteristic with new depth and angle
                if (HAGDesired != null) {
                    byte[] value = new byte[2];
                    value[0] = (byte)(int)currentWorkout.getAngles().get(0);
                    value[1] = (byte)(int)currentWorkout.getDepths().get(0);
                    HAGDesired.setValue(value);
                    mBluetoothLeService.writeCharac(HAGDesired, value);
                }

                // Update text
                depthText.setText(currentWorkout.getDepths().get(0) + "mm");
                angleText.setText(currentWorkout.getAngles().get(0) + "°");
                updateText();

                // If timer not started, update variables with new workout info
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

            weightList = new ArrayList<>();
            workList = new ArrayList<>();
            for (long i = 0; i <= mModel.getTimeRemaining().getValue()/1000; i++) {
                weightList.add(0);
                workList.add(false);
            }

        }
    };


    // Called every time the timer activity is created. This includes after orientation changes
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        final Toolbar myToolbar = findViewById(R.id.workout_toolbar);
        setSupportActionBar(myToolbar);

        // Setup Bluetooth
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        if (mDeviceName != null) {
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        }

        // Define UI elements
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

        //ViewModel and live data observers
        mModel = ViewModelProviders.of(this).get(TimerViewModel.class);
        mModel.getTimerState().observe(this, timerStateObserver);
        mModel.getTimerValue().observe(this, timerValueObserver);
        mModel.getTimerStarted().observe(this, timerStartedObserver);
        mModel.getCurrentRep().observe(this, currentRepObserver);
        mModel.getCurrentSet().observe(this, currentSetObserver);
        mModel.getCurrentExercise().observe(this, currentExerciseObserver);
        mModel.getTimeRemaining().observe(this, timeRemainingObserver);
        mModel.getWorkout().observe(this, currentWorkoutObserver);

        // timer sounds
        pitch1 = MediaPlayer.create(getApplicationContext(), R.raw.beep1);
        pitch2 = MediaPlayer.create(getApplicationContext(), R.raw.beep2);

        // Start/Pause button click listener
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

        // stop button listener
        stopButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                mModel.stopTimer();
                if (weightList != null) {
                    weightList.clear();
                    workList.clear();
                }
            }
        });

        // skip button listener
        skipButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                mModel.skipTimer();
            }
        });

    }

    // Called when the activity is resumed
    @Override
    protected void onResume() {
        super.onResume();

        // register bluetooth receiver
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        // connect to bluetooth
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
            if (!result) {
                Toast.makeText(this, R.string.hag_board_disconnect,
                        Toast.LENGTH_SHORT).show();
            }
        }

        invalidateOptionsMenu();  // Reset toolbar

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
            if(getResources().getConfiguration().orientation ==
                    Configuration.ORIENTATION_LANDSCAPE){
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            // Hide action bar
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();

            // Set keep screen on flag
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    // Called every time the activity is paused
    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mGattUpdateReceiver);
    }

    // Called every time the activity is closed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnected) {
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
    }

    // called when the toolbar is first created
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.action_timer).setVisible(false);
        if (mConnected ){
            menu.findItem(R.id.action_connect).setVisible(false);
        }
        else {
            menu.findItem(R.id.action_disconnect).setVisible(false);
            menu.findItem(R.id.action_freeHang).setVisible(false);
        }
        return true;
    }

    // Called every time an item in the toolbar is tapped
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_editWorkout:
                // User chose the "edit_workout" item, show the app edit workout UI
                final Intent intent1 = new Intent(this, ViewWorkoutsActivity.class);
                startActivityForResult(intent1, 1);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;

            case R.id.action_workoutLog:
                // Show the app workout log UI
                final Intent intent2 = new Intent(this, LogActivity.class);
                startActivity(intent2);
                return true;

            case R.id.action_freeHang:
                // Show the app free hang UI
                final Intent intent3 = new Intent(this, MoveActivity.class);
                intent3.putExtra(MoveActivity.EXTRAS_DEVICE_NAME, mDeviceName);
                intent3.putExtra(MoveActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                startActivity(intent3);
                return true;

            case R.id.action_disconnect:
                // disconnect for bluetooth and go to connect activity
                unbindService(mServiceConnection);
                mBluetoothLeService = null;
                final Intent intent4 = new Intent(this, ConnectActivity.class);
                startActivity(intent4);
                return true;

            case R.id.action_connect:
                // go to connect activity
                final Intent intent5 = new Intent(this, ConnectActivity.class);
                startActivity(intent5);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    // Called when another activity returns a result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If a current workout was chosen, update to that workout
        if (requestCode == 1 & resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            String workoutTitle = (String) extras.get("result");
            mModel.getWorkoutByTitle(workoutTitle);
            mModel.getWorkout().observe(this, currentWorkoutObserver);
        }

    }

    // intents for starting bluetooth service
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    // Update reps, sets, exercises texts
    private void updateText() {
        repText.setText("Rep: " + Integer.toString(rep) + " of " + Integer.toString(reps));
        setText.setText("Set: " + Integer.toString(set) + " of " + Integer.toString(sets));
        exerciseText.setText("Exercise: " + Integer.toString(exercise) + " of " +
                Integer.toString(exercises));
    }

    private void workOutComplete() {
        // create and show dialog
        addWorkout = new WorkoutCompleteDialogFragment();
        addWorkout.show(getSupportFragmentManager(), "AddWorkout");
        getSupportFragmentManager().executePendingTransactions();

        currentWorkout = mModel.getWorkout().getValue(); // needed to prevent currentWorkout from being null

        TextView dateText = addWorkout.getDialog().findViewById(R.id.logDFDate);
        TextView title = addWorkout.getDialog().findViewById(R.id.logDFWorkoutTitle);
        TextView scoreText = addWorkout.getDialog().findViewById(R.id.logDFScore);
        TextView weightText = addWorkout.getDialog().findViewById(R.id.logDFTotalWeight);
        TextView depthText = addWorkout.getDialog().findViewById(R.id.logDFDepth);
        TextView angleText = addWorkout.getDialog().findViewById(R.id.logDFAngle);
        TextView hangTimeText = addWorkout.getDialog().findViewById(R.id.logDFHangTime);
        TextView repsText = addWorkout.getDialog().findViewById(R.id.logDFReps);
        TextView setsText = addWorkout.getDialog().findViewById(R.id.logDFSets);
        TextView workTimeText = addWorkout.getDialog().findViewById(R.id.logDFWorktime);
        TextView restTimeText = addWorkout.getDialog().findViewById(R.id.logDFRestTime);
        TextView breakTimeText = addWorkout.getDialog().findViewById(R.id.logDFBreakTime);
        GraphView graph = addWorkout.getDialog().findViewById(R.id.graph);
        DateFormat dateFormat = new SimpleDateFormat("MMM. dd yyyy");
        Date date = Calendar.getInstance().getTime();


        workTime = (currentWorkout.getWorkTime()/1000)*sets*reps*exercises;

        dateText.setText(dateFormat.format(date));
        title.setText(currentWorkout.getWorkoutTitle());
        repsText.setText("Reps: " + reps);
        setsText.setText("Sets: " + sets);
        workTimeText.setText("Work Time: " + Integer.toString(
                currentWorkout.getWorkTime()/1000) + "sec");
        restTimeText.setText("Rest Time: " + Integer.toString(
                currentWorkout.getRestTime()/1000) + "sec");
        breakTimeText.setText("Break Time: " + Integer.toString(
                currentWorkout.getBreakTime()/60000) + "min");
        depthText.setText("Depth: " + currentWorkout.getDepths().get(0));
        angleText.setText("Angle: " + currentWorkout.getAngles().get(0));

        if (mConnected) {
            // Create data points from weight and work lists
            int dataPoints = weightList.size();
            DataPoint[] weightDP = new DataPoint[dataPoints];
            DataPoint[] workDP = new DataPoint[dataPoints];

            long totWeight = 0;
            int maxWeight = 0;

            for (int x = 0; x < dataPoints; x++) {
                int weight = weightList.get(x);

                weightDP[dataPoints-x-1] = new DataPoint((dataPoints-x-1), weight);

                // find max weight
                if (weight > maxWeight) {
                    maxWeight = weight;
                }

                // Calculate average weight during work periods
                if (weight > 20) {
                    actualWorkTime++;
                    totWeight += weight;
                }
            }

            for (int x = 0; x < dataPoints; x++) {
                int work;

                if (workList.get(x)) {
                    if (weightList.get(x) >= 10) {
                        work = 1000;
                    }
                    else {
                        work = 2000;
                    }
                }
                else {
                    work = 3000;
                }

                workDP[dataPoints-x-1] = new DataPoint((dataPoints-x-1), work);
            }


            // add data to graph
            LineGraphSeries<DataPoint> lineSeries = new LineGraphSeries<>(weightDP);
            BarGraphSeries<DataPoint> barSeries = new BarGraphSeries<>(workDP);

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMaxY(maxWeight+10);
            graph.getViewport().setMaxX(60);
            graph.getViewport().setScrollable(true);
            graph.getGridLabelRenderer().setHorizontalAxisTitle("Time (seconds)");
            graph.getGridLabelRenderer().setVerticalAxisTitle("Weight (LBS)");
            graph.addSeries(lineSeries);
            graph.addSeries(barSeries);

            // set color of bar graph
            barSeries.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                @Override
                public int get(DataPoint data) {

                    if (data.getY() == 1000) {
                        return Color.argb(100,0,255,0);
                    }
                    else if (data.getY() == 2000){
                        return Color.argb(100,255,0,0);
                    }
                    return android.R.color.transparent;
                }
            });

            barSeries.setSpacing(0);

            if (actualWorkTime != 0) {
                avgWeight = (int) (totWeight / actualWorkTime);
            }
            else {
                avgWeight = 0;
            }

            // calculate score
            if (currentWorkout.getDepths().get(0) > 0) {
                score = avgWeight * 10 / currentWorkout.getDepths().get(0); // lbs per cm
            }
            else {
                score = 0;
            }

            scoreText.setText("Score: " + score);
            weightText.setText("Weight: " + avgWeight);
            hangTimeText.setText("Hang Time: " + actualWorkTime + " of " + workTime + "sec");

            weightList.clear();
            workList.clear();
        }
        else {
            graph.setVisibility(View.GONE);
            scoreText.setText("Score: N/A");
            weightText.setText("Weight: N/A");
            hangTimeText.setText("Hang Time: N/A" + " of " + workTime + "sec");
        }
    }

    void addWorkout() {
        if (mConnected) {
            // Add log entry
            mModel.addLogEntry(currentWorkout.getWorkoutTitle(), reps, sets,
                    currentWorkout.getWorkTime(), currentWorkout.getRestTime(),
                    currentWorkout.getBreakTime(), currentWorkout.getAngles().get(0),
                    currentWorkout.getDepths().get(0), avgWeight, actualWorkTime, score,
                    Calendar.getInstance().getTime(),
                    addWorkout.getDialog().findViewById(R.id.logDFNotes).toString());
        }
        else {
            // Add log entry
            mModel.addLogEntry(currentWorkout.getWorkoutTitle(), reps, sets,
                    currentWorkout.getWorkTime(), currentWorkout.getRestTime(),
                    currentWorkout.getBreakTime(), currentWorkout.getAngles().get(0),
                    currentWorkout.getDepths().get(0), 0, 0, 0,
                    Calendar.getInstance().getTime(),
                    addWorkout.getDialog().findViewById(R.id.logDFNotes).toString());
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); // run in portrait or landscape
    }
}