package com.example.financialcompanion;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private List<Item> items;
    private InventoryDialogFragment.OnItemSelectedListener listener;

    // Listener interface to notify the parent fragment/activity
    public interface OnItemSelectedListener {
        void onItemSelected(int imageResourceId, String itemName);
    }

    public InventoryAdapter(List<Item> items, InventoryDialogFragment.OnItemSelectedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.itemIcon.setImageResource(item.getImageResourceId());
        holder.itemName.setText(item.getName());

        // Retrieve the saved "wearing" state from SharedPreferences
        SharedPreferences sharedPreferences = holder.itemView.getContext().getSharedPreferences("WornItemsPrefs", Context.MODE_PRIVATE);

        // Retrieve the ID of the last worn item from SharedPreferences
        String lastWornItemId = sharedPreferences.getString("lastWornItemId", null);

        // Check if this item is the last worn item
        boolean isWearing = item.getId().equals(lastWornItemId);

        // Set the button state based on whether the item is "wearing" or not
        if (isWearing) {
            holder.wearButton.setText("Remove");
            holder.wearButton.setBackgroundColor(Color.LTGRAY);
            holder.wearButton.setEnabled(true);
        } else {
            holder.wearButton.setText("Wear");
            holder.wearButton.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
            holder.wearButton.setEnabled(true);
        }

        holder.wearButton.setOnClickListener(v -> {
            String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

            // Get a reference to Firebase Database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userRef = database.getReference("users").child(uid).child("petType");

            // If the button text is "Remove", reset to default image
            if (holder.wearButton.getText().toString().equals("Remove")) {

                // Use a listener to fetch the petType from the database
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Check if the data exists and set the petType
                        if (dataSnapshot.exists()) {
                            String petType = dataSnapshot.getValue(String.class); // Get the petType value from the database
                            Log.d(TAG, "Pet Type: " + petType);

                            // Get the default "Dog Dog" image resource
                            int imageResourceId = PetItemImageMapper.getImageResource(petType, petType); // Pass the default name

                            if (imageResourceId != -1) {
                                // Pass the default image to the listener
                                listener.onItemSelected(imageResourceId, petType);
                                Toast.makeText(holder.itemView.getContext(), "Pet image reset to default", Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle error if image resource not found
                                Toast.makeText(holder.itemView.getContext(), "Default image not found!", Toast.LENGTH_SHORT).show();
                            }

                            // Reset the button state to "Wear"
                            holder.wearButton.setText("Wear");
                            holder.wearButton.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
                            sharedPreferences.edit().remove("lastWornItemId").apply(); // Remove the "wearing" state from SharedPreferences
                            sharedPreferences.edit().remove("lastWornItemName").apply(); // Remove the "wearing" state from SharedPreferences
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Failed to read petType: " + databaseError.getMessage());
                    }
                });
            } else {
                // Handle the "Wear" action here
                holder.wearButton.setText("Remove");
                holder.wearButton.setBackgroundColor(Color.LTGRAY);
                holder.wearButton.setEnabled(true); // Disable button

                // Save the ID of the currently worn item in SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("lastWornItemId", item.getId()); // Save the "wearing" state for this item
                editor.putString("lastWornItemName", item.getName()); // Save the "wearing" state for this item
                editor.apply();

                // Notify the adapter to refresh all items so only the current item shows "Wearing"
                notifyDataSetChanged();

                // Use a listener to fetch the petType from the database
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Check if the data exists and set the petType
                        if (dataSnapshot.exists()) {
                            String petType = dataSnapshot.getValue(String.class); // Get the petType value from the database
                            Log.d(TAG, "Pet Type: " + petType);

                            // Now that petType is fetched, proceed with item name
                            String itemName = item.getName(); // Get the item name

                            // Get the image resource based on the pet type and item name
                            int imageResourceId = PetItemImageMapper.getImageResource(petType, itemName);

                            // Check if image resource was found
                            if (imageResourceId != -1) {
                                // Pass the image resource to the Inventory Dialog Fragment
                                listener.onItemSelected(imageResourceId, itemName);
                                Toast.makeText(holder.itemView.getContext(), "Item image found!", Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle error if image resource not found
                                Toast.makeText(holder.itemView.getContext(), "Item image not found!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "Pet type does not exist in the database");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Failed to read petType: " + databaseError.getMessage());
                    }
                });
            }
        });
    }




    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemIcon;
        TextView itemName;
        Button wearButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemIcon = itemView.findViewById(R.id.itemIcon);
            itemName = itemView.findViewById(R.id.itemName);
            wearButton = itemView.findViewById(R.id.wearButton);
        }
    }
}

