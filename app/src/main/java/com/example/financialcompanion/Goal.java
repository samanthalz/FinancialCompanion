package com.example.financialcompanion;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Goal {
    private String id;
    private String label;
    private double amount;
    private Long createDate;
    private Long dueDate;
    private List<Account> accounts;  // List of accounts associated with the goal
    private String description;
    private String status;
    private Boolean rewardGiven;

    // Constructor
    public Goal(String id, String label, double amount, Long createDate, Long dueDate, List<Account> accounts, String description, String status) {
        this.id = id;
        this.label = label;
        this.amount = amount;
        this.createDate = createDate;
        this.dueDate = dueDate;
        this.accounts = accounts;
        this.description = description;
        this.status = status;
        this.rewardGiven = false;
    }

    public Goal() {
        // Default constructor
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Long createDate) {
        this.createDate = createDate;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
    }

    public List<Account> getGoalsAccounts() {
        return accounts;
    }

    public void setGoalsAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @NonNull
    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()); // Adjust format as needed
        String currentDateStr = dateFormat.format(new Date()); // Format the current date
        String dueDateStr = (dueDate != null) ? dateFormat.format(dueDate) : "N/A"; // Format due date if it exists

        return "Goal{id='" + id + "', label='" + label + "', amount=" + amount
                + ", status='" + status + "', currentDate='" + currentDateStr
                + "', dueDate='" + dueDateStr + "'}";
    }

    public Boolean getRewardGiven() {
        return rewardGiven;
    }

    public void setRewardGiven(Boolean rewardGiven) {
        this.rewardGiven = rewardGiven;
    }
}

