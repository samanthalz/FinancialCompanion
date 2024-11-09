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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.financialcompanion.databinding.ActivityMainBinding;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EditProfileFragment extends Fragment {

    private TextInputEditText usernameTextInput;
    private TextInputEditText nameTextInput;
    private TextInputEditText emailTextInput;
    private DatabaseReference databaseReference;
    private String userId;
    private Button submitButton;
    private ImageView editProfileIcon;
    private ImageView profilePicture;
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
        editProfileIcon = view.findViewById(R.id.edit_profile_icon);
        profilePicture = view.findViewById(R.id.profile_picture);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Get the current user's ID
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Fetch user data from the database
        databaseReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch and set default text
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    Integer profileImageVectorId = dataSnapshot.child("profileImageVectorId").getValue(Integer.class);

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
                    if (profileImageVectorId != null) {
                        profilePicture.setImageResource(profileImageVectorId);
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

        // Set an onClickListener for the edit button
        editProfileIcon.setOnClickListener(v -> {

            // Example list of drawable resources for each account
            List<Integer> accountDrawables = Arrays.asList(
                    R.drawable.afro_avatar_man_male,
                    R.drawable.afro_female_woman,
                    R.drawable.afro_kid_child_boy,
                    R.drawable.artist_monroe_marilyn_avatar,
                    R.drawable.avatar_1_woman_portrait_female,
                    R.drawable.avatar_indian_hindi_woman,
                    R.drawable.avatar_kid_girl_child,
                    R.drawable.avatar_muslim_man,
                    R.drawable.avatar_russian_bear_animal,
                    R.drawable.beard_male_hipster_man,
                    R.drawable.boy_kid_person_avatar,
                    R.drawable.boy_male_portrait_avatar,
                    R.drawable.breaking_chemisrty_heisenberg_avatar_bad,
                    R.drawable.bug_spider_avatar_insect,
                    R.drawable.cactus_pirate_cacti_avatar,
                    R.drawable.child_baby_toddler_kid,
                    R.drawable.christmas_clous_santa,
                    R.drawable.coffee_cup_zorro_avatar,
                    R.drawable.crying_avatar_rain_cloud,
                    R.drawable.food_avatar_avocado_scream,
                    R.drawable.geisha_avatar_woman_japanese,
                    R.drawable.grandma_avatar_nanny_elderly,
                    R.drawable.halloween_movie_jason_friday,
                    R.drawable.helmet_builder_worker,
                    R.drawable.indian_boy_native_kid,
                    R.drawable.indian_male_man_person,
                    R.drawable.joker_squad_woman_avatar_suicide,
                    R.drawable.kid_child_person_girl,
                    R.drawable.love_addicted_draw_pencil,
                    R.drawable.male_avatar_portrait_man,
                    R.drawable.male_trump_avatar_president_donald_trump,
                    R.drawable.man_comedy_actor_chaplin,
                    R.drawable.man_portrait_old_male,
                    R.drawable.man_sikh_indian_turban,
                    R.drawable.monster_zombie_dead_avatar,
                    R.drawable.muslim_avatar_paranja_woman,
                    R.drawable.ozzy_avatar_singer_male_rock,
                    R.drawable.person_avatar_pilot_traveller,
                    R.drawable.person_avatar_punk_man,
                    R.drawable.scientist_avatar_einstein_professor,
                    R.drawable.avatar_batman_hero_comics,
                    R.drawable.sluggard_sloth_lazybones,
                    R.drawable.spirited_no_face_anime_away_nobody,
                    R.drawable.ufo_space_alien_avatar,
                    R.drawable.woman_avatar_female_girl,
                    R.drawable.woman_avatar_female_portrait,
                    R.drawable.woman_sister_avatar_nun,
                    R.drawable.wrestler_man_fighter_luchador
            );

            // Show the dialog with the list of profile accounts and their drawables
            ProfileAccountsDialogFragment dialogFragment = new ProfileAccountsDialogFragment(accountDrawables);
            dialogFragment.show(getChildFragmentManager(), "ProfileAccountsDialog");
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Ensure the activity is not null
        if (getActivity() != null) {
            // Hide the BottomNavigationView
            View bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.GONE);
            }

            // Hide the BottomAppBar using View Binding (replace `binding` with your binding instance)
            ActivityMainBinding binding = ((MainActivity) getActivity()).binding;
            if (binding != null) {
                binding.bottomAppBar.setVisibility(View.GONE);
                binding.fabAdd.setVisibility(View.GONE);
            }

            // Hide the Financial Summary Layout
            View financialSummaryLayout = getActivity().findViewById(R.id.financial_summary_layout);
            if (financialSummaryLayout != null) {
                financialSummaryLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Ensure the activity is not null
        if (getActivity() != null) {
            // Show the BottomNavigationView again
            View bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }

            // Show the BottomAppBar and FAB using View Binding (replace `binding` with your binding instance)
            ActivityMainBinding binding = ((MainActivity) getActivity()).binding;
            if (binding != null) {
                binding.bottomAppBar.setVisibility(View.VISIBLE);
                binding.fabAdd.setVisibility(View.VISIBLE);
            }

            // Show the Financial Summary Layout again
            View financialSummaryLayout = getActivity().findViewById(R.id.financial_summary_layout);
            if (financialSummaryLayout != null) {
                financialSummaryLayout.setVisibility(View.VISIBLE);
            }
        }
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