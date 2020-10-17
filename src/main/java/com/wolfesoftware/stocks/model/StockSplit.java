package com.wolfesoftware.stocks.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;


@Entity
@Table(name="stocksplit")
public class StockSplit extends PersistentEntity {

    @ManyToOne
    private Stock       stock;
    private LocalDate   date;
    @Column(name = "afteramount")
    private BigDecimal  afterAmount;
    @Column(name = "beforeamount")
    private BigDecimal  beforeAmount;

    private static final long serialVersionUID = 1L;


    public static final LocalDate EARLIEST_STOCK_SPLIT = LocalDate.of(2011, Month.JANUARY, 1);  // Jan 1, 1980


    // Constructors

    public StockSplit(){}

    public StockSplit(Stock stock, LocalDate date, BigDecimal afterAmount, BigDecimal beforeAmount) {
        this.stock = stock;
        this.date = date;
        this.afterAmount = afterAmount;
        this.beforeAmount = beforeAmount;
    }


    // Getters and Setters

    public Stock getStock() {
        return stock;
    }
    public void setStock(Stock stock) {
        this.stock = stock;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public BigDecimal getAfterAmount() {
        return afterAmount;
    }
    public void setAfterAmount(BigDecimal after) {
        this.afterAmount = after;
    }
    public BigDecimal getBeforeAmount() {
        return beforeAmount;
    }
    public void setBeforeAmount(BigDecimal before) {
        this.beforeAmount = before;
    }

}
