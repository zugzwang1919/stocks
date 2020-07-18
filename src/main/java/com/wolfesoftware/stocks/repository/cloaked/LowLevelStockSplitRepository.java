package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockPrice;
import com.wolfesoftware.stocks.model.StockSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LowLevelStockSplitRepository extends JpaRepository<StockSplit, Long> {

    void deleteAllByStock(Stock s);

    List<StockSplit> findByStock(Stock stock);
    List<StockSplit> findByStockAndDate(Stock stock, LocalDate date);
    List<StockSplit> findByStockAndDateBetween(Stock stock, LocalDate beginDate, LocalDate endDate);
}
