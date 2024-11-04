package com.example.financialcompanion;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Objects;

public class ChangePasswordFragment extends Fragment {

    private FirebaseAuth auth;
    private EditText emailInput;
    private String userId;
    private NavController navController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

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

        // Enable Up button and set the back arrow icon
        Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);

        // Set Navigation click listener for the back button
        toolbar.setNavigationOnClickListener(v -> {
            navController.navigateUp(); // Navigate back to the previous fragment (AccountFragment)
        });

        auth = FirebaseAuth.getInstance(); // Initialize Firebase Auth
        emailInput = view.findViewById(R.id.email_input);


        Button sendCodeButton = view.findViewById(R.id.send_code_button);
        sendCodeButton.setOnClickListener(v -> sendPasswordResetEmail());

        return view;
    }

    private void sendPasswordResetEmail() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Please enter your email");
            emailInput.requestFocus();
            return;
        }

        // Send the password reset email
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Reset email sent! Check your inbox.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error sending reset email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}