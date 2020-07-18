package com.wolfesoftware.stocks.repository.cloaked;


import com.wolfesoftware.stocks.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBasedRepositoryForOptions extends JpaRepository<Option, Long> {
}
