package com.example.android.hangboard;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;

import java.util.Arrays;
import java.util.List;

public class ViewWorkoutsActivity extends AppCompatActivity {

    private ViewWorkoutsViewModel mViewWorkoutsViewModel;
    private FloatingActionButton fab;
    private Workout newWorkout;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout);

        RecyclerView recyclerView = findViewById(R.id.rvWorkouts);
        fab = findViewById(R.id.fab);
        final WorkoutListAdapter adapter = new WorkoutListAdapter(this);
        recyclerView.setAdapter(adapter);
        mLayoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                mLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        //TODO: Add edit and delete workout options

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
        getSupportFragmentManager().executePendingTransactions();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

//        // calculate height and width and placement
//        // TODO: save below in viewModel or lock portrait
//        int margin = 20;
//        int screenWidth = size.x;
//        int screenHeight = size.y;
//        lp.copyFrom(AddWorkout.getDialog().getWindow().getAttributes());
//        lp.width = screenWidth - margin;
//        lp.height = screenHeight - margin;
//        lp.x = -margin;
//        lp.y = margin;
//        AddWorkout.getDialog().getWindow().setAttributes(lp);

        newWorkout = new Workout("", 0, 0, 0, 0, 0, 0,
                Arrays.asList(0), Arrays.asList(0));

        NumberPicker repsNP = AddWorkout.getDialog().findViewById(R.id.repsNumberPicker);
        NumberPicker setsNP = AddWorkout.getDialog().findViewById(R.id.setsNumberPicker);
        NumberPicker exercisesNP = AddWorkout.getDialog().findViewById(R.id.exercisesNumberPicker);
        NumberPicker workNP = AddWorkout.getDialog().findViewById(R.id.workNumberPicker);
        NumberPicker restNP = AddWorkout.getDialog().findViewById(R.id.restNumberPicker);
        NumberPicker breakNP = AddWorkout.getDialog().findViewById(R.id.breakNumberPicker);

        repsNP.setOnValueChangedListener(repsNPListener);
        setsNP.setOnValueChangedListener(setsNPListener);
        exercisesNP.setOnValueChangedListener(exercisesNPListener);
        workNP.setOnValueChangedListener(workNPListener);
        restNP.setOnValueChangedListener(restNPListener);
        breakNP.setOnValueChangedListener(breakNPListener);

        repsNP.setMinValue(1);
        setsNP.setMinValue(1);
        exercisesNP.setMinValue(1);
        workNP.setMinValue(1);
        restNP.setMinValue(1);
        breakNP.setMinValue(1);
        repsNP.setMaxValue(10);
        setsNP.setMaxValue(10);
        exercisesNP.setMaxValue(10);
        workNP.setMaxValue(60);
        restNP.setMaxValue(60);
        breakNP.setMaxValue(60);

        // TODO: set setOnValueChangedListener for each numberPicker
    }

    void addWorkout() {
        if (newWorkout!=null) {
            if (newWorkout.isValid()){
                mViewWorkoutsViewModel.addWorkout(newWorkout);
            }
        }
    }

    NumberPicker.OnValueChangeListener setsNPListener =
            new 	NumberPicker.OnValueChangeListener(){
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    newWorkout.setSets(numberPicker.getValue());
                }
            };

    NumberPicker.OnValueChangeListener repsNPListener =
            new 	NumberPicker.OnValueChangeListener(){
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    newWorkout.setReps(numberPicker.getValue());
                }
            };

    NumberPicker.OnValueChangeListener exercisesNPListener =
            new 	NumberPicker.OnValueChangeListener(){
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    newWorkout.setExercises(numberPicker.getValue());
                }
            };

    NumberPicker.OnValueChangeListener workNPListener =
            new 	NumberPicker.OnValueChangeListener(){
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    newWorkout.setWorkTime(numberPicker.getValue());
                }
            };

    NumberPicker.OnValueChangeListener restNPListener =
            new 	NumberPicker.OnValueChangeListener(){
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    newWorkout.setRestTime(numberPicker.getValue());
                }
            };

    NumberPicker.OnValueChangeListener breakNPListener =
            new 	NumberPicker.OnValueChangeListener(){
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    newWorkout.setBreakTime(numberPicker.getValue());
                }
            };
}
