package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UserBasedRepositoryForOptionTransactions extends JpaRepository<OptionTransaction, Long> {

    @Query("SELECT ot FROM OptionTransaction ot JOIN ot.option.stock s JOIN ot.portfolio p " +
           "WHERE s = :stock AND ot.user = :user AND ot.date >= :beginDate AND ot.date <= :endDate AND p in :portfolios")
    List<OptionTransaction> findByUserAndStockAndDateBetweenAndPortfolioIn(@Param("user") User user,
                                                                           @Param("stock") Stock stock,
                                                                           @Param("beginDate") LocalDate beginDate,
                                                                           @Param("endDate") LocalDate endDate,
                                                                           @Param("portfolios") List<Portfolio> portfolios);


    @Query("SELECT ot FROM OptionTransaction ot JOIN ot.option.stock s JOIN ot.portfolio p " +
           "WHERE ot.user = :user AND s in :stocks AND p in :portfolios AND ot.date <= :endDate ")
    List<OptionTransaction> findByUserAndStockInAndPortfolioInAndDateBefore(User user,
                                                                            List<Stock> stocks,
                                                                            List<Portfolio> portfolios,
                                                                            LocalDate endDate);

    void deleteByUserAndIdIn(User currentUser, List<Long> ids);

}
