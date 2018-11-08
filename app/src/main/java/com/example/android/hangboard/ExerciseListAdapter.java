package com.example.android.hangboard;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ExerciseListAdapter extends
        RecyclerView.Adapter<ExerciseListAdapter.ExerciseViewHolder> {
    
    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private final TextView numberTV;
        private final TextView angleTV;
        private final TextView depthTV;

        private ExerciseViewHolder(View itemView) {
            super(itemView);
            numberTV = itemView.findViewById(R.id.exerciseNumberTextView);
            angleTV = itemView.findViewById(R.id.angleTextView);
            depthTV = itemView.findViewById(R.id.depthTextView);
        }
    }

    private final LayoutInflater mInflater;
    private List<Exercise> mExercises; // Cached copy of exercises
    
    ExerciseListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public ExerciseListAdapter.ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View itemView = mInflater.inflate(R.layout.item_exercise, parent, false);
        return new ExerciseListAdapter.ExerciseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ExerciseListAdapter.ExerciseViewHolder holder, int position) {
        if (mExercises != null) {
            Exercise current = mExercises.get(position);
            holder.numberTV.setText(position);
            holder.depthTV.setText("Depth: " + current.getmDepth() + "mm");
            holder.angleTV.setText("Angle: " + current.getmAngle() + "°");
        }
        else {
        }
    }

    @Override
    public int getItemCount() {
        if (mExercises != null)
            return mExercises.size();
        else return 0;
    }

    void addExercise(Exercise exercise){
        mExercises.add(exercise);
        notifyDataSetChanged();
    }
}
