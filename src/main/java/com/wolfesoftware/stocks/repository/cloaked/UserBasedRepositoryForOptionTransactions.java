package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.OptionTransaction;
import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UserBasedRepositoryForOptionTransactions extends JpaRepository<OptionTransaction, Long> {
    @Query("SELECT ot FROM OptionTransaction ot JOIN ot.option.stock s WHERE s = :stock AND ot.user = :user AND ot.date >= :beginDate AND ot.date <= :endDate")
    List<OptionTransaction> findByUserAndStockAndDateBetween(@Param("user") User user, @Param("stock") Stock stock, @Param("beginDate") LocalDate beginDate, @Param("endDate") LocalDate endDate);
}
