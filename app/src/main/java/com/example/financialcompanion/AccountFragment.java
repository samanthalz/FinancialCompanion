package com.example.financialcompanion;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AccountFragment extends Fragment {

    private TextView usernameTextView;
    private TextView userEmailTextView;
    private DatabaseReference databaseReference;
    private String userId;
    private NavController navController;
    Button logoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Initialize the NavController
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);



        // Set click listeners for each setting option
        setupNavigation(view);

        // Initialize TextViews
        usernameTextView = view.findViewById(R.id.user_name);
        userEmailTextView = view.findViewById(R.id.user_email);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Load user details from SharedPreferences
        loadUserDetailsFromPreferences();

        // Load user details from Firebase (optional, if you want to refresh data)
        loadUserDetails();

        logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> logout());

        return view;
    }

    private void setupNavigation(View view) {
        LinearLayout editProfileLayout = view.findViewById(R.id.edit_profile_layout);
        LinearLayout changePasswordLayout = view.findViewById(R.id.change_pass_layout);
        LinearLayout manageAccountsLayout = view.findViewById(R.id.manage_acc_layout);

        editProfileLayout.setOnClickListener(v -> {
            navController.navigate(R.id.editProfileFragment);
        });

        changePasswordLayout.setOnClickListener(v -> {
            navController.navigate(R.id.changePasswordFragment);
        });

        manageAccountsLayout.setOnClickListener(v -> {
            navController.navigate(R.id.manageAccountsFragment);
        });
    }

    private void loadUserDetails() {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("username").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);

                    // Save to SharedPreferences
                    SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", name);
                    editor.putString("email", email);
                    editor.apply();

                    // Display user details
                    usernameTextView.setText(name);
                    userEmailTextView.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors here
            }
        });
    }

    private void loadUserDetailsFromPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("username", "Default Name");
        String email = sharedPreferences.getString("email", "default@example.com");

        // Display user details
        usernameTextView.setText(name);
        userEmailTextView.setText(email);
    }

    // Method to handle logout
    private void logout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Navigate to the Login Activity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish(); // Close the current activity
    }

}