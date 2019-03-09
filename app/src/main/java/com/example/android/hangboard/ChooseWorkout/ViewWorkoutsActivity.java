/*
This is the ViewWorkoutsActivity class. It controls the UI for the view workouts activity. This
activity displays a list of workouts and provides the user a way to create new workouts. When a
workout is chosen, it is passed back to the timer activity.
 */

package com.example.android.hangboard.ChooseWorkout;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;

import com.example.android.hangboard.R;
import com.example.android.hangboard.WorkoutDB.Workout;

import java.util.Arrays;
import java.util.List;

public class ViewWorkoutsActivity extends AppCompatActivity {

    private ViewWorkoutsViewModel mViewWorkoutsViewModel;
    private Workout newWorkout;
    private LinearLayoutManager mLayoutManager;
    private NumberPicker angleNP;
    private NumberPicker depthNP;
    private Button positiveButton;
    private WorkoutListAdapter workoutListAdapter;
    private ExerciseListAdapter exerciseListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Pass savedInstanceState to super class

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // run only in portrait mode
        setContentView(R.layout.activity_edit_workout); // Set the layout

        // definitions
        final RecyclerView workoutRecyclerView = findViewById(R.id.rvWorkouts);
        FloatingActionButton fab = findViewById(R.id.fab);
        workoutListAdapter = new WorkoutListAdapter();
        workoutRecyclerView.setAdapter(workoutListAdapter);
        mLayoutManager = new LinearLayoutManager(workoutRecyclerView.getContext());
        workoutRecyclerView.setLayoutManager(mLayoutManager);

        // add touch and click listeners
        workoutRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                workoutRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                final Button button = view.findViewById(R.id.workoutOptionsButton);
                final Button workoutButton = view.findViewById(R.id.workoutButton);

                // on click listener for the workout items
                workoutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // When a workout is taped, send it to the timer activity
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("result",workoutListAdapter.getWorkout(position).getWorkoutTitle());
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    }
                });

                // on click listener for the options menu of the workout items
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popup = new PopupMenu(workoutRecyclerView.getContext(), button);

                        popup.inflate(R.menu.workout_menu);

                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.itemDelete:
                                        // Delete workout from the database
                                        mViewWorkoutsViewModel.deleteWorkout(workoutListAdapter.getWorkout(position));
                                        return true;
                                }
                                return false;
                            }
                        });
                        popup.show();
                    }
                });
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        // set the view model
        mViewWorkoutsViewModel = ViewModelProviders.of(this).get(ViewWorkoutsViewModel.class);

        mViewWorkoutsViewModel.getAllWorkouts().observe(this, new Observer<List<Workout>>() {
            @Override
            public void onChanged(@Nullable List<Workout> workouts) {
                // Update the cached copy of the workouts in the adapter.
                workoutListAdapter.submitList(workouts);
            }
        });

        // use the fab button for adding workouts
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewWorkout();
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
        // add animation to the changing of activities
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    void createNewWorkout() {
        // create and show dialog
        DialogFragment addWorkout = new AddWorkoutDialogFragment();
        addWorkout.show(getSupportFragmentManager(), "AddWorkout");
        getSupportFragmentManager().executePendingTransactions();

        newWorkout = new Workout("", 1, 1, 0, 1000, 1000, 60000,
                Arrays.asList(0), Arrays.asList(0));

        // Define UI components
        NumberPicker repsNP = addWorkout.getDialog().findViewById(R.id.repsNumberPicker);
        NumberPicker setsNP = addWorkout.getDialog().findViewById(R.id.setsNumberPicker);
        NumberPicker workNP = addWorkout.getDialog().findViewById(R.id.workNumberPicker);
        NumberPicker restNP = addWorkout.getDialog().findViewById(R.id.restNumberPicker);
        NumberPicker breakNP = addWorkout.getDialog().findViewById(R.id.breakNumberPicker);
        angleNP = addWorkout.getDialog().findViewById(R.id.addExAngleNP);
        depthNP = addWorkout.getDialog().findViewById(R.id.addExDepthNP);
        EditText workoutTitleET = addWorkout.getDialog().findViewById(R.id.titleEditText);
        ImageButton addExButton = addWorkout.getDialog().findViewById(R.id.addExerciseButton);
        RecyclerView exerciseRecyclerView = addWorkout.getDialog().findViewById(R.id.exercisesRecyclerView);
        positiveButton = ((AlertDialog) addWorkout.getDialog()).getButton(Dialog.BUTTON_POSITIVE);

        // Set listeners
        repsNP.setOnValueChangedListener(repsNPListener);
        setsNP.setOnValueChangedListener(setsNPListener);
        workNP.setOnValueChangedListener(workNPListener);
        restNP.setOnValueChangedListener(restNPListener);
        breakNP.setOnValueChangedListener(breakNPListener);
        workoutTitleET.addTextChangedListener(workoutTitleETListener);
        addExButton.setOnClickListener(addExButtonListener);

        // Set MinValue for each number picker
        repsNP.setMinValue(1);
        setsNP.setMinValue(1);
        workNP.setMinValue(1);
        restNP.setMinValue(1);
        breakNP.setMinValue(1);
        angleNP.setMinValue(0);
        depthNP.setMinValue(0);

        // Set MaxValues for each number picker
        repsNP.setMaxValue(10);
        setsNP.setMaxValue(10);
        workNP.setMaxValue(60);
        restNP.setMaxValue(60);
        breakNP.setMaxValue(60);
        depthNP.setMaxValue(100);
        angleNP.setMaxValue(60);

        // Set the number pickers to not wrap around
        repsNP.setWrapSelectorWheel(false);
        setsNP.setWrapSelectorWheel(false);
        workNP.setWrapSelectorWheel(false);
        restNP.setWrapSelectorWheel(false);
        breakNP.setWrapSelectorWheel(false);
        angleNP.setWrapSelectorWheel(false);
        depthNP.setWrapSelectorWheel(false);

        // Set formatters for the number pickers
        workNP.setFormatter(secFormatter);
        restNP.setFormatter(secFormatter);
        breakNP.setFormatter(minFormatter);
        depthNP.setFormatter(mmFormatter);
        angleNP.setFormatter(degFormatter);

        // Below is a workaround to fix an android bug that causes the first value of number pickers
        //      to not format until touched.
        View firstItem = workNP.getChildAt(0);
        if (firstItem != null) {
            firstItem.setVisibility(View.INVISIBLE);
        }
        firstItem = restNP.getChildAt(0);
        if (firstItem != null) {
            firstItem.setVisibility(View.INVISIBLE);
        }
        firstItem = breakNP.getChildAt(0);
        if (firstItem != null) {
            firstItem.setVisibility(View.INVISIBLE);
        }
        firstItem = depthNP.getChildAt(0);
        if (firstItem != null) {
            firstItem.setVisibility(View.INVISIBLE);
        }
        firstItem = angleNP.getChildAt(0);
        if (firstItem != null) {
            firstItem.setVisibility(View.INVISIBLE);
        }

        // set exercise list
        exerciseListAdapter = new ExerciseListAdapter(addWorkout.getDialog().getContext());
        exerciseRecyclerView.setAdapter(exerciseListAdapter);
        mLayoutManager = new LinearLayoutManager(exerciseRecyclerView.getContext());
        exerciseRecyclerView.setLayoutManager(mLayoutManager);
        exerciseRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                exerciseRecyclerView, new ClickListener() {
            // set click listeners for delete exercise buttons
            @Override
            public void onClick(View view, final int position) {
                ImageButton delete = view.findViewById(R.id.deleteExerciseIB);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // delete the exercise
                        exerciseListAdapter.deleteExercise(position);
                        // dis/enable add workout button according to if workout is valid
                        if (mViewWorkoutsViewModel.isValid(newWorkout)){
                            positiveButton.setEnabled(true);
                        }
                        else {
                            positiveButton.setEnabled(false);
                        }
                    }
                });
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }

    void addWorkout() {
        if (newWorkout!=null) {
            if (mViewWorkoutsViewModel.isValid(newWorkout)){
                mViewWorkoutsViewModel.addWorkout(newWorkout);
            }
        }
    }

    // Add "sec" to the end of each value in number picker
    NumberPicker.Formatter secFormatter = new NumberPicker.Formatter(){
        @Override
        public String format(int i) {
            return i + "sec";
        }
    };

    // Add "min" to the end of each value in number picker
    NumberPicker.Formatter minFormatter = new NumberPicker.Formatter(){
        @Override
        public String format(int i) {
            return i + "min";
        }
    };

    // Add "°" to the end of each value in number picker
    NumberPicker.Formatter degFormatter = new NumberPicker.Formatter(){
        @Override
        public String format(int i) {
            return i + "°";
        }
    };

    // Add "mm" to the end of each value in number picker
    NumberPicker.Formatter mmFormatter = new NumberPicker.Formatter(){
        @Override
        public String format(int i) {
            return i + "mm";
        }
    };

    NumberPicker.OnValueChangeListener setsNPListener =
            new 	NumberPicker.OnValueChangeListener(){
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    newWorkout.setSets(numberPicker.getValue());
                    // dis/enable add workout button according to if workout is valid
                    if (mViewWorkoutsViewModel.isValid(newWorkout)){
                        positiveButton.setEnabled(true);
                    }
                    else {
                        positiveButton.setEnabled(false);
                    }
                }
            };

    NumberPicker.OnValueChangeListener repsNPListener =
            new 	NumberPicker.OnValueChangeListener(){
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    newWorkout.setReps(numberPicker.getValue());
                    // dis/enable add workout button according to if workout is valid
                    if (mViewWorkoutsViewModel.isValid(newWorkout)){
                        positiveButton.setEnabled(true);
                    }
                    else {
                        positiveButton.setEnabled(false);
                    }
                }
            };

    NumberPicker.OnValueChangeListener workNPListener =
            new 	NumberPicker.OnValueChangeListener(){
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    newWorkout.setWorkTime(numberPicker.getValue()*1000); // milliseconds to seconds
                    // dis/enable add workout button according to if workout is valid
                    if (mViewWorkoutsViewModel.isValid(newWorkout)){
                        positiveButton.setEnabled(true);
                    }
                    else {
                        positiveButton.setEnabled(false);
                    }
                }
            };

    NumberPicker.OnValueChangeListener restNPListener =
            new 	NumberPicker.OnValueChangeListener(){
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    newWorkout.setRestTime(numberPicker.getValue()*1000); // milliseconds to seconds
                    // dis/enable add workout button according to if workout is valid
                    if (mViewWorkoutsViewModel.isValid(newWorkout)){
                        positiveButton.setEnabled(true);
                    }
                    else {
                        positiveButton.setEnabled(false);
                    }
                }
            };

    NumberPicker.OnValueChangeListener breakNPListener =
            new 	NumberPicker.OnValueChangeListener(){
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    newWorkout.setBreakTime(numberPicker.getValue()*60000); // milliseconds to min
                    // dis/enable add workout button according to if workout is valid
                    if (mViewWorkoutsViewModel.isValid(newWorkout)){
                        positiveButton.setEnabled(true);
                    }
                    else {
                        positiveButton.setEnabled(false);
                    }
                }
            };

    TextWatcher workoutTitleETListener =
            new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Fires right as the text is being changed (even supplies the range of text)
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
        int after) {
            // Fires right before text is changing
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Fires right after the text has changed
            newWorkout.setWorkoutTitle(s.toString());
            // dis/enable add workout button according to if workout is valid
            if (mViewWorkoutsViewModel.isValid(newWorkout)){
                positiveButton.setEnabled(true);
            }
            else {
                positiveButton.setEnabled(false);
            }
        }
    };

    // add exercise button click listener
    ImageButton.OnClickListener addExButtonListener =
            new ImageButton.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Exercise exercise = new Exercise(angleNP.getValue(), depthNP.getValue());
                    exerciseListAdapter.addExercise(exercise);
                    newWorkout.setExercises(exerciseListAdapter.getItemCount());
                    newWorkout.setAngles(exerciseListAdapter.getAngles());
                    newWorkout.setDepths(exerciseListAdapter.getDepths());
                    // dis/enable add workout button according to if workout is valid
                    if (mViewWorkoutsViewModel.isValid(newWorkout)){
                        positiveButton.setEnabled(true);
                    }
                    else {
                        positiveButton.setEnabled(false);
                    }
                }
            };
    /**
     * RecyclerView: Implementing single item click and long press (Part-II)
     *
     * - creating an Interface for single tap and long press
     * - Parameters are its respective view and its position
     * */

    public interface ClickListener{
        void onClick(View view,int position);
        void onLongClick(View view,int position);
    }

    /**
     * RecyclerView: Implementing single item click and long press (Part-II)
     *
     * - creating an innerclass implementing RevyvlerView.OnItemTouchListener
     * - Pass clickListener interface as parameter
     * */

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }


}
