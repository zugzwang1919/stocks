package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockDividend;
import com.wolfesoftware.stocks.model.StockSplit;
import com.wolfesoftware.stocks.repository.cloaked.LowLevelStockSplitRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class StockSplitRepository {
    @Resource
    LowLevelStockSplitRepository lowLevelStockSplitRepository;

    @Resource
    RepositoryUtil repositoryUtil;

    // Public methods

    // CREATE
    public StockSplit createStockSplit(Stock stock, LocalDate date, BigDecimal afterAmount, BigDecimal beforeAmount) {
        StockSplit stockSplitToBeCreated = new StockSplit(stock, date, afterAmount, beforeAmount);
        return lowLevelStockSplitRepository.save(stockSplitToBeCreated);
    }

    public StockSplit persistStockSplit(StockSplit stockPriceToBePersisted) {
        return lowLevelStockSplitRepository.save(stockPriceToBePersisted);
    }

    public List<StockSplit> persistStockSplits(List<StockSplit> stockPrices) {
        return lowLevelStockSplitRepository.saveAll(stockPrices);
    }

    // RETRIEVE
    public List<StockSplit> retrieveAllForOneStock(Stock stock) {
        return lowLevelStockSplitRepository.findByStock(stock);
    }

    public List<StockSplit> retrieveForOneStockOnOneDate(Stock stock, LocalDate date) {
        return lowLevelStockSplitRepository.findByStockAndDate(stock, date);
    }

    public List<StockSplit> retrieveForOneStockBetweenDates(Stock stock, LocalDate startDate, LocalDate endDate) {
        return lowLevelStockSplitRepository.findByStockAndDateBetween(stock, startDate, endDate);
    }


    // UPDATE
    public StockSplit updateStockSplit(Long id, LocalDate date, BigDecimal beforeAmount, BigDecimal afterAmount) {
        Optional<StockSplit> optionalStockSplit = lowLevelStockSplitRepository.findById(id);
        if (optionalStockSplit.isEmpty())
            throw new NotFoundException("The requested stock split could not be found.");
        StockSplit updatedStockSplit = optionalStockSplit.get();
        updatedStockSplit.setDate(date);
        updatedStockSplit.setBeforeAmount(beforeAmount);
        updatedStockSplit.setAfterAmount(afterAmount);
        return lowLevelStockSplitRepository.save(updatedStockSplit);
    }

}
