package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockDividend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LowLevelStockDividendRepository extends JpaRepository<StockDividend, Long> {

    List<StockDividend> findByStock(Stock stock);
    List<StockDividend> findByStockAndExDividendDate(Stock stock, LocalDate exDividendDate);
    List<StockDividend> findByStockAndExDividendDateBetween(Stock stock, LocalDate beginExDividendDate, LocalDate endExDividendDate);
}
