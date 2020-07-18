package com.wolfesoftware.stocks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wolfesoftware.stocks.common.BigDecimalUtil;
import com.wolfesoftware.stocks.common.LocalDateUtil;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="optiontransaction")
public class OptionTransaction extends UserBasedPersistentEntity  {

    @ManyToOne
    private Portfolio portfolio;

    private LocalDate date;

    @ManyToOne
    private Option option;

    @Enumerated(EnumType.STRING)
    private Activity activity;

    @Column(name = "numberofcontracts")
    private Long numberOfContracts;

    private BigDecimal amount;

    private static final long serialVersionUID = 1L;


    // Getters and Setters
    public Portfolio getPortfolio() {
        return portfolio;
    }
    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public void setDateFromString(String dateString) {
        this.date = LocalDateUtil.createLocalDateAllowingAVarietyOfFormats(dateString);
    }
    public Option getOption() {
        return option;
    }
    public void setOption(Option option) {
        this.option = option;
    }
    public Activity getActivity() {
        return activity;
    }
    public void setActivity(Activity activity) {
        this.activity = activity;
    }
    public Long getNumberOfContracts() {
        return numberOfContracts;
    }
    public void setNumberOfContracts(Long numberOfContracts) {
        this.numberOfContracts = numberOfContracts;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = BigDecimalUtil.createUSDBigDecimal(amount);
    }


    // Other Methods

    @JsonIgnore
    public BigDecimal getProceeds() {
        return (activity.equals(Activity.BUY_TO_OPEN) || activity.equals(Activity.BUY_TO_CLOSE)) ?
                amount.negate() : amount;
    }

    @JsonIgnore
    public BigDecimal getExercisableAmount() {
        BigDecimal excercisableAmount = option.getStrikePrice().multiply(BigDecimal.valueOf(numberOfContracts)).multiply(BigDecimal.valueOf(100)).setScale(2);
        return excercisableAmount;
    }

    @JsonIgnore
    public BigDecimal getPricePerContract() {
        BigDecimal numberOfContractsBD = new BigDecimal(numberOfContracts);
        BigDecimal result = amount.divide(numberOfContractsBD, BigDecimal.ROUND_HALF_EVEN).divide(BigDecimalUtil.ONE_HUNDRED, BigDecimal.ROUND_HALF_EVEN);
        BigDecimal resultInUSDollars = BigDecimalUtil.createUSDBigDecimal(result);
        return resultInUSDollars;
    }


    public enum Activity {

        BUY_TO_OPEN("Buy to Open", "Buy"),
        BUY_TO_CLOSE("Buy to Close", "Buy"),
        SELL_TO_OPEN("Sell to Open", "Sell"),
        SELL_TO_CLOSE("Sell to Close", "Sell");

        private final String description;
        private final String shortDescription;

        Activity(String description, String shortDescription) {
            this.description = description;
            this.shortDescription = shortDescription;
        }

        public String getDescription() {
            return description;
        }

        public String getShortDescription() {
            return shortDescription;
        }
    }
}




