package com.wolfesoftware.stocks.repository;

import com.wolfesoftware.stocks.exception.DuplicateException;
import com.wolfesoftware.stocks.exception.NotFoundException;
import com.wolfesoftware.stocks.model.Authority;
import com.wolfesoftware.stocks.model.User;
import com.wolfesoftware.stocks.repository.cloaked.LowLevelAuthorityRepository;
import com.wolfesoftware.stocks.repository.cloaked.LowLevelUserRepository;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Optional;

@Repository
public class AuthorityRepository {

    @Resource
    LowLevelAuthorityRepository lowLevelAuthorityRepository;

    public Authority createAuthority(Authority authority) {
        return lowLevelAuthorityRepository.save(authority);
    }

}
