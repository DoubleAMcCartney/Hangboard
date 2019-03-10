package com.example.android.hangboard.WorkoutDB;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.android.hangboard.WorkoutDB.Workout;

import java.util.List;

@Dao
public interface WorkoutDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Workout... workouts);

    @Update
    void update(Workout... workouts);

    @Delete
    void delete(Workout workout);

    @Query("SELECT * FROM workout")
    LiveData<List<Workout>> getWorkouts();

    @Query("SELECT * FROM workout WHERE workoutTitle = :Id")
    LiveData<Workout> getWorkoutWithId(int Id);

    @Query("SELECT * FROM workout WHERE workoutTitle = :title")
    LiveData<Workout> getWorkoutWithTitle(String title);

    @Query("SELECT * FROM workout WHERE workoutTitle = :title")
    Workout getWorkoutWithTitleDirect(String title);

    @Query("SELECT * FROM workout WHERE workoutTitle = :Id")
    Workout getWorkoutWithIdDirect(int Id);

}
