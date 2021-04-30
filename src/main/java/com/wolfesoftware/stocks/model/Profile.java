package com.wolfesoftware.stocks.model;

import java.util.List;

public class Profile {

    private String                          userName;
    private List<AuthenticationSupported>   authenticationsSupported;
    private Long                            numberOfStocks;
    private Long                            numberOfOptions;
    private Long                            numberOfStockTransactions;
    private Long                            numberOfOptionTransactions;


    // Getters and Setters


    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public List<AuthenticationSupported> getAuthenticationsSupported() {
        return authenticationsSupported;
    }
    public void setAuthenticationsSupported(List<AuthenticationSupported> authenticationsSupported) {
        this.authenticationsSupported = authenticationsSupported;
    }
    public Long getNumberOfStocks() {
        return numberOfStocks;
    }
    public void setNumberOfStocks(Long numberOfStocks) {
        this.numberOfStocks = numberOfStocks;
    }
    public Long getNumberOfOptions() {
        return numberOfOptions;
    }
    public void setNumberOfOptions(Long numberOfOptions) {
        this.numberOfOptions = numberOfOptions;
    }
    public Long getNumberOfStockTransactions() {
        return numberOfStockTransactions;
    }
    public void setNumberOfStockTransactions(Long numberOfStockTransactions) {
        this.numberOfStockTransactions = numberOfStockTransactions;
    }
    public Long getNumberOfOptionTransactions() {
        return numberOfOptionTransactions;
    }
    public void setNumberOfOptionTransactions(Long numberOfOptionTransactions) {
        this.numberOfOptionTransactions = numberOfOptionTransactions;
    }

    /*
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User Name = ");
        sb.append(userName);
        authenticationsSupported.forEach( (authenticationSupported) -> {
           sb.append(", Authentication Supported = ");
           sb.append(authenticationSupported);
        });
        sb.append(".  Number of Stocks = ");
        sb.append(numberOfStocks);
        sb.append(".  Number of Options = ");
        sb.append(numberOfOptions);
        sb.append(".  Number of Stock Transactions = ");
        sb.append(numberOfStockTransactions);
        sb.append(".  Number of Option Transactions = ");
        sb.append(numberOfOptionTransactions);

        return sb.toString();
    }
    */

    public enum AuthenticationSupported {
        ID_PW,
        GOOGLE
    }

}
