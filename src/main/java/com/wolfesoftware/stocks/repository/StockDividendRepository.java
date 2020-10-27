package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockDividend;
import com.wolfesoftware.stocks.repository.cloaked.LowLevelStockDividendRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class StockDividendRepository {
    @Resource
    LowLevelStockDividendRepository lowLevelStockDividendRepository;


    // Public methods

    // CREATE
    public StockDividend persistStockDividend(StockDividend stockPriceToBePersisted) {
        return lowLevelStockDividendRepository.save(stockPriceToBePersisted);
    }

    public List<StockDividend> persistStockDividends(List<StockDividend> stockPrices) {
        return lowLevelStockDividendRepository.saveAll(stockPrices);
    }

    // RETRIEVE

    public List<StockDividend> retrieveAllForOneStock(Stock stock) {
        return lowLevelStockDividendRepository.findByStock(stock);
    }


    public List<StockDividend> retrieveForOneStockOnOneDate(Stock stock, LocalDate date) {
        return lowLevelStockDividendRepository.findByStockAndExDividendDate(stock, date);
    }

    public List<StockDividend> retrieveForOneStockBetweenDates(Stock stock, LocalDate startDate, LocalDate endDate) {
        return lowLevelStockDividendRepository.findByStockAndExDividendDateBetween(stock, startDate, endDate);
    }


    // UPDATE
    public StockDividend updateStockDividend(Long id, LocalDate exDividendDate, BigDecimal dividendAmount) {
        Optional<StockDividend> optionalStockDividend = lowLevelStockDividendRepository.findById(id);
        if (optionalStockDividend.isEmpty())
            throw new NotFoundException("The requested stock dividend could not be found.");
        StockDividend updatedStockDividend = optionalStockDividend.get();
        updatedStockDividend.setExDividendDate(exDividendDate);
        updatedStockDividend.setDividendAmount(dividendAmount);
        return lowLevelStockDividendRepository.save(updatedStockDividend);
    }


}
