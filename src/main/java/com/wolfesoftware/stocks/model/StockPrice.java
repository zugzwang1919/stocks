package com.wolfesoftware.stocks.model;


import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;


/**
 * Entity implementation class for Entity: Stock Price
 *
 */
@Entity
@Table(name="stockprice")
public class StockPrice extends PersistentEntity  {

    @ManyToOne
    @NotNull
    private Stock           stock;

    private LocalDate       date;

    private BigDecimal      price;

    private static final long serialVersionUID = 1L;

    public static final LocalDate EARLIEST_DAILY_PRICE = LocalDate.of(2008, Month.JANUARY, 1);  // Jan 1, 2015

    // Constructors

    public StockPrice() {
    }

    public StockPrice(Stock stock, LocalDate date, BigDecimal price){
        this.stock = stock;
        this.date = date;
        this.price = price;
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
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    

}
