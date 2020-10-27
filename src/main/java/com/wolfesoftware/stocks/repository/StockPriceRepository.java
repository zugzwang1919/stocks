package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockPrice;
import com.wolfesoftware.stocks.repository.cloaked.LowLevelStockPriceRepository;
import com.wolfesoftware.stocks.repository.cloaked.UserBasedRepositoryForStocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class StockPriceRepository {
    @Resource
    LowLevelStockPriceRepository lowLevelStockPriceRepository;

    @Resource
    CacheManager cacheManager;

    public static final Logger logger = LoggerFactory.getLogger(StockPriceRepository.class);

    // Public methods

    // CREATE
    public StockPrice persistStockPrice(StockPrice stockPriceToBePersisted) {
        return lowLevelStockPriceRepository.save(stockPriceToBePersisted);
    }

    public List<StockPrice> persistStockPrices(List<StockPrice> stockPrices) {
        return lowLevelStockPriceRepository.saveAll(stockPrices);
    }


    // RETRIEVE
    public Optional<StockPrice> retrieveByStockAndDate(Stock stock, LocalDate localDate) {
        return lowLevelStockPriceRepository.findByStockAndDate(stock, localDate);
    }

    @Scheduled(fixedRate = 5*60*1000) // Evict the cache every five minutes
    public void evictStockPriceCache() {
        logger.debug("We're evicting the stock-price cache.");
        cacheManager.getCache("stock-prices").clear();
    }

    @Cacheable("stock-prices")
    public List<StockPrice> retrieveByStockAndDateDescending(Stock stock, LocalDate beginDate, LocalDate endDate) {
        return lowLevelStockPriceRepository.findByStockAndDateBetweenOrderByDateDesc(stock, beginDate, endDate);
    }

    // UPDATE
    public StockPrice updateStockPrice(Long id, BigDecimal price) {
        Optional<StockPrice> optionalStockPrice = lowLevelStockPriceRepository.findById(id);
        if (optionalStockPrice.isEmpty())
            throw new NotFoundException("The requested stock price could not be found.");
        StockPrice updatedStockPrice = optionalStockPrice.get();
        updatedStockPrice.setPrice(price);
        return lowLevelStockPriceRepository.save(updatedStockPrice);
    }


    // OTHER
    public LocalDate mostRecentStockPriceDate(Stock stock) {
        return lowLevelStockPriceRepository.findMostRecentPriceDateForOneStock(stock);
    }


}
