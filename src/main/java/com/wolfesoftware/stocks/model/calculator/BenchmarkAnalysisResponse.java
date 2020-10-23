package com.wolfesoftware.stocks.model.calculator;

import com.wolfesoftware.stocks.model.Stock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BenchmarkAnalysisResponse {

        private CalculatorResults       calculatorResults;
        private List<ResultOverTime> resultsOverTime;

        public BenchmarkAnalysisResponse(CalculatorResults calculatorResults, List<ResultOverTime> resultsOverTime) {
            this.calculatorResults = calculatorResults;
            this.resultsOverTime = resultsOverTime;
        }
        public CalculatorResults getCalculatorResults() {
            return calculatorResults;
        }
        public List<ResultOverTime> getResultsOverTime() {
            return resultsOverTime;
        }


    public static class CalculatorResults {
        private List<SingleSecurityResult>  listOfSingleSecurityResults;
        private AccumulatedResults          accumulatedResults;

        public CalculatorResults(List<Stock> benchmarks) {
            accumulatedResults = new AccumulatedResults(benchmarks);
        }

        public AccumulatedResults getAccumulatedResults() {
            return accumulatedResults;
        }

        public List<SingleSecurityResult> getListOfSingleSecurityResults() {
            return listOfSingleSecurityResults;
        }

        public void setListOfSingleSecurityResults(List<SingleSecurityResult> listOfSingleSecurityResults) {
            this.listOfSingleSecurityResults = listOfSingleSecurityResults;
        }
    }

    public static class SingleSecurityResult {
        private LifeCycle           baseLifeCycle;
        private List<LifeCycle>     benchmarkLifeCycles = new ArrayList<LifeCycle>();
        private List<BigDecimal>    outperformances = new ArrayList<BigDecimal>();

        public LifeCycle getBaseLifeCycle() {
            return baseLifeCycle;
        }

        public void setBaseLifeCycle(LifeCycle baseLifeCycle) {
            this.baseLifeCycle = baseLifeCycle;
        }

        public List<LifeCycle> getBenchmarkLifeCycles() {
            return benchmarkLifeCycles;
        }

        public void setBenchmarkLifeCycles(List<LifeCycle> benchmarkLifeCycles) {
            this.benchmarkLifeCycles = benchmarkLifeCycles;
        }

        public List<BigDecimal> getOutperformances() {
            return outperformances;
        }

        public void setOutperformances(List<BigDecimal> outperformances) {
            this.outperformances = outperformances;
        }

    }
    public static class AccumulatedResults {
        private LocalDate            beginDate;
        private LocalDate            endDate;
        private BigDecimal           baseTotalInflows = BigDecimal.ZERO;
        private BigDecimal           baseTotalOutflows = BigDecimal.ZERO;
        private BigDecimal           baseTotalReturn;
        private List<BenchmarkData> listOfBenchmarkData = new ArrayList<>();

        AccumulatedResults(List<Stock> benchmarks) {
            for(Stock benchmark: benchmarks) {
                listOfBenchmarkData.add(new BenchmarkData(benchmark));
            }
        }
        public LocalDate getBeginDate() {
            return beginDate;
        }
        public  LocalDate getEndDate() {
            return endDate;
        }
        public BigDecimal getBaseTotalInflows() {
            return baseTotalInflows;
        }
        public BigDecimal getBaseTotalOutflows() {
            return baseTotalOutflows;
        }
        public BigDecimal getBaseTotalReturn() {
            return baseTotalReturn;
        }
        public void setBeginDate(LocalDate beginDate) {
            this.beginDate = beginDate;
        }
        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public List<BenchmarkData> getListOfBenchmarkData() {
            return listOfBenchmarkData;
        }

        public void accumulateResults(SingleSecurityResult ssr) {
            baseTotalInflows = baseTotalInflows.add(ssr.getBaseLifeCycle().getInflows());
            baseTotalOutflows = baseTotalOutflows.add(ssr.getBaseLifeCycle().getOutflows());
            for(int i=0; i< ssr.getBenchmarkLifeCycles().size(); i++) {
                listOfBenchmarkData.get(i).accumulateInflows(ssr.benchmarkLifeCycles.get(i).getInflows());
                listOfBenchmarkData.get(i).accumulateOutflows(ssr.benchmarkLifeCycles.get(i).getOutflows());
            }
        }

        public void caclulateReturnsAndOutperformances() {
            if (baseTotalInflows.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal baseProfit = baseTotalOutflows.subtract(baseTotalInflows);
                baseTotalReturn = baseProfit.divide(baseTotalInflows,10,BigDecimal.ROUND_HALF_EVEN);
            }
            else {
                baseTotalReturn = BigDecimal.ZERO;
            }
            for(int i=0; i<listOfBenchmarkData.size(); i++ ) {
                BenchmarkData bd = listOfBenchmarkData.get(i);
                if (baseTotalInflows.compareTo(BigDecimal.ZERO) != 0) {

                    BigDecimal benchmarkProfit = bd.getBenchmarkTotalOutflows().subtract(bd.getBenchmarkTotalInflows());
                    BigDecimal benchmarkTotalReturn = benchmarkProfit.divide(bd.getBenchmarkTotalInflows(),10,BigDecimal.ROUND_HALF_EVEN);
                    bd.setBenchmarkTotalReturn(benchmarkTotalReturn);
                    bd.setBenchmarkOutPerformance(baseTotalReturn.subtract(benchmarkTotalReturn));
                }
                else {
                    bd.setBenchmarkTotalReturn(BigDecimal.ZERO);
                    bd.setBenchmarkOutPerformance(BigDecimal.ZERO);
                }
            }
        }
    }

    public static class BenchmarkData {
        private Stock      benchmarkSecurity;
        private BigDecimal benchmarkTotalInflows = BigDecimal.ZERO;
        private BigDecimal benchmarkTotalOutflows = BigDecimal.ZERO;
        private BigDecimal benchmarkTotalReturn = BigDecimal.ZERO;
        private BigDecimal benchmarkOutPerformance = BigDecimal.ZERO;

        public BenchmarkData(Stock benchmarkSecurity) {
            this.benchmarkSecurity = benchmarkSecurity;
        }

        public Stock getBenchmarkSecurity() {
            return benchmarkSecurity;
        }

        public BigDecimal getBenchmarkTotalInflows() {
            return benchmarkTotalInflows;
        }

        public BigDecimal getBenchmarkTotalOutflows() {
            return benchmarkTotalOutflows;
        }

        public BigDecimal getBenchmarkTotalReturn() {
            return benchmarkTotalReturn;
        }

        public void setBenchmarkTotalReturn(BigDecimal benchmarkTotalReturn) {
            this.benchmarkTotalReturn = benchmarkTotalReturn;
        }

        public BigDecimal getBenchmarkOutPerformance() {
            return benchmarkOutPerformance;
        }

        public void setBenchmarkOutPerformance(BigDecimal benchmarkOutPerformance) {
            this.benchmarkOutPerformance = benchmarkOutPerformance;
        }

        public void accumulateInflows(BigDecimal newInflow) {
            benchmarkTotalInflows = benchmarkTotalInflows.add(newInflow);
        }

        public void accumulateOutflows(BigDecimal newOutflow) {
            benchmarkTotalOutflows = benchmarkTotalOutflows.add(newOutflow);
        }

    }

    public static class ResultOverTime {
        private String                    name;
        private List<IntermediateResult>  intermediateResults = new ArrayList<IntermediateResult>();

        public ResultOverTime(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public List<IntermediateResult> getIntermediateResults() {
            return intermediateResults;
        }
    }

    public final static class IntermediateResult {
        private final LocalDate       date;
        private final BigDecimal      returnOnDate; // where a 2.5% return is .025

        public IntermediateResult(LocalDate date, BigDecimal returnOnDate) {
            this.date = date;
            this.returnOnDate = returnOnDate;
        }
        public LocalDate getDate() {
            return date;
        }
        public BigDecimal getReturnOnDate() {
            return returnOnDate;
        }
    }
}
