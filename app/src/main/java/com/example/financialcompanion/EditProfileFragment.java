package com.example.financialcompanion;

import android.app.AlertDialog;
import android.content.Intent;
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

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
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

        // Set submit button listener to validate inputs and submit data to Firebase
        submitButton.setOnClickListener(v -> {
            if (validateFields()) {
                // If validation passes, push updated data to Firebase
                String username = Objects.requireNonNull(usernameTextInput.getText()).toString();
                String name = Objects.requireNonNull(nameTextInput.getText()).toString();
                String email = Objects.requireNonNull(emailTextInput.getText()).toString();

                // Update the user data in Firebase
                Map<String, Object> userUpdates = new HashMap<>();
                userUpdates.put("username", username);
                userUpdates.put("name", name);
                userUpdates.put("email", email);

                // Push the updated data to the Firebase database
                databaseReference.child(userId).updateChildren(userUpdates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Optionally show a success message or navigate back
                                Log.d("EditProfileFragment", "Profile updated successfully");
                                navController.navigateUp(); // Navigate back after successful update
                            } else {
                                // Handle failure (e.g., show an error message)
                                Log.e("EditProfileFragment", "Failed to update profile", task.getException());
                            }
                        });
            }
        });

        // Set click listener for the delete account button
        deleteAccount.setOnClickListener(v -> {
            // Show confirmation dialog
            new AlertDialog.Builder(requireContext())
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setCancelable(false) // Make the dialog non-cancelable
                    .setPositiveButton("Yes", (dialog, id) -> {
                        // Proceed with account deletion
                        deleteAccount();
                    })
                    .setNegativeButton("No", null) // Do nothing on "No"
                    .show();
        });

        return view;
    }

    // Method to delete the account
    private void deleteAccount() {
        // Initialize Firebase references
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        if (user != null) {
            String userId = user.getUid();

            // First, delete the user's data from Firebase Realtime Database
            databaseReference.child(userId).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // If database deletion was successful, now attempt to delete the user from Firebase Authentication
                            reAuthenticateUser(user);
                        } else {
                            // Handle errors in Firebase Realtime Database deletion
                            Log.e("EditProfileFragment", "Error deleting user from Realtime Database", task.getException());
                        }
                    });
        }
    }

    // Re-authenticate the user before proceeding with deletion
    private void reAuthenticateUser(FirebaseUser user) {
        // Get the user's email (this is used for re-authentication)
        String email = user.getEmail();

        if (email != null) {
            // Show dialog to prompt for the password
            showPasswordDialog(email, user);
        } else {
            // Handle the case where email is null (although unlikely)
            Log.e("EditProfileFragment", "User email is null");
        }
    }

    // Show dialog to prompt the user for their password
    private void showPasswordDialog(String email, FirebaseUser user) {
        // Create an EditText for password input
        final EditText passwordEditText = new EditText(requireContext());
        passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Password")
                .setMessage("Please enter your password to confirm account deletion")
                .setView(passwordEditText)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String password = passwordEditText.getText().toString().trim();
                    if (!password.isEmpty()) {
                        // Re-authenticate the user with the entered password
                        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                        user.reauthenticate(credential)
                                .addOnCompleteListener(authTask -> {
                                    if (authTask.isSuccessful()) {
                                        // If re-authentication is successful, delete the user
                                        deleteUser(user);
                                    } else {
                                        // Handle re-authentication failure
                                        Toast.makeText(requireContext(), "Re-authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
                                        Log.e("EditProfileFragment", "Re-authentication failed", authTask.getException());
                                    }
                                });
                    } else {
                        Toast.makeText(requireContext(), "Password cannot be empty.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    // Delete the user from Firebase Authentication
    private void deleteUser(FirebaseUser user) {
        // Now that the user is re-authenticated, delete the user from Firebase Authentication
        user.delete()
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        // If user deletion was successful, navigate to the login page
                        navigateToLoginPage();
                    } else {
                        // Handle errors in Firebase Authentication deletion
                        Log.e("EditProfileFragment", "Error deleting user from FirebaseAuth", authTask.getException());
                    }
                });
    }

    // Navigate to the login page after deletion
    private void navigateToLoginPage() {
        // Clear any user-related data if necessary
        FirebaseAuth.getInstance().signOut();

        // Navigate to the login fragment/activity
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish(); // Close the current activity
    }


    private boolean validateFields() {
        // Validate username, name, and email fields
        return validateField(nameTextInput, "name") &&
                validateField(usernameTextInput, "username") &&
                validateEmail(emailTextInput);
    }

    private boolean validateField(TextInputEditText field, String fieldType) {
        String val = Objects.requireNonNull(field.getText()).toString().trim();
        switch (fieldType) {
            case "name":
                if (val.isEmpty()) {
                    field.setError("Name cannot be empty");
                    return false;
                }
                break;
            case "username":
                if (val.isEmpty()) {
                    field.setError("Username cannot be empty");
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean validateEmail(TextInputEditText field) {
        String val = Objects.requireNonNull(field.getText()).toString().trim();
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        if (val.isEmpty()) {
            field.setError("Email cannot be empty");
            return false;
        } else if (!val.matches(emailPattern)) {
            field.setError("Invalid email format");
            return false;
        }
        return true;
    }

}