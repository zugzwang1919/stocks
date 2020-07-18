package com.wolfesoftware.stocks.model.calculator;

import com.wolfesoftware.stocks.model.Stock;

import java.time.LocalDate;

public class ClosingPosition extends Position {

    private boolean positionActiveAtEndDate;

    // Constructor
    public ClosingPosition(Stock stock, LocalDate date) {
        super(stock, date);
    }

    // Getters and Setters
    public boolean isPositionActiveAtEndDate() {
        return positionActiveAtEndDate;
    }
    public void setPositionActiveAtEndDate(boolean positionActiveAtEndDate) {
        this.positionActiveAtEndDate = positionActiveAtEndDate;
    }

}
