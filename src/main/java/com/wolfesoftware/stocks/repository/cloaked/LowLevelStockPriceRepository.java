package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LowLevelStockPriceRepository extends JpaRepository<StockPrice, Long> {

    @Query("SELECT MAX(sp.date) FROM StockPrice sp WHERE sp.stock = ?1")
    LocalDate findMostRecentPriceDateForOneStock(Stock s);

    Optional<StockPrice> findByStockAndDate(Stock s, LocalDate d);

    List<StockPrice> findByStockAndDateBetweenOrderByDateDesc(Stock stock, LocalDate beginDate, LocalDate endDate);

}
