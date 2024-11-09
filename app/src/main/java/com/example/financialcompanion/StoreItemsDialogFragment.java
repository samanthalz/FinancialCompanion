package com.example.financialcompanion;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class StoreItemsDialogFragment extends DialogFragment {

    private TextView totalPetCoinsTextView, totalItemsChosenTextView, coinsNeededTextView;
    private Button buyButton;
    private int totalItemsChosen = 0;
    private int coinsNeeded = 0;
    private int petCoins;
    private List<Item> selectedItems = new ArrayList<>();
    private RecyclerView recyclerView;

    public static String generateFixedUUID(String itemName) {
        return UUID.nameUUIDFromBytes(itemName.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private List<Item> getStoreItems() {
        List<Item> items = new ArrayList<>();

        // Add your 8 items to the list with fixed UUIDs
        items.add(new Item(generateFixedUUID("Black Sunglasses"), "Black Sunglasses", 10, R.drawable.black_sunglasses));
        items.add(new Item(generateFixedUUID("Yellow Sunglasses"), "Yellow Sunglasses", 12, R.drawable.yellow_sunglasses));
        items.add(new Item(generateFixedUUID("Black Spectacles"), "Black Spectacles", 15, R.drawable.black_specs));
        items.add(new Item(generateFixedUUID("Purple Bone Collar"), "Purple Bone Collar", 8, R.drawable.purplebonecollar));
        items.add(new Item(generateFixedUUID("Pink Ribbon"), "Pink Ribbon", 10, R.drawable.pink_ribbon));
        items.add(new Item(generateFixedUUID("Pink Bone Collar"), "Pink Bone Collar", 9, R.drawable.pinkbonecollar));
        items.add(new Item(generateFixedUUID("Yellow Bone Collar"), "Yellow Bone Collar", 8, R.drawable.yellowbonecollar));
        items.add(new Item(generateFixedUUID("Gold Chain"), "Gold Chain", 10, R.drawable.gold_chain));

        return items;
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_store_items, container, false);

        // Hide the dialog's root view initially
        view.setVisibility(View.INVISIBLE);

        // Initialize views
        totalPetCoinsTextView = view.findViewById(R.id.total_pet_coins);
        totalItemsChosenTextView = view.findViewById(R.id.total_items_chosen);
        coinsNeededTextView = view.findViewById(R.id.coins_needed);
        buyButton = view.findViewById(R.id.buy_button);

        // Retrieve petCoins from arguments
        petCoins = getArguments() != null ? getArguments().getInt("petCoins") : 0;
        totalPetCoinsTextView.setText("Total Pet Coins: " + petCoins);

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.items_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        // Handle Buy Button
        buyButton.setOnClickListener(v -> handleBuy());

        // Fetch user's purchased items and then set up the RecyclerView adapter
        fetchPurchasedItems(purchasedItems -> setupStoreRecyclerView(view, purchasedItems));

        // Set the dialog's window to be fully transparent initially
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Adjust dialog size here
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT); // Adjust height as needed
        }

        // Find the close button by its ID
        ImageView closeButton = view.findViewById(R.id.close_btn);

        // Set a click listener to dismiss the dialog
        closeButton.setOnClickListener(v -> {
            // Dismiss the dialog when the close button is clicked
            dismiss();
        });
    }

    public static StoreItemsDialogFragment newInstance(int petCoins) {
        StoreItemsDialogFragment fragment = new StoreItemsDialogFragment();
        Bundle args = new Bundle();
        args.putInt("petCoins", petCoins); // Add petCoins as an argument
        fragment.setArguments(args);
        return fragment;
    }

    private void setupStoreRecyclerView(View dialogView, Set<String> purchasedItems) {
        List<Item> items = getStoreItems();
        StoreItemAdapter adapter = new StoreItemAdapter(items, this::onItemAddedToCart, purchasedItems);
        recyclerView.setAdapter(adapter);

        // After setting up RecyclerView, make the dialog view visible
        dialogView.setVisibility(View.VISIBLE);

        // Make the dialog visible once setup is complete
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Fade in the dialog's window background from transparent to opaque
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));  // or any background color
        }
        dialogView.setAlpha(0f);  // Start transparent
        dialogView.animate().alpha(1f).setDuration(300);  // Fade in over 300ms

    }

    private void fetchPurchasedItems(FirebaseCallback callback) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        DatabaseReference purchasedItemsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("purchasedItems");

        purchasedItemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> purchasedItemIds = new HashSet<>();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    String itemId = itemSnapshot.getKey();
                    purchasedItemIds.add(itemId);
                }
                callback.onCallback(purchasedItemIds);  // Use the callback to pass the data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DatabaseError", "Failed to load purchased items.", databaseError.toException());
                callback.onCallback(new HashSet<>()); // Return an empty set in case of error
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void onItemAddedToCart(Item item, boolean isSelected) {
        // Create the selectedItems list if not already initialized
        if (selectedItems == null) {
            selectedItems = new ArrayList<>();
        }

        if (isSelected) {
            // Item was selected, add it to the list
            selectedItems.add(item);

            // Update totals
            totalItemsChosen++;
            coinsNeeded += item.getCoinsNeeded();
        } else {
            // Item was deselected, remove it from the list
            selectedItems.remove(item);

            // Update totals
            totalItemsChosen--;
            coinsNeeded -= item.getCoinsNeeded();
        }

        // Update UI with new totals
        totalItemsChosenTextView.setText("Total Items Chosen: " + totalItemsChosen);
        coinsNeededTextView.setText("Coins Needed: " + coinsNeeded);
    }


    // Callback interface for asynchronous data retrieval
    public interface FirebaseCallback {
        void onCallback(Set<String> purchasedItemIds);
    }

    private void handleBuy() {
        // Check if any items are selected
        if (selectedItems.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one item to purchase.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the user has enough coins to buy the selected items
        if (coinsNeeded <= petCoins) {
            // Deduct the coins and complete the purchase
            int newBalance = petCoins - coinsNeeded;
            updatePetCoins(newBalance);

            // Get the user ID (replace with actual user ID retrieval if needed)
            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            DatabaseReference purchasedItemsRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(uid)
                    .child("purchasedItems");

            // Add each purchased item to the database
            for (Item item : selectedItems) {
                String itemId = item.getId();
                if (itemId != null) {
                    purchasedItemsRef.child(itemId).setValue(item)
                            .addOnSuccessListener(aVoid -> Log.d("Purchase", "Item added to purchasedItems"))
                            .addOnFailureListener(e -> Log.e("Purchase", "Failed to add item", e));
                }
            }

            // Close the dialog and show a success message
            dismiss();
            Toast.makeText(getContext(), "Purchase successful!", Toast.LENGTH_SHORT).show();

        } else {
            // Close the dialog and show an insufficient coins message
            dismiss();
            Toast.makeText(getContext(), "Not enough coins to complete purchase.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePetCoins(int newBalance) {
        // Get the current user's ID
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Reference to the user's pet coin balance in Firebase
        DatabaseReference balanceRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("petCoin")
                .child("balance");

        // Update the balance in Firebase
        balanceRef.setValue(newBalance);
    }


}

