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
