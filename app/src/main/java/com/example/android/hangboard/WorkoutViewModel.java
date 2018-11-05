package com.example.android.hangboard;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.CountDownTimer;

public class WorkoutViewModel extends AndroidViewModel {
    private CountDownTimer timer;
    private int workTime;
    private int breakTime;
    private int restTime;
    private int totalReps;
    private int totalSets;
    private int totalExercises;

    // Define  LiveData
    private LiveData<Workout> mCurrentWorkout;
    private MutableLiveData<Boolean> mConnected;
    private MutableLiveData<Integer> mPrepareTime;
    private MutableLiveData<Boolean> mTimerStarted;
    private MutableLiveData<String> mTimerState;
    private MutableLiveData<Long> mTimerValue;
    private MutableLiveData<Integer> mCurrentRep;
    private MutableLiveData<Integer> mCurrentSet;
    private MutableLiveData<Integer> mCurrentExercise;

    //Constructor
    public WorkoutViewModel (Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(this.getApplication());
        WorkoutDAO mWorkoutDAO = db.getWorkoutDAO();

        mCurrentWorkout = mWorkoutDAO.getWorkoutWithTitle("Intermediate");

        getConnected().setValue(false);
        getPrepareTime().setValue(10000);
        getTimerStarted().setValue(false);
        getTimerState().setValue("Prepare");
        getTimerValue().setValue((long)getPrepareTime().getValue());
        getCurrentRep().setValue(1);
        getCurrentSet().setValue(1);
        getCurrentExercise().setValue(1);
    }

    public MutableLiveData<Boolean> getConnected() {
        if (mConnected == null) {
            mConnected = new MutableLiveData<>();
        }
        return mConnected;
    }

    public MutableLiveData<Integer> getPrepareTime() {
        if (mPrepareTime == null) {
            mPrepareTime = new MutableLiveData<>();
        }
        return mPrepareTime;
    }

    public MutableLiveData<Boolean> getTimerStarted() {
        if (mTimerStarted == null) {
            mTimerStarted = new MutableLiveData<>();
        }
        return mTimerStarted;
    }

    public MutableLiveData<String> getTimerState() {
        if (mTimerState == null) {
            mTimerState = new MutableLiveData<>();
        }
        return mTimerState;
    }

    public MutableLiveData<Long> getTimerValue() {
        if (mTimerValue == null) {
            mTimerValue = new MutableLiveData<>();
        }
        return mTimerValue;
    }

    public MutableLiveData<Integer> getCurrentRep() {
        if (mCurrentRep == null) {
            mCurrentRep = new MutableLiveData<>();
        }
        return mCurrentRep;
    }

    public MutableLiveData<Integer> getCurrentSet() {
        if (mCurrentSet == null) {
            mCurrentSet = new MutableLiveData<>();
        }
        return mCurrentSet;
    }

    public MutableLiveData<Integer> getCurrentExercise() {
        if (mCurrentExercise == null) {
            mCurrentExercise = new MutableLiveData<>();
        }
        return mCurrentExercise;
    }

    public LiveData<Workout> getWorkout() {
        if (mCurrentWorkout == null) {
            mCurrentWorkout = new MutableLiveData<>();
        }
        return mCurrentWorkout;
    }

    void setTotalRep(int i) {
        totalReps = i;
    }

    void setTotalSet(int i) {
        totalSets = i;
    }

    void setTotalExercises(int i) {
        totalExercises = i;
    }

    void setWorkTime(int i) {
        workTime = i;
    }

    void setBreakTime(int i) {
        breakTime = i;
    }

    void setRestTime(int i) {
        restTime = i;
    }


    public void startTimer() {
        getTimerStarted().setValue(true);
        timer = new CountDownTimer(getTimerValue().getValue(), 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                getTimerValue().setValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timerFinished();
            }
        }.start();
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
        getCurrentRep().setValue(1);
        getCurrentSet().setValue(1);
        getCurrentExercise().setValue(1);
        getTimerValue().setValue((long)getPrepareTime().getValue());
        getTimerState().setValue("Prepare");
        getTimerStarted().setValue(false);
    }

    public void skipTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timerFinished();
    }

    public void pauseTimer() {
        timer.cancel();
        getTimerStarted().setValue(false);
    }

    private void timerFinished() {

        if (getTimerState().getValue()=="Work") {
            if (getCurrentRep().getValue() < totalReps) {
                getCurrentRep().setValue(getCurrentRep().getValue()+1);
                getTimerState().setValue("Rest");
                getTimerValue().setValue((long) restTime);
                if (getTimerStarted().getValue()) startTimer();
            }
            else if (getCurrentSet().getValue() < totalSets) {
                getCurrentSet().setValue(getCurrentSet().getValue()+1);
                getCurrentRep().setValue(1);
                getTimerState().setValue("Break");
                getTimerValue().setValue((long) breakTime);
                if (getTimerStarted().getValue()) startTimer();
            }
            else if (getCurrentExercise().getValue() < totalExercises) {
                getCurrentExercise().setValue(getCurrentExercise().getValue()+1);
                getCurrentRep().setValue(1);
                getCurrentSet().setValue(1);
                getTimerState().setValue("Break");
                getTimerValue().setValue((long) breakTime);
                if (getTimerStarted().getValue()) startTimer();
            }
            else {
                getCurrentRep().setValue(1);
                getCurrentSet().setValue(1);
                getCurrentExercise().setValue(1);
                getTimerState().setValue("Done");
                getTimerStarted().setValue(false);
            }
        }
        else if (getTimerState().getValue()=="Done"){
            getTimerState().setValue("Prepare");
            getTimerValue().setValue((long)getPrepareTime().getValue());
            if (getTimerStarted().getValue()) startTimer();
        }
        else {
            getTimerState().setValue("Work");
            getTimerValue().setValue((long) workTime);
            if (getTimerStarted().getValue()) startTimer();
        }
    }
}
