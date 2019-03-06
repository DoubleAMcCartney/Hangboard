package com.example.android.hangboard.WorkoutLog;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.hangboard.R;
import com.example.android.hangboard.WorkoutLogDB.WorkoutLog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class WorkoutLogListAdapter extends
        ListAdapter<WorkoutLog, WorkoutLogListAdapter.WorkoutLogViewHolder> {

    public WorkoutLogListAdapter() {
        super(DIFF_CALLBACK);
    }

    // DiffUtil handles data set changes in the recycler view, so the entire database doesn't
    // get reloaded every time it is changed. This also allows for animations when workouts are
    // added and deleted.
    private static final DiffUtil.ItemCallback<WorkoutLog> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<WorkoutLog>() {
                @Override
                public boolean areItemsTheSame(@NonNull WorkoutLog oldItem, @NonNull WorkoutLog newItem) {
                    return oldItem.getDate().equals(newItem.getDate());
                }

                @Override
                public boolean areContentsTheSame(@NonNull WorkoutLog oldItem, @NonNull WorkoutLog newItem) {
                    return oldItem.getWorkoutTitle().equals(newItem.getWorkoutTitle()) &&
                            oldItem.getAngle() == (newItem.getAngle()) &&
                            oldItem.getDepth() == (newItem.getDepth()) &&
                            oldItem.getBreakTime() == newItem.getBreakTime() &&
                            oldItem.getRestTime() == newItem.getRestTime() &&
                            oldItem.getWorkTime() == newItem.getWorkTime() &&
                            oldItem.getReps() == newItem.getReps() &&
                            oldItem.getSets() == newItem.getSets() &&
                            oldItem.getDate().equals(newItem.getDate()) &&
                            oldItem.getActualWorkTime() == newItem.getActualWorkTime() &&
                            oldItem.getScore() == newItem.getScore() &&
                            oldItem.getNotes().equals(newItem.getNotes());
                }
            };


    class WorkoutLogViewHolder extends RecyclerView.ViewHolder {
        private final TextView workoutTitleItemView;
        private final TextView dateItemView;
        private final TextView DepthItemView;
        private final TextView scoreItemView;
        private final TextView weightItemView;

        private WorkoutLogViewHolder(View itemView) {
            super(itemView);
            workoutTitleItemView = itemView.findViewById(R.id.workoutLogTitle);
            dateItemView = itemView.findViewById(R.id.workoutLogDate);
            DepthItemView = itemView.findViewById(R.id.logDepth);
            scoreItemView = itemView.findViewById(R.id.logScore);
            weightItemView = itemView.findViewById(R.id.logWeight);
        }
    }


    @Override
    public WorkoutLogViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_log, parent, false);
        return new WorkoutLogViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(WorkoutLogViewHolder holder, int position) {
        WorkoutLog current = getItem(position);
        DateFormat dateFormat = new SimpleDateFormat("MMM. dd yyyy");
        // set the text
        holder.workoutTitleItemView.setText(current.getWorkoutTitle());
        holder.dateItemView.setText(dateFormat.format(current.getDate()));
        holder.DepthItemView.setText(current.getDepth() + "mm");
        holder.scoreItemView.setText("Score: " + current.getScore());
        holder.weightItemView.setText(current.getWeight() + "lbs");
    }


    WorkoutLog getWorkout(int position) {
        return getItem(position);
    }
}