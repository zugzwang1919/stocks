package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.Portfolio;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockTransaction;
import com.wolfesoftware.stocks.model.User;
import com.wolfesoftware.stocks.repository.cloaked.UserBasedRepositoryForStockTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;

@Repository
public class StockTransactionRepository extends UserBasedRepository<StockTransaction> {

    @Resource
    UserBasedRepositoryForStockTransactions userBasedRepositoryForStockTransactions;


    // Configure this class to be a subclass of  UserBasedRepository
    public StockTransactionRepository() {
        super(StockTransaction.class);
    }
    @Override
    protected JpaRepository<StockTransaction, Long> getUserBasedPersistEntityRepository() {
        return userBasedRepositoryForStockTransactions;
    }


    // RETRIEVE
    public List<StockTransaction> retrieveForOneStock(Stock stock, LocalDate beginDate, LocalDate endDate, List<Portfolio> portfolios) {
        User currentUser = RepositoryUtil.getCurrentUser();
        return userBasedRepositoryForStockTransactions.findByUserAndStockAndDateBetweenAndPortfolioIn(currentUser, stock, beginDate, endDate, portfolios);
    }

    // Retrieve all StockTransactions that are used in the list of portfolios
    public List<StockTransaction> retrieve(List<Portfolio> portfolios) {
        // Just in case the caller provided no portfolios return an empty list.
        // Hibernate does not like providing a list that is empty
        if (portfolios == null || portfolios.isEmpty()) {
            return new ArrayList<>();
        }
        // Get the current user
        User currentUser = RepositoryUtil.getCurrentUser();
        // Retrieve the StockTransactions with the cloaked repo
        return userBasedRepositoryForStockTransactions.findByUserAndPortfolioIn(currentUser, portfolios);
    }

    // Retrieve all StockTransactions that are used by a list of Stocks and in a list of Portfolios before a specific end date
    // And then Group the transactions into a Map by Stock
    public Map<Stock,List<StockTransaction>> retrieveAndGroup(List<Stock> stocks, List<Portfolio> portfolios, LocalDate endDate) {
        List<StockTransaction> allTransactions = retrieve(stocks, portfolios, endDate);
        return allTransactions.stream().collect(groupingBy(StockTransaction::getStock));
    }

    // Retrieve all StockTransactions that are used by a list of Stocks and in a list of Portfolios before a specific end date
    public List<StockTransaction> retrieve(List<Stock> stocks, List<Portfolio> portfolios, LocalDate endDate) {
        // Get the current user
        User currentUser = RepositoryUtil.getCurrentUser();
        // Retrieve the StockTransactions with the cloaked repo
        return userBasedRepositoryForStockTransactions.findByUserAndStockInAndPortfolioInAndDateBefore(currentUser, stocks, portfolios, endDate);
    }


    // UPDATE

    // NOTE: For anyone using this as a model, UPDATE is the only thing that is not handled well, by the
    // NOTE: persistent objects.  This layer must ensure that the caller has not manipulated things like the "user" and
    // NOTE: the "createdate".
    // NOTE:
    public StockTransaction update(Long id, Portfolio portfolio, LocalDate date, Stock stock, StockTransaction.Activity activity, BigDecimal tradeSize, BigDecimal amount) {

        // If the stock transaction does not exist OR does not belong to the 'current user', just stop
        Optional<StockTransaction> possibleStockTransactionInDB = retrieveById(id);
        if (possibleStockTransactionInDB.isEmpty())
            throw new NotFoundException("The requested stock transaction could not be found.");

        StockTransaction intermediateStockTransaction = possibleStockTransactionInDB.get();

        // Update the stock transaction with new info (leaving alone stuff like User, createdate, updatedate)
        intermediateStockTransaction.setPortfolio(portfolio);
        intermediateStockTransaction.setDate(date);
        intermediateStockTransaction.setStock(stock);
        intermediateStockTransaction.setActivity(activity);
        intermediateStockTransaction.setTradeSize(tradeSize);
        intermediateStockTransaction.setAmount(amount);



        // Send it to the database
        return userBasedRepositoryForStockTransactions.save(intermediateStockTransaction);
    }




}
