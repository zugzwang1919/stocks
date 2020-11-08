package com.wolfesoftware.stocks.service;

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
    @Resource
    private YahooFinanceService yahooFinanceService;


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
    public void initialLoadOfStockDividends(Stock stock) {
        LocalDate beginDate = StockDividend.EARLIEST_STOCK_DIVIDEND_DATE;
        LocalDate today = LocalDate.now();
        List<StockDividend> stockDividends = yahooFinanceService.getHistoricalStockDividends(stock, beginDate, today);
        stockDividendRepository.persistStockDividends(stockDividends);
    }


    @Scheduled(cron = "0 9 1 * * SAT")  // 1:09 am on Saturday morning
    @Transactional
    public void loadOrUpdateAllDividends() {

        LoadOrUpdateResponse response = loadOrUpdateDividendsForAllSecurities(StockDividend.EARLIEST_STOCK_DIVIDEND_DATE, LocalDate.now());
        logger.info(response.getSummary());

    }


    public LoadOrUpdateResponse loadOrUpdateDividendsForAllSecurities(LocalDate beginDate, LocalDate endDate) {
        LoadOrUpdateResponse response = new LoadOrUpdateResponse();
        LocalDate today = LocalDate.now();
        for( Stock stock : stockRepository.retrieveAll() ) {
            LocalDate mostRecentPriceDate = stockPriceRepository.mostRecentStockPriceDate(stock);
            // As long as this stock still exists
            if (mostRecentPriceDate != null && mostRecentPriceDate.isAfter(today.minusDays(30))) {
                LoadOrUpdateResponse resultsFromOneSecurity = loadOrUpdateDividendsForOneStock(stock, beginDate, endDate);
                response.accumulate(resultsFromOneSecurity);
            }
        }
        response.buildSummary();
        return response;
    }



    private LoadOrUpdateResponse loadOrUpdateDividendsForOneStock(Stock stock, LocalDate beginDate, LocalDate endDate) {
        LoadOrUpdateResponse result = new LoadOrUpdateResponse();
        List<StockDividend> yahooStockDividends = yahooFinanceService.getHistoricalStockDividends(stock, beginDate, endDate);
        for (StockDividend foundStockDividend : yahooStockDividends) {
            List<StockDividend> existingStockDividends = stockDividendRepository.retrieveForOneStockOnOneDate(stock, foundStockDividend.getExDividendDate());
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
