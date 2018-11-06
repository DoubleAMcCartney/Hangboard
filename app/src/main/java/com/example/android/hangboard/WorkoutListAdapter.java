package com.example.android.hangboard;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class WorkoutListAdapter extends
        RecyclerView.Adapter<WorkoutListAdapter.WorkoutViewHolder> {

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

    private final LayoutInflater mInflater;
    private List<Workout> mWorkouts; // Cached copy of words

    WorkoutListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public WorkoutViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View itemView = mInflater.inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(WorkoutViewHolder holder, int position) {
        if (mWorkouts != null) {
            Workout current = mWorkouts.get(position);
            holder.workoutItemView.setText(current.getWorkoutTitle());
            holder.repItemView.setText("Reps: " + current.getReps());
            holder.setItemView.setText("Sets: " + current.getSets());
            holder.exerciseItemView.setText("Exercises: " + current.getExercises());
            holder.workTimeItemView.setText("Work: " + (current.getWorkTime()/1000));
            holder.restTimeItemView.setText("Rest: " + (current.getRestTime()/1000));
            holder.breakTimeItemView.setText("Break: " + (current.getBreakTime()/1000));
        }
        else {
            holder.workoutItemView.setText("No Workout");
        }
    }

    void setWorkouts(List<Workout> workouts){
        mWorkouts = workouts;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mWorkouts != null)
            return mWorkouts.size();
        else return 0;
    }


}
