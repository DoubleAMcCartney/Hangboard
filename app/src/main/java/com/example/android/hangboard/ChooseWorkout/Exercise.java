/*
Exercise class. This is the class used when a workout is being created by the user to hold
information about each exercise in the workout.
 */

package com.example.android.hangboard.ChooseWorkout;

class Exercise {
    private int angle;
    private int depth;

    // Constructor
    Exercise(int angle, int depth) {
        this.angle = angle;
        this.depth = depth;
    }

    // Getters
    int getAngle() {
        return angle;
    }

    int getDepth() {
        return depth;
    }

}
