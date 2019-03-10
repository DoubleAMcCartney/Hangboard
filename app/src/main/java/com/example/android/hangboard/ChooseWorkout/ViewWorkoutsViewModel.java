/*
The ViewWorkoutsViewModel provides the ViewWorkoutsActivity a way to interact with the workouts
database. It connects directly to the workouts repository and provides the activity with live data.
 */

package com.example.android.hangboard.ChooseWorkout;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.example.android.hangboard.WorkoutDB.Workout;
import com.example.android.hangboard.WorkoutDB.WorkoutRepository;

import java.util.List;

public class ViewWorkoutsViewModel extends AndroidViewModel {
    private WorkoutRepository repository;
    private LiveData<List<Workout>> allWorkouts;

    // constructor
    public ViewWorkoutsViewModel (Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        allWorkouts = repository.getAllWorkouts();
    }

    // get all workouts from the database
    LiveData<List<Workout>> getAllWorkouts() {
        return allWorkouts;
    }

    // add a workout to the database
    void addWorkout(Workout workout) {
        repository.insert(workout);
    }

    // delete a workout from the database
    void deleteWorkout(Workout workout) {
        repository.delete(workout);
    }

    // edit a workout in the database
    void updateWorkout(Workout workout) {
        repository.update(workout);
    }

}
