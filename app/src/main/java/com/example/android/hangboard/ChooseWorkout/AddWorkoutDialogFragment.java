/*
This is the java file containing AddWorkoutDialogFragment class. This the dialog fragment that
handles adding workouts. This is called when the add workout button is tapped.
*/

package com.example.android.hangboard.ChooseWorkout;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.widget.Button;

import com.example.android.hangboard.R;


public class AddWorkoutDialogFragment extends DialogFragment {

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
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // add workout ...
                        ((ViewWorkoutsActivity) getActivity()).addWorkout();
                    }
                })
                // 'Cancel' button
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddWorkoutDialogFragment.this.getDialog().cancel();
                    }
                })
                .setTitle("Create Workout"); // Title displayed at the top of the dialog

        return builder.create();
    }

    // Called when the Fragment is visible to the user
    // Overrode to disable the 'Add' button at start
    @Override
    public void onStart() {
        super.onStart();

        // variable declarations
        Button positiveButton;
        AlertDialog d = (AlertDialog) getDialog(); //Add workout dialog

        // Ensure dialog has been created
        if (d != null) {
            positiveButton = d.getButton(Dialog.BUTTON_POSITIVE); //'Add' button
            positiveButton.setEnabled(false); //disable 'Add' button
        }
    }

}
