package com.github.ridesmart;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class RouteNameDialogFragment extends DialogFragment {

    RouteNameDialogListener listener;

    private EditText input;

    public interface RouteNameDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        //void onDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (RouteNameDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + "must implement RouteNameDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.route_name_dialog_message)
                .setTitle(R.string.route_name_dialog_title);

        input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDialogPositiveClick(RouteNameDialogFragment.this);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //listener.onDialogNegativeClick(RouteNameDialogFragment.this);
                RouteNameDialogFragment.this.getDialog().cancel();
            }
        });

        return builder.create();
    }

    public String getInput() {
        return input.getText().toString();
    }
}
