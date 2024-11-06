package com.example.financialcompanion;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.UUID;

public class AddAccountDialog extends DialogFragment {

    private TextInputEditText accountNameInput;
    private TextInputEditText initialAmountInput;
    private LinearLayout iconSelectionContainer;
    private ImageView selectedIconImageView;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.WrapContentDialog);

        // Inflate the account_dialog XML layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_account, null);

        // Set the inflated view as the dialog view
        builder.setView(dialogView);

        // Create and configure the dialog
        Dialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize views
        accountNameInput = dialogView.findViewById(R.id.account_name_input);
        initialAmountInput = dialogView.findViewById(R.id.initial_amount_input);
        iconSelectionContainer = dialogView.findViewById(R.id.icon_selection_container);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button saveButton = dialogView.findViewById(R.id.save_button);

        // Set up icon selection
        int[] iconIds = {
                R.drawable.american_payment_payment_american_express_method_card_card_method_express,
                R.drawable.card_ideal_method_visa_master_payment,
                R.drawable.card_payu_payment_money,
                R.drawable.google_payment_method_wallet_payment_method_google_wallet,
                R.drawable.pay_payment_world_pay_method_payment_world_method,
                R.drawable.payment_bitcoin_method_payment_method_bitcoin,
                R.drawable.payment_card_maestro,
                R.drawable.payment_jcb_card,
                R.drawable.payment_master_method_card_payment_master_method_card,
                R.drawable.payment_square_payment_method_square_method,
                R.drawable.paypal_payment_method_paypal_payment_method,
                R.drawable.union_payment_western_method_union_payment_western_method,
                R.drawable.unionpay_card_payment,
                R.drawable.visa_payment_card_method_visa_payment_card_method,
                R.drawable.finance_bank_piggy_business_money_icon
        };

        int fixedSize = 64;
        int marginSize = 12;

        // Variable to keep track of selected icon
        selectedIconImageView = null;

        // Create icons
        for (int iconId : iconIds) {
            ImageView iconImageView = new ImageView(getContext());
            iconImageView.setImageResource(iconId);

            // Set the resource ID as a tag for easy retrieval
            iconImageView.setTag(iconId);

            // Convert dp to pixels for layout params
            int sizeInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, fixedSize, getResources().getDisplayMetrics());
            int marginInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginSize, getResources().getDisplayMetrics());

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
            layoutParams.setMargins(marginInPx, marginInPx, marginInPx, marginInPx); // Set margins for spacing

            iconImageView.setLayoutParams(layoutParams);

            // Set the default background color
            iconImageView.setBackgroundColor(Color.TRANSPARENT); // Default color

            // Set an OnClickListener for the icon
            iconImageView.setOnClickListener(v -> {
                // Deselect previously selected icon
                if (selectedIconImageView != null) {
                    selectedIconImageView.setBackgroundColor(Color.TRANSPARENT); // Reset color
                }

                // Select the new icon
                iconImageView.setBackgroundColor(Color.LTGRAY); // Change background color to indicate selection
                selectedIconImageView = iconImageView; // Keep track of the selected icon
            });

            iconSelectionContainer.addView(iconImageView);
        }

        // Set up cancel button listener
        cancelButton.setOnClickListener(v -> dismiss());

        saveButton.setOnClickListener(v -> {
            String accountName = Objects.requireNonNull(accountNameInput.getText()).toString().trim();
            String initialAmountStr = Objects.requireNonNull(initialAmountInput.getText()).toString().trim();

            if (accountName.isEmpty() || initialAmountStr.isEmpty() || selectedIconImageView == null) {
                Toast.makeText(getContext(), "Please fill in all fields and select an icon", Toast.LENGTH_SHORT).show();
                return;
            }

            double initialAmount = Double.parseDouble(initialAmountStr);
            String accountId = UUID.randomUUID().toString(); // Generate unique ID for the account
            int selectedIconResId = (int) selectedIconImageView.getTag(); // Retrieve the icon ID from the tag

            Account newAccount = new Account(accountId, accountName, initialAmount, selectedIconResId);

            // Update ViewModel
            SharedViewModel accountViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            accountViewModel.addAccount(newAccount);

            // Save to Firebase
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("accounts");

            // Add the new account to Firebase
            databaseReference.child(accountId).setValue(newAccount)
                    .addOnSuccessListener(aVoid -> {
                        // Success callback
                        Log.d("AccountInfo", "Account saved successfully to Firebase");
                    })
                    .addOnFailureListener(e -> {
                        // Failure callback
                        Log.e("AccountInfo", "Failed to save account to Firebase: " + e.getMessage());
                    });


            // Clear selection and dismiss
            selectedIconImageView.setBackgroundColor(Color.TRANSPARENT);
            selectedIconImageView = null;

            dismiss(); // Close the dialog
        });

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();

            // Set the dialog width to 80% of the screen width
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            params.width = (int) (displayMetrics.widthPixels * 0.8); // Set custom width
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT; // Maintain wrap content for height

            dialog.getWindow().setAttributes(params);

            Log.d("DialogDimensions", "Width: " + params.width + ", Height: " + params.height);
        }
    }


}
