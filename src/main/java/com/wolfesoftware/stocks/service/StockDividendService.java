package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.common.YahooFinance;
import com.wolfesoftware.stocks.model.LoadOrUpdateResponse;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockDividend;
import com.wolfesoftware.stocks.repository.StockDividendRepository;
import com.wolfesoftware.stocks.repository.StockPriceRepository;
import com.wolfesoftware.stocks.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@Service
public class StockDividendService {

    @Resource
    private StockRepository stockRepository;

    @Resource
    private StockDividendRepository stockDividendRepository;

    @Resource
    private StockPriceRepository stockPriceRepository;

    private static Logger logger = LoggerFactory.getLogger("StockDividendService.class");



    // RETRIEVE
    public List<StockDividend> retrieveAllForOneStock(Stock stock){
        return stockDividendRepository.retrieveAllForOneStock(stock);
    }


    // OTHER - Non-CRUD Services

    /*
    Typically used when a stock is created.  This method will retrieve and
    then persist StockDividends.
    */
    public List<StockDividend> initialLoadOfStockDividends(Stock stock) {
        LocalDate beginDate = StockDividend.EARLIEST_STOCK_DIVIDEND_DATE;
        LocalDate today = LocalDate.now();
        List<StockDividend> stockDividends = YahooFinance.getHistoricalStockDividends(stock, beginDate, today);
        return stockDividendRepository.persistStockDividends(stockDividends);
    }


    @Scheduled(cron = "0 0 0 * * SAT")
    @Transactional
    public void loadOrUpdateStockSplitsFromLastTenYears() {

        LoadOrUpdateResponse response = loadOrUpdateStockSplitsForAllSecurities(LocalDate.now().minusDays(365*10), LocalDate.now());
        logger.info(response.getSummary());

    }


    public LoadOrUpdateResponse loadOrUpdateStockSplitsForAllSecurities(LocalDate beginDate, LocalDate endDate) {
        LoadOrUpdateResponse response = new LoadOrUpdateResponse();
        LocalDate today = LocalDate.now();
        for( Stock stock : stockRepository.retrieveAll() ) {
            LocalDate mostRecentPriceDate = stockPriceRepository.mostRecentStockPriceDate(stock);
            // As long as this stock still exists
            if (mostRecentPriceDate != null && mostRecentPriceDate.isAfter(today.minusDays(30))) {
                LoadOrUpdateResponse resultsFromOneSecurity = loadOrUpdateStockDividendsForOneStock(stock, beginDate, endDate);
                response.accumulate(resultsFromOneSecurity);
            }
        }

        return response;
    }



    private LoadOrUpdateResponse loadOrUpdateStockDividendsForOneStock(Stock stock, LocalDate beginDate, LocalDate endDate) {
        LoadOrUpdateResponse result = new LoadOrUpdateResponse();
        List<StockDividend> yahooStockDividends = YahooFinance.getHistoricalStockDividends(stock, beginDate, endDate);
        for (StockDividend foundStockDividend : yahooStockDividends) {
            List<StockDividend> existingStockDividends = stockDividendRepository.retrieveForOneStockOnOneDate(stock, foundStockDividend.getExDividendDate());
            logger.debug("Stock dividend found - {} - to be compared with existing ones.", foundStockDividend);
            if (existingStockDividends.size() > 1) {
                // Unexpected discovery.  Multiple stock dividends for the same date are already in the database
                logger.error("Multiple stock dividends were found for {} on {}.  No action will be taken.", stock.getTicker(), foundStockDividend.getExDividendDate());
                result.addToItemsUnmodified(1);
            }
            else if (existingStockDividends.isEmpty()) {
                // Stock Dividend does not exist.  Add it.
                stockDividendRepository.persistStockDividend(foundStockDividend);
                result.addToItemsLoaded(1);
            }
            else {
                // There is exactly ONE existing Stock Dividend
                StockDividend existingStockDividend = existingStockDividends.get(0);
                if (existingStockDividend.getDividendAmount().compareTo(foundStockDividend.getDividendAmount()) == 0) {
                    // Before and after amounts match. Do nothing.
                    result.addToItemsUnmodified(1);
                }
                else {
                    // A stock dividend already exists and the amounts do not match.  Update the existing dividend.
                    logger.info("A stock dividend will be replaced for {} which went ex-dividend on {}.", existingStockDividend.getStock().getTicker(), existingStockDividend.getExDividendDate());
                    logger.info("Previous amount was {}.  New  is {}.", existingStockDividend.getDividendAmount(), foundStockDividend.getDividendAmount());
                    existingStockDividend.setDividendAmount(foundStockDividend.getDividendAmount());
                    stockDividendRepository.updateStockDividend(existingStockDividend.getId(), foundStockDividend.getExDividendDate(), foundStockDividend.getDividendAmount());
                    result.addToItemsUpdated(1);
                }
            }

        }
        return result;
    }

}
