package com.wolfesoftware.stocks.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

@Entity
@Table(name="stockdividend")
public class StockDividend extends PersistentEntity  {

    @ManyToOne
    private Stock       stock;

    @Column(name = "exdividenddate")
    private LocalDate   exDividendDate;

    @Column(name = "dividendamount")
    private BigDecimal  dividendAmount;

    public static final LocalDate EARLIEST_STOCK_DIVIDEND_DATE = LocalDate.of(2008, Month.JANUARY, 1); // Jan 1, 2000


    // Constructors
    public StockDividend(){}

    public StockDividend(Stock stock, LocalDate exDividendDate, BigDecimal dividendAmount) {
        this.stock = stock;
        this.exDividendDate = exDividendDate;
        this.dividendAmount = dividendAmount;
    }


    // Getters and Setters

    public Stock getStock() {
        return stock;
    }
    public void setSecurity(Stock stock) {
        this.stock = stock;
    }
    public LocalDate getExDividendDate() {
        return exDividendDate;
    }
    public void setExDividendDate(LocalDate exDividendDate) {
        this.exDividendDate = exDividendDate;
    }
    public BigDecimal getDividendAmount() {
        return dividendAmount;
    }
    public void setDividendAmount(BigDecimal dividendAmount) {
        this.dividendAmount = dividendAmount;
    }



}