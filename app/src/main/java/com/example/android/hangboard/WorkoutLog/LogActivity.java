/*
This is the workout lot activity. This is yet to be implemented. It will provide the users a way to
view completed workouts and track their progress.
 */

package com.example.android.hangboard.WorkoutLog;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.ActivityInfo;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.example.android.hangboard.ChooseWorkout.WorkoutListAdapter;
import com.example.android.hangboard.R;
import com.example.android.hangboard.WorkoutLogDB.WorkoutLog;

import java.util.List;

public class LogActivity extends AppCompatActivity {

    private LogViewModel mLogViewModel;
    private LinearLayoutManager mLayoutManager;
    private WorkoutLogListAdapter workoutLogListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // run only in portrait mode
        setContentView(R.layout.activity_log); // Set the layout

        // Add toolbar
        final Toolbar myToolbar = findViewById(R.id.workoutLog_toolbar);
        setSupportActionBar(myToolbar);
        setTitle("Workout Log");

        // definitions
        final RecyclerView workoutLogRecyclerView = findViewById(R.id.rvWorkoutLog);
        workoutLogListAdapter = new WorkoutLogListAdapter();
        workoutLogRecyclerView.setAdapter(workoutLogListAdapter);
        mLayoutManager = new LinearLayoutManager(workoutLogRecyclerView.getContext());
        workoutLogRecyclerView.setLayoutManager(mLayoutManager);

        // set the view model
        mLogViewModel = ViewModelProviders.of(this).get(LogViewModel.class);

        mLogViewModel.getAllWorkouts().observe(this, new Observer<List<WorkoutLog>>() {
            @Override
            public void onChanged(@Nullable List<WorkoutLog> workouts) {
                // Update the cached copy of the words in the adapter.
                workoutLogListAdapter.submitList(workouts);
            }
        });
    }

}
