package com.wolfesoftware.stocks.model;

public class LoadOrUpdateResponse {
    private int itemsLoaded = 0;
    private int itemsUpdated = 0;
    private int itemsUnmodified = 0;

    // Getters and Setters

    public int getItemsLoaded() {
        return itemsLoaded;
    }
    public void setItemsLoaded(int itemsLoaded) {
        this.itemsLoaded = itemsLoaded;
    }
    public int getItemsUpdated() {
        return itemsUpdated;
    }
    public void setItemsUpdated(int itemsUpdated) {
        this.itemsUpdated = itemsUpdated;
    }
    public int getItemsUnmodified() {
        return itemsUnmodified;
    }
    public void setItemsUnmodified(int itemsUnmodified) {
        this.itemsUnmodified = itemsUnmodified;
    }


    // Other Methods

    public void addToItemsLoaded(int addend) {
        itemsLoaded += addend;
    }

    public void addToItemsUpdated(int addend) {
        itemsUpdated += addend;
    }

    public void addToItemsUnmodified(int addend) {
        itemsUnmodified += addend;
    }



    public void accumulate(LoadOrUpdateResponse otherResults) {
        addToItemsLoaded(otherResults.itemsLoaded);
        addToItemsUpdated(otherResults.itemsUpdated);
        addToItemsUnmodified(otherResults.itemsUnmodified);
    }

    public String getSummary() {
        return  "There were " + itemsLoaded + " items loaded.  " + itemsUpdated +
                " items updated.  " + itemsUnmodified  + " items unmodified.";
    }
}
