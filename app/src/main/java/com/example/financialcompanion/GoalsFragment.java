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
    private SharedViewModel viewModel;
    private NavController navController;
    private String userId;
    private List<Goal> goalsList = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goals, container, false);

        // Get the user ID, ensuring the user is logged in
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Initialize the RecyclerView
        goalsRecyclerView = view.findViewById(R.id.goals_recycler_view);

        // Set up an empty list and adapter
        goalsList = new ArrayList<>();
        goalsAdapter = new GoalsAdapter(goalsList);
        goalsRecyclerView.setAdapter(goalsAdapter);

        // Observe the goals list and update the RecyclerView automatically
        viewModel.getGoals().observe(getViewLifecycleOwner(), updatedGoalsList -> {
            goalsAdapter.setGoals(updatedGoalsList);
            goalsAdapter.notifyDataSetChanged();  // Refresh the adapter with the latest data
        });

        // Call the method to update goal statuses
        updateGoalStatuses();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch goals in real-time to keep the adapter updated
        fetchLatestGoals();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String originFragment = getArguments() != null ? getArguments().getString("originFragment") : "";

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);

        NavController navController = Navigation.findNavController(view);

        Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24);

        toolbar.setNavigationOnClickListener(v -> {
            if ("home".equals(originFragment)) {
                navController.navigate(R.id.action_goalsFragment_to_homeFragment);
            } else {
                navController.popBackStack();
            }
        });
    }

    // Fetch goals in real-time to keep the list updated
    private void fetchLatestGoals() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("goals");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                goalsList.clear();
                for (DataSnapshot goalSnapshot : dataSnapshot.getChildren()) {
                    Goal goal = goalSnapshot.getValue(Goal.class);
                    if (goal != null) {
                        goalsList.add(goal);
                    }
                }
                goalsAdapter.updateGoalsList(goalsList);  // Update the adapter with new goals list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load goals", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGoalStatuses() {
        DatabaseReference goalsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("goals");

        goalsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                goalsList.clear();

                for (DataSnapshot goalSnapshot : dataSnapshot.getChildren()) {
                    Goal goal = goalSnapshot.getValue(Goal.class);
                    if (goal != null) {
                        goalsList.add(goal); // Add goal to the list
                    }
                }

                updateGoalsStatusesAfterLoading();  // Call to update the statuses
                goalsAdapter.updateGoalsList(goalsList);  // Refresh the adapter with the updated goal statuses
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error loading goals", databaseError.toException());
            }
        });
    }

    private void updateGoalsStatusesAfterLoading() {
        // Get the current system date
        Date currentDate = new Date();

        for (Goal goal : goalsList) {
            final double[] totalSavedAmount = {0.0};
            Long dueDateLong = goal.getDueDate();  // Assuming this returns the long value (milliseconds)
            String[] status = {goal.getStatus()};

            // Check if the dueDate is not null and compare it with currentDate
            if (dueDateLong != null) {
                Date dueDate = new Date(dueDateLong);  // Convert long value to Date object
                if (currentDate.after(dueDate)) {  // If the current date is after the due date
                    for (Account account : goal.getGoalsAccounts()) {
                        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("users")
                                .child(userId).child("accounts").child(account.getId()).child("transactions");

                        transactionsRef.orderByChild("type").equalTo("income")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        double accountTotal = 0.0;

                                        for (DataSnapshot transactionSnapshot : dataSnapshot.getChildren()) {
                                            Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                                            if (transaction != null) {
                                                accountTotal += transaction.getAmount();
                                            }
                                        }

                                        totalSavedAmount[0] += accountTotal;

                                        if (totalSavedAmount[0] >= goal.getAmount()) {
                                            status[0] = "Achieved";
                                        } else {
                                            status[0] = "Missed";
                                        }

                                        goal.setStatus(status[0]);

                                        DatabaseReference goalRef = FirebaseDatabase.getInstance()
                                                .getReference("users").child(userId).child("goals").child(goal.getId());
                                        goalRef.child("status").setValue(status[0]);

                                        goalsAdapter.updateGoalsList(goalsList);  // Refresh adapter after updating
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e("GoalStatusUpdate", "Error fetching transactions: " + databaseError.getMessage());
                                    }
                                });
                    }
                }
            }

            // Additional condition when the goal's due date has not passed and saved amount is checked
            else if (totalSavedAmount[0] >= goal.getAmount()) {
                status[0] = "Achieved";
                goal.setStatus(status[0]);
            }
        }
    }

}
