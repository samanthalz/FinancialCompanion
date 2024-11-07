package com.example.financialcompanion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    String uid, name, email, username;
    private boolean isFirstTimeUser; // Flag for first-time user
    private Map<String, Account> accounts; // List of accounts
    private PetCoin petCoin;

    public User() {
        accounts = new HashMap<>(); // Initialize the list of accounts
        this.isFirstTimeUser = true; // Default to true for new users
    }

    public User(String name, String email, String username) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.accounts = new HashMap<>(); // Initialize the list of accounts
        this.isFirstTimeUser = true; // Default to true for new users
        this.petCoin = new PetCoin();;
    }

    public String getUid() {return uid;}

    public void setUid(String name) {
        this.uid = uid;
    }

    public String getName() {return name;}

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isFirstTimeUser() {
        return isFirstTimeUser;
    }

    public void setFirstTimeUser(boolean firstTimeUser) {
        isFirstTimeUser = firstTimeUser;
    }

    public Map<String, Account> getAccounts() {
        return accounts; // Return the accounts map
    }

    public void addAccount(Account account) {
        accounts.put(account.getId(), account); // Add the account with its ID as the key
    }

    public PetCoin getPetCoin() {
        return petCoin;
    }

    public void setPetCoin(PetCoin petCoin) {
        this.petCoin = petCoin;
    }
}
