package com.example.android.hangboard.ChooseWorkout;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.Button;

import com.example.android.hangboard.R;

public class UpdateWorkoutDialogFragment  extends DialogFragment {
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
        builder.setView(inflater.inflate(R.layout.add_workout, null))

                // Add action buttons
                // 'Add' button
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // add workout ...
                        ((ViewWorkoutsActivity) getActivity()).updateWorkout();
                    }
                })
                // 'Cancel' button
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        UpdateWorkoutDialogFragment.this.getDialog().cancel();
                    }
                })
                .setTitle("Edit Workout"); // Title displayed at the top of the dialog

        return builder.create();
    }
}
