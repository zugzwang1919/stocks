package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockTransaction;
import com.wolfesoftware.stocks.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserBasedRepositoryForStockTransactions extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByUserAndStockAndDateBetween(User user, Stock stock, LocalDate beginDate, LocalDate endDate);
}
