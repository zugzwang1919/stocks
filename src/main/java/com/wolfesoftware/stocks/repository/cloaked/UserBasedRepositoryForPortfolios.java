package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserBasedRepositoryForPortfolios extends JpaRepository<Portfolio, Long> {
}
