package com.wolfesoftware.stocks.repository.cloaked;


import com.wolfesoftware.stocks.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBasedRepositoryForStocks extends JpaRepository<Stock, Long> {
}
