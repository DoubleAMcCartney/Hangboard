package com.example.android.hangboard;

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
public abstract class AppDatabase extends RoomDatabase {
    public abstract WorkoutDAO getWorkoutDAO();

    private static AppDatabase INSTANCE;

    public synchronized static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = buildDatabase(context);
        }
        return INSTANCE;
    }

    static AppDatabase buildDatabase(final Context context) {
        final AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "my-database")
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

        return db;
    }
}
