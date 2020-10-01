package com.wolfesoftware.stocks.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import java.util.Comparator;


/**
 * Entity implementation class for Entity: Stock
 *
 */
@Entity
@Table(name="stock")
public class Stock extends UserBasedPersistentEntity {

    @NotEmpty
    private String ticker;

    @NotEmpty
    private String name;

    @Column(columnDefinition = "tinyint", nullable = false)
    private Boolean benchmark;

    // Constructors


    // Getters and Setters
    public String getTicker() {
        return this.ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Boolean isBenchmark() {
        return benchmark;
    }
    public void setBenchmark(Boolean benchmark) {
        this.benchmark = benchmark;
    }



    public enum SortBy {
        TICKER("Ticker"),
        NAME("Name");

        SortBy(String displayName) {
            this.displayName = displayName;
        }

        private String displayName;
        
        public String getDisplayName() {
            return displayName;
        }
        
    }
    
     

    public static class SecurityComparator implements Comparator {

        private SortBy sortBy;
        
        public SecurityComparator( SortBy sortBy ) {
            this.sortBy = sortBy;
        }
                
        @Override
        public int compare(Object o1, Object o2) {
            Stock sec1 = (Stock)o1;
            Stock sec2 = (Stock)o2;
            switch (sortBy) {
                case TICKER:
                    if (sec1.ticker == null && sec2.ticker == null)
                        return 0;
                    if (sec1.ticker == null)
                        return -1;
                    if (sec2.ticker == null)
                        return 1;
                    return sec1.ticker.compareTo(sec2.ticker);
                case NAME:
                    if (sec1.name == null && sec2.name == null)
                        return 0;
                    if (sec1.name == null)
                        return -1;
                    if (sec2.name == null)
                        return 1;
                    return sec1.name.compareTo(sec2.name);

                default:
                    throw new IllegalStateException("Unexpected compare request received.");
            }
        }
    }



}
