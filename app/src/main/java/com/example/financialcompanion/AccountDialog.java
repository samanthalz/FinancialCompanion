package com.example.financialcompanion;

import static androidx.core.util.TypedValueCompat.dpToPx;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AccountDialog extends DialogFragment implements TransAccountAdapter.OnAccountSelectedListener {

    private SharedViewModel viewModel;
    private RecyclerView recyclerView;
    private TransAccountAdapter transAccountAdapter;
    private List<Account> accounts = new ArrayList<>();  // Initialize as an empty list

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fetch accounts from the database (or any data source)
        fetchAccountsFromDatabase();
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.WrapContentDialog);

        // Inflate the account_dialog XML layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.account_dialog, null);

        // Set the inflated view as the dialog view
        builder.setView(dialogView);
        Log.d("LayoutStructure", "View structure: " + dialogView.toString());

        // Initialize RecyclerView
        recyclerView = dialogView.findViewById(R.id.accounts_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

//        // Set up the adapter with the account list
//        transAccountAdapter = new TransAccountAdapter(getContext(), accounts);
//        recyclerView.setAdapter(transAccountAdapter);

        // Set up the adapter with the account list
        transAccountAdapter = new TransAccountAdapter(getContext(), accounts, this); // Pass 'this' as the listener
        recyclerView.setAdapter(transAccountAdapter);

        //fetchAccountsFromDatabase();

        // Remove the title and set the dialog to be cancelable
        Dialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateAccountButtonText(Account account) {
        TransactionFragment transactionFragment = (TransactionFragment) getParentFragment();
        if (transactionFragment != null) {
            // Find the button in the TransactionFragment's view hierarchy
            Button accountButton = transactionFragment.requireView().findViewById(R.id.btn_account);
            if (accountButton != null && account != null) {
                // Update the button text
                accountButton.setText(account.getAccountName());

                // Get the drawable and scale it to a specific size (48dp converted to pixels)
                Drawable drawable = requireContext().getDrawable(account.getIcon_id());  // Corrected this line
                if (drawable != null) {
                    int sizeInPixels = (int) dpToPx(24);  // Convert 48dp to pixels
                    drawable.setBounds(0, 0, sizeInPixels, sizeInPixels);  // Set width and height in pixels
                    accountButton.setCompoundDrawables(drawable, null, null, null);
                }
            }
        }
    }

    // Convenience function to convert dp to pixels
    public float dpToPx(float dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
    }


    private void fetchAccountsFromDatabase() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference accountsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId) // Replace with the actual user ID
                .child("accounts");

        accountsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {
                    Account account = accountSnapshot.getValue(Account.class);
                    if (account != null) {
                        accounts.add(account);  // Add to the already initialized list
                    }
                }

                // Update the adapter with the fetched accounts
                transAccountAdapter.setAccounts(accounts);  // Set the updated list
                transAccountAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AccountDialogFragment", "Error fetching data: " + databaseError.getMessage());
            }
        });
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

    @Override
    public void onAccountSelected(Account account) {
        updateAccountButtonText(account);
    }
}