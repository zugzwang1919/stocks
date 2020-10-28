package com.wolfesoftware.stocks.service.calculator;

import com.wolfesoftware.stocks.common.LocalDateUtil;
import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.*;
import com.wolfesoftware.stocks.model.calculator.BenchmarkAnalysisResponse;
import static com.wolfesoftware.stocks.model.calculator.BenchmarkAnalysisResponse.CalculatorResults;
import static com.wolfesoftware.stocks.model.calculator.BenchmarkAnalysisResponse.IntermediateResult;
import static com.wolfesoftware.stocks.model.calculator.BenchmarkAnalysisResponse.ResultOverTime;
import static com.wolfesoftware.stocks.model.calculator.BenchmarkAnalysisResponse.SingleSecurityResult;

import com.wolfesoftware.stocks.model.calculator.LifeCycle;
import com.wolfesoftware.stocks.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import javax.swing.text.DateFormatter;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class BenchmarkAnalysisService {

    @Resource
    LifeCycleService lifeCycleService;
    @Resource
    StockRepository stockRepository;
    @Resource
    PortfolioRepository portfolioRepository;
    @Resource
    StockTransactionRepository stockTransactionRepository;
    @Resource
    OptionTransactionRepository optionTransactionRepository;

    private static final Logger logger  = LoggerFactory.getLogger(BenchmarkAnalysisService.class);

    @Transactional
    public BenchmarkAnalysisResponse analyze(LocalDate startDate, LocalDate endDate, List<Long> portfolioIds, List<Long> stockIds, List<Long> benchmarkIds,
                                             boolean includeDividends, boolean includeCallsPuts) {
        LocalDate beginningOfTime = LocalDate.of(1900,1,1);
        LocalDate augmentedStartDate = startDate == null ? beginningOfTime : startDate;
        LocalDate augmentedEndDate = endDate == null ? LocalDate.now() : endDate;

        List<Stock> stocks = new Converter<Stock>().convertFromIdsToEntities(stockIds, stockRepository, "stock");
        List<Portfolio> portfolios = new Converter<Portfolio>().convertFromIdsToEntities(portfolioIds, portfolioRepository, "portfolio");
        List<Stock> benchmarks = new Converter<Stock>().convertFromIdsToEntities(benchmarkIds, stockRepository, "stock");
        Map<Stock,List<StockTransaction>> allStockTransactions = stockTransactionRepository.retrieveAndGroup(stocks, portfolios, augmentedEndDate);
        Map<Stock,List<OptionTransaction>> allOptionTransactions = optionTransactionRepository.retrieveAndGroup(stocks, portfolios, augmentedEndDate);
        Map<Stock,List<StockDividend>> dividendCache = new HashMap<>();

        BenchmarkAnalysisResponse.CalculatorResults calculatorResults = calculateBasicResults(augmentedStartDate, augmentedEndDate, allStockTransactions, allOptionTransactions, benchmarks,
                                                                                              dividendCache, includeDividends, includeCallsPuts);
        if (calculatorResults == null)
            return null;
        List<ResultOverTime> resultsOverTime = calculateResultsOverTime(calculatorResults, allStockTransactions, allOptionTransactions, benchmarks,
                                                                        dividendCache, includeDividends, includeCallsPuts);
        return new BenchmarkAnalysisResponse(calculatorResults, resultsOverTime);

    }

    private CalculatorResults calculateBasicResults(LocalDate beginDate, LocalDate endDate,
                                                    Map<Stock, List<StockTransaction>> allStockTransactions,
                                                    Map<Stock, List<OptionTransaction>> allOptionTransactions,
                                                    List<Stock> benchmarks,
                                                    Map<Stock,List<StockDividend>> dividendCache,
                                                    boolean includeDividends, boolean includeCallPuts) {
        CalculatorResults calculatorResults = new CalculatorResults(benchmarks);
        List<SingleSecurityResult> detailedResults = new ArrayList<>();
        LocalDate earliestBeginDate = null;
        for (Stock oneStock: allStockTransactions.keySet()) {
            try {
                List<StockTransaction> stockTransactionsForOneStock = allStockTransactions.get(oneStock);
                List<OptionTransaction> optionTransactionsForOneStock = allOptionTransactions.get(oneStock);
                SingleSecurityResult ssr = buildSingleSecurityResult(oneStock, stockTransactionsForOneStock, optionTransactionsForOneStock,
                        benchmarks, dividendCache, beginDate, endDate, includeDividends, includeCallPuts);
                if (ssr != null) {
                    detailedResults.add(ssr);
                    calculatorResults.getAccumulatedResults().accumulateResults(ssr);
                    LocalDate ssrBeginDate = ssr.getBaseLifeCycle().getOpeningPosition().getDate();
                    if (earliestBeginDate == null || ssrBeginDate.isBefore(earliestBeginDate)) {
                        earliestBeginDate = ssrBeginDate;
                    }
                }
            }
            catch (Exception e) {
                DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE;
                String newMessage = "While calculating results for " + oneStock.getTicker() +  " from " +
                                    dtf.format(beginDate) + " to " + dtf.format(endDate) + " a severe error occurred.  Details follow: " + e.toString();
                throw new RuntimeException(newMessage, e);
            }
        }
        if (detailedResults.isEmpty())
            return null;
        calculatorResults.getAccumulatedResults().setBeginDate(earliestBeginDate);
        calculatorResults.getAccumulatedResults().setEndDate(endDate);
        calculatorResults.getAccumulatedResults().caclulateReturnsAndOutperformances();
        calculatorResults.setListOfSingleSecurityResults(detailedResults);
        return calculatorResults;
    }

    private SingleSecurityResult buildSingleSecurityResult( Stock stock, List<StockTransaction> transactions, List<OptionTransaction> optionTransactions,
                                                            List<Stock> benchmarks,
                                                            Map<Stock,List<StockDividend>> dividendCache,
                                                            LocalDate beginDate, LocalDate endDate,
                                                            boolean includeDividends, boolean includeCallPuts ) {
        String debugString = "Building Single Security Result for " + stock.getTicker();
        StopWatch stopWatch = new StopWatch(debugString);
        logger.debug(debugString);
        stopWatch.start("Building Single Security Result - Building LifeCycle for Stock");
        SingleSecurityResult ssr = new SingleSecurityResult();
        LifeCycle lifeCycle = lifeCycleService.buildStockLifeCycle(stock, beginDate, endDate, transactions, optionTransactions, dividendCache, includeDividends, includeCallPuts);
        // Lifecycles can be  used for purely option based transactions.
        // We cannot calculate a return (especially against a benchmark for this scenario), so exclude this security from the overall calculation
        if (lifeCycle == null || lifeCycle.getOpeningPosition() == null)
            return null;
        ssr.setBaseLifeCycle(lifeCycle);
        stopWatch.stop();
        stopWatch.start("Building Single Security Result - Building LifeCycle for Benchmarks");
        for(Stock benchmark : benchmarks) {
            // Notice that for benchmarks, there is no notion of 'including options'
            LifeCycle benchmarkLifeCycle = lifeCycleService.newBenchmarkBasedOnExisting(lifeCycle, benchmark, dividendCache, includeDividends);
            ssr.getBenchmarkLifeCycles().add(benchmarkLifeCycle);
            ssr.getOutperformances().add(ssr.getBaseLifeCycle().getSimpleReturn().subtract(benchmarkLifeCycle.getSimpleReturn()));
        }
        stopWatch.stop();
        logger.debug(stopWatch.prettyPrint());
        return ssr;
    }

    private List<ResultOverTime> calculateResultsOverTime(CalculatorResults calculatorResults,
                                                          Map<Stock, List<StockTransaction>> allSecurityTransactions,
                                                          Map<Stock, List<OptionTransaction>> allOptionTransactions,
                                                          List<Stock> benchmarks,
                                                          Map<Stock,List<StockDividend>> dividendCache,
                                                          boolean includeDividends, boolean includeCallPuts ) {
        List<ResultOverTime> resultsOverTime = new ArrayList<>();
        ResultOverTime portfolioResultOverTime = new ResultOverTime("Portfolio");
        List<ResultOverTime> benchmarksResultOverTime = new ArrayList<>();
        for (Stock stock : benchmarks) {
            ResultOverTime benchmarkResultOverTime = new ResultOverTime(stock.getTicker());
            benchmarksResultOverTime.add(benchmarkResultOverTime);
        }

        // Special case for initial date
        portfolioResultOverTime.getIntermediateResults().add(new IntermediateResult(calculatorResults.getAccumulatedResults().getBeginDate(), BigDecimal.ZERO));
        for (ResultOverTime benchmarkResultOverTime : benchmarksResultOverTime) {
            benchmarkResultOverTime.getIntermediateResults().add(new IntermediateResult(calculatorResults.getAccumulatedResults().getBeginDate(), BigDecimal.ZERO));
        }
        // All other dates
        List<LocalDate> endDates = getListOfEndDates(calculatorResults.getAccumulatedResults().getBeginDate(), calculatorResults.getAccumulatedResults().getEndDate());
        for (LocalDate intermediateEndDate : endDates) {
            CalculatorResults cr = calculateBasicResults(calculatorResults.getAccumulatedResults().getBeginDate(),
                    intermediateEndDate, allSecurityTransactions, allOptionTransactions, benchmarks, dividendCache, includeDividends, includeCallPuts);
            portfolioResultOverTime.getIntermediateResults().add(new IntermediateResult(intermediateEndDate, cr.getAccumulatedResults().getBaseTotalReturn()));
            for (int i = 0; i < benchmarksResultOverTime.size(); i++) {
                benchmarksResultOverTime.get(i).getIntermediateResults().add(new IntermediateResult(intermediateEndDate, cr.getAccumulatedResults().getListOfBenchmarkData().get(i).getBenchmarkTotalReturn()));
            }
        }

        // Special case for final date
        portfolioResultOverTime.getIntermediateResults().add(new IntermediateResult(calculatorResults.getAccumulatedResults().getEndDate(), calculatorResults.getAccumulatedResults().getBaseTotalReturn()));
        for(int i=0; i < benchmarksResultOverTime.size(); i++ ) {
            IntermediateResult ir = new IntermediateResult(calculatorResults.getAccumulatedResults().getEndDate(), calculatorResults.getAccumulatedResults().getListOfBenchmarkData().get(i).getBenchmarkTotalReturn());
            benchmarksResultOverTime.get(i).getIntermediateResults().add(ir);
        }

        // Add all of the results to the collection
        resultsOverTime.add(portfolioResultOverTime);
        resultsOverTime.addAll(benchmarksResultOverTime);

        return resultsOverTime;
    }

    private List<LocalDate> getListOfEndDates(LocalDate beginDate, LocalDate endDate) {

        long daysInThreeYears = 3L*365;
        long daysInSixMonths = 365/2;

        // Figure out the frequency of calculation
        Frequency frequency;
        long numDaysInPeriod = endDate.toEpochDay() - beginDate.toEpochDay();
        if (numDaysInPeriod > daysInThreeYears)
            frequency = Frequency.QUARTERLY;
        else if (numDaysInPeriod > daysInSixMonths)
            frequency = Frequency.MONTHLY;
        else if (numDaysInPeriod > 62 )
            frequency = Frequency.WEEKLY;
        else
            frequency = Frequency.DAILY;
        logger.debug("Frequency for end dates = " + frequency);

        List<LocalDate> endDates = new ArrayList<>();

        // Find the first date
        LocalDate calculationDate;
        switch (frequency) {
            case QUARTERLY:
                calculationDate = LocalDateUtil.lastDayOfQuarter(beginDate);
                break;
            case MONTHLY:
                calculationDate = beginDate.with(TemporalAdjusters.lastDayOfMonth());
                break;
            case WEEKLY:
                calculationDate = LocalDateUtil.fridayOfWeek(beginDate);
                break;
            case DAILY:
                calculationDate = beginDate.plusDays(1);
                break;
            default:
                throw new IllegalStateException("Encountered a frequency for calculation that was not expected.");
        }
        if(calculationDate.isAfter(beginDate) && calculationDate.isBefore(endDate)) {
            logger.debug("Adding {} as a date that must have values calculated.", calculationDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            endDates.add(calculationDate);
        }
        // Find all subsequent dates
        while (calculationDate.isBefore(endDate)) {
            switch (frequency) {
                case QUARTERLY:
                    calculationDate = calculationDate.with(TemporalAdjusters.firstDayOfMonth()).plusMonths(3);
                    calculationDate = LocalDateUtil.lastDayOfMonth(calculationDate);
                    break;
                case MONTHLY:
                    calculationDate = calculationDate.with(TemporalAdjusters.firstDayOfMonth()).plusMonths(1);
                    calculationDate = LocalDateUtil.lastDayOfMonth(calculationDate);
                    break;
                case WEEKLY:
                    calculationDate = calculationDate.plusDays(7);
                    break;
                case DAILY:
                    calculationDate = calculationDate.plusDays(1);
                    DayOfWeek dayOfWeek = calculationDate.getDayOfWeek();
                    if (dayOfWeek == DayOfWeek.SATURDAY ||
                            dayOfWeek == DayOfWeek.SUNDAY)
                        calculationDate = calculationDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                    break;
                default:
                    throw new IllegalStateException("Encountered a frequency for calculation that was not expected while looking for subsequent dates.");
            }
            if (calculationDate.isBefore(endDate)) {
                logger.debug("Adding {} as a date that must have values calculated.", calculationDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
                endDates.add(calculationDate);
            }
        }
        return endDates;
    }

    private enum Frequency {
        QUARTERLY, MONTHLY, WEEKLY, DAILY
    }


    // FIXME: There's another one of these in IncomeAnalysisService
    private static class Converter<T> {
        private List<T> convertFromIdsToEntities(List<Long> ids, UserBasedRepository userBasedRepository, String nameOfUserBasedEntity) {
            List<T> entities = ids.stream().map(id -> {
                Optional<T> optionalEntity = userBasedRepository.retrieveById(id);
                if (optionalEntity.isEmpty())
                    throw new NotFoundException("The requested " + nameOfUserBasedEntity + " with an ID of " + id + " could not be found.");
                return optionalEntity.get();
            }).collect(Collectors.toList());
            return entities;
        }
    }
}
