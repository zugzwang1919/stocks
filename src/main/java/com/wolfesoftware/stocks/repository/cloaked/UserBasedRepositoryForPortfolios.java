package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.Portfolio;
import com.wolfesoftware.stocks.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface UserBasedRepositoryForPortfolios extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUserAndIdIn(User user, List<Long> portfolioIds);
}
