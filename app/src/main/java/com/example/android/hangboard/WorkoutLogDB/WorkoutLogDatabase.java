package com.example.android.hangboard.WorkoutLogDB;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import com.example.android.hangboard.WorkoutDB.Workout;

import java.util.concurrent.Executors;

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
        final WorkoutLogDatabase db = Room.databaseBuilder(context, WorkoutLogDatabase.class, "log-database")
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                getInstance(context).getWorkoutLogDAO().insert(WorkoutLog.populateData());
                            }
                        });
                    }
                }).allowMainThreadQueries().build();
        db.beginTransaction();
        db.endTransaction();
        return db;
    }
}
