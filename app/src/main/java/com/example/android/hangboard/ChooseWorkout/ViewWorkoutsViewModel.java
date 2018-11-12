package com.example.android.hangboard.ChooseWorkout;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.example.android.hangboard.WorkoutDB.WorkoutDatabase;
import com.example.android.hangboard.WorkoutDB.Workout;
import com.example.android.hangboard.WorkoutDB.WorkoutDAO;
import com.example.android.hangboard.WorkoutDB.WorkoutRepository;

import java.util.List;

public class ViewWorkoutsViewModel extends AndroidViewModel {
    private WorkoutRepository repository;
    private LiveData<List<Workout>> allWorkouts;

    public ViewWorkoutsViewModel (Application application) {
        super(application);
        repository = new WorkoutRepository(application);
        allWorkouts = repository.getAllWorkouts();
    }

    LiveData<List<Workout>> getAllWorkouts() {
        return allWorkouts;
    }

    void addWorkout(Workout workout) {
        repository.insert(workout);
    }

    void deleteWorkout(Workout workout) {
        repository.delete(workout);
    }

    void updateWorkout(Workout workout) {
        repository.update(workout);
    }

    public boolean isValid(Workout workout) {
        Workout test = repository.getWorkoutWithTitleDirect(workout.getWorkoutTitle());
        return (workout.getReps()!=0)&(workout.getSets()!=0)&(workout.getExercises()!=0)&
                (workout.getRestTime()!=0)&(workout.getWorkTime()!=0)&(workout.getSets()!=0)&
                (workout.getAngles().size()==workout.getExercises())&(workout.getDepths().size()==workout.getExercises())&
                (workout.getWorkoutTitle()!="")
                &(test==null);
    }

}
