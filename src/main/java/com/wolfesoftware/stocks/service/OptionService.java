package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.exception.IllegalActionException;
import com.wolfesoftware.stocks.model.Option;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.repository.OptionRepository;
import com.wolfesoftware.stocks.repository.UserBasedRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class OptionService extends UserBasedService<Option> {

    @Resource
    private OptionRepository optionRepository;

    @Resource
    private StockService stockService;

    // Methods required for the Base Class (UserBasedService) to work
    @Override
    protected UserBasedRepository<Option> getRepo() {
        return optionRepository;
    }
    @Override
    protected String getEntityNameForMessage() {
        return "option";
    }



    @Transactional
    public Option create(Option.OptionType optionType, Long stockId, BigDecimal strikePrice, LocalDate expirationDate) {

        Stock s = validateParametersAndFindStock(optionType, stockId, strikePrice, expirationDate);

        Option o = new Option();
        o.setOptionType(optionType);
        o.setStock(s);
        o.setStrikePrice(strikePrice);
        o.setExpirationDate(expirationDate);

        return optionRepository.create(o);
    }

    @Transactional
    public Option update(Long id, Option.OptionType optionType, Long stockId, BigDecimal strikePrice, LocalDate expirationDate) {

        if (id == null)
            throw new IllegalActionException("Null value is not allowed for id.");
        Stock s = validateParametersAndFindStock(optionType, stockId, strikePrice, expirationDate);

        return optionRepository.update(id, optionType, s, strikePrice, expirationDate);
    }


    // Private Methods

    @Transactional(propagation= Propagation.REQUIRES_NEW)
    private Stock validateParametersAndFindStock(Option.OptionType optionType, Long stockId, BigDecimal strikePrice, LocalDate expirationDate) {
        if (optionType == null || stockId == null || strikePrice == null || expirationDate == null)
            throw new IllegalActionException("Null values found in either option type, stock id, strike price or expiration date.");
        // NOTE: This will throw a NotFoundException if the caller doesn't have access to this stock
        return stockService.retrieveById(stockId);
    }


}
