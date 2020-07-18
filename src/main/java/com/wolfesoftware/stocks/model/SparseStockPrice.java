package com.wolfesoftware.stocks.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.persistence.*;

@Entity
@Table(name="sparsestockprice")
public class SparseStockPrice extends PersistentEntity {

    @ManyToOne
    private Stock       stock;

    private LocalDate   requestedDate;

    private LocalDate   date;

    private BigDecimal  price;


    // Getters and Setters
    public Stock getStock() {
        return stock;
    }
    public void setStock(Stock stock) {
        this.stock = stock;
    }
    public LocalDate getRequestedDate() {
        return requestedDate;
    }
    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
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

    @Override
    public String toString() {
        return stock.getTicker() + " " + date.format(DateTimeFormatter.ofPattern("M-d-yyyy")) + " " + price;
    }
}
