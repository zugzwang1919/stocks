package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.Portfolio;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.StockTransaction;
import com.wolfesoftware.stocks.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface UserBasedRepositoryForStockTransactions extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByUserAndStockAndDateBetweenAndPortfolioIn(User user, Stock stock, LocalDate beginDate, LocalDate endDate, List<Portfolio> portfolios);
    List<StockTransaction> findByUserAndStockInAndPortfolioInAndDateBefore(User user, List<Stock> stock, List<Portfolio> portfolios, LocalDate endDate);
    void deleteByUserAndIdIn(User currentUser, List<Long> ids);

    @Query( "SELECT distinct s FROM StockTransaction st JOIN st.stock s JOIN st.portfolio p "  +
            "WHERE st.user = :user AND p.id in :portfolioIds")
    List<Stock> findUniqueStocksInStockTransactionByUserAndPortfolioIn( User user,
                                                                        List<Long> portfolioIds);
}
