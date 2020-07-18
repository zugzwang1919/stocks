package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.exception.IllegalActionException;
import com.wolfesoftware.stocks.model.Option;
import com.wolfesoftware.stocks.model.OptionTransaction;
import com.wolfesoftware.stocks.model.Portfolio;
import com.wolfesoftware.stocks.repository.OptionTransactionRepository;
import com.wolfesoftware.stocks.repository.UserBasedRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class OptionTransactionService extends UserBasedService<OptionTransaction> {

    @Resource
    private OptionTransactionRepository optionTransactionRepository;

    @Resource
    private OptionService optionService;

    @Resource
    private PortfolioService portfolioService;

    // Methods required for the Base Class (UserBasedService) to work
    @Override
    protected UserBasedRepository<OptionTransaction> getRepo() {
        return optionTransactionRepository;
    }
    @Override
    protected String getEntityNameForMessage() {
        return "option transaction";
    }


    @Transactional
    public OptionTransaction create(Long portfolioId, LocalDate date, Long optionId, OptionTransaction.Activity activity, Long numberOfContracts, BigDecimal amount) {

        Pair<Portfolio, Option> pair = validateParameters(portfolioId, date, optionId, activity, numberOfContracts, amount);

        OptionTransaction optionTransaction = new OptionTransaction();
        optionTransaction.setPortfolio(pair.getFirst());
        optionTransaction.setDate(date);
        optionTransaction.setOption(pair.getSecond());
        optionTransaction.setActivity(activity);
        optionTransaction.setNumberOfContracts(numberOfContracts);
        optionTransaction.setAmount(amount);

        return optionTransactionRepository.create(optionTransaction);

    }

    @Transactional
    public OptionTransaction update(Long id, Long portfolioId, LocalDate date, Long optionId, OptionTransaction.Activity activity, Long numberOfContracts, BigDecimal amount) {

        Pair<Portfolio, Option> pair = validateParameters(portfolioId, date, optionId, activity, numberOfContracts, amount);

        return optionTransactionRepository.update(id, pair.getFirst(), date, pair.getSecond(), activity, numberOfContracts, amount);
    }


    // Private Methods

    private Pair<Portfolio, Option> validateParameters(Long portfolioId, LocalDate date, Long optionId, OptionTransaction.Activity activity, Long numberOfContracts, BigDecimal amount) {
        // Guarantee that the portfolioId refers to a portfolio that the user owns.  Ask the Portfolio Service
        // to return the portfolio.  If it does not belong to the user, it will throw a NotFoundException
        Portfolio portfolio = portfolioService.retrieveById(portfolioId);

        // Guarantee that the optionId refers to a stock that the user owns.  Ask the Stock Service to retrieve
        // the stock.  If it does not belong to the user, it will throw a NotFoundException
        Option option = optionService.retrieveById(optionId);

        if (date == null || activity == null || numberOfContracts == null || amount == null)
            throw new IllegalActionException("Null values found in either date, activity, number of contracts or amount.");
        return Pair.of(portfolio, option);
    }
}
