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
    private Account accountToEdit;
    private Button deleteButton;
    private int selectedIconId;


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
        deleteButton = dialogView.findViewById(R.id.delete_button);

        // Initially hide the delete button
        deleteButton.setVisibility(View.GONE);

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

        // If we are editing an account, prefill the fields
        if (accountToEdit != null) {
            accountNameInput.setText(accountToEdit.getAccountName());
            initialAmountInput.setText(String.valueOf(accountToEdit.getBalance()));
            deleteButton.setVisibility(View.VISIBLE); // Show delete button
        }

        // Set up cancel button listener
        cancelButton.setOnClickListener(v -> dismiss());

        saveButton.setOnClickListener(v -> {
            String accountName = Objects.requireNonNull(accountNameInput.getText()).toString().trim();
            String initialAmountStr = Objects.requireNonNull(initialAmountInput.getText()).toString().trim();

            float initialAmount = 0f;
            if (!initialAmountStr.isEmpty()) {
                try {
                    initialAmount = Float.parseFloat(initialAmountStr); // Parse as float
                } catch (NumberFormatException e) {
                    // Handle the exception (e.g., show a message to the user)
                    Log.e("ParseError", "Invalid number format in initialAmountInput");
                }
            }

            if (accountName.isEmpty() || initialAmountStr.isEmpty() || selectedIconImageView == null) {
                Toast.makeText(getContext(), "Please fill in all fields and select an icon", Toast.LENGTH_SHORT).show();
                return;
            }

            if (accountToEdit != null) {
                // Update the existing account
                accountToEdit.setAccountName(String.valueOf(accountNameInput.getText()));
                accountToEdit.setBalance(initialAmount);
                accountToEdit.setIcon_id((Integer) selectedIconImageView.getTag());

                // Update the account in the database
                updateAccountInDatabase(accountToEdit);
            } else {
                initialAmount = Float.parseFloat(initialAmountStr);
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
            }

            // Clear selection and dismiss
            selectedIconImageView.setBackgroundColor(Color.TRANSPARENT);
            selectedIconImageView = null;

            dismiss(); // Close the dialog
        });

        // Delete button logic
        deleteButton.setOnClickListener(v -> {
            if (accountToEdit != null && !accountToEdit.getAccountName().equals("Savings")) {
                // Proceed with delete if it's not the "Savings" account
                FirebaseDatabase.getInstance().getReference("users")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                        .child("accounts")
                        .child(accountToEdit.getId())
                        .removeValue()
                        .addOnSuccessListener(aVoid -> {
                            // Successfully deleted account from Firebase
                            Log.d("AccountInfo", "Account deleted successfully");
                            // Update the ViewModel or RecyclerView as needed
                        })
                        .addOnFailureListener(e -> {
                            // Failure callback
                            Log.e("AccountInfo", "Failed to delete account from Firebase: " + e.getMessage());
                        });

                dismiss(); // Close the dialog after deleting
            } else {
                Toast.makeText(getContext(), "The Savings account cannot be deleted", Toast.LENGTH_SHORT).show();
            }
        });


        return dialog;
    }

    // Method to update the account in Firebase
    private void updateAccountInDatabase(Account account) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        // Get a reference to the user's accounts in Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("accounts");

        // Use the account's ID to update the specific account in Firebase
        String accountId = account.getId(); // Assuming the account has a valid ID
        databaseReference.child(accountId).setValue(account)
                .addOnSuccessListener(aVoid -> {
                    // Success callback
                    Log.d("AccountInfo", "Account updated successfully in Firebase");
                })
                .addOnFailureListener(e -> {
                    // Failure callback
                    Log.e("AccountInfo", "Failed to update account in Firebase: " + e.getMessage());
                });
    }

    // Setter method to pass the account details for editing
    public void setAccountToEdit(Account account) {
        this.accountToEdit = account;
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
