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

    public DividendPayment(StockDividend dividend, BigDecimal numberOfShares, List<StockSplit> stockSplits) {
        this.dividend = dividend;
        this.numberOfShares = numberOfShares;
        BigDecimal stockSplitMultiplier = stockSplitMultiplier(dividend.getExDividendDate(), stockSplits);
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


    /**
     * Sadly (for us), Yahoo (the source of our dividend info), reports an adjusted dividend rather
     * than the actual dividend that was paid. This method calculates the adjustment factor that
     * should be used.
     *
     */
    private BigDecimal stockSplitMultiplier(LocalDate exDividendDate, List<StockSplit> stockSplits) {

        BigDecimal returnValue = BigDecimal.ONE;

        for (StockSplit ss: stockSplits) {
            if (exDividendDate.isBefore(ss.getDate())) {
                returnValue = returnValue.multiply(ss.getAfterAmount()).divide(ss.getBeforeAmount(),3,BigDecimal.ROUND_HALF_EVEN);
            }
        }

        return returnValue;
    }
}
