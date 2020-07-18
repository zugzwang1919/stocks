package com.wolfesoftware.stocks.model;

import java.util.ArrayList;
import java.util.List;

public class TodaysStockPriceLoadResponse {
    private List<Stock> abandonedStocks = new ArrayList<>();
    private List<Stock> previouslyLoadedStocks = new ArrayList<>();
    private List<Stock> successfullyLoadedStocks = new ArrayList<>();
    private List<Stock> nonLoadedStocks = new ArrayList<>();

    // Getters and Setters

    public List<Stock> getAbandonedStocks() {
        return abandonedStocks;
    }
    public void setAbandonedStocks(List<Stock> abandonedStocks) {
        this.abandonedStocks = abandonedStocks;
    }
    public List<Stock> getPreviouslyLoadedStocks() {
        return previouslyLoadedStocks;
    }
    public void setPreviouslyLoadedStocks(List<Stock> previouslyLoadedStocks) {
        this.previouslyLoadedStocks = previouslyLoadedStocks;
    }
    public List<Stock> getNonLoadedStocks() {
        return nonLoadedStocks;
    }
    public void setSuccessfullyLoadedStocks(List<Stock> successfullyLoadedStocks) {
        this.successfullyLoadedStocks = successfullyLoadedStocks;
    }
    public List<Stock> getSuccessfullyLoadedStocks() {
        return successfullyLoadedStocks;
    }
    public void setNonLoadedStocks(List<Stock> nonLoadedStocks) {
        this.nonLoadedStocks = nonLoadedStocks;
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder("Result from loading today's prices. Loaded earlier = ");
        sb.append(previouslyLoadedStocks.size());
        sb.append(".  Loaded now = ");
        sb.append(successfullyLoadedStocks.size());
        sb.append(".  Failed now = ");
        sb.append(nonLoadedStocks.size());
        sb.append(". Stocks abandoned = ");
        boolean firstTimeThrough = true;
        for (Stock s: abandonedStocks) {
            if (!firstTimeThrough) {
                sb.append(", ");
            }
            sb.append(s.getTicker());
            firstTimeThrough = false;
        }
        sb.append(".");
        return sb.toString();
    }

}
