package com.example.android.hangboard.WorkoutDB;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.Executors;

@Database(entities = {Workout.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class WorkoutDatabase extends RoomDatabase {
    public abstract WorkoutDAO getWorkoutDAO();

    private static WorkoutDatabase INSTANCE;

    public synchronized static WorkoutDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = buildDatabase(context);
        }
        return INSTANCE;
    }

    static WorkoutDatabase buildDatabase(final Context context) {
        final WorkoutDatabase db = Room.databaseBuilder(context, WorkoutDatabase.class, "my-database")
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                getInstance(context).getWorkoutDAO().insert(Workout.populateData());
                            }
                        });
                    }
                }).allowMainThreadQueries().build();
                db.beginTransaction();
                db.endTransaction();
        return db;
    }
}
