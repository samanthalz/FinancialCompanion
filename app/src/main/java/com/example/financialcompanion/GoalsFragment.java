package com.example.financialcompanion;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class GoalsFragment extends Fragment {
    private RecyclerView goalsRecyclerView;
    private GoalsAdapter goalsAdapter;
    private List<Goal> goalsList;
    private SharedViewModel viewModel;
    private NavController navController;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goals, container, false);  // Ensure your layout is for goals

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Initialize the RecyclerView
        goalsRecyclerView = view.findViewById(R.id.goals_recycler_view);  // Make sure this is the correct RecyclerView ID for goals

        // Set up an empty list and adapter
        goalsList = new ArrayList<>();
        goalsAdapter = new GoalsAdapter(goalsList);
        goalsRecyclerView.setAdapter(goalsAdapter);

        // Observe goals in ViewModel to update RecyclerView automatically
//        viewModel.getGoals().observe(getViewLifecycleOwner(), goals -> {
//            goalsAdapter.setGoals(goals);  // Update adapter with new data
//            goalsAdapter.notifyDataSetChanged();  // Refresh the adapter
//        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchLatestGoals();  // Fetch goals when the fragment is resumed
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
            Log.d("GoalsFragment", "Back pressed from origin: " + originFragment);

            // Check the origin fragment and navigate accordingly
            if ("home".equals(originFragment)) {
                // Check if the action exists in the navigation graph
                navController.navigate(R.id.action_goalsFragment_to_homeFragment);
            } else {
                // If the origin is not specified, just go back normally
                navController.popBackStack();
                Log.d("GoalsFragment", "Navigating back to previous fragment.");
            }
        });
    }

    // Method to fetch goals from Firebase
    private void fetchLatestGoals() {
        List<Goal> goalsList = new ArrayList<>();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("goals");

        // Listener to get all goals
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot goalsSnapshot) {
                // Loop through each goal
                for (DataSnapshot goalSnapshot : goalsSnapshot.getChildren()) {
                    // Directly map the DataSnapshot to a Goal object
                    Goal goal = goalSnapshot.getValue(Goal.class);

                    // Check if the goal is not null
                    if (goal != null) {
                        // Add goal to the list
                        goalsList.add(goal);

                        // Log the goal information
                        Log.d("GoalInfo", "Goal ID: " + goalSnapshot.getKey() +
                                " | Label: " + goal.getLabel() +
                                " | Amount: " + goal.getAmount() +
                                " | Due Date: " + goal.getDueDate() +
                                " | Description: " + goal.getDescription() +
                                " | Status: " + goal.getStatus());
                    } else {
                        Log.w("GoalInfo", "Goal with ID " + goalSnapshot.getKey() + " is null.");
                    }
                }
                // After all goals are fetched, update the adapter
                if (goalsAdapter != null) {
                    goalsAdapter.updateGoals(goalsList);  // Update the adapter with the latest goals
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(getContext(), "Failed to load goals", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
