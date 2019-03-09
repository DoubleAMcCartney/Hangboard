package com.example.android.hangboard;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.Button;

import com.example.android.hangboard.R;


public class WorkoutCompleteDialogFragment extends DialogFragment {

    // Called to do initial creation of this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Overrode to build custom Dialog container
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.add_workout_to_log, null))

                // Add action buttons
                // 'Add' button
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // add workout ...
                        ((TimerActivity) getActivity()).addWorkout();
                    }
                })
                // 'Cancel' button
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        WorkoutCompleteDialogFragment.this.getDialog().cancel();
                    }
                })
                .setTitle("Save Workout to Log"); // Title displayed at the top of the dialog

        return builder.create();
    }

}
