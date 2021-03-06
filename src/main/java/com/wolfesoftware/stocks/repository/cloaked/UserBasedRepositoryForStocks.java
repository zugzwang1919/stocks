package com.wolfesoftware.stocks.repository.cloaked;


import com.wolfesoftware.stocks.model.Stock;
import com.wolfesoftware.stocks.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserBasedRepositoryForStocks extends JpaRepository<Stock, Long> {

    @Query("SELECT s FROM Stock s " +
            "WHERE s.user = :user AND s.benchmark = true ")
    List<Stock> findAllBenchmarksByUser(User user);

    void deleteByUserAndIdIn(User currentUser, List<Long> ids);
}
