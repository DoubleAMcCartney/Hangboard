package com.example.android.hangboard;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.CountDownTimer;

public class WorkoutViewModel extends ViewModel {
    private CountDownTimer timer;

    // Define  LiveData
    private MutableLiveData<Integer> mPrepareTime;
    private MutableLiveData<Integer> mWorkTime;
    private MutableLiveData<Integer> mBreakTime;
    private MutableLiveData<Integer> mRestTime;
    private MutableLiveData<Boolean> mTimerStarted;
    private MutableLiveData<String> mTimerState;
    private MutableLiveData<Long> mTimerValue;
    private MutableLiveData<Integer> mCurrentRep;
    private MutableLiveData<Integer> mCurrentSet;
    private MutableLiveData<Integer> mCurrentExercise;
    private MutableLiveData<Integer> mTotalReps;
    private MutableLiveData<Integer> mTotalSets;
    private MutableLiveData<Integer> mTotalExercises;
    private MutableLiveData<Integer> mCurrentAngle;
    private MutableLiveData<Integer> mCurrentDepth;

    //Constructor
    public WorkoutViewModel () {
        getPrepareTime().setValue(10000);
        getWorkTime().setValue(7000);
        getBreakTime().setValue(180000);
        getRestTime().setValue(3000);
        getTimerStarted().setValue(false);
        getTimerState().setValue("Prepare");
        getTimerValue().setValue((long)getPrepareTime().getValue());
        getCurrentRep().setValue(1);
        getCurrentSet().setValue(1);
        getCurrentExercise().setValue(1);
        getTotalSet().setValue(3);
        getTotalRep().setValue(2);
        getTotalExercise().setValue(3);
        getAngle().setValue(0);
        getDepth().setValue(0);
    }

    public MutableLiveData<Integer> getPrepareTime() {
        if (mPrepareTime == null) {
            mPrepareTime = new MutableLiveData<>();
        }
        return mPrepareTime;
    }

    public MutableLiveData<Integer> getWorkTime() {
        if (mWorkTime == null) {
            mWorkTime = new MutableLiveData<>();
        }
        return mWorkTime;
    }

    public MutableLiveData<Integer> getBreakTime() {
        if (mBreakTime == null) {
            mBreakTime = new MutableLiveData<>();
        }
        return mBreakTime;
    }

    public MutableLiveData<Integer> getRestTime() {
        if (mRestTime == null) {
            mRestTime = new MutableLiveData<>();
        }
        return mRestTime;
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

    public MutableLiveData<Integer> getTotalRep() {
        if (mTotalReps == null) {
            mTotalReps = new MutableLiveData<>();
        }
        return mTotalReps;
    }

    public MutableLiveData<Integer> getTotalSet() {
        if (mTotalSets == null) {
            mTotalSets = new MutableLiveData<>();
        }
        return mTotalSets;
    }

    public MutableLiveData<Integer> getTotalExercise() {
        if (mTotalExercises == null) {
            mTotalExercises = new MutableLiveData<>();
        }
        return mTotalExercises;
    }

    public MutableLiveData<Integer> getAngle() {
        if (mCurrentAngle == null) {
            mCurrentAngle = new MutableLiveData<>();
        }
        return mCurrentAngle;
    }

    public MutableLiveData<Integer> getDepth() {
        if (mCurrentDepth == null) {
            mCurrentDepth = new MutableLiveData<>();
        }
        return mCurrentDepth;
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
        timer.cancel();
        getCurrentRep().setValue(1);
        getCurrentSet().setValue(1);
        getCurrentExercise().setValue(1);
        getTimerValue().setValue((long)getPrepareTime().getValue());
        getTimerState().setValue("Prepare");
        getTimerStarted().setValue(false);
    }

    public void pauseTimer() {
        timer.cancel();
        getTimerStarted().setValue(false);
    }

    private void timerFinished() {

        if (getTimerState().getValue()=="Work") {
            if (getCurrentRep().getValue() < getTotalRep().getValue()) {
                getCurrentRep().setValue(getCurrentRep().getValue()+1);
                getTimerState().setValue("Rest");
                getTimerValue().setValue((long)getRestTime().getValue());
                startTimer();
            }
            else if (getCurrentSet().getValue() < getTotalSet().getValue()) {
                getCurrentSet().setValue(getCurrentSet().getValue()+1);
                getCurrentRep().setValue(1);
                getTimerState().setValue("Break");
                getTimerValue().setValue((long)getBreakTime().getValue());
                startTimer();
            }
            else if (getCurrentExercise().getValue() < getTotalExercise().getValue()) {
                getCurrentExercise().setValue(getCurrentExercise().getValue()+1);
                getCurrentRep().setValue(1);
                getCurrentSet().setValue(1);
                getTimerState().setValue("Break");
                getTimerValue().setValue((long)getBreakTime().getValue());
                startTimer();
            }
            else {
                getCurrentRep().setValue(1);
                getCurrentSet().setValue(1);
                getCurrentExercise().setValue(1);
                getTimerState().setValue("Done");
                getTimerStarted().setValue(false);
            }
        }
        else {
            getTimerState().setValue("Work");
            getTimerValue().setValue((long)getWorkTime().getValue());
            startTimer();
        }
    }
}
