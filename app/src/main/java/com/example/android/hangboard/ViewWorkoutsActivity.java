package com.example.android.hangboard;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

public class ViewWorkoutsActivity extends AppCompatActivity {

    private ViewWorkoutsViewModel mViewWorkoutsViewModel;
    private FloatingActionButton fab = findViewById(R.id.fab);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout);

        RecyclerView recyclerView = findViewById(R.id.rvWorkouts);
        final WorkoutListAdapter adapter = new WorkoutListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mViewWorkoutsViewModel = ViewModelProviders.of(this).get(ViewWorkoutsViewModel.class);

        mViewWorkoutsViewModel.getAllWorkouts().observe(this, new Observer<List<Workout>>() {
            @Override
            public void onChanged(@Nullable final List<Workout> workouts) {
                // Update the cached copy of the words in the adapter.
                adapter.setWorkouts(workouts);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewWorkout();
            }
        });

    }

    void createNewWorkout() {
        DialogFragment AddWorkout = new AddWorkoutDialogFragment();
        AddWorkout.show(getSupportFragmentManager(), "AddWorkout");
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((ViewWorkoutsActivity) getApplicationContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        // TODO: calculate height and width and placement
        lp.copyFrom(AddWorkout.getDialog().getWindow().getAttributes());
        lp.width = displayMetrics.widthPixels;
        lp.height = displayMetrics.heightPixels;
        lp.x=-170;
        lp.y=100;
        AddWorkout.getDialog().getWindow().setAttributes(lp);
    }

    void addWorkout(Workout newWorkout) {
        mViewWorkoutsViewModel.addWorkout(newWorkout);
    }




}
