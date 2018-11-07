package com.example.android.hangboard;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

@Entity(tableName = "workout")
public class Workout {
    @NonNull
    @PrimaryKey
    private String workoutTitle;
    private int sets;
    private int reps;
    private int exercises;
    private int workTime;
    private int restTime;
    private int breakTime;
    private List<Integer> angles;
    private List<Integer> depths;

    public Workout(String workoutTitle, int reps, int sets, int exercises, int workTime, int restTime, int breakTime,
                   List<Integer> angles, List<Integer> depths) {
        this.workoutTitle = workoutTitle;
        this.reps = reps;
        this.sets = sets;
        this.exercises = exercises;
        this.angles = angles;
        this.depths = depths;
        this.workTime = workTime;
        this.restTime = restTime;
        this.breakTime = breakTime;
    }

    // Getters
    @NonNull
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

    public int getWorkTime() {
        return workTime;
    }

    public int getRestTime() {
        return restTime;
    }

    public int getBreakTime() {
        return breakTime;
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

    public void setWorkTime(int workTime) {
        this.workTime = workTime;
    }

    public void setRestTime(int restTime) {
        this.restTime = restTime;
    }

    public void setBreakTime(int breakTime) {
        this.breakTime = breakTime;
    }

    public void setAngles(List<Integer> angles) {
        this.angles = angles;
    }

    public void setDepths(List<Integer> depths) {
        this.depths = depths;
    }

    public static Workout[] populateData() {
        return new Workout[] {
                new Workout("Intermediate", 6, 5, 1, 7000, 3000, 240000,
                        Arrays.asList(0), Arrays.asList(10))
        };
    }

    public boolean isValid() {
        return (reps!=0)&(sets!=0)&(exercises!=0)&
                (workTime!=0)&(restTime!=0)&(breakTime!=0)&
                (angles.size()==exercises)&(depths.size()==exercises);
    }
}
