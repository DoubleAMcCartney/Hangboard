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

        private WorkoutViewHolder(View itemView) {
            super(itemView);
            workoutItemView = itemView.findViewById(R.id.textView);
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
