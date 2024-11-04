package com.example.financialcompanion;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SavingsEntryActivity extends AppCompatActivity {

    private EditText etBalance;
    private FirebaseAuth auth;
    private DatabaseReference database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_entry);

        // Initialize Firebase Auth and Database reference
        auth = FirebaseAuth.getInstance();
        // Get the current user
        FirebaseUser currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference("users");

        if (currentUser != null) {
            // User is signed in
            String uid = currentUser.getUid();
            Log.d(TAG, "Current user ID: " + uid);

            // Call the method to get the account ID
            getAccountIdForUser(uid, new OnAccountIdRetrievedListener() {
                @Override
                public void onAccountIdRetrieved(String accountId) {
                    if (accountId != null) {
                        Log.d(TAG, "Retrieved account ID: " + accountId);

                        etBalance = findViewById(R.id.et_balance);
                        Button btnSubmitBalance = findViewById(R.id.btn_submit_balance);

                        btnSubmitBalance.setOnClickListener(v -> {
                            String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

                            // Get the balance from input
                            String balanceStr = etBalance.getText().toString();
                            if (!balanceStr.isEmpty()) {
                                double balance = Double.parseDouble(balanceStr);

                                // Update the user's savings balance directly
                                userRef.child("accounts").child(accountId).child("balance").setValue(balance).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Now update isFirstTimeUser to false
                                        // After updating the user data
                                        userRef.child("firstTimeUser").setValue(false).addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                Log.d(TAG, "Successfully updated firstTimeUser to false.");
                                                // Proceed to the main activity
                                                Intent intent = new Intent(SavingsEntryActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // Handle possible errors
                                                Log.e(TAG, "Failed to update user data: " + updateTask.getException().getMessage());
                                                Toast.makeText(SavingsEntryActivity.this, "Failed to update user data.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Toast.makeText(SavingsEntryActivity.this, "Failed to save balance.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(SavingsEntryActivity.this, "Please enter a valid balance.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(SavingsEntryActivity.this, "No accounts found for user.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // No user is signed in, handle accordingly (e.g., show a message or redirect to login)
            Log.e(TAG, "No user is signed in.");
        }
    }

    private void getAccountIdForUser(String userId, OnAccountIdRetrievedListener listener) {
        DatabaseReference userRef = database.child(userId).child("accounts");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if the user has any accounts
                if (snapshot.exists()) {
                    for (DataSnapshot accountSnapshot : snapshot.getChildren()) {
                        // Retrieve the account ID
                        String accountId = accountSnapshot.getKey(); // This is the account ID
                        listener.onAccountIdRetrieved(accountId); // Callback with the ID
                        return; // Exit after getting the first account ID
                    }
                } else {
                    listener.onAccountIdRetrieved(null); // No accounts found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to retrieve account ID: " + error.getMessage());
                listener.onAccountIdRetrieved(null); // Handle error
            }
        });
    }

    // Define the interface for the callback
    interface OnAccountIdRetrievedListener {
        void onAccountIdRetrieved(String accountId);
    }

}
