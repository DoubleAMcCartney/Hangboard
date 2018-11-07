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
import android.widget.NumberPicker;

import java.util.Arrays;
import java.util.List;

public class ViewWorkoutsActivity extends AppCompatActivity {

    private ViewWorkoutsViewModel mViewWorkoutsViewModel;
    private FloatingActionButton fab = findViewById(R.id.fab);
    private Workout newWorkout;

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

        // calculate height and width and placement
        int margin = 20;
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        lp.copyFrom(AddWorkout.getDialog().getWindow().getAttributes());
        lp.width = screenWidth - margin;
        lp.height = screenHeight - margin;
        lp.x = -margin;
        lp.y = margin;
        AddWorkout.getDialog().getWindow().setAttributes(lp);

        NumberPicker setsNP = findViewById(R.id.setsNumberPicker);
        NumberPicker repsNP = findViewById(R.id.repsNumberPicker);
        NumberPicker exercisesNP = findViewById(R.id.exercisesNumberPicker);
        NumberPicker workNP = findViewById(R.id.workNumberPicker);
        NumberPicker restNP = findViewById(R.id.restNumberPicker);
        NumberPicker breakNP = findViewById(R.id.breakNumberPicker);

        setsNP.setMinValue(1);
        repsNP.setMinValue(1);
        exercisesNP.setMinValue(1);
        workNP.setMinValue(1);
        restNP.setMinValue(1);
        breakNP.setMinValue(1);
        setsNP.setMaxValue(10);
        repsNP.setMaxValue(10);
        exercisesNP.setMaxValue(10);
        workNP.setMaxValue(60);
        restNP.setMaxValue(60);
        breakNP.setMaxValue(60);

        newWorkout = new Workout("Intermediate", 6, 5, 1, 7000, 3000, 240000,
                        Arrays.asList(0), Arrays.asList(10));
        // TODO: set setOnValueChangedListener for each numberPicker
    }

    void addWorkout() {
        if (newWorkout!=null) {
            if (newWorkout.isValid()){
                mViewWorkoutsViewModel.addWorkout(newWorkout);
            }
        }
    }




}
