package com.example.android.hangboard;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.CountDownTimer;

import com.example.android.hangboard.WorkoutDB.Workout;
import com.example.android.hangboard.WorkoutDB.WorkoutRepository;

public class TimerViewModel extends AndroidViewModel {
    private CountDownTimer timer;
    private int workTime;
    private int breakTime;
    private int restTime;
    private int totalReps;
    private int totalSets;
    private int totalExercises;
    private WorkoutRepository repository;

    // Define  LiveData
    private LiveData<Workout> mCurrentWorkout;
    private MutableLiveData<Boolean> mConnected;
    private MutableLiveData<Integer> mPrepareTime;
    private MutableLiveData<Boolean> mTimerStarted;
    private MutableLiveData<String> mTimerState;
    private MutableLiveData<Long> mTimerValue;
    private MutableLiveData<Long> mTimeRemaining;
    private MutableLiveData<Integer> mCurrentRep;
    private MutableLiveData<Integer> mCurrentSet;
    private MutableLiveData<Integer> mCurrentExercise;

    //Constructor
    public TimerViewModel(Application application) {
        super(application);
        repository = new WorkoutRepository(application);

        mCurrentWorkout = repository.getWorkoutWithTitle("Intermediate");

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

    public MutableLiveData<Long> getTimeRemaining() {
        if (mTimeRemaining == null) {
            mTimeRemaining = new MutableLiveData<>();
        }
        return mTimeRemaining;
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
        return mCurrentWorkout;
    }

    public LiveData<Workout> getWorkoutByTitle(String workoutTitle) {
        this.mCurrentWorkout = this.repository.getWorkoutWithTitle(workoutTitle);
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

    void setTimeRemaining() {
        long r = ((totalExercises - getCurrentExercise().getValue()+1) * (totalSets - getCurrentSet().getValue()+1) * (((totalReps - getCurrentRep().getValue()+1) * (workTime + restTime) - restTime) + breakTime) - breakTime)+getPrepareTime().getValue();
        getTimeRemaining().setValue(r);
    }


    public void startTimer() {
        getTimerStarted().setValue(true);
        timer = new CountDownTimer(getTimerValue().getValue(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                getTimerValue().setValue(millisUntilFinished);
                getTimeRemaining().setValue(getTimeRemaining().getValue()-1000);
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
        setTimeRemaining();
    }

    public void skipTimer() {
        if (timer != null) {
            timer.cancel();
        }
        mTimeRemaining.setValue(mTimeRemaining.getValue()-mTimerValue.getValue());
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
            }
            else if (getCurrentSet().getValue() < totalSets) {
                getCurrentSet().setValue(getCurrentSet().getValue()+1);
                getCurrentRep().setValue(1);
                getTimerState().setValue("Break");
                getTimerValue().setValue((long) breakTime);
            }
            else if (getCurrentExercise().getValue() < totalExercises) {
                getCurrentExercise().setValue(getCurrentExercise().getValue()+1);
                getCurrentRep().setValue(1);
                getCurrentSet().setValue(1);
                getTimerState().setValue("Break");
                getTimerValue().setValue((long) breakTime);
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
            setTimeRemaining();
        }
        else {
            getTimerState().setValue("Work");
            getTimerValue().setValue((long) workTime);
        }
        if (getTimerStarted().getValue()) startTimer();
    }
}
