package com.example.android.hangboard.ChooseWorkout;

public class Exercise {
    private int mNumber;
    private int mAngle;
    private int mDepth;

    public Exercise(int angle, int depth) {
        mAngle = angle;
        mDepth = depth;
    }

    public int getmAngle() {
        return mAngle;
    }

    public int getmDepth() {
        return mDepth;
    }

}
