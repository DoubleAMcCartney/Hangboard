package com.example.android.hangboard;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "workout")
public class Workout {
    @PrimaryKey
    private String workoutTitle;
    private int sets;
    private int reps;
    private int exercises;
    private List<Integer> angles;
    private List<Integer> depths;

    // Getters
    public String getWorkoutTitle() {
        return workoutTitle;
    }

    public int getSets() {
        return sets;
    }

    public int getReps() {
        return reps;
    }

    public int getExercises() {
        return exercises;
    }

    public List<Integer> getAngles() {
        return angles;
    }

    public List<Integer> getDepths() {
        return depths;
    }

    // Setters
    public void setWorkoutTitle(String workoutTitle) {
        this.workoutTitle = workoutTitle;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public void setExercises(int exercises) {
        this.exercises = exercises;
    }

    public void setAngles(List<Integer> angles) {
        this.angles = angles;
    }

    public void setDepths(List<Integer> depths) {
        this.depths = depths;
    }
}
