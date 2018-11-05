package com.example.android.hangboard;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class ViewWorkoutsViewModel extends AndroidViewModel {
    private LiveData<List<Workout>> mWorkouts;

    public ViewWorkoutsViewModel (Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(this.getApplication());
        WorkoutDAO mWorkoutDAO = db.getWorkoutDAO();
        mWorkouts = mWorkoutDAO.getWorkouts();
    }

    LiveData<List<Workout>> getAllWorkouts() {
        return mWorkouts;
    }
}
