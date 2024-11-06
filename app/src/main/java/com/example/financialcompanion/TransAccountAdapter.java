package com.example.financialcompanion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransAccountAdapter extends RecyclerView.Adapter<TransAccountAdapter.AccountViewHolder> {

    private List<Account> accounts;
    private Context context;
    private Account selectedAccount;

    // Create a listener interface to notify the fragment about the account selection
    public interface OnAccountSelectedListener {
        void onAccountSelected(Account account);
    }

    private OnAccountSelectedListener listener;

    // Constructor with listener
    public TransAccountAdapter(Context context, List<Account> accounts, OnAccountSelectedListener listener) {
        this.context = context;
        this.accounts = accounts;
        this.listener = listener;
    }

    // ViewHolder for each account item
    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        public ImageView accountIcon;
        public TextView accountName;
        public LinearLayout accountItem;

        public AccountViewHolder(View view) {
            super(view);
            accountItem = view.findViewById(R.id.savings_item);
            accountIcon = view.findViewById(R.id.image_icon_savings);
            accountName = view.findViewById(R.id.text_label_savings);
        }
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.trans_account_item, parent, false);
        return new AccountViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accounts.get(position);
        holder.accountIcon.setImageResource(account.getIcon_id());
        holder.accountName.setText(account.getAccountName());

        // Set a click listener for each account item
        holder.accountItem.setOnClickListener(v -> {
            // Save the selected account
            selectedAccount = account;

            // Notify the fragment (or activity) that an account has been selected
            if (listener != null) {
                listener.onAccountSelected(account); // Notify listener (AccountDialogFragment)
            }

            // Optionally show a toast
            Toast.makeText(context, "Selected: " + account.getAccountName(), Toast.LENGTH_SHORT).show();
        });
    }

    // Update the list of accounts and refresh the RecyclerView
    @SuppressLint("NotifyDataSetChanged")
    public void setAccounts(List<Account> newAccountList) {
        this.accounts = newAccountList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    // Method to get the selected account
    public Account getSelectedAccount() {
        return selectedAccount;
    }
}

