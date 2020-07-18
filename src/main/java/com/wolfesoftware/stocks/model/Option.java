package com.wolfesoftware.stocks.model;

import com.wolfesoftware.stocks.common.BigDecimalUtil;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="optiontable") // "option" is an SQL Reserved Keyword
public class Option extends UserBasedPersistentEntity  {


    @Enumerated(EnumType.STRING)
    @Column(name = "optiontype")
    private OptionType  optionType;

    @ManyToOne
    private Stock       stock;

    @Column(name = "strikeprice")
    private BigDecimal  strikePrice;

    @Column(name = "expirationdate")
    private LocalDate   expirationDate;


    // Getters and Setters
    public OptionType getOptionType() {
        return optionType;
    }
    public void setOptionType(OptionType optionType) {
        this.optionType = optionType;
    }
    public Stock getStock() {
        return stock;
    }
    public void setStock(Stock stock) {
        this.stock = stock;
    }
    public BigDecimal getStrikePrice() {
        return strikePrice;
    }
    // FIXME:  Long Term, this probably needs to be kept in sync with Stock Transaction
    public void setStrikePrice(BigDecimal strikePrice) {
        this.strikePrice =  BigDecimalUtil.createUSDBigDecimal(strikePrice);
    }
    public LocalDate getExpirationDate() {
        return expirationDate;
    }
    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }


    public enum OptionType {
        CALL,
        PUT
    }


}
