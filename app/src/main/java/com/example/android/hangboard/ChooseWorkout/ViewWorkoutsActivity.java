package com.example.android.hangboard.ChooseWorkout;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
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
import android.view.Display;
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
    private FloatingActionButton fab;
    private Workout newWorkout;
    private LinearLayoutManager mLayoutManager;
    private DialogFragment AddWorkout;

    private NumberPicker repsNP;
    private NumberPicker setsNP;
    private NumberPicker workNP;
    private NumberPicker restNP;
    private NumberPicker breakNP;
    private NumberPicker angleNP;
    private NumberPicker depthNP;
    private EditText workoutTitleET;
    private ImageButton addExButton;
    private RecyclerView exerciseRecyclerView;
    private ExerciseListAdapter exerciseListAdapter;
    private Button positiveButton;
    private WorkoutListAdapter workoutListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // run only in portrait mode
        setContentView(R.layout.activity_edit_workout);

        final RecyclerView workoutRecyclerView = findViewById(R.id.rvWorkouts);
        fab = findViewById(R.id.fab);
        workoutListAdapter = new WorkoutListAdapter();
        workoutRecyclerView.setAdapter(workoutListAdapter);
        mLayoutManager = new LinearLayoutManager(workoutRecyclerView.getContext());
        workoutRecyclerView.setLayoutManager(mLayoutManager);

        workoutRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                workoutRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                final Button button = view.findViewById(R.id.workoutOptionsButton);
                final Button workoutButton = view.findViewById(R.id.workoutButton);

                workoutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("result",workoutListAdapter.getWorkout(position).getWorkoutTitle());
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    }
                });

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

        mViewWorkoutsViewModel = ViewModelProviders.of(this).get(ViewWorkoutsViewModel.class);


        mViewWorkoutsViewModel.getAllWorkouts().observe(this, new Observer<List<Workout>>() {
            @Override
            public void onChanged(@Nullable final List<Workout> workouts) {
                // Update the cached copy of the words in the adapter.
                workoutListAdapter.submitList(workouts);
            }
        });

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
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    void createNewWorkout() {
        AddWorkout = new AddWorkoutDialogFragment();
        AddWorkout.show(getSupportFragmentManager(), "AddWorkout");
        getSupportFragmentManager().executePendingTransactions();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        newWorkout = new Workout("", 1, 1, 0, 1, 1, 1,
                Arrays.asList(0), Arrays.asList(0));

        repsNP = AddWorkout.getDialog().findViewById(R.id.repsNumberPicker);
        setsNP = AddWorkout.getDialog().findViewById(R.id.setsNumberPicker);
        workNP = AddWorkout.getDialog().findViewById(R.id.workNumberPicker);
        restNP = AddWorkout.getDialog().findViewById(R.id.restNumberPicker);
        breakNP = AddWorkout.getDialog().findViewById(R.id.breakNumberPicker);
        angleNP = AddWorkout.getDialog().findViewById(R.id.addExAngleNP);
        depthNP = AddWorkout.getDialog().findViewById(R.id.addExDepthNP);
        workoutTitleET = AddWorkout.getDialog().findViewById(R.id.titleEditText);
        addExButton = AddWorkout.getDialog().findViewById(R.id.addExerciseButton);
        exerciseRecyclerView = AddWorkout.getDialog().findViewById(R.id.exercisesRecyclerView);
        positiveButton = ((AlertDialog)AddWorkout.getDialog()).getButton(Dialog.BUTTON_POSITIVE);

        repsNP.setOnValueChangedListener(repsNPListener);
        setsNP.setOnValueChangedListener(setsNPListener);
        workNP.setOnValueChangedListener(workNPListener);
        restNP.setOnValueChangedListener(restNPListener);
        breakNP.setOnValueChangedListener(breakNPListener);
        workoutTitleET.addTextChangedListener(workoutTitleETListener);
        addExButton.setOnClickListener(addExButtonListener);

        repsNP.setMinValue(1);
        setsNP.setMinValue(1);
        workNP.setMinValue(1);
        restNP.setMinValue(1);
        breakNP.setMinValue(1);
        angleNP.setMinValue(0);
        depthNP.setMinValue(0);
        repsNP.setMaxValue(10);
        setsNP.setMaxValue(10);
        workNP.setMaxValue(60);
        restNP.setMaxValue(60);
        breakNP.setMaxValue(60);
        depthNP.setMaxValue(100);
        angleNP.setMaxValue(60);

        workNP.setFormatter(secFormatter);
        restNP.setFormatter(secFormatter);
        breakNP.setFormatter(minFormatter);
        depthNP.setFormatter(mmFormatter);
        angleNP.setFormatter(degFormatter);

        exerciseListAdapter = new ExerciseListAdapter(AddWorkout.getDialog().getContext());
        exerciseRecyclerView.setAdapter(exerciseListAdapter);
        mLayoutManager = new LinearLayoutManager(exerciseRecyclerView.getContext());
        exerciseRecyclerView.setLayoutManager(mLayoutManager);
        exerciseRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                exerciseRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                ImageButton delete = view.findViewById(R.id.deleteExerciseIB);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        exerciseListAdapter.deleteExercise(position);
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

    NumberPicker.Formatter secFormatter = new NumberPicker.Formatter(){
        @Override
        public String format(int i) {
            return i + "sec";
        }
    };

    NumberPicker.Formatter minFormatter = new NumberPicker.Formatter(){
        @Override
        public String format(int i) {
            return i + "min";
        }
    };

    NumberPicker.Formatter degFormatter = new NumberPicker.Formatter(){
        @Override
        public String format(int i) {
            return i + "°";
        }
    };

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
                    newWorkout.setWorkTime(numberPicker.getValue()*1000);
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
                    newWorkout.setRestTime(numberPicker.getValue()*1000);
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
                    newWorkout.setBreakTime(numberPicker.getValue()*60000);
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
            if (mViewWorkoutsViewModel.isValid(newWorkout)){
                positiveButton.setEnabled(true);
            }
            else {
                positiveButton.setEnabled(false);
            }
        }
    };

    ImageButton.OnClickListener addExButtonListener =
            new ImageButton.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Exercise exercise = new Exercise(angleNP.getValue(), depthNP.getValue());
                    exerciseListAdapter.addExercise(exercise);
                    newWorkout.setExercises(exerciseListAdapter.getItemCount());
                    newWorkout.setAngles(exerciseListAdapter.getAngles());
                    newWorkout.setDepths(exerciseListAdapter.getDepths());
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
