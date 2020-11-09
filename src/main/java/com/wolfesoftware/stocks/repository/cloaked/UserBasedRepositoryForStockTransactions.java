package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.Portfolio;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockTransaction;
import com.wolfesoftware.stocks.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserBasedRepositoryForStockTransactions extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByUserAndStockAndDateBetweenAndPortfolioIn(User user, Stock stock, LocalDate beginDate, LocalDate endDate, List<Portfolio> portfolios);
    List<StockTransaction> findByUserAndStockInAndPortfolioInAndDateBefore(User user, List<Stock> stock, List<Portfolio> portfolios, LocalDate endDate);
    List<StockTransaction> findByUserAndPortfolioIn(User user, List<Portfolio> portfolios);
    void deleteByUserAndIdIn(User currentUser, List<Long> ids);
}
