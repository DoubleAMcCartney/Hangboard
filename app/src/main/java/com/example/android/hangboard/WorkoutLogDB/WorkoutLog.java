package com.example.android.hangboard.WorkoutLogDB;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

import com.example.android.hangboard.Converters;

import java.util.Calendar;
import java.util.Date;

@Entity(tableName = "workoutLog")
@TypeConverters({Converters.class})
public class WorkoutLog {
    @NonNull
    @PrimaryKey
    private String workoutTitle;
    private int sets;
    private int reps;
    private int workTime;
    private int restTime;
    private int breakTime;
    private int angle;
    private int depth;

    private int weight;
    private int actualWorkTime;
    private int score;
    private Date date;
    private String notes;

    public WorkoutLog(String workoutTitle, int reps, int sets, int workTime, int restTime, int breakTime,
                   int angle, int depth, int weight, int actualWorkTime, int score, Date date, String notes) {
        this.workoutTitle = workoutTitle;
        this.reps = reps;
        this.sets = sets;
        this.angle = angle;
        this.depth = depth;
        this.weight = weight;
        this.workTime = workTime;
        this.restTime = restTime;
        this.breakTime = breakTime;
        this.actualWorkTime = actualWorkTime;
        this.score = score;
        this.date = date;
        this.notes = notes;
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

    public int getWorkTime() {
        return workTime;
    }

    public int getRestTime() {
        return restTime;
    }

    public int getBreakTime() {
        return breakTime;
    }

    public int getAngle() {
        return angle;
    }

    public int getDepth() {
        return depth;
    }

    public int getWeight() {
        return weight;
    }

    public int getActualWorkTime() {
        return actualWorkTime;
    }

    public int getScore() {
        return score;
    }

    public Date getDate() {
        return date;
    }

    public String getNotes() {
        return notes;
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

    public void setWorkTime(int workTime) {
        this.workTime = workTime;
    }

    public void setRestTime(int restTime) {
        this.restTime = restTime;
    }

    public void setBreakTime(int breakTime) {
        this.breakTime = breakTime;
    }

    public void setAngle(int angles) {
        this.angle = angles;
    }

    public void setDepth(int depths) {
        this.depth = depths;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setActualWorkTime(int actualWorkTime) {
        this.actualWorkTime = actualWorkTime;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public static WorkoutLog[] populateData() {
        return new WorkoutLog[] {
                new WorkoutLog("Beginner", 6, 5, 1, 10000, 5000, 240000,
                        10, 150, 10000, 20, Calendar.getInstance().getTime(), "Test")
        };
    }
}
