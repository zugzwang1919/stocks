package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.common.YahooFinance;
import com.wolfesoftware.stocks.model.*;
import com.wolfesoftware.stocks.repository.StockPriceRepository;
import com.wolfesoftware.stocks.repository.StockRepository;
import com.wolfesoftware.stocks.repository.StockSplitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class StockSplitService {

    @Resource
    private StockRepository stockRepository;

    @Resource
    private StockPriceRepository stockPriceRepository;

    @Resource
    private StockSplitRepository stockSplitRepository;


    private static Logger logger = LoggerFactory.getLogger("StockSplitService.class");


    // RETRIEVE
    public List<StockSplit> retrieveAllForOneStock(Stock stock){
        return stockSplitRepository.retrieveAllForOneStock(stock);
    }


    // OTHER - Non-CRUD Services

    public BigDecimal stockSplitFactorSince(Stock stock, LocalDate sinceDate) {

        BigDecimal returnValue = BigDecimal.ONE;

        List<StockSplit> stockSplits = stockSplitRepository.retrieveForOneStockBetweenDates(stock, sinceDate, LocalDate.now());
        for (StockSplit ss: stockSplits) {
            returnValue = returnValue.multiply(ss.getAfterAmount()).divide(ss.getBeforeAmount());
        }
        return returnValue;
    }

    /*
    Typically used when a stock is created.  This method will retrieve and
    then persist StockSplits.
    */
    public List<StockSplit> initialLoadOfStockSplits(Stock stock) {
        LocalDate beginDate = StockSplit.EARLIEST_STOCK_SPLIT;
        LocalDate today = LocalDate.now();
        List<StockSplit> stockSplits = YahooFinance.getHistoricalStockSplits(stock, beginDate, today);
        return stockSplitRepository.persistStockSplits(stockSplits);
    }


    @Scheduled(cron = "0 0 0 * * SAT")
    @Transactional
    public void loadOrUpdateAllStockSplits() {

        LoadOrUpdateResponse response = loadOrUpdateStockSplitsForAllSecurities(StockSplit.EARLIEST_STOCK_SPLIT, LocalDate.now());
        logger.info(response.getSummary());

    }


    public LoadOrUpdateResponse loadOrUpdateStockSplitsForAllSecurities(LocalDate beginDate, LocalDate endDate) {
        LoadOrUpdateResponse response = new LoadOrUpdateResponse();
        LocalDate today = LocalDate.now();
        for( Stock stock : stockRepository.retrieveAll() ) {
            LocalDate mostRecentPriceDate = stockPriceRepository.mostRecentStockPriceDate(stock);
            // As long as this stock still exists
            if (mostRecentPriceDate != null && mostRecentPriceDate.isAfter(today.minusDays(30))) {
                LoadOrUpdateResponse resultsFromOneSecurity = loadOrUpdateStockSplitsForOneStock(stock, beginDate, endDate);
                response.accumulate(resultsFromOneSecurity);
            }
        }

        response.buildSummary();
        logger.info("Completing load/update of stock splits.");
        logger.info("{} stock splits were loaded.", response.getItemsLoaded());
        logger.info("{} stock splits were updated.", response.getItemsUpdated());
        logger.info("{} stock splits were unmodified.", response.getItemsUnmodified());
        return response;
    }

    private LoadOrUpdateResponse loadOrUpdateStockSplitsForOneStock(Stock stock, LocalDate beginDate, LocalDate endDate) {
        LoadOrUpdateResponse result = new LoadOrUpdateResponse();
        List<StockSplit> yahooStockSplits = YahooFinance.getHistoricalStockSplits(stock, beginDate, endDate);
        for (StockSplit foundStockSplit : yahooStockSplits) {
            List<StockSplit> existingStockSplits = stockSplitRepository.retrieveForOneStockOnOneDate(stock, foundStockSplit.getDate());
            logger.debug("Stock split found - {} - to be compared with existing ones.", foundStockSplit);
            if (existingStockSplits.size() > 1) {
                // Unexpected discovery.  Multiple stock splits for the same date are already in the database
                logger.error("Multiple stock splits were found for {} on {}.  No action will be taken.", stock.getTicker(), foundStockSplit.getDate());
                result.addToItemsUnmodified(1);
            }
            else if (existingStockSplits.isEmpty()) {
                // Stock Split does not exist.  Add it.
                stockSplitRepository.persistStockSplit(foundStockSplit);
                result.addToItemsLoaded(1);
            }
            else {
                // There is exactly ONE existing Stock Split
                StockSplit existingStockSplit = existingStockSplits.get(0);
                if (existingStockSplit.getAfterAmount().compareTo(foundStockSplit.getAfterAmount()) == 0 &&
                        existingStockSplit.getBeforeAmount().compareTo(foundStockSplit.getBeforeAmount()) == 0) {
                    // Before and after amounts match. Do nothing.
                    result.addToItemsUnmodified(1);
                }
                else {
                    // Stock splits already exists and the amounts do not match.  Update the existing dividend.
                    logger.info("A stock split will be replaced for {} which split on {}.", existingStockSplit.getStock().getTicker(), existingStockSplit.getDate());
                    logger.info("Previous after value was {}.  New after value is {}.", existingStockSplit.getAfterAmount(), foundStockSplit.getAfterAmount());
                    logger.info("Previous before value was {}.  New before value is {}.", existingStockSplit.getBeforeAmount(), foundStockSplit.getBeforeAmount());
                    existingStockSplit.setBeforeAmount(foundStockSplit.getBeforeAmount());
                    existingStockSplit.setAfterAmount(foundStockSplit.getAfterAmount());
                    stockSplitRepository.updateStockSplit(  existingStockSplit.getId(),foundStockSplit.getDate(),
                                                            foundStockSplit.getBeforeAmount(), foundStockSplit.getAfterAmount());
                    result.addToItemsUpdated(1);
                }
            }

        }
        return result;
    }

}
