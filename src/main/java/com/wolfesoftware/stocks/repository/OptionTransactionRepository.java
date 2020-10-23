package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.*;
import com.wolfesoftware.stocks.repository.cloaked.UserBasedRepositoryForOptionTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;

@Repository
public class OptionTransactionRepository extends UserBasedRepository<OptionTransaction> {
    @Resource
    UserBasedRepositoryForOptionTransactions userBasedRepositoryForOptionTransactions;

    @Resource
    RepositoryUtil repositoryUtil;

    // Configure this class to be a subclass of  UserBasedRepository
    public OptionTransactionRepository() {
        super(OptionTransaction.class);
    }
    @Override
    protected JpaRepository<OptionTransaction, Long> getUserBasedPersistEntityRepository() {
        return userBasedRepositoryForOptionTransactions;
    }

    // RETRIEVE
    public List<OptionTransaction> retrieveForOneStock(Stock stock, LocalDate beginDate, LocalDate endDate, List<Portfolio> portfolios) {
        User currentUser = repositoryUtil.getCurrentUser();
        return userBasedRepositoryForOptionTransactions.findByUserAndStockAndDateBetweenAndPortfolioIn(currentUser, stock, beginDate, endDate, portfolios);
    }

    // Retrieve all OptionTransactions that are used by a list of Stocks and in a list of Portfolios before a specific end date
    // And then Group the transactions into a Map by Stock
    public Map<Stock,List<OptionTransaction>> retrieveAndGroup(List<Stock> stocks, List<Portfolio> portfolios, LocalDate endDate) {
        List<OptionTransaction> allOptionTransactions = retrieve(stocks, portfolios, endDate);
        return allOptionTransactions.stream().collect(groupingBy(ot -> ot.getOption().getStock()));
    }

    // Retrieve all StockTransactions that are used by a list of Stocks and in a list of Portfolios before a specific end date
    public List<OptionTransaction> retrieve(List<Stock> stocks, List<Portfolio> portfolios, LocalDate endDate) {
        // Get the current user
        User currentUser = repositoryUtil.getCurrentUser();
        // Retrieve the StockTransactions with the cloaked repo
        return userBasedRepositoryForOptionTransactions.findByUserAndStockInAndPortfolioInAndDateBefore(currentUser, stocks, portfolios, endDate);
    }



    // UPDATE

    // NOTE: For anyone using this as a model, UPDATE is the only thing that is not handled well, by the
    // NOTE: persistent objects.  This layer must ensure that the caller has not manipulated things like the "user" and
    // NOTE: the "createdate".
    // NOTE:
    public OptionTransaction update(Long id, Portfolio portfolio, LocalDate date, Option option, OptionTransaction.Activity activity, Long numberOfContracts, BigDecimal amount) {

        // If the stock transaction does not exist OR does not belong to the 'current user', just stop
        Optional<OptionTransaction> possibleStockOptionInDB = retrieveById(id);
        if (possibleStockOptionInDB.isEmpty())
            throw new NotFoundException("The requested stock transaction could not be found.");

        OptionTransaction intermediateOptionTransaction = possibleStockOptionInDB.get();

        // Update the stock transaction with new info (leaving alone stuff like User, createdate, updatedate)
        intermediateOptionTransaction.setPortfolio(portfolio);
        intermediateOptionTransaction.setDate(date);
        intermediateOptionTransaction.setOption(option);
        intermediateOptionTransaction.setActivity(activity);
        intermediateOptionTransaction.setNumberOfContracts(numberOfContracts);
        intermediateOptionTransaction.setAmount(amount);



        // Send it to the database
        return userBasedRepositoryForOptionTransactions.save(intermediateOptionTransaction);
    }




}
