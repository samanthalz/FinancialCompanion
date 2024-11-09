package com.example.financialcompanion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private final List<Budget> budgetSnapshots;
    private final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private final String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private static final String TAG = "BudgetAdapter";

    public BudgetAdapter(List<Budget> budgetSnapshots) {
        this.budgetSnapshots = budgetSnapshots;
    }

    // Callback Interface for Category ID
    public interface CategoryIdCallback {
        void onCategoryIdFetched(int categoryId);
    }

    // Callback Interface for Total Spent Calculation
    public interface OnTotalSpentCalculatedListener {
        void onTotalSpentCalculated(double totalSpent);
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        // Get the budget at the given position
        Budget budget = budgetSnapshots.get(position);

        if (budget != null) {
            // Bind the data to the views in the holder
            holder.categoryTextView.setText(budget.getCategory());
            holder.amountTextView.setText("Budget: RM " + budget.getAmount());

            // Fetch categoryId based on category name
            getCategoryIdByName(budget.getCategory(), new CategoryIdCallback() {
                @Override
                public void onCategoryIdFetched(int categoryId) {
                    getSpentAmountForCategory(categoryId, new OnTotalSpentCalculatedListener() {
                        @Override
                        public void onTotalSpentCalculated(double totalSpent) {
                            holder.spentTextView.setText("Spent: RM " + totalSpent);
                            double remainingAmount = budget.getAmount() - totalSpent;
                            holder.remainingTextView.setText("Remaining: RM " + remainingAmount);
                            holder.iconImageView.setImageResource(categoryId);
                        }
                    });
                }
            });

            // Set the edit icon click listener inside onBindViewHolder
            holder.editIcon.setOnClickListener(v -> {
                // Create the dialog for editing the budget
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                // Create a layout to hold the EditText
                LinearLayout layout = new LinearLayout(v.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);

                // Create the EditText for user input
                EditText editText = new EditText(v.getContext());
                editText.setHint("Enter new budget amount");
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                layout.addView(editText);

                // Set the dialog's view to the layout we created
                builder.setView(layout);
                builder.setTitle("Change Budget");

                // Set up the "OK" button to update the budget amount
                builder.setPositiveButton("OK", (dialog, which) -> {
                    String newBudget = editText.getText().toString();
                    if (!newBudget.isEmpty()) {
                        double updatedBudget = Double.parseDouble(newBudget);

                        // Update the budget object with the new balance
                        budget.setAmount(updatedBudget);

                        // Get the current user's UID from FirebaseAuth
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference budgetRef = database.getReference("users").child(userId).child("budget").child(budget.getId());

                        // Create a map of the updates
                        Map<String, Object> updatedBudgetMap = new HashMap<>();
                        updatedBudgetMap.put("amount", updatedBudget);  // Update the amount field

                        // Apply the update to Firebase
                        budgetRef.updateChildren(updatedBudgetMap)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Budget updated successfully in Firebase");
                                    // Optionally, refresh the RecyclerView or perform other UI updates
                                    notifyItemChanged(position);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to update budget in Firebase: " + e.getMessage());
                                    Toast.makeText(v.getContext(), "Failed to update budget", Toast.LENGTH_SHORT).show();
                                });

                        // And notify the adapter to refresh the view
                        notifyItemChanged(position);
                    } else {
                        Toast.makeText(v.getContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                    }
                });

                // Set up the "Cancel" button to dismiss the dialog
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                // Add a "Delete" button to the dialog
                builder.setNeutralButton("Delete", (dialog, which) -> {
                    // Get the current user's UID from FirebaseAuth
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference budgetRef = database.getReference("users").child(userId).child("budget").child(budget.getId());

                    // Remove the budget from Firebase
                    budgetRef.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Budget deleted successfully from Firebase");
                                // Optionally, remove from the RecyclerView as well
                                budgetSnapshots.remove(position); // Remove from the list
                                notifyItemRemoved(position); // Refresh the RecyclerView
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to delete budget from Firebase: " + e.getMessage());
                                Toast.makeText(v.getContext(), "Failed to delete budget", Toast.LENGTH_SHORT).show();
                            });
                });

                // Show the dialog
                builder.create().show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return budgetSnapshots.size();
    }

    // Helper method to fetch the categoryId by category name
    private void getCategoryIdByName(String categoryName, CategoryIdCallback listener) {
        Log.d(TAG, "Fetching categoryId for categoryName: " + categoryName);

        mDatabase.child("users").child(userId).child("categories").child("expense")
                .orderByChild("name")
                .equalTo(categoryName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Log.d(TAG, "Category found for: " + categoryName);
                            // Loop through the categories and find the matching categoryId
                            for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                                Integer categoryId = categorySnapshot.child("vectorResource").getValue(Integer.class);
                                if (categoryId != null) {
                                    Log.d(TAG, "CategoryId fetched: " + categoryId);
                                    listener.onCategoryIdFetched(categoryId);
                                } else {
                                    Log.d(TAG, "CategoryId is null for category: " + categoryName);
                                    listener.onCategoryIdFetched(-1); // Error value
                                }
                            }
                        } else {
                            Log.d(TAG, "No category found for: " + categoryName);
                            listener.onCategoryIdFetched(-1); // Error value if not found
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "DatabaseError: " + databaseError.getMessage(), databaseError.toException());
                    }
                });
    }

    // Interface for async callback to return the fetched categoryId
    interface OnCategoryIdFetchedListener {
        void onCategoryIdFetched(int categoryId);
    }

    // Method to calculate the total spent for a specific categoryId
    private void getSpentAmountForCategory(int categoryId, final OnTotalSpentCalculatedListener listener) {
        final double[] totalSpent = {0};

        mDatabase.child("users").child(userId).child("accounts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot accountsSnapshot) {
                        for (DataSnapshot accountSnapshot : accountsSnapshot.getChildren()) {
                            String accountId = accountSnapshot.getKey();

                            assert accountId != null;
                            mDatabase.child("users").child(userId).child("accounts").child(accountId)
                                    .child("transactions")
                                    .orderByChild("categoryId")
                                    .equalTo(categoryId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot transactionsSnapshot) {
                                            for (DataSnapshot transactionSnapshot : transactionsSnapshot.getChildren()) {
                                                Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                                                if (transaction != null && "expense".equals(transaction.getType())) {
                                                    totalSpent[0] += transaction.getAmount();
                                                }
                                            }

                                            // Notify listener when total spent is calculated
                                            listener.onTotalSpentCalculated(totalSpent[0]);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.e(TAG, "DatabaseError: " + databaseError.getMessage(), databaseError.toException());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "DatabaseError: " + databaseError.getMessage(), databaseError.toException());
                    }
                });
    }


    // ViewHolder for the budget
    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTextView, amountTextView, spentTextView, remainingTextView;
        ImageView iconImageView, editIcon;

        public BudgetViewHolder(View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.iconNameTextView);
            amountTextView = itemView.findViewById(R.id.limitTextView);
            spentTextView = itemView.findViewById(R.id.spentTextView);
            remainingTextView = itemView.findViewById(R.id.remainingTextView);
            iconImageView = itemView.findViewById(R.id.iconImageView);
            editIcon = itemView.findViewById(R.id.editIcon);
        }
    }
}

