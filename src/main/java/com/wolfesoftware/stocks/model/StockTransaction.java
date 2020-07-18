package com.wolfesoftware.stocks.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wolfesoftware.stocks.common.BigDecimalUtil;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;


@Entity
@Table(name="stocktransaction")
public class StockTransaction extends UserBasedPersistentEntity {

    @ManyToOne
    private Portfolio   portfolio;

    private LocalDate   date;

    @ManyToOne
    private Stock       stock;

    @Enumerated(EnumType.STRING)
    private Activity    activity;

    @Column(name = "tradesize")
    private BigDecimal tradeSize;

    private BigDecimal  amount;

    private static final long serialVersionUID = 1L;	
	
    // Standard Constructor
    public StockTransaction() {
    }

    
    /*
    // Copy Constructor
    public StockTransaction(EntityManager em, StockTransaction thatTransaction, Stock security) {
        this.date = thatTransaction.getDate();
        this.user = thatTransaction.getUser();
        this.portfolio = thatTransaction.getPortfolio();
        this.security = security;
        this.activity = thatTransaction.getActivity();
        this.tradeSize = thatTransaction.getTradeSize();
        SecurityPriceRetriever spr = new SecurityPriceRetriever();
        BigDecimal price = spr.getClosingPrice(em, this.security, this.getDate());
        this.amount = this.tradeSize.multiply(price).setScale(2);
    }
    */

	// Getters and Setters
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
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
    public Activity getActivity() {
            return activity;
    }
    public void setActivity(Activity activity) {
            this.activity = activity;
    }
    public Stock getStock() {
        return stock;
    }
    public void setStock(Stock stock) {
        this.stock = stock;
    }
    public BigDecimal getTradeSize() {
        return tradeSize;
    }
    public void setTradeSize(BigDecimal tradeSize) {
        this.tradeSize = tradeSize;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    // FIXME:  Should this be changed to use createUSDBigDecimal()
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    // Methods providing some functionality
    @JsonIgnore
    public BigDecimal getPricePerShare() {
        BigDecimal result = amount.divide(tradeSize, RoundingMode.HALF_EVEN);
        BigDecimal resultInUSDollars = BigDecimalUtil.createUSDBigDecimal(result);
        return resultInUSDollars;
    }
    @JsonIgnore
    public BigDecimal getProceeds() {
        return activity == Activity.SELL ? amount : amount.negate();
    }
    

    public enum Activity {
        
        BUY("Buy", "Buy"),
        SELL("Sell", "Sell");
        
        private String description;
        private String shortDescription;

        Activity(String name, String shortName) {
            this.description = name;
            this.shortDescription = shortName;
        }

        public String getDescription() {
            return description;
        }

        public String getShortDescription() {
            return shortDescription;
        }
    }
 
    public enum SortBy {
        DATE("Date"), 
        SECURITY("Stock"),
        ACTIVITY("Activity"),
        TRADE_SIZE("Trade Size"),
        AMOUNT("Amount");
        
        SortBy(String displayName) {
            this.displayName = displayName;
        }
        
        
        private String displayName;
        
        public String getDisplayName() {
            return displayName;
        }
        
    }

    // Constructors


    public static StockTransaction createExample(Long id,  User user) {
        StockTransaction st= new StockTransaction();
        st.id = id;
        st.user = user;
        return st;
    }
 
    public static class StockTransactionComparator implements Comparator {

        private StockTransaction.SortBy sortBy;
        
        public StockTransactionComparator(StockTransaction.SortBy sortBy ) {
            this.sortBy = sortBy;
        }
                
        @Override
        public int compare(Object o1, Object o2) {
           
            StockTransaction tr1 = (StockTransaction)o1;
            StockTransaction tr2 = (StockTransaction)o2;
            switch (sortBy) {
                case DATE:
                    return tr1.date.compareTo(tr2.date);
                case SECURITY:
                    if (tr1.stock.getTicker() == null && tr2.stock.getTicker() == null)
                        return 0;
                    if (tr1.stock.getTicker() == null)
                        return -1;
                    if (tr2.stock.getTicker() == null)
                        return 1;
                    return tr1.stock.getTicker().compareTo(tr2.stock.getTicker());
                case ACTIVITY:
                    return tr1.activity.compareTo(tr2.activity);
                case TRADE_SIZE:
                    return tr1.tradeSize.compareTo(tr2.tradeSize);
                case AMOUNT:
                    return tr1.amount.compareTo(tr2.amount);
                default:
                    throw new IllegalStateException("Unexpected compare request received.");
            }
        }
    }
}
