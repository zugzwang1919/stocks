package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.common.StringUtil;
import com.wolfesoftware.stocks.exception.DuplicateException;
import com.wolfesoftware.stocks.exception.IllegalActionException;
import com.wolfesoftware.stocks.model.Portfolio;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockTransaction;
import com.wolfesoftware.stocks.repository.PortfolioRepository;
import com.wolfesoftware.stocks.repository.StockTransactionRepository;
import com.wolfesoftware.stocks.repository.UserBasedRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PortfolioService extends UserBasedService<Portfolio> {

    @Resource
    private PortfolioRepository portfolioRepository;

    @Resource
    private StockTransactionRepository stockTransactionRepository;

    // Methods required for the Base Class (UserBasedService) to work
    @Override
    protected UserBasedRepository<Portfolio> getRepo() {
        return portfolioRepository;
    }
    @Override
    protected String getEntityNameForMessage() {
        return "portfolio";
    }



    @Transactional
    public Portfolio create(String portfolioName) {

        validateProposedPortfolioName(portfolioName);

        Portfolio p = new Portfolio();
        p.setPortfolioName(portfolioName);
        return portfolioRepository.create(p);
    }

    @Transactional
    public Portfolio updatePortfolio(Long id, String portfolioName) {

        if (id == null)
            throw new IllegalActionException("Null value is not allowed for id.");

        validateProposedPortfolioName(portfolioName);

        return portfolioRepository.updatePortfolio(id, portfolioName);
    }

    @Transactional
    public List<Stock> retrieveStocksUsedInPortfolios(List<Long> portfolioIds) {
        // Get the Portfolios
        List<Portfolio> portfolios = portfolioRepository.retrievePortfolios(portfolioIds);
        // Get the Transactions
        List<StockTransaction> stockTransactions = stockTransactionRepository.retrieve(portfolios);
        // Get the Unique Stocks contained in all of the transactions
        List<Stock> uniqueStocks = stockTransactions.stream().map(st -> st.getStock()).distinct().collect(Collectors.toList());
        return uniqueStocks;

    }


    // Private Methods

    private void validateProposedPortfolioName(String proposedPortfolioName) {

        if (!StringUtil.hasContent(proposedPortfolioName))
            throw new IllegalActionException("Portfolio name had no value.");

        validatePortfolioNameNotInUse(proposedPortfolioName);
    }

    @Transactional(propagation= Propagation.REQUIRES_NEW)
    private void validatePortfolioNameNotInUse(String proposedNewPortfolioName) {
        List<Portfolio> thisUsersPortfolios = portfolioRepository.retrieveAll();
        Optional<Portfolio> p = thisUsersPortfolios.stream().
                                filter(x -> proposedNewPortfolioName.equals(x.getPortfolioName())).
                                findFirst();
        if (p.isPresent())
            throw new DuplicateException("Portfolio already exists with the specified name.");

    }

}
