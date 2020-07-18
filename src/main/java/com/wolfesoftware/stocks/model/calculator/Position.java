package com.wolfesoftware.stocks.model.calculator;

import com.wolfesoftware.stocks.common.BridgeToSpringBean;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockSplit;
import com.wolfesoftware.stocks.model.StockTransaction;
import com.wolfesoftware.stocks.repository.StockSplitRepository;
import com.wolfesoftware.stocks.service.StockPriceService;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Position {

    private Stock       stock;
    private LocalDate   date;
    private BigDecimal  size;
    private BigDecimal  value;


    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Position.class);


    // Constructors

    public Position(Position thatPosition ) {
        this.stock = thatPosition.getStock();
        this.date = thatPosition.getDate();
        this.size = thatPosition.getSize();
        this.value = thatPosition.getValue();
    }

    public Position(Stock stock, LocalDate date) {
        this.stock = stock;
        this.date = date;
        size = BigDecimal.ZERO;
        value = BigDecimal.ZERO;
    }

    public Position(Position thatPosition, Stock newStock) {
        this(newStock, thatPosition.getDate());
        this.value = thatPosition.getValue();
        StockPriceService stockPriceService = BridgeToSpringBean.getBean(StockPriceService.class);
        BigDecimal priceOnDate = stockPriceService.retrieveClosingPrice(newStock, thatPosition.date).getPrice();
        this.size = this.value.divide(priceOnDate,3, RoundingMode.HALF_EVEN);
        logger.debug("PositionService size for benchmark = " + this.size);
    }



    // Getters and Setters

    public Stock getStock() {
        return stock;
    }
    public LocalDate getDate() {
        return date;
    }
    public BigDecimal getSize() {
        return size;
    }
    public BigDecimal getValue() {
        return value;
    }
    public void setStock(Stock stock) {
        this.stock = stock;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public void setSize(BigDecimal size) {
        this.size = size;
    }
    public void setValue(BigDecimal value) {
        this.value = value;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Stock = ");
        sb.append(stock ==null ? "null" : stock.getTicker());
        sb.append(".  Date = ");
        sb.append(date.format(DateTimeFormatter.ofPattern("M/d/yyyy")));
        sb.append(".  Size = ");
        sb.append(size.toPlainString());
        sb.append(".  Value = ");
        sb.append(value.toPlainString());

        return sb.toString();
    }
}
