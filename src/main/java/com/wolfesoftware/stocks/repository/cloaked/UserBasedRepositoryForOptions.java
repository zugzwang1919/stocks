package com.wolfesoftware.stocks.repository.cloaked;


import com.wolfesoftware.stocks.model.Option;
import com.wolfesoftware.stocks.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBasedRepositoryForOptions extends JpaRepository<Option, Long> {
    void deleteByUserAndIdIn(User currentUser, List<Long> ids);
}
