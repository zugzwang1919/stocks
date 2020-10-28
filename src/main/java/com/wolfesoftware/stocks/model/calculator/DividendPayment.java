package com.wolfesoftware.stocks.model.calculator;

import com.wolfesoftware.stocks.model.StockDividend;
import com.wolfesoftware.stocks.model.StockSplit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class DividendPayment {
    private  StockDividend  dividend;
    private  BigDecimal     numberOfShares;
    private  BigDecimal     totalAmount;

    public DividendPayment(StockDividend dividend, BigDecimal numberOfShares, BigDecimal stockSplitMultiplier) {
        this.dividend = dividend;
        this.numberOfShares = numberOfShares;
        this.totalAmount = numberOfShares.multiply(dividend.getDividendAmount()).multiply(stockSplitMultiplier).setScale(2, RoundingMode.HALF_EVEN);
    }


    // Getters and Setters

    public StockDividend getDividend() {
        return dividend;
    }
    public void setDividend(StockDividend dividend) {
        this.dividend = dividend;
    }
    public BigDecimal getNumberOfShares() {
        return numberOfShares;
    }
    public void setNumberOfShares(BigDecimal numberOfShares) {
        this.numberOfShares = numberOfShares;
    }
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }


}
