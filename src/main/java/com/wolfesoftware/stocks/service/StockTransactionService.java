package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.exception.IllegalActionException;
import com.wolfesoftware.stocks.model.Portfolio;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockTransaction;
import com.wolfesoftware.stocks.repository.StockTransactionRepository;
import com.wolfesoftware.stocks.repository.UserBasedRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class StockTransactionService extends UserBasedService<StockTransaction> {

    @Resource
    private StockTransactionRepository stockTransactionRepository;

    @Resource
    private StockService stockService;

    @Resource
    private PortfolioService portfolioService;

    // Methods required for the Base Class (UserBasedService) to work
    @Override
    protected UserBasedRepository<StockTransaction> getRepo() {
        return stockTransactionRepository;
    }
    @Override
    protected String getEntityNameForMessage() {
        return "stock transaction";
    }


    @Transactional
    public StockTransaction create(Long portfolioId, LocalDate date, Long stockId, StockTransaction.Activity activity, BigDecimal tradeSize, BigDecimal amount) {

        Pair<Portfolio, Stock> pair = validateParameters(portfolioId, date, stockId, activity, tradeSize, amount);

        StockTransaction stockTransaction = new StockTransaction();
        stockTransaction.setPortfolio(pair.getFirst());
        stockTransaction.setDate(date);
        stockTransaction.setStock(pair.getSecond());
        stockTransaction.setActivity(activity);
        stockTransaction.setTradeSize(tradeSize);
        stockTransaction.setAmount(amount);

        return stockTransactionRepository.create(stockTransaction);

    }

    @Transactional
    public StockTransaction update(Long id, Long portfolioId, LocalDate date, Long stockId, StockTransaction.Activity activity, BigDecimal tradeSize, BigDecimal amount) {

        Pair<Portfolio, Stock> pair = validateParameters(portfolioId, date, stockId, activity, tradeSize, amount);

        return stockTransactionRepository.update(id, pair.getFirst(), date, pair.getSecond(), activity, tradeSize, amount);
    }


    // Private Methods

    private Pair<Portfolio, Stock> validateParameters(Long portfolioId, LocalDate date, Long stockId, StockTransaction.Activity activity, BigDecimal tradeSize, BigDecimal amount) {
        // Guarantee that the portfolioId refers to a portfolio that the user owns.  Ask the Portfolio Service
        // to return the portfolio.  If it does not belong to the user, it will throw a NotFoundException
        Portfolio portfolio = portfolioService.retrieveById(portfolioId);

        // Guarantee that the stockId refers to a stock that the user owns.  Ask the Stock Service to retrieve
        // the stock.  If it does not belong to the user, it will throw a NotFoundException
        Stock stock = stockService.retrieveById(stockId);

        if (date == null || activity == null || tradeSize == null || amount == null)
            throw new IllegalActionException("Null values found in either date, activity, trade size or amount.");
        return Pair.of(portfolio, stock);
    }
}
