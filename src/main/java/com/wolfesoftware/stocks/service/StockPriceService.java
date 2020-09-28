package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.common.YahooFinance;
import com.wolfesoftware.stocks.model.*;
import com.wolfesoftware.stocks.repository.StockPriceRepository;
import com.wolfesoftware.stocks.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class StockPriceService {

    @Resource
    private StockRepository stockRepository;

    @Resource
    private StockPriceRepository stockPriceRepository;

    @Resource
    private StockSplitService stockSplitService;

    private static final long DAYS_TO_STILL_BE_CONSIDERED_TIMELY = 14;

    private final Logger logger = LoggerFactory.getLogger(StockPriceService.class);



    public BigDecimal retrieveClosingPrice(Stock stock, LocalDate requestedDate) {
        StockPrice dbStockPrice = retrieveDbClosingPrice(stock, requestedDate);
        BigDecimal actualPriceOnDate = stockSplitService.stockSplitFactorSince(stock, requestedDate).
                                        multiply(dbStockPrice.getPrice());
        return actualPriceOnDate;
    }


    @Transactional
    // NOTE:  It's important to note that unlike other methods in this class this method will use
    // NOTE:  both the SparseStockPrice and StockPrice PersistentEntities
    public StockPrice retrieveDbClosingPrice(Stock stock, LocalDate requestedDate) {
        if (requestedDate.isBefore(StockPrice.EARLIEST_DAILY_PRICE)) {
            // FIXME - Need to use SparseStockPrice
            return null;
        }
        else {
            LocalDate beginDate = requestedDate.minusDays(DAYS_TO_STILL_BE_CONSIDERED_TIMELY);
            List<StockPrice> possiblePrices = stockPriceRepository.retrieveByStockAndDateDescending(stock, beginDate, requestedDate);
            return possiblePrices.isEmpty() ? null : possiblePrices.get(0);
        }
    }




    @Scheduled(cron = "0 30 16,20,23 * * *")
    @Transactional
    public void loadTodaysPrices() {

        TodaysStockPriceLoadResponse response = loadTodaysStockPriceForAllStocks();
        logger.info(response.getSummary());

    }


    @Transactional
    public TodaysStockPriceLoadResponse loadTodaysStockPriceForAllStocks() {
        TodaysStockPriceLoadResponse response = new TodaysStockPriceLoadResponse();
        LocalDate today = LocalDate.now();
        for( Stock stock : stockRepository.retrieveAll() ) {
            logger.debug("Starting to work on " + stock.getTicker());
            LocalDate mostRecentPriceDate = stockPriceRepository.mostRecentStockPriceDate(stock);

            // Three possibilities
            // #1 - This stock doesn't exist any more
            if (mostRecentPriceDate != null && mostRecentPriceDate.isBefore(today.minusDays(30))) {
                logger.info("Price retrieval for " + stock.getTicker() + " has been abandoned.");
                response.getAbandonedStocks().add(stock);
            }
            // #2 - This stock exists & we don't have today's price in the database
            else if (mostRecentPriceDate == null || !mostRecentPriceDate.equals(today)) {
                logger.debug("Stock price was not found in database for today's date.");
                StockPrice todaysStockPrice = YahooFinance.getTodaysStockPrice(stock);
                if (todaysStockPrice != null) {
                    // OK.  Here's where we do the work we want to do.
                    stockPriceRepository.persistStockPrice(todaysStockPrice);
                    response.getSuccessfullyLoadedStocks().add(stock);
                    logger.debug("Stock price was loaded for " + stock.getTicker());
                }
                else {
                    response.getNonLoadedStocks().add(stock);
                    logger.debug("Stock price was not loaded for "  + stock.getTicker());
                }
            }
            // #3 - We've already loaded today's price
            else {
                response.getPreviouslyLoadedStocks().add(stock);
                logger.debug(stock.getTicker() + " price was not loaded.  Stock price for today is already in database.");
            }
        }
        return response;

    }

    /*
    Typically used when a stock is created.  This method will retrieve and
    then persist SecurityPrices.
    */
    public List<StockPrice> loadInitialPileOfStockPrices(Stock stock) {
        LocalDate beginDate = StockPrice.EARLIEST_DAILY_PRICE;
        LocalDate today = LocalDate.now();
        List<StockPrice> stockPrices = YahooFinance.getHistoricalStockPrices(stock, beginDate, today, null);
        return stockPriceRepository.persistStockPrices(stockPrices);
    }


    /*
    Typically used administratively to ensure that prices are up to date.  This method
    will retrieve SecurityPrices compare them with previously persisted
    SecurityPrices and update them as necessary.  It will do this for all stocks.
    */
    public LoadOrUpdateResponse loadOrUpdateAllStockPrices(LocalDate beginDate, LocalDate endDate) {
        logger.info("Beginning load/update of stock prices.");
        LoadOrUpdateResponse response = new LoadOrUpdateResponse();
        for (Stock stock : stockRepository.retrieveAll()) {
            try {
                LoadOrUpdateResponse oneStockResults;
                oneStockResults = loadOrUpdateStockPrices(stock, beginDate, endDate);
                response.accumulate(oneStockResults);
            } catch (Exception e) {
                logger.error("Error occurred during mass load of data for " + stock.getTicker() + ".  Processing will continue.");
                logger.error("Message from exception =  "  + e.getMessage());
            }
        }
        logger.info("Completing load/update of stock prices.");
        logger.info("{} prices were loaded.", response.getItemsLoaded());
        logger.info("{} prices were updated.", response.getItemsUpdated());
        logger.info("{} prices were unmodified.", response.getItemsUnmodified());
        return response;
    }

    /*
    Typically used administratively to ensure that prices are up to date for ONE SECURITY.
    This method will retrieve StockPrices compare them with previously persisted
    StockPrices and update them as necessary.
    */
    private LoadOrUpdateResponse loadOrUpdateStockPrices(Stock stock, LocalDate beginDate, LocalDate endDate) {
        LoadOrUpdateResponse response = new LoadOrUpdateResponse();
        List<StockPrice> stockPrices = YahooFinance.getHistoricalStockPrices(stock, beginDate, endDate, null);
        for (StockPrice sp : stockPrices) {
            Optional<StockPrice> optionalStockPrice = stockPriceRepository.retrieveByStockAndDate(stock, sp.getDate());
            if (optionalStockPrice.isEmpty()) {
                // StockPrice does not exist.  Add it.
                stockPriceRepository.persistStockPrice(sp);
                Object[] loggerData = {sp.getStock().getTicker(), sp.getPrice().toString(), sp.getDate().toString() };
                logger.debug("{}'s price of {} was added for {}.", loggerData);
                response.addToItemsLoaded(1);
            }
            else if ( optionalStockPrice.get().getPrice().compareTo(sp.getPrice()) != 0 ) {
                StockPrice oldStockPrice = optionalStockPrice.get();
                Object[] loggerData = {sp.getStock().getTicker(), oldStockPrice.getPrice().toPlainString(), sp.getPrice().toPlainString(), sp.getDate().toString()};
                logger.info("{}'s original price of {} is being replaced with {} for {}.", loggerData);
                // StockPrice exists, but the amount is different.  Modify it.
                stockPriceRepository.updateStockPrice(oldStockPrice.getId(), sp.getPrice());
                response.addToItemsUpdated(1);
            }
            else {
                // StockPrice exists and it matches the one we found.  Do nothing.
                Object[] loggerData = {sp.getStock().getTicker(), sp.getPrice().toString(), sp.getDate().toString() };
                logger.debug("{}'s price of {} was not modified for {}.", loggerData);
                response.addToItemsUnmodified(1);
            }
        }
        return response;
    }


}
