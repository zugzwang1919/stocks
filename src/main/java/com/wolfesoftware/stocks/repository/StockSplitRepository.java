package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockSplit;
import com.wolfesoftware.stocks.repository.cloaked.LowLevelStockSplitRepository;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
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
    CacheManager cacheManager;

    // Public methods

    // CREATE
    public StockSplit persistStockSplit(StockSplit stockSplitToBePersisted) {
        return lowLevelStockSplitRepository.save(stockSplitToBePersisted);
    }

    public List<StockSplit> persistStockSplits(List<StockSplit> stockSplits) {
        return lowLevelStockSplitRepository.saveAll(stockSplits);
    }

    // RETRIEVE

    @Scheduled(fixedRate = 5*60*1000) // Evict the cache every five minutes
    public void evictStockSplitCache() {
        //noinspection ConstantConditions
        cacheManager.getCache("stock-splits").clear();
    }

    @Cacheable("stock-splits")
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
