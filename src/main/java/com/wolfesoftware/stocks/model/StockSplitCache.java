package com.wolfesoftware.stocks.model;

import com.wolfesoftware.stocks.common.BridgeToSpringBean;
import com.wolfesoftware.stocks.repository.StockSplitRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StockSplitCache {

    private Map<Stock, List<StockSplit>> mapOfStockSplits = new HashMap<>();

    public List<StockSplit> getAllStockSplits(Stock stock) {
       if (mapOfStockSplits.containsKey(stock)) {
           return mapOfStockSplits.get(stock);
       }
       else {
           StockSplitRepository stockSplitRepository = BridgeToSpringBean.getBean(StockSplitRepository.class);
           List<StockSplit> stockSplits = stockSplitRepository.retrieveAllForOneStock(stock);
           mapOfStockSplits.put(stock, stockSplits);
           return stockSplits;
       }
    }

    public List<StockSplit> getStockSplitsBetweenDates(Stock stock, LocalDate startDate, LocalDate endDate) {
        return getAllStockSplits(stock)
                .stream()
                .filter(ss -> !ss.getDate().isBefore(startDate) && !ss.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }
}
