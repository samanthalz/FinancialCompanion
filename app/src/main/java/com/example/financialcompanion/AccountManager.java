package com.example.financialcompanion;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AccountManager {
    private HashMap<String, String> accountMap; // Maps account names to their unique IDs
    private DatabaseReference databaseReference;
    // Flag to indicate if data has been loaded
    private boolean dataLoaded = false;

    public AccountManager(String userId) {
        accountMap = new HashMap<>();
        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("accounts");
        retrieveAccounts();
    }

    private void retrieveAccounts() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot accountSnapshot : dataSnapshot.getChildren()) {
                        String accountId = accountSnapshot.getKey();
                        String accountName = accountSnapshot.child("accountName").getValue(String.class);

                        if (accountName != null) {
                            accountMap.put(accountName.trim(), accountId);
                            Log.d("AccountManager", "Added to Map - Account Name: " + accountName + ", Account ID: " + accountId);
                        }
                    }
                    // Set the flag to indicate data has been loaded
                    dataLoaded = true;
                    Log.d("AccountManager", "Account Map: " + accountMap.toString());
                } else {
                    Log.d("AccountManager", "No accounts found for user ID: " );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AccountManager", "Failed to retrieve accounts: " + databaseError.getMessage());
            }
        });
    }

    public String getAccountId(String accountType) {
        // Ensure data is loaded before trying to access the account map
        if (!dataLoaded) {
            Log.d("AccountManager", "Data not loaded yet, cannot retrieve account ID.");
            return null; // Or handle accordingly
        }

        String accountId = accountMap.get(accountType); // Retrieves the ID or null if not found
        Log.d("AccountManager", "Account type: " + accountType + ", Account ID: " + accountId);
        return accountId;
    }
}

