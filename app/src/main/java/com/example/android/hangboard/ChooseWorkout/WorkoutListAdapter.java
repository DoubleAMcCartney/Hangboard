/*
Adapter that provides a binding from the Exercise data set to views that are displayed within a
RecyclerView in the AddWorkout Dialog.
 */

package com.example.android.hangboard.ChooseWorkout;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.hangboard.R;
import com.example.android.hangboard.WorkoutDB.Workout;


public class WorkoutListAdapter extends
        ListAdapter<Workout, WorkoutListAdapter.WorkoutViewHolder> {

    public WorkoutListAdapter() {
        super(DIFF_CALLBACK);
    }

    // DiffUtil handles data set changes in the recycler view, so the entire database doesn't
    // get reloaded every time it is changed. This also allows for animations when workouts are
    // added and deleted.
    private static final DiffUtil.ItemCallback<Workout> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Workout>() {
        @Override
        public boolean areItemsTheSame(@NonNull Workout oldItem, @NonNull Workout newItem) {
            return oldItem.getWorkoutTitle().equals(newItem.getWorkoutTitle());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Workout oldItem, @NonNull Workout newItem) {
            return oldItem.getWorkoutTitle().equals(newItem.getWorkoutTitle()) &&
                    oldItem.getAngles().equals(newItem.getAngles()) &&
                    oldItem.getDepths().equals(newItem.getDepths()) &&
                    oldItem.getBreakTime() == newItem.getBreakTime() &&
                    oldItem.getRestTime() == newItem.getRestTime() &&
                    oldItem.getWorkTime() == newItem.getWorkTime() &&
                    oldItem.getReps() == newItem.getReps() &&
                    oldItem.getSets() == newItem.getSets() &&
                    oldItem.getExercises() == newItem.getExercises();
        }
    };


    class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private final TextView workoutItemView;
        private final TextView repItemView;
        private final TextView setItemView;
        private final TextView exerciseItemView;
        private final TextView workTimeItemView;
        private final TextView restTimeItemView;
        private final TextView breakTimeItemView;

        private WorkoutViewHolder(View itemView) {
            super(itemView);
            workoutItemView = itemView.findViewById(R.id.workoutTitle);
            repItemView = itemView.findViewById(R.id.reps);
            setItemView = itemView.findViewById(R.id.sets);
            exerciseItemView = itemView.findViewById(R.id.exercises);
            workTimeItemView = itemView.findViewById(R.id.workTime);
            restTimeItemView = itemView.findViewById(R.id.restTime);
            breakTimeItemView = itemView.findViewById(R.id.breakTime);
        }
    }


    @Override
    public WorkoutViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(WorkoutViewHolder holder, int position) {
        Workout current = getItem(position);
        // set the text
        holder.workoutItemView.setText(current.getWorkoutTitle());
        holder.repItemView.setText("Reps: " + current.getReps());
        holder.setItemView.setText("Sets: " + current.getSets());
        holder.exerciseItemView.setText("Exercises: " + current.getExercises());
        holder.workTimeItemView.setText("Work: " + (current.getWorkTime()/1000) + "sec");
        holder.restTimeItemView.setText("Rest: " + (current.getRestTime()/1000) + "sec");
        holder.breakTimeItemView.setText("Break: " + (current.getBreakTime()/60000) + "min");
    }


    Workout getWorkout(int position) {
        return getItem(position);
    }
}
