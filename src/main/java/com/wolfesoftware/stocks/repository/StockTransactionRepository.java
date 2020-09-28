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
import java.util.List;
import java.util.Optional;

@Repository
public class StockTransactionRepository extends UserBasedRepository<StockTransaction> {
    @Resource
    UserBasedRepositoryForStockTransactions userBasedRepositoryForStockTransactions;

    @Resource
    RepositoryUtil repositoryUtil;


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
        User currentUser = repositoryUtil.getCurrentUser();
        // FIXME: NOTE I'm not using portfolios yet.
        return userBasedRepositoryForStockTransactions.findByUserAndStockAndDateBetweenAndPortfolioIn(currentUser, stock, beginDate, endDate, portfolios);
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
