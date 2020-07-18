package com.wolfesoftware.stocks.service.calculator;

import com.wolfesoftware.stocks.exception.IllegalActionException;
import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.*;
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

        LocalDate augmentedStartDate = startDate == null ? LocalDate.of(1900, 1, 1) : startDate;
        LocalDate augmentedEndDate = endDate == null ? LocalDate.now() : endDate;

        List<Stock> stocks = new Converter<Stock>().convertFromIdsToEntities(stockIds, stockRepository, "stock");
        List<Portfolio> portfolios = new Converter<Portfolio>().convertFromIdsToEntities(portfolioIds, portfolioRepository, "portfolio");
        List<LifeCycle> lifeCycles = new ArrayList<>();
        Map<Stock,List<StockDividend>> dividendCache =  new HashMap<>();
        stocks.forEach(s->{
            List<StockTransaction> stockTransactions = stockTransactionRepository.retrieveForOneStock(s, augmentedStartDate, augmentedEndDate, portfolios);
            List<OptionTransaction> optionTransactions = optionTransactionRepository.retrieveForOneStock(s, augmentedStartDate, augmentedEndDate, portfolios);
            // FIXME - hardcoded true & true
            LifeCycle lifeCycle = lifeCycleService.buildStockLifeCycle(s, augmentedStartDate, augmentedEndDate, stockTransactions, optionTransactions, dividendCache, includeDividends, includeOptions);
            lifeCycles.add(lifeCycle);
        });

        IncomeAnalysisResponse incomeAnalysisResponse = new IncomeAnalysisResponse();
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
