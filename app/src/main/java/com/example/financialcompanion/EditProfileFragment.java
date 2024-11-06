package com.example.financialcompanion;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class EditProfileFragment extends Fragment {

    private TextInputEditText usernameTextInput;
    private TextInputEditText nameTextInput;
    private TextInputEditText emailTextInput;
    private DatabaseReference databaseReference;
    private String userId;
    private Button submitButton;
    private TextView deleteAccount;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Initialize the Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar); // Set the toolbar as the action bar

        // Obtain NavController
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        // Set up AppBarConfiguration with top destinations
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();

        // Link the toolbar with NavController
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

        // Initialize views
        usernameTextInput = view.findViewById(R.id.input_username);
        nameTextInput = view.findViewById(R.id.input_name);
        emailTextInput = view.findViewById(R.id.input_email);
        submitButton = view.findViewById(R.id.submit_button);
        deleteAccount = view.findViewById(R.id.delete_account_text);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Get the current user's ID
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Fetch user data from the database
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch and set default text
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);

                    // Set default text for each TextInputEditText field
                    if (username != null) {
                        usernameTextInput.setText(username);
                    }
                    if (name != null) {
                        nameTextInput.setText(name);
                    }
                    if (email != null) {
                        emailTextInput.setText(email);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors (e.g., log them or show a toast)
                Log.e("EditProfileFragment", "Failed to fetch user data", databaseError.toException());
            }
        });

        // Set Navigation click listener for the back button
        toolbar.setNavigationOnClickListener(v -> {
            navController.navigateUp(); // Navigate back to the previous fragment (AccountFragment)
        });

        return view;
    }

}