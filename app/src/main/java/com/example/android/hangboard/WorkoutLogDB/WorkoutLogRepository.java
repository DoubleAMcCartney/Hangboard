package com.example.android.hangboard.WorkoutLogDB;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class WorkoutLogRepository {
    private WorkoutLogDAO WorkoutLogDAO;
    private LiveData<List<WorkoutLog>> allWorkouts;

    public WorkoutLogRepository(Application application) {
        WorkoutLogDatabase database = WorkoutLogDatabase.getInstance(application);
        WorkoutLogDAO = database.getWorkoutLogDAO();
        allWorkouts = WorkoutLogDAO.getWorkouts();
    }

    public void insert(WorkoutLog Workout) {
        new InsertWorkoutAsyncTask(WorkoutLogDAO).execute(Workout);
    }

    public void update(WorkoutLog Workout) {
        new UpdateWorkoutAsyncTask(WorkoutLogDAO).execute(Workout);
    }

    public void delete(WorkoutLog Workout) {
        new DeleteWorkoutAsyncTask(WorkoutLogDAO).execute(Workout);
    }

    public LiveData<List<WorkoutLog>> getAllWorkouts() {
        return allWorkouts;
    }

    public LiveData<WorkoutLog> getWorkoutWithTitle(String title) {
        return WorkoutLogDAO.getWorkoutWithTitle(title);
    }

    public WorkoutLog getWorkoutWithTitleDirect(String title) {
        return WorkoutLogDAO.getWorkoutWithTitleDirect(title);
    }

    private static class InsertWorkoutAsyncTask extends AsyncTask<WorkoutLog, Void, Void> {
        private WorkoutLogDAO WorkoutLogDAO;

        private InsertWorkoutAsyncTask(WorkoutLogDAO WorkoutLogDAO) {
            this.WorkoutLogDAO = WorkoutLogDAO;
        }

        @Override
        protected Void doInBackground(WorkoutLog... Workouts) {
            WorkoutLogDAO.insert(Workouts[0]);
            return null;
        }
    }

    private static class UpdateWorkoutAsyncTask extends AsyncTask<WorkoutLog, Void, Void> {
        private WorkoutLogDAO WorkoutLogDAO;

        private UpdateWorkoutAsyncTask(WorkoutLogDAO WorkoutLogDAO) {
            this.WorkoutLogDAO = WorkoutLogDAO;
        }

        @Override
        protected Void doInBackground(WorkoutLog... Workouts) {
            WorkoutLogDAO.update(Workouts[0]);
            return null;
        }
    }

    private static class DeleteWorkoutAsyncTask extends AsyncTask<WorkoutLog, Void, Void> {
        private WorkoutLogDAO WorkoutLogDAO;

        private DeleteWorkoutAsyncTask(WorkoutLogDAO WorkoutLogDAO) {
            this.WorkoutLogDAO = WorkoutLogDAO;
        }

        @Override
        protected Void doInBackground(WorkoutLog... Workouts) {
            WorkoutLogDAO.delete(Workouts[0]);
            return null;
        }
    }

}
