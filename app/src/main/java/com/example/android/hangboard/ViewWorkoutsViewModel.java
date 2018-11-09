package com.example.android.hangboard;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class ViewWorkoutsViewModel extends AndroidViewModel {
    private LiveData<List<Workout>> mWorkouts;
    private WorkoutDAO mWorkoutDAO;

    public ViewWorkoutsViewModel (Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(this.getApplication());
        mWorkoutDAO = db.getWorkoutDAO();
        mWorkouts = mWorkoutDAO.getWorkouts();
    }

    LiveData<List<Workout>> getAllWorkouts() {
        return mWorkouts;
    }

    void addWorkout(Workout workout) {
        new insertAsyncTask(mWorkoutDAO).execute(workout);
    }

    public boolean isValid(Workout workout) {
        Workout test = mWorkoutDAO.getWorkoutWithTitleDirect(workout.getWorkoutTitle());
        return (workout.getReps()!=0)&(workout.getSets()!=0)&(workout.getExercises()!=0)&
                (workout.getRestTime()!=0)&(workout.getWorkTime()!=0)&(workout.getSets()!=0)&
                (workout.getAngles().size()==workout.getExercises())&(workout.getDepths().size()==workout.getExercises())&
                (workout.getWorkoutTitle()!="")
                &(test==null);
    }

    private static class insertAsyncTask extends AsyncTask<Workout, Void, Void> {

        private WorkoutDAO mAsyncTaskDao;

        insertAsyncTask(WorkoutDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Workout... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
