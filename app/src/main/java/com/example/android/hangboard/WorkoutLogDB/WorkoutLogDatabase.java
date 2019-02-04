package com.example.android.hangboard.WorkoutLogDB;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {WorkoutLog.class}, version = 1)
public abstract class WorkoutLogDatabase extends RoomDatabase {
    public abstract WorkoutLogDAO getWorkoutLogDAO();

    private static WorkoutLogDatabase INSTANCE;

    public synchronized static WorkoutLogDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = buildDatabase(context);
        }
        return INSTANCE;
    }

    static WorkoutLogDatabase buildDatabase(final Context context) {
        final WorkoutLogDatabase db = Room.databaseBuilder(context, WorkoutLogDatabase.class, "log-database").build();
        db.beginTransaction();
        db.endTransaction();
        return db;
    }
}
