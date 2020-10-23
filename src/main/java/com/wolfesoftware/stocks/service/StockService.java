package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.common.StringUtil;
import com.wolfesoftware.stocks.exception.DuplicateException;
import com.wolfesoftware.stocks.exception.IllegalActionException;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.repository.StockRepository;
import com.wolfesoftware.stocks.repository.UserBasedRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class StockService extends UserBasedService<Stock> {

    @Resource
    private StockRepository stockRepository;

    @Resource
    private StockPriceService stockPriceService;

    @Resource
    private StockSplitService stockSplitService;

    @Resource
    private StockDividendService stockDividendService;

    @Resource
    private YahooFinanceService yahooFinanceService;


    // Methods required for the Base Class (UserBasedService) to work
    @Override
    protected UserBasedRepository<Stock> getRepo() {
        return stockRepository;
    }
    @Override
    protected String getEntityNameForMessage() {
        return "stock";
    }


    @Transactional
    public Stock create(String ticker, String name, Boolean benchmark) {

        validateProposedTickerSymbol(ticker);
        validateProposedStockName(name);
        validateProposedBenchmark(benchmark);

        Stock stock = new Stock();
        stock.setTicker(ticker);
        stock.setName(name);
        stock.setBenchmark(benchmark);
        Stock createdStock = stockRepository.create(stock);

        stockPriceService.loadInitialPileOfStockPrices(createdStock);
        stockSplitService.initialLoadOfStockSplits(createdStock);
        stockDividendService.initialLoadOfStockDividends(createdStock);

        return createdStock;

    }

    public List<Stock> retrieveAllBenchmarks() {
        return stockRepository.retrieveAllBenchmarks();
    }

    @Transactional
    // NOTE: We don't allow the user to change the Ticker Symbol
    public Stock updateStock(Long id, String name, Boolean benchmark) {
        validateProposedStockName(name);
        validateProposedBenchmark(benchmark);
        return stockRepository.updateStock(id, name, benchmark);
    }



    public Stock suggestName(String tickerSymbol) {
        String name = yahooFinanceService.getTickersStockName(tickerSymbol);
        Stock returnedStock = new Stock();
        returnedStock.setTicker(tickerSymbol);
        returnedStock.setName(name);
        returnedStock.setBenchmark(false);
        return returnedStock;
    }

    //
    // Private Methods
    //

    private void validateProposedStockName(String name) {
        if (!StringUtil.hasContent(name))
            throw new IllegalActionException("Stock name had no value.");
    }

    private void validateProposedBenchmark(Boolean benchmark) {
        if (benchmark == null)
            throw new IllegalActionException("Null value not supported for benchmark.");
    }

    private void validateProposedTickerSymbol(String ticker) {
        if (!StringUtil.hasContent(ticker))
            throw new IllegalActionException("Ticker symbol had no value.");

        // Throw exception if the stock is already present based on ticker symbol
        validateStockNotAlreadyPresent(ticker);

        // Throw exception if Yahoo doesn't recognize the ticker
        validateYahooRecognizedTicker(ticker);
    }

    @Transactional(propagation= Propagation.REQUIRES_NEW)
    private void validateStockNotAlreadyPresent(String proposedNewTickerSymbol) throws DuplicateException {
        if (stockRepository.existsByTicker(proposedNewTickerSymbol))
            throw new DuplicateException("Stock already exists with the specified name.");

    }

    private void validateYahooRecognizedTicker(String ticker) {
        if (!yahooFinanceService.isTickerValid(ticker)) {
            throw new IllegalActionException("Invalid ticker symbol.");
        }
    }
}
