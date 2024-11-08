package com.example.financialcompanion;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ChangePetFragment extends Fragment {

    private Button catChangeButton, dogChangeButton;
    private TextView catChosenText, dogChosenText;
    private ImageView catImageView, dogImageView;
    private DatabaseReference userPetTypeRef;
    private static final String TAG = "ChangePetFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_pet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the origin fragment info from arguments
        String originFragment = getArguments() != null ? getArguments().getString("originFragment") : "";

        // Initialize the toolbar and set the navigation icon click listener
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar); // Set the toolbar as the action bar

        // Get the NavController for this fragment
        NavController navController = Navigation.findNavController(view);

        // Enable the Up button and set the back arrow icon
        Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24); // Set your back arrow icon

        // Set Navigation click listener for the back button
        toolbar.setNavigationOnClickListener(v -> {
            Log.d("ManageAccountsFragment", "Back pressed from origin: " + originFragment);

            // Check the origin fragment and navigate accordingly
            if ("account".equals(originFragment)) {
                // Check if the action exists in the navigation graph
                navController.navigate(R.id.action_changePetFragment_to_accountFragment);

            } else {
                // If the origin is not specified, just go back normally
                navController.popBackStack();
                Log.d("ManageAccountsFragment", "Navigating back to previous fragment.");
            }
        });

        // Initialize views
        catImageView = view.findViewById(R.id.catImageView);
        dogImageView = view.findViewById(R.id.dogImageView);
        catChangeButton = view.findViewById(R.id.catChangeButton);
        dogChangeButton = view.findViewById(R.id.dogChangeButton);
        catChosenText = view.findViewById(R.id.catChosenText);
        dogChosenText = view.findViewById(R.id.dogChosenText);

        // Get the user's petType reference in Firebase
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        userPetTypeRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("petType");

        // Set default pet selection based on Firebase data
        userPetTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String petType = snapshot.getValue(String.class);
                if ("Dog".equals(petType)) {
                    setDogAsChosen();
                } else {
                    setCatAsChosen(); // Default to Cat if no data or it's not "Dog"
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read pet type from Firebase: " + error.getMessage());
            }
        });

        // Set up click listeners
        catChangeButton.setOnClickListener(v -> setCatAsChosen());
        dogChangeButton.setOnClickListener(v -> {
            setDogAsChosen();
            updatePetTypeInFirebase("Dog"); // Update Firebase to "Dog" on selection
        });
    }

    private void setCatAsChosen() {
        // Show Cat as chosen and reset Dog
        catChangeButton.setVisibility(View.GONE);
        catChosenText.setVisibility(View.VISIBLE);
        dogChangeButton.setVisibility(View.VISIBLE);
        dogChosenText.setVisibility(View.GONE);

        updatePetTypeInFirebase("Cat"); // Update Firebase to "Cat" when selected
    }

    private void setDogAsChosen() {
        // Show Dog as chosen and reset Cat
        dogChangeButton.setVisibility(View.GONE);
        dogChosenText.setVisibility(View.VISIBLE);
        catChangeButton.setVisibility(View.VISIBLE);
        catChosenText.setVisibility(View.GONE);
    }

    private void updatePetTypeInFirebase(String petType) {
        userPetTypeRef.setValue(petType).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Pet type updated to " + petType);
            } else {
                Log.e(TAG, "Failed to update pet type: " + task.getException());
                Toast.makeText(getContext(), "Failed to update pet type", Toast.LENGTH_SHORT).show();
            }
        });
    }
}