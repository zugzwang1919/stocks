package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockSplit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LowLevelStockSplitRepository extends JpaRepository<StockSplit, Long> {

    List<StockSplit> findByStock(Stock stock);
    List<StockSplit> findByStockAndDate(Stock stock, LocalDate date);
}
