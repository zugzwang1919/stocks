package com.wolfesoftware.stocks.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;




/**
 * Entity implementation class for Entity: Stock
 *
 */
@Entity
@Table(name="portfolio")
public class Portfolio extends UserBasedPersistentEntity  {

    @NotEmpty
    @Column(name = "portfolioname")
    private String portfolioName;

    private static final long serialVersionUID = 1L;

    // Constructor

    public static Portfolio createExample(Long id, String portfolioName, User user) {
        Portfolio p = new Portfolio();
        p.id = id;
        p.portfolioName = portfolioName;
        p.user = user;
        return p;
    }


    // Getters and Setters
    public String getPortfolioName() {
        return portfolioName;
    }
    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

} 