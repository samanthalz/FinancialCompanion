package com.example.financialcompanion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.GoalViewHolder> {

    private List<Goal> goalsList;
    private TextView totalSavedTextView;

    // Constructor to initialize goals list
    public GoalsAdapter(List<Goal> goalsList) {
        this.goalsList = goalsList;
    }

    // ViewHolder class for holding individual goal views
    public class GoalViewHolder extends RecyclerView.ViewHolder {
        public TextView labelTextView;
        public TextView dueDateTextView;
        public TextView statusTextView;
        //public TextView totalSavedTextView;
        public TextView goalAmountTextView;
        public TextView descriptionLabelTextView;
        public TextView descriptionTextView;
        ImageView deleteIcon;

        public GoalViewHolder(View itemView) {
            super(itemView);
            labelTextView = itemView.findViewById(R.id.goalLabel);
            dueDateTextView = itemView.findViewById(R.id.goalDueDate);
            statusTextView = itemView.findViewById(R.id.goalStatus);
            //totalSavedTextView = itemView.findViewById(R.id.goalTotalSaved).findViewById(R.id.goalAmount); // Update to match layout
            totalSavedTextView = itemView.findViewById(R.id.goalTotalSavedText);
            goalAmountTextView = itemView.findViewById(R.id.goalAmount);
            descriptionLabelTextView = itemView.findViewById(R.id.goalDescriptionLabel);
            descriptionTextView = itemView.findViewById(R.id.goalDescription);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
        }
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item_goal layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal, parent, false);

        return new GoalViewHolder(itemView);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(GoalViewHolder holder, int position) {
        Goal goal = goalsList.get(position);

        // Bind the data to the views
        holder.labelTextView.setText(goal.getLabel());

        // Assuming dueDate is in long (milliseconds since epoch)
        Long dueDateLong = goal.getDueDate();  // Assuming this returns a long value (milliseconds)
        if (dueDateLong != null) {
            // Create a Date object from the long value
            Date dueDate = new Date(dueDateLong);

            // Format the Date object into "dd/MM/yyyy"
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = sdf.format(dueDate);  // Convert Date to formatted string

            // Set the formatted date in the TextView
            holder.dueDateTextView.setText(formattedDate);
        }

        holder.statusTextView.setText(goal.getStatus());

        // Set the statusTextView color based on the goal status
        String status = goal.getStatus();
        if (status != null) {
            switch (status) {
                case "In Progress":
                    holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange));  // In Progress - orange
                    break;
                case "Achieved":
                    holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));  // Achieved - green
                    break;
                case "Missed":
                    holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));  // Missed - red
                    break;
                default:
                    holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));  // Default color if none match
                    break;
            }
        }

        // Format the goal amount and description
        holder.goalAmountTextView.setText(String.format("RM%.2f", goal.getAmount()));
        holder.descriptionTextView.setText(goal.getDescription());

        // Set up the delete icon
        holder.deleteIcon.setOnClickListener(v -> {
            // Show confirmation dialog
            new AlertDialog.Builder(holder.itemView.getContext())  // Use itemView context to avoid context leaks
                    .setTitle("Delete Goal")
                    .setMessage("Are you sure you want to delete this goal?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // If user confirms, delete the goal from Firebase
                        deleteGoalFromFirebase(holder.itemView.getContext(), goal.getId());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteGoalFromFirebase(Context context, String goalId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId != null) {
            // Get the reference to the goal in Firebase
            DatabaseReference goalRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("goals")
                    .child(goalId);

            // Delete the goal from Firebase
            goalRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Goal successfully deleted, notify adapter
                        goalsList.removeIf(goal -> goal.getId().equals(goalId));
                        notifyDataSetChanged();
                        Toast.makeText(context, "Goal deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Toast.makeText(context, "Failed to delete goal", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return goalsList.size();
    }

    // Method to update the list of goals
    @SuppressLint("NotifyDataSetChanged")
    public void updateGoalsList(List<Goal> newGoalsList) {
        this.goalsList = newGoalsList;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setGoals(List<Goal> newGoalList) {
        this.goalsList = newGoalList;
        notifyDataSetChanged();
    }

    @SuppressLint({"NotifyDataSetChanged", "DefaultLocale"})
    public void setTotalSaved(double totalSavedAmount) {
        totalSavedTextView.setText(String.format("RM%.2f", totalSavedAmount));
        notifyDataSetChanged();
    }


}
