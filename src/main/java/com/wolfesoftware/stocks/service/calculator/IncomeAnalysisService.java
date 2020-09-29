package com.wolfesoftware.stocks.service.calculator;

import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.*;
import com.wolfesoftware.stocks.model.calculator.ClosingPosition;
import com.wolfesoftware.stocks.model.calculator.IncomeAnalysisResponse;
import com.wolfesoftware.stocks.model.calculator.LifeCycle;
import com.wolfesoftware.stocks.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


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

        List<Stock> stocks = new Converter<Stock>().convertFromIdsToEntities(stockIds, stockRepository, "stock");
        List<Portfolio> portfolios = new Converter<Portfolio>().convertFromIdsToEntities(portfolioIds, portfolioRepository, "portfolio");
        List<LifeCycle> lifeCycles = new ArrayList<>();
        Map<Stock,List<StockDividend>> dividendCache =  new HashMap<>();
        IncomeAnalysisResponse incomeAnalysisResponse = new IncomeAnalysisResponse();
        IncomeAnalysisResponse.AnalysisTotals analysisTotals = incomeAnalysisResponse.getAnalysisTotals();
        stocks.forEach(s->{
            List<StockTransaction> stockTransactions = stockTransactionRepository.retrieveForOneStock(s, beginningOfTime, augmentedEndDate, portfolios);
            List<OptionTransaction> optionTransactions = optionTransactionRepository.retrieveForOneStock(s, augmentedStartDate, augmentedEndDate, portfolios);
            LifeCycle lifeCycle = lifeCycleService.buildStockLifeCycle(s, augmentedStartDate, augmentedEndDate, stockTransactions, optionTransactions, dividendCache, includeDividends, includeOptions);
            // If a LifeCycle was created
            if (lifeCycle != null) {

                // Increment Totals for the entire LifeCycle
                analysisTotals.incrementProceeds(lifeCycle.getClosingPosition().getValue());
                analysisTotals.incrementDividendProceeds(lifeCycle.getDividendsAccrued());
                analysisTotals.incrementOptionProceeds(lifeCycle.getOptionProceedsAccrued());
                analysisTotals.incrementTotalGains(lifeCycle.getProfitsFromSecurities());




                // Note whether or not this stock should be included in the end of year snapshot
                // And then help build the GRAND TOTALS for all of the snapshots
                lifeCycle.setIncludedInSnapshot(augmentedEndDate.equals(lifeCycle.getClosingPosition().getDate()));
                if (lifeCycle.isIncludedInSnapshot()) {
                    IncomeAnalysisResponse.SnapshotTotals snapshotTotals = incomeAnalysisResponse.getSnapshotTotals();
                    ClosingPosition closingPosition = lifeCycle.getClosingPosition();
                    snapshotTotals.incrementStockValue(closingPosition.getValue());
                    snapshotTotals.incrementPutExposure(lifeCycle.getOptionExposureToPutsAtRequestedEndDate());
                    snapshotTotals.incrementTotalLongExposure(closingPosition.getValue().add(lifeCycle.getOptionExposureToPutsAtRequestedEndDate()));
                    snapshotTotals.incrementCallableExposure(lifeCycle.getOptionExposureToCallsAtRequestedEndDate());
                }
                // Add the LifeCycle to the response
                lifeCycles.add(lifeCycle);

            }
        });

        incomeAnalysisResponse.setLifeCycles(lifeCycles);



        return incomeAnalysisResponse;
    }


    private class Converter<T> {
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
