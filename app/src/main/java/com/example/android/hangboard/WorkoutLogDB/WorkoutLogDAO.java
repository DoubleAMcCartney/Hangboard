package com.example.android.hangboard.WorkoutLogDB;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface WorkoutLogDAO {
    @Insert
    void insert(WorkoutLog... workouts);

    @Update
    void update(WorkoutLog... workouts);

    @Delete
    void delete(WorkoutLog workout);

    @Query("SELECT * FROM workout")
    LiveData<List<WorkoutLog>> getWorkouts();

    @Query("SELECT * FROM workout WHERE workoutTitle = :title")
    LiveData<WorkoutLog> getWorkoutWithTitle(String title);

    @Query("SELECT * FROM workout WHERE workoutTitle = :title")
    WorkoutLog getWorkoutWithTitleDirect(String title);

}
