package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LowLevelAuthorityRepository extends JpaRepository<Authority, Long> {

    <A extends Authority> A save(A authority);

}
