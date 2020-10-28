package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LowLevelUserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.authorities a WHERE u.username = :username ")
    Optional<User> findByUsername(String username);


    boolean existsByUsername(String username);

    <U extends User> U save(U user);

}
