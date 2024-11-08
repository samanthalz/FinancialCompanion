package com.example.financialcompanion;

import java.util.UUID;

public class Item {
    private String id;
    private String name;
    private int coinsNeeded;
    private int imageResourceId;

    // Constructor
    public Item(String id, String name, int coinsNeeded, int imageResourceId) {
        this.id = id;
        this.name = name;
        this.coinsNeeded = coinsNeeded;
        this.imageResourceId = imageResourceId;
    }

    public Item() {
        // Default constructor required for Firebase
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter and Setter methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCoinsNeeded() {
        return coinsNeeded;
    }

    public void setCoinsNeeded(int coinsNeeded) {
        this.coinsNeeded = coinsNeeded;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }
}
