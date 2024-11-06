package com.example.financialcompanion;

import java.util.Date;
import java.util.List;

public class Goal {
    private String id;
    private String label;
    private double amount;
    private Date createDate;
    private Date dueDate;
    private List<Account> accounts;  // List of accounts associated with the goal
    private String description;
    private String status;

    // Constructor
    public Goal(String id, String label, double amount, Date createDate, Date dueDate, List<Account> accounts, String description, String status) {
        this.id = id;
        this.label = label;
        this.amount = amount;
        this.createDate = createDate;
        this.dueDate = dueDate;
        this.accounts = accounts;
        this.description = description;
        this.status = status;
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

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
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
}

