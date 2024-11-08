package com.example.financialcompanion;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PetFragment extends Fragment {

    Button gradientStoreButton;
    Button gradientCupboardButton;
    private TextView petCoinBalanceTextView;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private Integer petCoinBalance;
    private ImageView petImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pet, container, false);

        // Initialize views
        petCoinBalanceTextView = view.findViewById(R.id.pet_coin_balance);
        gradientStoreButton = view.findViewById(R.id.gradientStoreButton);
        gradientCupboardButton = view.findViewById(R.id.gradientCupboardButton);

        auth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Retrieve and display the PetCoin balance
        fetchPetCoinBalance();

        // Start listening for goal updates
        listenForGoalUpdates();

        // Set click listener for the button
        gradientStoreButton.setOnClickListener(v -> {
            showStoreItemsDialog(petCoinBalance);
        });

//        gradientCupboardButton.setOnClickListener(v -> {
//            getPurchasedItems(purchasedItems -> {
//                InventoryDialogFragment dialog = InventoryDialogFragment.newInstance(purchasedItems);
//                dialog.show(getParentFragmentManager(), "InventoryDialogFragment");
//            });
//        });

        gradientCupboardButton.setOnClickListener(v -> {
            getPurchasedItems(purchasedItems -> {
                // Create a new instance of the InventoryDialogFragment
                InventoryDialogFragment dialog = InventoryDialogFragment.newInstance(purchasedItems);

                // Set the listener for the dialog fragment
                dialog.setItemSelectedListener(new InventoryDialogFragment.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(int imageResourceId) {
                        // Update the pet image view with the selected item image
                        petImageView.setImageResource(imageResourceId);
                    }
                });

                // Show the dialog fragment
                dialog.show(getParentFragmentManager(), "InventoryDialogFragment");
            });
        });


        petImageView = view.findViewById(R.id.pet_image_view);


        return view;
    }

    private void getPurchasedItems(FirebaseCallback callback) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(); // Replace with actual user ID retrieval if needed
        DatabaseReference purchasedItemsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("purchasedItems");

        purchasedItemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Item> purchasedItems = new ArrayList<>();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    Item item = itemSnapshot.getValue(Item.class);
                    purchasedItems.add(item);
                }
                callback.onCallback(purchasedItems); // Pass data to callback
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DatabaseError", "Failed to load purchased items.", databaseError.toException());
            }
        });
    }

    // Define a callback interface for asynchronous data retrieval
    public interface FirebaseCallback {
        void onCallback(List<Item> purchasedItems);
    }


    private void fetchPetCoinBalance() {
        userRef.child("petCoin").child("balance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded()) return; // Only proceed if fragment is attached

                // Get the pet coins balance (default to 0 if not set)
                petCoinBalance = dataSnapshot.exists() ? dataSnapshot.getValue(Integer.class) : 0;
                if (petCoinBalance == null) petCoinBalance = 0;

                // Update the UI with the pet coin balance
                petCoinBalanceTextView.setText(getString(R.string.pet_coin_balance, petCoinBalance));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PetFragment", "Failed to read pet coin balance: " + databaseError.getMessage());
            }
        });
    }

    private void showStoreItemsDialog(int petCoinBalance) {
        // Create the dialog with petCoinBalance as an argument
        StoreItemsDialogFragment dialog = StoreItemsDialogFragment.newInstance(petCoinBalance);
        dialog.show(getParentFragmentManager(), "StoreItemsDialogFragment");
    }

    private void listenForGoalUpdates() {
        DatabaseReference goalsRef = userRef.child("goals");

        goalsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded()) return; // Ensure fragment is attached before updating

                for (DataSnapshot goalSnapshot : dataSnapshot.getChildren()) {
                    Goal goal = goalSnapshot.getValue(Goal.class);

                    if (goal != null && "Achieved".equals(goal.getStatus()) && !goal.getRewardGiven()) {
                        addPetCoins();
                        goalSnapshot.getRef().child("rewardGiven").setValue(true);
                        fetchPetCoinBalance();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PetFragment", "Failed to read goal updates: " + databaseError.getMessage());
            }
        });
    }

    private void addPetCoins() {
        userRef.child("petCoin").child("balance").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer currentBalance = snapshot.getValue(Integer.class);
                if (currentBalance == null) currentBalance = 0;

                userRef.child("petCoin").child("balance").setValue(currentBalance + 2);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PetFragment", "Failed to update pet coin balance: " + error.getMessage());
            }
        });
    }

}