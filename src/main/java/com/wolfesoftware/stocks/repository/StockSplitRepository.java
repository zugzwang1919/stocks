package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.Stock;
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

    // Public methods

    // CREATE
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
