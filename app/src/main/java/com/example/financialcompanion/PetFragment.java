package com.example.financialcompanion;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class PetFragment extends Fragment {

    Button gradientStoreButton;
    private TextView petCoinBalanceTextView;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pet, container, false);

        // Initialize views
        petCoinBalanceTextView = view.findViewById(R.id.pet_coin_balance);
        auth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Retrieve and display the PetCoin balance
        fetchPetCoinBalance();

        return view;
    }

    private void fetchPetCoinBalance() {
        // Add a listener to fetch the pet coin balance from Firebase
        userRef.child("petCoin").child("balance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if balance exists and update the TextView
                if (dataSnapshot.exists()) {
                    // Safely handle the null case
                    Integer petCoinBalance = dataSnapshot.getValue(Integer.class);

                    // If petCoinBalance is null, set to 0
                    if (petCoinBalance == null) {
                        petCoinBalance = 0;
                    }

                    // Update the TextView with the value
                    petCoinBalanceTextView.setText(getString(R.string.pet_coin_balance, petCoinBalance));
                } else {
                    // Default value if the petCoin data is not found
                    petCoinBalanceTextView.setText(getString(R.string.pet_coin_balance, 0));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PetFragment", "Failed to read pet coin balance: " + databaseError.getMessage());
            }
        });
    }




}