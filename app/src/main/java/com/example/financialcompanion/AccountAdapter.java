package com.example.financialcompanion;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private List<Account> accountList;
    private View.OnClickListener onClickListener;

    // Define an interface for the item click listener
    public interface OnAccountClickListener {
        void onAccountClick(Account account);
    }

    public AccountAdapter(List<Account> accountList, View.OnClickListener listener) {
        this.accountList = accountList;
        this.onClickListener = listener; // Set the listener
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accountList.get(position);
        holder.accountName.setText(account.getAccountName());
        holder.accountBalance.setText("RM " + account.getBalance());
        holder.accountLogo.setImageResource(account.getIcon_id());

        // Set the account as a tag and attach the listener
        holder.itemView.setTag(account);  // Set the account object as the tag
        holder.itemView.setOnClickListener(onClickListener);  // Set the listener
    }

    // Update the list of accounts and refresh the RecyclerView
    @SuppressLint("NotifyDataSetChanged")
    public void setAccounts(List<Account> newAccountList) {
        this.accountList = newAccountList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView accountName, accountBalance;
        ImageView accountLogo;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            accountName = itemView.findViewById(R.id.account_name);
            accountBalance = itemView.findViewById(R.id.account_balance);
            accountLogo = itemView.findViewById(R.id.account_logo);
        }
    }
}

