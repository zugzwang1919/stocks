package com.wolfesoftware.stocks.model.calculator;

import com.wolfesoftware.stocks.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LifeCycle {

    private static final Logger logger = LoggerFactory.getLogger(LifeCycle.class);

    private Stock                           stock;
    private LocalDate                       requestedStartDate;
    private LocalDate                       requestedEndDate;
    private OpeningPosition                 openingPosition;
    private ClosingPosition                 closingPosition;
    private List<StockTransaction>          interveningStockTransactions = new ArrayList<>();
    private List<OptionTransaction>         optionTransactions = new ArrayList<>();
    private List<DividendPayment>           dividendPayments = new ArrayList<>();

    // Basic calculated values
    private BigDecimal                      profitsFromSecurities = BigDecimal.ZERO;
    private BigDecimal                      dividendsAccrued = BigDecimal.ZERO;
    private BigDecimal                      optionProceedsAccrued = BigDecimal.ZERO;
    private BigDecimal                      totalGains = BigDecimal.ZERO;

    private BigDecimal                      inflows = BigDecimal.ZERO;
    private BigDecimal                      outflows = BigDecimal.ZERO;
    private BigDecimal                      simpleReturn = BigDecimal.ZERO;

    // Calculated values related to income analysis
    private BigDecimal                      optionExposureToPutsAtRequestedEndDate = BigDecimal.ZERO;
    private BigDecimal                      optionExposureToCallsAtRequestedEndDate = BigDecimal.ZERO;
    private BigDecimal                      totalDollarDays = BigDecimal.ZERO;
    private Long                            lengthOfTimePeriodOfInterest;
    private BigDecimal                      dailyAverageCapitalAtRisk = BigDecimal.ZERO;
    private BigDecimal                      annualizedIncomeReturnOnInvestment = BigDecimal.ZERO;
    private BigDecimal                      totalLongExposure = BigDecimal.ZERO;
    private boolean                         includedInSnapshot = false;


    public Stock getStock() {
        return stock;
    }
    public void setStock(Stock stock) {
        this.stock = stock;
    }
    public LocalDate getRequestedStartDate() {
        return requestedStartDate;
    }
    public void setRequestedStartDate(LocalDate requestedStartDate) {
        this.requestedStartDate = requestedStartDate;
    }
    public LocalDate getRequestedEndDate() {
        return requestedEndDate;
    }
    public void setRequestedEndDate(LocalDate requestedEndDate) {
        this.requestedEndDate = requestedEndDate;
    }
    public OpeningPosition getOpeningPosition() {
        return openingPosition;
    }
    public void setOpeningPosition(OpeningPosition openingPosition) {
        this.openingPosition = openingPosition;
    }
    public ClosingPosition getClosingPosition() {
        return closingPosition;
    }
    public void setClosingPosition(ClosingPosition closingPosition) {
        this.closingPosition = closingPosition;
    }
    public List<StockTransaction> getInterveningStockTransactions() {
        return interveningStockTransactions;
    }
    public void setInterveningStockTransactions(List<StockTransaction> interveningStockTransactions) {
        this.interveningStockTransactions = interveningStockTransactions;
    }
    public List<OptionTransaction> getOptionTransactions() {
        return optionTransactions;
    }
    public void setOptionTransactions(List<OptionTransaction> optionTransactions) {
        this.optionTransactions = optionTransactions;
    }
    public List<DividendPayment> getDividendPayments() {
        return dividendPayments;
    }
    public void setDividendPayments(List<DividendPayment> dividendPayments) {
        this.dividendPayments = dividendPayments;
    }
    public BigDecimal getProfitsFromSecurities() {
        return profitsFromSecurities;
    }
    public void setProfitsFromSecurities(BigDecimal profitsFromSecurities) {
        this.profitsFromSecurities = profitsFromSecurities;
    }
    public BigDecimal getDividendsAccrued() {
        return dividendsAccrued;
    }
    public void setDividendsAccrued(BigDecimal dividendsAccrued) {
        this.dividendsAccrued = dividendsAccrued;
    }
    public BigDecimal getOptionProceedsAccrued() {
        return optionProceedsAccrued;
    }
    public void setOptionProceedsAccrued(BigDecimal optionProceedsAccrued) {
        this.optionProceedsAccrued = optionProceedsAccrued;
    }
    public BigDecimal getTotalGains() {
        return totalGains;
    }
    public void setTotalGains(BigDecimal totalGains) {
        this.totalGains = totalGains;
    }
    public BigDecimal getInflows() {
        return inflows;
    }
    public void setInflows(BigDecimal inflows) {
        this.inflows = inflows;
    }
    public BigDecimal getOutflows() {
        return outflows;
    }
    public void setOutflows(BigDecimal outflows) {
        this.outflows = outflows;
    }
    public BigDecimal getSimpleReturn() {
        return simpleReturn;
    }
    public void setSimpleReturn(BigDecimal simpleReturn) {
        this.simpleReturn = simpleReturn;
    }
    public BigDecimal getOptionExposureToPutsAtRequestedEndDate() {
        return optionExposureToPutsAtRequestedEndDate;
    }
    public void setOptionExposureToPutsAtRequestedEndDate(BigDecimal optionExposureToPutsAtRequestedEndDate) {
        this.optionExposureToPutsAtRequestedEndDate = optionExposureToPutsAtRequestedEndDate;
    }
    public BigDecimal getOptionExposureToCallsAtRequestedEndDate() {
        return optionExposureToCallsAtRequestedEndDate;
    }
    public void setOptionExposureToCallsAtRequestedEndDate(BigDecimal optionExposureToCallsAtRequestedEndDate) {
        this.optionExposureToCallsAtRequestedEndDate = optionExposureToCallsAtRequestedEndDate;
    }
    public BigDecimal getTotalDollarDays() {
        return totalDollarDays;
    }
    public void setTotalDollarDays(BigDecimal totalDollarDays) {
        this.totalDollarDays = totalDollarDays;
    }
    public Long getLengthOfTimePeriodOfInterest() {
        return lengthOfTimePeriodOfInterest;
    }
    public void setLengthOfTimePeriodOfInterest(Long lengthOfTimePeriodOfInterest) {
        this.lengthOfTimePeriodOfInterest = lengthOfTimePeriodOfInterest;
    }
    public BigDecimal getDailyAverageCapitalAtRisk() {
        return dailyAverageCapitalAtRisk;
    }
    public void setDailyAverageCapitalAtRisk(BigDecimal dailyAverageCapitalAtRisk) {
        this.dailyAverageCapitalAtRisk = dailyAverageCapitalAtRisk;
    }
    public BigDecimal getAnnualizedIncomeReturnOnInvestment() {
        return annualizedIncomeReturnOnInvestment;
    }
    public void setAnnualizedIncomeReturnOnInvestment(BigDecimal annualizedIncomeReturnOnInvestment) {
        this.annualizedIncomeReturnOnInvestment = annualizedIncomeReturnOnInvestment;
    }
    public BigDecimal getTotalLongExposure() {
        return totalLongExposure;
    }
    public void setTotalLongExposure(BigDecimal totalLongExposure) {
        this.totalLongExposure = totalLongExposure;
    }
    public boolean isIncludedInSnapshot() {
        return includedInSnapshot;
    }
    public void setIncludedInSnapshot(boolean includedInSnapshot) {
        this.includedInSnapshot = includedInSnapshot;
    }

}
