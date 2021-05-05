package com.wolfesoftware.stocks.model;

import java.util.List;

public class Profile {

    private String                          userName;
    private String                          emailAddress;
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
    public String getEmailAddress() {
        return emailAddress;
    }
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
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

    public enum AuthenticationSupported {
        ID_PW,
        GOOGLE
    }

}
