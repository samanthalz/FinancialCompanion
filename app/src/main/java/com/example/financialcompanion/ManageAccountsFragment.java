package com.example.financialcompanion;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ManageAccountsFragment extends Fragment {

    private RecyclerView accountRecyclerView;
    private AccountAdapter accountAdapter;
    private List<Account> accountList;
    private SharedViewModel accountViewModel;
    private NavController navController;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_accounts, container, false);

        accountViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);


        // Initialize the RecyclerView
        accountRecyclerView = view.findViewById(R.id.account_recycler_view);

        // Set up an empty list and adapter
        accountList = new ArrayList<>();
        accountAdapter = new AccountAdapter(accountList);
        accountRecyclerView.setAdapter(accountAdapter);

        // Observe accounts in ViewModel to update RecyclerView automatically
        accountViewModel.getAccounts().observe(getViewLifecycleOwner(), accounts -> {
            accountAdapter.setAccounts(accounts);  // Update adapter with new data
            accountAdapter.notifyItemInserted(accountList.size() - 1);
        });

        Button addNewAccountButton = view.findViewById(R.id.add_account_button);
        addNewAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the AccountDialogFragment
                AddAccountDialog dialog = new AddAccountDialog();
                dialog.show(getChildFragmentManager(), "addAccountDialog");
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchLatestAccounts();
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
            Log.d("ManageAccountsFragment", "Back pressed from origin: " + originFragment);

            // Check the origin fragment and navigate accordingly
            if ("home".equals(originFragment)) {
                // Check if the action exists in the navigation graph
               navController.navigate(R.id.action_manageAccountsFragment_to_homeFragment);

            } else if ("account".equals(originFragment)) {
                navController.navigate(R.id.action_manageAccountsFragment_to_accountFragment);
                    Log.d("ManageAccountsFragment", "Navigating back to AccountFragment.");
            } else {
                // If the origin is not specified, just go back normally
                navController.popBackStack();
                Log.d("ManageAccountsFragment", "Navigating back to previous fragment.");
            }
        });
    }


    private void fetchLatestAccounts() {
        List<Account> accountList = new ArrayList<>();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("accounts");

        // Listener to get all accounts
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot accountsSnapshot) {
                for (DataSnapshot accountSnapshot : accountsSnapshot.getChildren()) {
                    // Retrieve each account
                    String accId = accountSnapshot.getKey();
                    String accountName = accountSnapshot.child("accountName").getValue(String.class);
                    Double balance = accountSnapshot.child("balance").getValue(Double.class);
                    Integer iconId = accountSnapshot.child("icon_id").getValue(Integer.class);

                    // Set default values if any data is missing
                    accountName = accountName != null ? accountName : "Unnamed Account";
                    balance = balance != null ? balance : 0.0;
                    iconId = iconId != null ? iconId : R.drawable.baseline_error_24; // Default icon if iconId is null

                    // Create Account object
                    Account account = new Account(accId, accountName, balance, iconId);
                    accountList.add(account);
                    accountViewModel.addAccount(account);
                    Log.d("AccountInfo", "Account: " + accountName + " | Balance: " + balance + " | Icon ID: " + iconId);
                }

                // Sort accounts by name (or balance, or any other field as needed)
                accountList.sort(new Comparator<Account>() {
                    @Override
                    public int compare(Account a1, Account a2) {
                        return a1.getAccountName().compareTo(a2.getAccountName());
                    }
                });

                // Get the latest 5 accounts (or limit as needed)
                List<Account> latestAccounts = accountList.size() > 5
                        ? accountList.subList(0, 5)
                        : accountList;

                // Update the adapter with the latest accounts
                accountAdapter.setAccounts(latestAccounts);
                accountAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ManageAccountFragment", "Database error: " + databaseError.getMessage());
            }
        });
    }
}
