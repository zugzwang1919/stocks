package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LowLevelAuthorityRepository extends JpaRepository<Authority, Long> {

    Authority save(Authority authority);

}
