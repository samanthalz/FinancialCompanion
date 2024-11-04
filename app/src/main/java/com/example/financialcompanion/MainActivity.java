package com.example.financialcompanion;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.financialcompanion.databinding.ActivityMainBinding;
import com.github.mikephil.charting.charts.PieChart;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    NavController navController;
    BottomNavigationView bottomNavigationView;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        // Obtain NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;

        // Obtain NavController
        navController = navHostFragment.getNavController();

        // Obtain BottomNavigationView from BottomAppBar
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up the BottomNavigationView with NavController
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        binding.fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                                             @NonNull NavDestination destination, @Nullable Bundle arguments) {
                Log.d("Navigation", "Destination ID: " + destination.getId() +
                        " (" + getResources().getResourceEntryName(destination.getId()) + ")");

                if(destination.getId() == R.id.transaction_fragment) {
                    bottomNavigationView.setVisibility(View.GONE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            }
        });

        // Check if the user is a first-time user
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        checkIfFirstTimeUser(userId);
    }

    private void checkIfFirstTimeUser(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Use get() to retrieve the user data once
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot dataSnapshot = task.getResult();
                Boolean isFirstTimeUser = dataSnapshot.child("firstTimeUser").getValue(Boolean.class);

                Log.d(TAG, "Retrieved isFirstTimeUser: " + isFirstTimeUser);

                if (isFirstTimeUser != null) {
                    Log.d(TAG, "isFirstTimeUser: " + isFirstTimeUser); // Log the boolean value
                    if (isFirstTimeUser) {
                        // Redirect to savings balance activity
                        Intent intent = new Intent(MainActivity.this, SavingsEntryActivity.class);
                        startActivity(intent);
                        finish(); // Close MainActivity if redirecting
                    }
                } else {
                    // Handle the case where the isFirstTimeUser field does not exist
                    Log.e(TAG, "isFirstTimeUser field is null for userId: " + userId);
                }

            } else {
                // Handle the case where the user data retrieval failed
                Log.e(TAG, "Failed to retrieve user data: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
            }
        });
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_item_menu);

        LinearLayout transLayout = dialog.findViewById(R.id.layout_addTrans);
        LinearLayout goalLayout = dialog.findViewById(R.id.layout_addGoal);
        LinearLayout budgetLayout = dialog.findViewById(R.id.layout_addBudget);
        ImageView cancelButton = dialog.findViewById(R.id.add_item_close_button);

        transLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                // Create an instance of TransactionFragment
                TransactionFragment transactionFragment = new TransactionFragment();

                // Get the FragmentManager and start a transaction
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                bottomNavigationView.setVisibility(View.GONE);
                binding.bottomAppBar.setVisibility(View.GONE);
                binding.fabAdd.setVisibility(View.GONE);
                findViewById(R.id.financial_summary_layout).setVisibility(View.GONE); // Hide financial summary

                // Replace the current fragment with TransactionFragment
                fragmentTransaction.replace(R.id.nav_host_fragment, transactionFragment);

                // Optional: Add the transaction to the back stack
                fragmentTransaction.addToBackStack(null);

                // Commit the transaction
                fragmentTransaction.commit();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }


}