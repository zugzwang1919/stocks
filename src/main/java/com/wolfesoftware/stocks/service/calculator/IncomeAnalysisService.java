package com.wolfesoftware.stocks.service.calculator;

import com.wolfesoftware.stocks.model.OptionTransaction;
import com.wolfesoftware.stocks.model.Portfolio;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockTransaction;
import com.wolfesoftware.stocks.model.calculator.ClosingPosition;
import com.wolfesoftware.stocks.model.calculator.IncomeAnalysisResponse;
import com.wolfesoftware.stocks.model.calculator.LifeCycle;
import com.wolfesoftware.stocks.repository.OptionTransactionRepository;
import com.wolfesoftware.stocks.repository.PortfolioRepository;
import com.wolfesoftware.stocks.repository.StockRepository;
import com.wolfesoftware.stocks.repository.StockTransactionRepository;
import com.wolfesoftware.stocks.service.IdToEntityConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
public class IncomeAnalysisService {

    @Resource
    StockRepository stockRepository;

    @Resource
    PortfolioRepository portfolioRepository;

    @Resource
    StockTransactionRepository stockTransactionRepository;

    @Resource
    OptionTransactionRepository optionTransactionRepository;

    @Resource
    LifeCycleService lifeCycleService;

    @Transactional
    public IncomeAnalysisResponse analyze(LocalDate startDate, LocalDate endDate, List<Long> portfolioIds, List<Long> stockIds, boolean includeDividends, boolean includeOptions) {

        LocalDate beginningOfTime = LocalDate.of(1900,1,1);
        LocalDate augmentedStartDate = startDate == null ? beginningOfTime : startDate;
        LocalDate augmentedEndDate = endDate == null ? LocalDate.now() : endDate;

        List<Stock> stocks = new IdToEntityConverter<Stock>().convertFromIdsToEntities(stockIds, stockRepository, "stock");
        List<Portfolio> portfolios = new IdToEntityConverter<Portfolio>().convertFromIdsToEntities(portfolioIds, portfolioRepository, "portfolio");
        List<LifeCycle> lifeCycles = new ArrayList<>();
        IncomeAnalysisResponse incomeAnalysisResponse = new IncomeAnalysisResponse();
        IncomeAnalysisResponse.AnalysisTotals analysisTotals = incomeAnalysisResponse.getAnalysisTotals();
        stocks.forEach(s->{
            List<StockTransaction> stockTransactions = stockTransactionRepository.retrieveForOneStock(s, beginningOfTime, augmentedEndDate, portfolios);
            List<OptionTransaction> optionTransactions = optionTransactionRepository.retrieveForOneStock(s, augmentedStartDate, augmentedEndDate, portfolios);
            LifeCycle lifeCycle = lifeCycleService.buildStockLifeCycle(s, augmentedStartDate, augmentedEndDate, stockTransactions, optionTransactions, includeDividends, includeOptions);
            // If a LifeCycle was created
            if (lifeCycle != null) {

                // Increment Totals for the entire LifeCycle
                analysisTotals.incrementProceeds(lifeCycle.getProfitsFromSecurities());
                analysisTotals.incrementDividendProceeds(lifeCycle.getDividendsAccrued());
                analysisTotals.incrementOptionProceeds(lifeCycle.getOptionProceedsAccrued());
                // Add the totals
                analysisTotals.incrementTotalGains(lifeCycle.getProfitsFromSecurities());
                analysisTotals.incrementTotalGains(lifeCycle.getDividendsAccrued());
                analysisTotals.incrementTotalGains(lifeCycle.getOptionProceedsAccrued());




                // Note whether or not this lifecycle should be included in the end of year snapshot
                // And then help build the GRAND TOTALS for all of the snapshots
                ClosingPosition closingPosition = lifeCycle.getClosingPosition();
                boolean lifeCycleEndDateInculdesStock = closingPosition != null && augmentedEndDate.equals(closingPosition.getDate());
                lifeCycle.setIncludedInSnapshot(    lifeCycleEndDateInculdesStock ||
                                                    lifeCycle.getOptionExposureToPutsAtRequestedEndDate().compareTo(BigDecimal.ZERO) > 0);
                if (lifeCycle.isIncludedInSnapshot()) {
                    IncomeAnalysisResponse.SnapshotTotals snapshotTotals = incomeAnalysisResponse.getSnapshotTotals();
                    snapshotTotals.incrementPutExposure(lifeCycle.getOptionExposureToPutsAtRequestedEndDate());
                    snapshotTotals.incrementTotalLongExposure(lifeCycle.getOptionExposureToPutsAtRequestedEndDate());
                    snapshotTotals.incrementCallableExposure(lifeCycle.getOptionExposureToCallsAtRequestedEndDate());
                    // NOTE: It's possible for the snapshot to only have options
                    // NOTE: If we still have stock at the end of the period, include it in the calculations
                    if (lifeCycleEndDateInculdesStock) {
                        snapshotTotals.incrementStockValue(closingPosition.getValue());
                        snapshotTotals.incrementTotalLongExposure(closingPosition.getValue());
                    }
                }
                // Add the LifeCycle to the response
                lifeCycles.add(lifeCycle);

            }
        });

        incomeAnalysisResponse.setLifeCycles(lifeCycles);

        // Once all lifeCycles have been created, we can calculate ...
        // the analysisTotal.annualReturn
        incomeAnalysisResponse.getAnalysisTotals().setAnnualReturn(calculateOverallAnnualizedReturn(lifeCycles));


        return incomeAnalysisResponse;
    }


    private BigDecimal calculateOverallAnnualizedReturn(List<LifeCycle> lifeCycles) {
        BigDecimal numerator = BigDecimal.ZERO;
        BigDecimal denominator = BigDecimal.ZERO;
        for(LifeCycle lifeCycle: lifeCycles) {
            numerator = numerator.add(lifeCycle.getAnnualizedIncomeReturnOnInvestment().multiply(lifeCycle.getTotalDollarDays()));
            denominator = denominator.add(lifeCycle.getTotalDollarDays());
        }
        if (denominator.compareTo(BigDecimal.ZERO) > 0 ) {
            return numerator.divide(denominator, 12, RoundingMode.HALF_EVEN);
        }
        return BigDecimal.ZERO;

    }
}
