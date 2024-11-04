package com.example.financialcompanion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class AccountDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.WrapContentDialog);

        // Inflate the account_dialog XML layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.account_dialog, null);

        // Set the inflated view as the dialog view
        builder.setView(dialogView);

        // Remove the title and set the dialog to be cancelable
        Dialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                // Get button's location
                Button yourButton = requireActivity().findViewById(R.id.btn_date);
                int[] buttonLocation = new int[2];
                yourButton.getLocationOnScreen(buttonLocation);

                // Calculate dialog position
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                // Position at the bottom right of the dialog above the button
                lp.gravity = Gravity.TOP | Gravity.START; // Set gravity to top-right
                lp.x = buttonLocation[0] + yourButton.getWidth() - window.getDecorView().getWidth(); // X position (align right)
                lp.y = buttonLocation[1] - window.getDecorView().getHeight(); // Y position (above button)


                // Set dialog to INVISIBLE initially
                dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                dialog.getWindow().getDecorView().setVisibility(View.INVISIBLE);

                // Set dialog position
                window.setAttributes(lp);

                // Add OnGlobalLayoutListener to observe dialog height changes
                window.getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Recalculate dialog position based on new height
                        WindowManager.LayoutParams lp = window.getAttributes();
                        lp.x = buttonLocation[0] + yourButton.getWidth() - window.getDecorView().getWidth(); // X position (align right)
                        lp.y = buttonLocation[1] - window.getDecorView().getHeight(); // Y position (above button)

                        window.setAttributes(lp);

                        // Make dialog VISIBLE after repositioning
                        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                        dialog.getWindow().getDecorView().setVisibility(View.VISIBLE);

                        // Remove listener
                        window.getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        }
    }

}