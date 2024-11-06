package com.example.financialcompanion;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.GoalViewHolder> {

    private List<Goal> goalsList;

    // Constructor to initialize goals list
    public GoalsAdapter(List<Goal> goalsList) {
        this.goalsList = goalsList;
    }

    // ViewHolder class for holding individual goal views
    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        public TextView labelTextView;
        public TextView dueDateTextView;
        public TextView statusTextView;
        public TextView totalSavedTextView;
        public TextView goalAmountTextView;
        public TextView descriptionLabelTextView;
        public TextView descriptionTextView;

        public GoalViewHolder(View itemView) {
            super(itemView);
            labelTextView = itemView.findViewById(R.id.goalLabel);
            dueDateTextView = itemView.findViewById(R.id.goalDueDate);
            statusTextView = itemView.findViewById(R.id.goalStatus);
            totalSavedTextView = itemView.findViewById(R.id.goalTotalSaved).findViewById(R.id.goalAmount); // Update to match layout
            goalAmountTextView = itemView.findViewById(R.id.goalAmount);
            descriptionLabelTextView = itemView.findViewById(R.id.goalDescriptionLabel);
            descriptionTextView = itemView.findViewById(R.id.goalDescription);
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

        // Convert Date to formatted string
        Date dueDate = goal.getDueDate();  // Assuming this returns a Date object
        if (dueDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());  // Customize format as needed
            String formattedDate = sdf.format(dueDate);
            holder.dueDateTextView.setText(formattedDate);
        }
        holder.statusTextView.setText(goal.getStatus());  // Assuming the status is part of Goal model
       // holder.totalSavedTextView.setText(String.format("RM%.2f", goal.getTotalSaved()));
        holder.goalAmountTextView.setText(String.format("RM%.2f", goal.getAmount()));
        holder.descriptionTextView.setText(goal.getDescription());
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
    public void updateGoals(List<Goal> updatedGoalsList) {
        // Update the adapter's data with the new list of goals
        this.goalsList = updatedGoalsList;
        notifyDataSetChanged();  // Notify the adapter that the data has changed
    }

}
