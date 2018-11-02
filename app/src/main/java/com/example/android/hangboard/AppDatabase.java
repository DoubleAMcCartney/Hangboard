package com.example.android.hangboard;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Workout.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WorkoutDAO getWorkoutDAO();
}
