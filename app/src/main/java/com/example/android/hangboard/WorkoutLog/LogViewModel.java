package com.example.android.hangboard.WorkoutLog;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.example.android.hangboard.WorkoutLogDB.WorkoutLog;
import com.example.android.hangboard.WorkoutLogDB.WorkoutLogRepository;

import java.util.List;

public class LogViewModel extends AndroidViewModel {
    private WorkoutLogRepository repository;
    private LiveData<List<WorkoutLog>> allWorkouts;

    // constructor
    public LogViewModel (Application application) {
        super(application);
        repository = new WorkoutLogRepository(application);
        allWorkouts = repository.getAllWorkouts();
    }

    // get all workouts from the database
    LiveData<List<WorkoutLog>> getAllWorkouts() {
        return allWorkouts;
    }

    // add a workout to the database
    void addWorkout(WorkoutLog workoutLog) {
        repository.insert(workoutLog);
    }

    // delete a workout from the database
    void deleteWorkout(WorkoutLog workoutLog) {
        repository.delete(workoutLog);
    }

    // edit a workout in the database
    void updateWorkout(WorkoutLog workoutLog) {
        repository.update(workoutLog);
    }
}

