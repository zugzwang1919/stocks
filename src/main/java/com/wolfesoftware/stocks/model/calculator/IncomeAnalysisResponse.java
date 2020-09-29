package com.wolfesoftware.stocks.model.calculator;

import java.math.BigDecimal;
import java.util.List;

public class IncomeAnalysisResponse {
    private List<LifeCycle> lifeCycles;
    private AnalysisTotals analysisTotals = new AnalysisTotals();
    private SnapshotTotals snapshotTotals = new SnapshotTotals();


    // Getters and Setters

    public List<LifeCycle> getLifeCycles() {
        return lifeCycles;
    }
    public void setLifeCycles(List<LifeCycle> lifeCycles) {
        this.lifeCycles = lifeCycles;
    }
    public SnapshotTotals getSnapshotTotals() {
        return snapshotTotals;
    }
    public AnalysisTotals getAnalysisTotals() {
        return analysisTotals;
    }


    // Nested classes

    public static class AnalysisTotals{
        private BigDecimal proceeds = BigDecimal.ZERO;
        private BigDecimal dividendProceeds = BigDecimal.ZERO;
        private BigDecimal optionProceeds = BigDecimal.ZERO;
        private BigDecimal totalGains = BigDecimal.ZERO;
        private BigDecimal annualReturn = BigDecimal.ZERO;

        // Getters, Accumulators, and one Setter

        public BigDecimal getProceeds() {
            return proceeds;
        }
        public void incrementProceeds(BigDecimal additionalProcceds) {
            proceeds = proceeds.add(additionalProcceds);
        }
        public BigDecimal getDividendProceeds() {
            return dividendProceeds;
        }
        public void incrementDividendProceeds(BigDecimal additionalDividendProcceds) {
            dividendProceeds = dividendProceeds.add(additionalDividendProcceds);
        }
        public BigDecimal getOptionProceeds() {
            return optionProceeds;
        }
        public void incrementOptionProceeds(BigDecimal additionalOptionProcceds) {
            optionProceeds = optionProceeds.add(additionalOptionProcceds);
        }
        public BigDecimal getTotalGains() {
            return totalGains;
        }
        public void incrementTotalGains(BigDecimal additionalTotalGains) {
            totalGains = totalGains.add(additionalTotalGains);
        }
        public BigDecimal getAnnualReturn() {
            return annualReturn;
        }
        public void setAnnualReturn(BigDecimal annualReturn) {
            this.annualReturn = annualReturn;
        }
    }




    public static class SnapshotTotals{
        private BigDecimal stockValue = BigDecimal.ZERO;
        private BigDecimal putExposure = BigDecimal.ZERO;
        private BigDecimal totalLongExposure = BigDecimal.ZERO;
        private BigDecimal callableExposure = BigDecimal.ZERO;


        // Getters and Accumulators
        public BigDecimal getStockValue() {
            return stockValue;
        }
        public void incrementStockValue(BigDecimal additionalStockValue) { stockValue = stockValue.add(additionalStockValue);}
        public BigDecimal getPutExposure() {
            return putExposure;
        }
        public void incrementPutExposure(BigDecimal additionalPutExposure) { putExposure = putExposure.add(additionalPutExposure);}
        public BigDecimal getTotalLongExposure() {
            return totalLongExposure;
        }
        public void incrementTotalLongExposure(BigDecimal additionalTotalLongExposure) { totalLongExposure = totalLongExposure.add(additionalTotalLongExposure);}
        public BigDecimal getCallableExposure() {
            return callableExposure;
        }
        public void incrementCallableExposure(BigDecimal additionalCallableExposure) { callableExposure = callableExposure.add(additionalCallableExposure);}
    }

}
