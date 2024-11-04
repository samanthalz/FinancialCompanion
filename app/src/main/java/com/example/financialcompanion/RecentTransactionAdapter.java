package com.example.financialcompanion;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RecentTransactionAdapter extends RecyclerView.Adapter<RecentTransactionAdapter.TransactionViewHolder> {
    private final List<Transaction> transactionList;
    private List<Category> expenseCategories;

    public RecentTransactionAdapter(List<Transaction> transactionList, List<Category> expenseCategories) {
        this.transactionList = transactionList;
        this.expenseCategories = expenseCategories;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.bind(transaction, expenseCategories);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateTransactions(List<Transaction> newTransactions) {
        transactionList.clear();
        transactionList.addAll(newTransactions);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setExpenseCategories(List<Category> expenseCategories) {
        this.expenseCategories = expenseCategories;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }


    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView transactionTextView;
        private final TextView transactionDateTimeTextView;
        private final TextView transactionAmountTextView;
        private final ImageView transactionIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionTextView = itemView.findViewById(R.id.transactionTextView);
            transactionDateTimeTextView = itemView.findViewById(R.id.transactionDateTimeTextView);
            transactionAmountTextView = itemView.findViewById(R.id.transactionAmountTextView);
            transactionIcon = itemView.findViewById(R.id.transactionIcon);

        }

        @SuppressLint("DefaultLocale")
        public void bind(Transaction transaction, List<Category> expenseCategories) {
            // Log the expenseCategories list to check if it's null
            if (expenseCategories == null) {
                Log.d("RecentTransactionAdapter", "expenseCategories is null");
            } else {
                Log.d("RecentTransactionAdapter", "expenseCategories size: " + expenseCategories.size());
            }

            // Look up category name based on categoryId
            assert expenseCategories != null;
            String categoryName = getCategoryNameByVectorId(transaction.getCategoryId(), expenseCategories);
            transactionTextView.setText(categoryName);

            // Format date as desired
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            String formattedDate = dateFormat.format(transaction.getDate()); // Ensure transaction.getDate() returns a Date object
            transactionDateTimeTextView.setText(formattedDate);

            // Find the corresponding Category object using categoryId
            Category category = getCategoryById(transaction.getCategoryId(), expenseCategories);
            if (category != null) {
                // Set the ImageView resource based on the vector resource ID
                int vectorResourceId = category.getVectorResource();
                transactionIcon.setImageResource(vectorResourceId); // Bind the image resource
            } else {
                // Optional: Set a default image resource if the category is not found
                transactionIcon.setImageResource(R.drawable.baseline_error_24);
            }

            // Set the initial amount format
            transactionAmountTextView.setText(String.format("RM%.2f", transaction.getAmount()));

            String formattedAmount;
            if ("income".equals(transaction.getType())) {
                // Format the amount for income
                formattedAmount = String.format("+RM%.2f", transaction.getAmount());
                transactionAmountTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark)); // Green for income
            } else if ("expense".equals(transaction.getType())) {
                // Format the amount for expense
                formattedAmount = String.format("-RM%.2f", transaction.getAmount());
                transactionAmountTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark)); // Red for expense
            } else {
                formattedAmount = String.format("RM%.2f", transaction.getAmount()); // Fallback for other types
                transactionAmountTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black)); // Default color
            }

            // Set the formatted amount to the TextView
            transactionAmountTextView.setText(formattedAmount);


        }

        // Helper method to get Category by ID
        private Category getCategoryById(int categoryId, List<Category> categories) {
            for (Category category : categories) {
                if (category.getVectorResource()==categoryId) {
                    return category;
                }
            }
            return null; // Return null if no matching category is found
        }

        private String getCategoryNameByVectorId(int vectorResourceId, List<Category> categories) {
            for (Category category : categories) {
                // Assuming category.getVectorResourceId() returns an int corresponding to the vector resource
                if (category.getVectorResource() == vectorResourceId) {
                    return category.getName();
                }
            }
            return "Unknown Category"; // Fallback if category not found
        }
    }
}

