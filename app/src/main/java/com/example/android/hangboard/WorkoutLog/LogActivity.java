/*
This is the workout lot activity. This is yet to be implemented. It will provide the users a way to
view completed workouts and track their progress.
 */

package com.example.android.hangboard.WorkoutLog;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.android.hangboard.ChooseWorkout.ViewWorkoutsActivity;
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

        // add touch and click listeners
        workoutLogRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                workoutLogRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                final Button optionsButton = view.findViewById(R.id.workoutLogOptionsButton);
                final Button workoutButton = view.findViewById(R.id.workoutLogButton);

                // on click listener for the workout items
                workoutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // todo: open details of workout
                    }
                });

                // on click listener for the options menu of the workout items
                optionsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popup = new PopupMenu(workoutLogRecyclerView.getContext(), optionsButton);

                        popup.inflate(R.menu.workout_menu);

                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.itemDelete:
                                        // Delete workout from the database
                                        mLogViewModel.deleteWorkout(workoutLogListAdapter.getWorkout(position));
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
        mLogViewModel = ViewModelProviders.of(this).get(LogViewModel.class);

        mLogViewModel.getAllWorkouts().observe(this, new Observer<List<WorkoutLog>>() {
            @Override
            public void onChanged(@Nullable List<WorkoutLog> workouts) {
                // Update the cached copy of the words in the adapter.
                workoutLogListAdapter.submitList(workouts);
            }
        });
    }

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

        private LogActivity.ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final LogActivity.ClickListener clicklistener){

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
