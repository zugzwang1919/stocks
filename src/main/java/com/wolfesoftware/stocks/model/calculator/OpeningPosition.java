package com.wolfesoftware.stocks.model.calculator;

import com.wolfesoftware.stocks.model.Stock;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OpeningPosition extends Position {

    private boolean containsOlderTransactions = false;

    // Constructor
    public OpeningPosition(Stock stock, LocalDate date) {
        super(stock, date);
    }

    // Copy Constructor
    public OpeningPosition(OpeningPosition thatOpeningPosition, Stock newStock) {
        super(thatOpeningPosition, newStock);
        this.containsOlderTransactions = thatOpeningPosition.containsOlderTransactions;
    }

    // Getters and Setters
    public boolean containsOlderTransactions() {
        return containsOlderTransactions;
    }
    public void setContainsOlderTransactions(boolean containsOlderTransactions) {
        this.containsOlderTransactions = containsOlderTransactions;
    }



    /*
        For those interested in cash flow associated with a position.  A positive value implies that cash was spent on the position,
        thus negative proceeds.
        */
    public BigDecimal getProceeds() {
        return getValue().negate();
    }


}
