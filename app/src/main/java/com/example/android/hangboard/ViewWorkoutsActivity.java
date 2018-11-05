package com.example.android.hangboard;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

public class ViewWorkoutsActivity extends AppCompatActivity {

    private ViewModel mViewWorkoutsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout);

        RecyclerView recyclerView = findViewById(R.id.rvWorkouts);
        final WorkoutListAdapter adapter = new WorkoutListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mViewWorkoutsViewModel = ViewModelProviders.of(this).get(ViewWorkoutsViewModel.class);

        ((ViewWorkoutsViewModel) mViewWorkoutsViewModel).getAllWorkouts().observe(this, new Observer<List<Workout>>() {
            @Override
            public void onChanged(@Nullable final List<Workout> workouts) {
                // Update the cached copy of the words in the adapter.
                adapter.setWorkouts(workouts);
            }
        });

    }
}
