package com.wolfesoftware.stocks.repository.cloaked;

import com.wolfesoftware.stocks.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LowLevelUserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.authorities a WHERE u.id = :id ")
    Optional<User> findById(Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.authorities a WHERE u.username = :username ")
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.authorities a WHERE u.googleid = :googleid ")
    Optional<User> findByGoogleid(String googleid);



    boolean existsByUsername(String username);

    <U extends User> U save(U user);

}
